package com.matiasdev.elecapp.features.inspections.domain

import com.matiasdev.elecapp.features.electricaltools.calculators.VoltageDropCalculator
import com.matiasdev.elecapp.features.electricaltools.domain.CalculationSource
import com.matiasdev.elecapp.features.electricaltools.domain.ElectricalSystemType
import com.matiasdev.elecapp.features.electricaltools.domain.TechnicalClassification
import com.matiasdev.elecapp.features.electricaltools.domain.TechnicalConductorMaterial
import com.matiasdev.elecapp.features.electricaltools.domain.TemperatureMode
import com.matiasdev.elecapp.features.electricaltools.domain.VoltageDropCurrentMode
import com.matiasdev.elecapp.features.electricaltools.domain.VoltageDropInput
import com.matiasdev.elecapp.features.electricaltools.summary.TechnicalValueFormatter
import kotlin.math.abs

data class AutoInspectionCalculation(
    val id: String,
    val title: String,
    val primaryResult: String,
    val detail: String,
    val classification: TechnicalClassification,
)

object AutoInspectionCalculationBuilder {
    fun build(aggregate: InspectionAggregate): List<AutoInspectionCalculation> = buildList {
        addAll(buildProtectionCompatibility(aggregate))
        buildMeasuredVoltageDrop(aggregate)?.let(::add)
        buildCalculatedVoltageDrop(aggregate)?.let(::add)
        buildGroundingAssessment(aggregate)?.let(::add)
    }.distinctBy { it.id }

    private fun buildProtectionCompatibility(aggregate: InspectionAggregate): List<AutoInspectionCalculation> = buildList {
        aggregate.pillar?.let { pillar ->
            val breaker = pillar.mainBreakerAmps ?: pillar.mainBreakerOtherAmps
            val section = pillar.conductorSectionMm2 ?: pillar.conductorOtherSectionMm2
            if (breaker != null && section != null) {
                add(protectionCalculation("auto:pillar:protection", "Pilar: térmica y cable", breaker, section, pillar.conductorMaterial))
            }
        }
        aggregate.mainPanelCircuits.filterNot(MainPanelCircuit::isDeleted).forEach { circuit ->
            val breaker = circuit.breakerAmps ?: circuit.breakerOtherAmps
            val section = circuit.conductorSectionMm2 ?: circuit.conductorOtherSectionMm2
            if (breaker != null && section != null) {
                val destination = circuit.destinationOther?.takeIf(String::isNotBlank) ?: circuit.destination.name.lowercase()
                add(protectionCalculation("auto:circuit:${circuit.id}:protection", "Circuito $destination: térmica y cable", breaker, section, circuit.conductorMaterial))
            }
        }
    }

    private fun protectionCalculation(
        id: String,
        title: String,
        breakerAmps: Int,
        sectionMm2: Double,
        material: ConductorMaterial,
    ): AutoInspectionCalculation {
        val reference = ampacityReference(sectionMm2, material)
        val classification = when {
            reference == null -> TechnicalClassification.NOT_CLASSIFIED
            breakerAmps <= reference -> TechnicalClassification.ACCEPTABLE
            breakerAmps <= reference * 1.15 -> TechnicalClassification.REQUIRES_REVIEW
            else -> TechnicalClassification.CRITICAL_REVIEW
        }
        val referenceText = reference?.let { "referencia orientativa ${TechnicalValueFormatter.withUnit(it, "A", 0)}" } ?: "sin referencia para el material/sección"
        return AutoInspectionCalculation(
            id = id,
            title = title,
            primaryResult = "${breakerAmps} A sobre ${TechnicalValueFormatter.withUnit(sectionMm2, "mm²")} · ${classification.shortLabel()}",
            detail = "Comparación orientativa entre calibre de protección y sección visible/declarada; $referenceText.",
            classification = classification,
        )
    }

    private fun buildMeasuredVoltageDrop(aggregate: InspectionAggregate): AutoInspectionCalculation? {
        val pillarVoltage = aggregate.pillarMeasurements.firstVoltageValue() ?: return null
        val panelVoltage = aggregate.mainPanelMeasurements.firstInputVoltageValue() ?: return null
        if (pillarVoltage <= 0.0 || panelVoltage <= 0.0) return null
        val drop = pillarVoltage - panelVoltage
        val percent = abs(drop) / pillarVoltage * 100.0
        val classification = when {
            percent <= 3.0 -> TechnicalClassification.ACCEPTABLE
            percent <= 5.0 -> TechnicalClassification.REQUIRES_REVIEW
            else -> TechnicalClassification.CRITICAL_REVIEW
        }
        return AutoInspectionCalculation(
            id = "auto:voltage-drop:measured",
            title = "Caída de tensión medida pilar-tablero",
            primaryResult = "${TechnicalValueFormatter.withUnit(percent, "%")} · ${classification.shortLabel()}",
            detail = "Pilar ${TechnicalValueFormatter.withUnit(pillarVoltage, "V")} / tablero ${TechnicalValueFormatter.withUnit(panelVoltage, "V")} / diferencia ${TechnicalValueFormatter.withUnit(drop, "V")}.",
            classification = classification,
        )
    }

