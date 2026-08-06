package com.matiasdev.elecapp.features.electricalrules.domain

import com.matiasdev.elecapp.features.electricaltools.data.CalculationJson
import com.matiasdev.elecapp.features.electricaltools.domain.CalculationSource
import com.matiasdev.elecapp.features.electricaltools.domain.ElectricalVariable
import com.matiasdev.elecapp.features.electricaltools.domain.PowerCurrentVoltageInput
import com.matiasdev.elecapp.features.electricaltools.domain.PowerCurrentVoltageResult
import com.matiasdev.elecapp.features.electricaltools.domain.TechnicalCalculation
import com.matiasdev.elecapp.features.electricaltools.domain.TechnicalCalculationType

data class ElectricalMeasurementReviewSummary(
    val items: List<ElectricalMeasurementReviewItem> = emptyList(),
) {
    val anomalyCount: Int = items.size
    val hasAnomalies: Boolean = items.isNotEmpty()
    val indicatorText: String = when (anomalyCount) {
        0 -> ""
        1 -> "1 medición para revisar"
        else -> "$anomalyCount mediciones para revisar"
    }
}

data class ElectricalMeasurementReviewItem(
    val calculationId: String,
    val title: String,
    val measuredValue: Double,
    val unit: String,
    val minimumAllowed: Double?,
    val maximumAllowed: Double?,
    val reason: String,
)

object ElectricalMeasurementReviewEvaluator {
    suspend fun evaluateSupplyVoltage(
        calculations: List<TechnicalCalculation>,
        useCase: EvaluateSupplyVoltageUseCase,
    ): ElectricalMeasurementReviewSummary {
        val items = calculations.mapNotNull { calculation ->
            val measurement = calculation.supplyVoltageMeasurement() ?: return@mapNotNull null
            val evaluations = useCase(
                SupplyVoltageInput(
                    voltage = measurement.voltage,
                    location = measurement.location,
                    origin = calculation.source,
                    sourceCalculationId = calculation.id,
                    inspectionId = calculation.inspectionId,
                ),
            )
            val failed = evaluations.firstOrNull { it.status == ElectricalRuleEvaluationStatus.FAILED } ?: return@mapNotNull null
            ElectricalMeasurementReviewItem(
                calculationId = calculation.id,
                title = calculation.title,
                measuredValue = measurement.voltage,
                unit = failed.unit.orEmpty().ifBlank { "V" },
                minimumAllowed = evaluations.firstOrNull { it.ruleCode == ElectricalRuleCode.MIN_SUPPLY_VOLTAGE }?.limitValue,
                maximumAllowed = evaluations.firstOrNull { it.ruleCode == ElectricalRuleCode.MAX_SUPPLY_VOLTAGE }?.limitValue,
                reason = failed.explanation ?: "La tensión medida quedó fuera del rango configurado.",
            )
        }
        return ElectricalMeasurementReviewSummary(items)
    }

    suspend fun evaluateSupplyVoltage(
        calculation: TechnicalCalculation,
        useCase: EvaluateSupplyVoltageUseCase,
    ): ElectricalMeasurementReviewSummary {
        return evaluateSupplyVoltage(listOf(calculation), useCase)
    }

    private fun TechnicalCalculation.supplyVoltageMeasurement(): SupplyVoltageMeasurement? {
        if (type != TechnicalCalculationType.POWER_CURRENT_VOLTAGE) return null
        if (source != CalculationSource.MEASURED) return null
        val input = CalculationJson.decodePowerInput(inputDataJson) ?: return null
        val result = CalculationJson.decodePowerResult(resultDataJson)
        val voltage = input.measuredSupplyVoltage(result) ?: return null
        if (!voltage.isFinite() || voltage <= 0.0) return null
        return SupplyVoltageMeasurement(
            voltage = voltage,
            location = input.context.measurementContext,
        )
    }

    private fun PowerCurrentVoltageInput.measuredSupplyVoltage(result: PowerCurrentVoltageResult?): Double? {
        return voltageVolts ?: result?.takeIf { it.calculatedVariable == ElectricalVariable.VOLTAGE }?.voltageVolts
    }
}

private data class SupplyVoltageMeasurement(
    val voltage: Double,
    val location: String?,
)
