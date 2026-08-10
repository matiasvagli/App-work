package com.matiasdev.elecapp.features.inspections.domain

import com.matiasdev.elecapp.features.electricalrules.domain.ElectricalRuleCode
import com.matiasdev.elecapp.features.electricalrules.domain.ElectricalRuleConfig
import com.matiasdev.elecapp.features.electricaltools.calculators.VoltageDropCalculator
import com.matiasdev.elecapp.features.electricaltools.domain.CalculationSource
import com.matiasdev.elecapp.features.electricaltools.domain.ElectricalSystemType
import com.matiasdev.elecapp.features.electricaltools.domain.TechnicalClassification
import com.matiasdev.elecapp.features.electricaltools.domain.TechnicalConductorMaterial
import com.matiasdev.elecapp.features.electricaltools.domain.TemperatureMode
import com.matiasdev.elecapp.features.electricaltools.domain.VoltageDropCurrentMode
import com.matiasdev.elecapp.features.electricaltools.domain.VoltageDropInput
import com.matiasdev.elecapp.features.electricaltools.domain.VoltageDropResult
import kotlin.math.abs

data class MeasuredVoltageDropResult(
    val sourceVoltageVolts: Double,
    val destinationVoltageVolts: Double,
    val differenceVolts: Double,
    val percent: Double,
    val classification: TechnicalClassification,
    /** Umbral efectivo con el que se clasificó, ya resuelto el default. */
    val limitPercent: Double,
)

data class FeederVoltageDropEstimate(
    val lengthMeters: Double,
    val sectionMm2: Double,
    val material: ConductorMaterial,
    val currentAmps: Double,
    val currentOrigin: MeasurementOrigin,
    val result: VoltageDropResult,
    val classification: TechnicalClassification,
    /** Umbral efectivo con el que se clasificó, ya resuelto el default. */
    val limitPercent: Double,
)

data class FeederVoltageDropReview(
    val measuredPercent: Double,
    val estimatedPercent: Double,
    val classification: TechnicalClassification,
)

object VoltageDropMeasuredEvaluator {
    fun evaluate(
        sourceVoltageVolts: Double?,
        destinationVoltageVolts: Double?,
        maxAllowedPercent: Double?,
    ): MeasuredVoltageDropResult? {
        val source = sourceVoltageVolts?.takeIf { it.isFinite() && it > 0.0 } ?: return null
        val destination = destinationVoltageVolts?.takeIf { it.isFinite() && it > 0.0 } ?: return null
        val difference = source - destination
        val percent = abs(difference) / source * 100.0
        val limit = VoltageDropClassification.limit(maxAllowedPercent)
        return MeasuredVoltageDropResult(
            sourceVoltageVolts = source,
            destinationVoltageVolts = destination,
            differenceVolts = difference,
            percent = percent,
            classification = VoltageDropClassification.classify(percent, maxAllowedPercent),
            limitPercent = limit,
        )
    }
}

object FeederVoltageDropCalculator {
    fun calculate(
        supplyType: SupplyType,
        sourceVoltageVolts: Double?,
        destinationVoltageVolts: Double?,
        lengthMeters: Double?,
        sectionMm2: Double?,
        material: ConductorMaterial,
        currentAmps: Double?,
        currentOrigin: MeasurementOrigin,
        maxAllowedPercent: Double?,
    ): FeederVoltageDropEstimate? {
        val length = lengthMeters?.takeIf { it.isFinite() && it > 0.0 } ?: return null
        val section = sectionMm2?.takeIf { it.isFinite() && it > 0.0 } ?: return null
        val current = currentAmps?.takeIf { it.isFinite() && it > 0.0 } ?: return null
        if (currentOrigin == MeasurementOrigin.NOT_VERIFIED) return null
        val technicalMaterial = material.toTechnicalMaterial() ?: return null
        val voltage = sourceVoltageVolts?.takeIf { it.isFinite() && it > 0.0 }
            ?: destinationVoltageVolts?.takeIf { it.isFinite() && it > 0.0 }
            ?: defaultNominalVoltage(supplyType)
        val result = VoltageDropCalculator.calculate(
            VoltageDropInput(
                systemType = supplyType.toElectricalSystemType(),
                nominalVoltageVolts = voltage,
                currentMode = VoltageDropCurrentMode.DIRECT_CURRENT,
                currentAmps = current,
                activePowerWatts = null,
                powerFactor = null,
                efficiency = null,
                conductorLengthMeters = length,
                conductorSectionMm2 = section,
                conductorMaterial = technicalMaterial,
                temperatureMode = TemperatureMode.NOT_CONSIDERED,
                conductorTemperatureCelsius = null,
                source = CalculationSource.CALCULATED,
            ),
        ).value ?: return null
        return FeederVoltageDropEstimate(
            lengthMeters = length,
            sectionMm2 = section,
            material = material,
            currentAmps = current,
            currentOrigin = currentOrigin,
            result = result,
            classification = VoltageDropClassification.classify(result.voltageDropPercent, maxAllowedPercent),
            limitPercent = VoltageDropClassification.limit(maxAllowedPercent),
        )
    }
}