    private fun buildCalculatedVoltageDrop(aggregate: InspectionAggregate): AutoInspectionCalculation? {
        val panel = aggregate.mainPanel ?: return null
        val section = panel.feederConductorSectionMm2 ?: return null
        val length = panel.feederDistanceMeters ?: return null
        val current = aggregate.pillar?.let { it.mainBreakerAmps ?: it.mainBreakerOtherAmps }?.toDouble()
            ?: aggregate.mainPanelCircuits.filterNot(MainPanelCircuit::isDeleted).mapNotNull { it.consumptionAmps }.maxOrNull()
            ?: return null
        val voltage = aggregate.pillarMeasurements.firstVoltageValue()
            ?: aggregate.mainPanelMeasurements.firstInputVoltageValue()
            ?: defaultNominalVoltage(aggregate.inspection.supplyType)
        val material = panel.feederConductorMaterial.toTechnicalMaterial() ?: return null
        val result = VoltageDropCalculator.calculate(
            VoltageDropInput(
                systemType = aggregate.inspection.supplyType.toElectricalSystemType(),
                nominalVoltageVolts = voltage,
                currentMode = VoltageDropCurrentMode.DIRECT_CURRENT,
                currentAmps = current,
                activePowerWatts = null,
                powerFactor = null,
                efficiency = null,
                conductorLengthMeters = length,
                conductorSectionMm2 = section,
                conductorMaterial = material,
                temperatureMode = TemperatureMode.NOT_CONSIDERED,
                conductorTemperatureCelsius = null,
                source = CalculationSource.CALCULATED,
            ),
        ).value ?: return null
        return AutoInspectionCalculation(
            id = "auto:voltage-drop:calculated",
            title = "Caída de tensión calculada del alimentador",
            primaryResult = "${TechnicalValueFormatter.withUnit(result.voltageDropPercent, "%")} · ${result.classification.shortLabel()}",
            detail = "${TechnicalValueFormatter.withUnit(length, "m")}, ${TechnicalValueFormatter.withUnit(section, "mm²")}, ${TechnicalValueFormatter.withUnit(result.currentUsedAmps, "A")} usados para estimar ${TechnicalValueFormatter.withUnit(result.voltageDropVolts, "V")}.",
            classification = result.classification,
        )
    }

    private fun buildGroundingAssessment(aggregate: InspectionAggregate): AutoInspectionCalculation? {
        val panel = aggregate.mainPanel ?: return null
        val checked = listOf(panel.groundBarPresent, panel.neutralAndGroundSeparated).any { it != YesNoUnknown.UNKNOWN } ||
            panel.protectionConductorsPresent != YesNoPartialUnknown.UNKNOWN ||
            panel.protectionConductorCheckResult != ProtectionConductorCheckResult.NOT_VERIFIED ||
            aggregate.mainPanelMeasurements.any { it.type == MainPanelMeasurementType.PROTECTION_VOLTAGE_NEUTRAL_GROUND && it.value != null && !it.isDeleted }
        if (!checked) return null
        val neutralGround = aggregate.mainPanelMeasurements
            .firstOrNull { it.type == MainPanelMeasurementType.PROTECTION_VOLTAGE_NEUTRAL_GROUND && it.value != null && !it.isDeleted }
            ?.value
        val classification = when {
            panel.groundBarPresent == YesNoUnknown.NO ||
                panel.neutralAndGroundSeparated == YesNoUnknown.NO ||
                panel.protectionConductorsPresent == YesNoPartialUnknown.NO ||
                panel.protectionConductorCheckResult == ProtectionConductorCheckResult.REQUIRES_REVIEW ||
                (neutralGround != null && neutralGround > 5.0) -> TechnicalClassification.CRITICAL_REVIEW
            panel.groundBarPresent == YesNoUnknown.YES &&
                panel.neutralAndGroundSeparated == YesNoUnknown.YES &&
                (panel.protectionConductorsPresent == YesNoPartialUnknown.YES || panel.protectionConductorsPresent == YesNoPartialUnknown.PARTIAL) &&
                (neutralGround == null || neutralGround <= 2.0) -> TechnicalClassification.ACCEPTABLE
            else -> TechnicalClassification.REQUIRES_REVIEW
        }
        return AutoInspectionCalculation(
            id = "auto:grounding:basic",
            title = "Puesta a tierra básica",
            primaryResult = classification.shortLabel(),
            detail = listOfNotNull(
                "Bornera: ${panel.groundBarPresent.basicLabel()}",
                "neutro/tierra separados: ${panel.neutralAndGroundSeparated.basicLabel()}",
                "conductores PE: ${panel.protectionConductorsPresent.basicLabel()}",
                neutralGround?.let { "N-PE ${TechnicalValueFormatter.withUnit(it, "V")}" },
                "orientativo; no reemplaza telurómetro",
            ).joinToString(" · "),
            classification = classification,
        )
    }

