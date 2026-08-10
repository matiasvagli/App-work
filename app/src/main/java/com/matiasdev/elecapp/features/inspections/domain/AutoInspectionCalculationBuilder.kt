package com.matiasdev.elecapp.features.inspections.domain

import com.matiasdev.elecapp.features.electricalrules.domain.DefaultElectricalRuleConfigs
import com.matiasdev.elecapp.features.electricalrules.domain.ElectricalRuleCode
import com.matiasdev.elecapp.features.electricalrules.domain.ElectricalRuleConfig
import com.matiasdev.elecapp.features.electricaltools.domain.TechnicalClassification
import com.matiasdev.elecapp.features.electricaltools.summary.TechnicalValueFormatter

data class AutoInspectionCalculation(
    val id: String,
    val title: String,
    val primaryResult: String,
    val detail: String,
    val classification: TechnicalClassification,
)

object AutoInspectionCalculationBuilder {
    fun build(
        aggregate: InspectionAggregate,
        rules: List<ElectricalRuleConfig> = DefaultElectricalRuleConfigs.all,
    ): List<AutoInspectionCalculation> = buildList {
        addAll(buildProtectionCompatibility(aggregate, rules))
        addAll(buildConsumptionCompatibility(aggregate))
        val measuredVoltageDrop = InspectionFeederVoltageDrop.measured(aggregate, rules)
        val estimatedVoltageDrop = InspectionFeederVoltageDrop.estimated(aggregate, rules)
        measuredVoltageDrop?.toAutoCalculation()?.let(::add)
        estimatedVoltageDrop?.toAutoCalculation()?.let(::add)
        FeederVoltageDropEvaluator.compare(measuredVoltageDrop, estimatedVoltageDrop)?.toAutoCalculation()?.let(::add)
        buildGroundingAssessment(aggregate, rules)?.let(::add)
    }.distinctBy { it.id }

    private fun buildProtectionCompatibility(
        aggregate: InspectionAggregate,
        rules: List<ElectricalRuleConfig>,
    ): List<AutoInspectionCalculation> = buildList {
        aggregate.pillar?.let { pillar ->
            val breaker = pillar.mainBreakerAmps ?: pillar.mainBreakerOtherAmps
            val section = pillar.conductorSectionMm2 ?: pillar.conductorOtherSectionMm2
            ProtectionConductorCompatibilityEvaluator
                .evaluate(breaker, section, pillar.conductorMaterial, rules)
                ?.toAutoCalculation("auto:pillar:protection", "Pilar")
                ?.let(::add)
        }
        aggregate.mainPanelCircuits.reportableCircuitsInReportOrder().forEachIndexed { index, circuit ->
            val breaker = circuit.breakerAmps ?: circuit.breakerOtherAmps
            val section = circuit.conductorSectionMm2 ?: circuit.conductorOtherSectionMm2
            val circuitName = circuit.reportCircuitName(index)
            ProtectionConductorCompatibilityEvaluator
                .evaluate(breaker, section, circuit.conductorMaterial, rules)
                ?.toAutoCalculation("auto:circuit:${circuit.id}:protection", circuitName)
                ?.let(::add)
        }
    }

    private fun buildConsumptionCompatibility(aggregate: InspectionAggregate): List<AutoInspectionCalculation> = buildList {
        aggregate.mainPanelCircuits.reportableCircuitsInReportOrder().forEachIndexed { index, circuit ->
            val breaker = circuit.breakerAmps ?: circuit.breakerOtherAmps
            val consumption = circuit.consumptionAmps
            if (breaker != null && consumption != null && circuit.consumptionOrigin != MeasurementOrigin.NOT_VERIFIED) {
                val circuitName = circuit.reportCircuitName(index)
                val evaluation = ProtectionLoadEvaluator.evaluate(consumption, breaker) ?: return@forEachIndexed
                add(
                    AutoInspectionCalculation(
                        id = "auto:circuit:${circuit.id}:consumption-breaker",
                        title = "$circuitName: consumo y térmica",
                        primaryResult = "${TechnicalValueFormatter.withUnit(consumption, "A")} sobre ${breaker} A · ${evaluation.label}",
                        detail = evaluation.detailText(),
                        classification = evaluation.classification,
                    ),
                )
            }
        }
    }