object FeederVoltageDropEvaluator {
    fun compare(
        measured: MeasuredVoltageDropResult?,
        estimated: FeederVoltageDropEstimate?,
    ): FeederVoltageDropReview? {
        if (measured == null || estimated == null) return null
        val estimatedPercent = estimated.result.voltageDropPercent
        val difference = measured.percent - estimatedPercent
        val significantlyHigher = measured.percent >= estimatedPercent * 2.0 && difference >= 2.0
        if (!significantlyHigher) return null
        return FeederVoltageDropReview(
            measuredPercent = measured.percent,
            estimatedPercent = estimatedPercent,
            classification = TechnicalClassification.REQUIRES_REVIEW,
        )
    }
}

/**
 * Extrae del relevamiento los datos del alimentador pilar-tablero y evalúa la caída de
 * tensión.
 *
 * Vive acá y no adentro de un builder porque lo consumen dos: el que arma los cálculos
 * [AUTO] del informe y el que propone los hallazgos. Si cada uno leyera las mediciones y
 * el umbral por su cuenta, el mismo informe podría decir "aceptable" en un lado y abrir
 * un hallazgo en el otro.
 */
object InspectionFeederVoltageDrop {

    fun measured(
        aggregate: InspectionAggregate,
        rules: List<ElectricalRuleConfig>,
    ): MeasuredVoltageDropResult? {
        val pillarVoltage = aggregate.pillarMeasurements.firstVoltageValue() ?: return null
        val panelVoltage = aggregate.mainPanelMeasurements.firstInputVoltageValue() ?: return null
        return VoltageDropMeasuredEvaluator.evaluate(
            sourceVoltageVolts = pillarVoltage,
            destinationVoltageVolts = panelVoltage,
            maxAllowedPercent = rules.maxVoltageDropPercent(),
        )
    }

    fun estimated(
        aggregate: InspectionAggregate,
        rules: List<ElectricalRuleConfig>,
    ): FeederVoltageDropEstimate? {
        val panel = aggregate.mainPanel ?: return null
        val current = aggregate.pillarMeasurements.feederMeasuredCurrent() ?: return null
        return FeederVoltageDropCalculator.calculate(
            supplyType = aggregate.inspection.supplyType,
            sourceVoltageVolts = aggregate.pillarMeasurements.firstVoltageValue(),
            destinationVoltageVolts = aggregate.mainPanelMeasurements.firstInputVoltageValue(),
            lengthMeters = panel.feederDistanceMeters,
            sectionMm2 = panel.feederConductorSectionMm2,
            material = panel.feederConductorMaterial,
            currentAmps = current,
            currentOrigin = MeasurementOrigin.MEASURED,
            maxAllowedPercent = rules.maxVoltageDropPercent(),
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

    private fun List<PillarMeasurement>.feederMeasuredCurrent(): Double? {
        return filter {
            !it.isDeleted && it.value != null && it.origin == MeasurementOrigin.MEASURED && it.type in setOf(
                PillarMeasurementType.SINGLE_PHASE_CURRENT,
                PillarMeasurementType.CURRENT_L1,
                PillarMeasurementType.CURRENT_L2,
                PillarMeasurementType.CURRENT_L3,
                PillarMeasurementType.CURRENT_NEUTRAL,
            )
        }.mapNotNull { it.value }.maxOrNull()
    }

    private fun List<MainPanelMeasurement>.firstInputVoltageValue(): Double? = firstOrNull {
        !it.isDeleted && it.value != null && it.type in setOf(
            MainPanelMeasurementType.INPUT_VOLTAGE_LN,
            MainPanelMeasurementType.INPUT_VOLTAGE_L1_N,
            MainPanelMeasurementType.INPUT_VOLTAGE_L2_N,
            MainPanelMeasurementType.INPUT_VOLTAGE_L3_N,
        )
    }?.value

    private fun List<ElectricalRuleConfig>.maxVoltageDropPercent(): Double? {
        return firstOrNull { it.code == ElectricalRuleCode.MAX_FEEDER_VOLTAGE_DROP_PERCENT && it.enabled }?.numericValue
    }
}

private object VoltageDropClassification {
    /** Umbral por defecto cuando la regla está deshabilitada o sin valor válido. */
    private const val DEFAULT_MAX_PERCENT = 3.0

    fun limit(maxAllowedPercent: Double?): Double =
        maxAllowedPercent?.takeIf { it.isFinite() && it > 0.0 } ?: DEFAULT_MAX_PERCENT

    fun classify(percent: Double, maxAllowedPercent: Double?): TechnicalClassification {
        return if (percent <= limit(maxAllowedPercent)) {
            TechnicalClassification.ACCEPTABLE
        } else {
            TechnicalClassification.CRITICAL_REVIEW
        }
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