    private fun List<PillarMeasurement>.firstVoltageValue(): Double? = firstOrNull {
        !it.isDeleted && it.value != null && it.type in setOf(
            PillarMeasurementType.SINGLE_PHASE_VOLTAGE_LN,
            PillarMeasurementType.VOLTAGE_L1_N,
            PillarMeasurementType.VOLTAGE_L2_N,
            PillarMeasurementType.VOLTAGE_L3_N,
        )
    }?.value

    private fun List<MainPanelMeasurement>.firstInputVoltageValue(): Double? = firstOrNull {
        !it.isDeleted && it.value != null && it.type in setOf(
            MainPanelMeasurementType.INPUT_VOLTAGE_LN,
            MainPanelMeasurementType.INPUT_VOLTAGE_L1_N,
            MainPanelMeasurementType.INPUT_VOLTAGE_L2_N,
            MainPanelMeasurementType.INPUT_VOLTAGE_L3_N,
        )
    }?.value

    private fun ampacityReference(sectionMm2: Double, material: ConductorMaterial): Double? {
        val copper = when {
            sectionMm2 <= 1.5 -> 10.0
            sectionMm2 <= 2.5 -> 16.0
            sectionMm2 <= 4.0 -> 20.0
            sectionMm2 <= 6.0 -> 25.0
            sectionMm2 <= 10.0 -> 40.0
            sectionMm2 <= 16.0 -> 55.0
            sectionMm2 <= 25.0 -> 75.0
            else -> null
        } ?: return null
        return when (material) {
            ConductorMaterial.COPPER -> copper
            ConductorMaterial.ALUMINUM -> copper * 0.75
            ConductorMaterial.OTHER, ConductorMaterial.UNKNOWN -> null
        }
    }

    private fun ConductorMaterial.toTechnicalMaterial(): TechnicalConductorMaterial? = when (this) {
        ConductorMaterial.COPPER -> TechnicalConductorMaterial.COPPER
        ConductorMaterial.ALUMINUM -> TechnicalConductorMaterial.ALUMINUM
        ConductorMaterial.OTHER, ConductorMaterial.UNKNOWN -> null
    }

    private fun SupplyType.toElectricalSystemType(): ElectricalSystemType = when (this) {
        SupplyType.THREE_PHASE -> ElectricalSystemType.AC_THREE_PHASE
        SupplyType.SINGLE_PHASE, SupplyType.UNKNOWN -> ElectricalSystemType.AC_SINGLE_PHASE
    }

    private fun defaultNominalVoltage(supplyType: SupplyType): Double = when (supplyType) {
        SupplyType.THREE_PHASE -> 380.0
        SupplyType.SINGLE_PHASE, SupplyType.UNKNOWN -> 220.0
    }

    private fun TechnicalClassification.shortLabel(): String = when (this) {
        TechnicalClassification.ACCEPTABLE -> "aceptable"
        TechnicalClassification.REQUIRES_REVIEW -> "revisar"
        TechnicalClassification.CRITICAL_REVIEW -> "crítico"
        TechnicalClassification.INFORMATIONAL -> "informativo"
        TechnicalClassification.NOT_CLASSIFIED -> "sin clasificar"
    }

    private fun YesNoUnknown.basicLabel(): String = when (this) {
        YesNoUnknown.YES -> "sí"
        YesNoUnknown.NO -> "no"
        YesNoUnknown.UNKNOWN -> "sin verificar"
    }

    private fun YesNoPartialUnknown.basicLabel(): String = when (this) {
        YesNoPartialUnknown.YES -> "sí"
        YesNoPartialUnknown.NO -> "no"
        YesNoPartialUnknown.PARTIAL -> "parcial"
        YesNoPartialUnknown.UNKNOWN -> "sin verificar"
    }
}