    private fun ProtectionConductorCompatibilityEvaluation.toAutoCalculation(
        id: String,
        title: String,
    ): AutoInspectionCalculation {
        return AutoInspectionCalculation(
            id = id,
            title = "$title: compatibilidad térmica-conductor",
            primaryResult = primaryResultText(),
            detail = detailText(),
            classification = classification,
        )
    }

    private fun MeasuredVoltageDropResult.toAutoCalculation(): AutoInspectionCalculation {
        return AutoInspectionCalculation(
            id = "auto:voltage-drop:measured",
            title = "Caída de tensión medida pilar-tablero",
            primaryResult = "${TechnicalValueFormatter.withUnit(percent, "%")} · ${classification.shortLabel()}",
            detail = "Pilar ${TechnicalValueFormatter.withUnit(sourceVoltageVolts, "V")} / tablero ${TechnicalValueFormatter.withUnit(destinationVoltageVolts, "V")} / diferencia ${TechnicalValueFormatter.withUnit(differenceVolts, "V")}.",
            classification = classification,
        )
    }

    private fun FeederVoltageDropEstimate.toAutoCalculation(): AutoInspectionCalculation {
        return AutoInspectionCalculation(
            id = "auto:voltage-drop:estimated",
            title = "Caída de tensión estimada del alimentador",
            primaryResult = "${TechnicalValueFormatter.withUnit(result.voltageDropPercent, "%")} · ${classification.shortLabel()}",
            detail = "${TechnicalValueFormatter.withUnit(lengthMeters, "m")} / ${material.basicLabel()} / ${TechnicalValueFormatter.withUnit(sectionMm2, "mm²")} / ${TechnicalValueFormatter.withUnit(currentAmps, "A")} ${currentOrigin.basicLabel()}s. Estimación: ${TechnicalValueFormatter.withUnit(result.voltageDropVolts, "V")}.",
            classification = classification,
        )
    }

    private fun FeederVoltageDropReview.toAutoCalculation(): AutoInspectionCalculation {
        return AutoInspectionCalculation(
            id = "auto:voltage-drop:feeder-review",
            title = "Alimentador pilar-tablero",
            primaryResult = "requiere revisión",
            detail = "La caída medida es significativamente superior a la estimada para los datos relevados. Verificar conexiones, empalmes, bornes, continuidad, sección efectiva, longitud real y estado de los conductores.",
            classification = classification,
        )
    }

    private fun buildGroundingAssessment(
        aggregate: InspectionAggregate,
        rules: List<ElectricalRuleConfig>,
    ): AutoInspectionCalculation? {
        val panel = aggregate.mainPanel
        val grounding = aggregate.grounding
        val checked = grounding != null ||
            panel?.let { listOf(it.groundBarPresent, it.neutralAndGroundSeparated).any { value -> value != YesNoUnknown.UNKNOWN } } == true ||
            panel?.protectionConductorsPresent != null && panel.protectionConductorsPresent != YesNoPartialUnknown.UNKNOWN ||
            panel?.protectionConductorCheckResult != null && panel.protectionConductorCheckResult != ProtectionConductorCheckResult.NOT_VERIFIED ||
            aggregate.mainPanelMeasurements.any { it.type == MainPanelMeasurementType.PROTECTION_VOLTAGE_NEUTRAL_GROUND && it.value != null && !it.isDeleted }
        if (!checked) return null
        val neutralGround = aggregate.mainPanelMeasurements
            .firstOrNull { it.type == MainPanelMeasurementType.PROTECTION_VOLTAGE_NEUTRAL_GROUND && it.value != null && !it.isDeleted }
            ?.value
        val resistanceLimit = rules.firstOrNull { it.code == ElectricalRuleCode.MAX_GROUND_RESISTANCE_OHMS && it.enabled }?.numericValue
        val measuredResistance = grounding?.resistanceOhms?.takeIf { grounding.resistanceOrigin != MeasurementOrigin.NOT_VERIFIED }
        val classification = when {
            grounding?.electrodePresent == YesNoUnknown.NO ||
                grounding?.mainGroundConductorPresent == YesNoUnknown.NO ||
                grounding?.protectiveConductorContinuity == YesNoUnknown.NO ||
                panel?.groundBarPresent == YesNoUnknown.NO ||
                panel?.neutralAndGroundSeparated == YesNoUnknown.NO ||
                panel?.protectionConductorsPresent == YesNoPartialUnknown.NO ||
                panel?.protectionConductorCheckResult == ProtectionConductorCheckResult.REQUIRES_REVIEW ||
                (measuredResistance != null && resistanceLimit != null && measuredResistance > resistanceLimit) ||
                (neutralGround != null && neutralGround > 5.0) -> TechnicalClassification.CRITICAL_REVIEW
            measuredResistance == null -> TechnicalClassification.NOT_CLASSIFIED
            (resistanceLimit == null || measuredResistance <= resistanceLimit) &&
                grounding?.electrodePresent == YesNoUnknown.YES &&
                grounding.mainGroundConductorPresent == YesNoUnknown.YES &&
                grounding.protectiveConductorContinuity == YesNoUnknown.YES &&
                panel?.groundBarPresent == YesNoUnknown.YES &&
                panel.neutralAndGroundSeparated == YesNoUnknown.YES &&
                (panel.protectionConductorsPresent == YesNoPartialUnknown.YES || panel.protectionConductorsPresent == YesNoPartialUnknown.PARTIAL) &&
                (neutralGround == null || neutralGround <= 2.0) -> TechnicalClassification.ACCEPTABLE
            else -> TechnicalClassification.REQUIRES_REVIEW
        }
        return AutoInspectionCalculation(
            id = "auto:grounding:basic",
            title = "Puesta a tierra",
            primaryResult = classification.shortLabel(),
            detail = listOfNotNull(
                grounding?.let { "Electrodo: ${it.electrodePresent.basicLabel()}" },
                grounding?.let { "conductor principal: ${it.mainGroundConductorPresent.basicLabel()}" },
                panel?.let { "bornera: ${it.groundBarPresent.basicLabel()}" },
                panel?.let { "neutro/tierra separados: ${it.neutralAndGroundSeparated.basicLabel()}" },
                panel?.let { "conductores PE: ${it.protectionConductorsPresent.basicLabel()}" },
                measuredResistance?.let { "resistencia ${TechnicalValueFormatter.withUnit(it, "Ω")}" },
                resistanceLimit?.let { "máximo configurado ${TechnicalValueFormatter.withUnit(it, "Ω")}" },
                neutralGround?.let { "N-PE ${TechnicalValueFormatter.withUnit(it, "V")}" },
                if (measuredResistance == null) "resistencia no verificada con telurómetro" else null,
            ).joinToString(" · "),
            classification = classification,
        )
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

    private fun MeasurementOrigin.basicLabel(): String = when (this) {
        MeasurementOrigin.MEASURED -> "medido"
        MeasurementOrigin.ESTIMATED -> "estimado"
        MeasurementOrigin.CALCULATED -> "calculado"
        MeasurementOrigin.DECLARED_BY_CLIENT -> "declarado"
        MeasurementOrigin.NOT_VERIFIED -> "no verificado"
    }

    private fun ConductorMaterial.basicLabel(): String = when (this) {
        ConductorMaterial.COPPER -> "cobre"
        ConductorMaterial.ALUMINUM -> "aluminio"
        ConductorMaterial.OTHER -> "otro material"
        ConductorMaterial.UNKNOWN -> "material no verificado"
    }
}
