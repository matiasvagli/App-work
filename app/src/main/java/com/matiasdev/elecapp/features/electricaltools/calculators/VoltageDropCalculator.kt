package com.matiasdev.elecapp.features.electricaltools.calculators

import com.matiasdev.elecapp.features.electricaltools.domain.CalculationValidationResult
import com.matiasdev.elecapp.features.electricaltools.domain.ElectricalSystemType
import com.matiasdev.elecapp.features.electricaltools.domain.ElectricalVariable
import com.matiasdev.elecapp.features.electricaltools.domain.PowerCurrentVoltageInput
import com.matiasdev.elecapp.features.electricaltools.domain.TechnicalClassification
import com.matiasdev.elecapp.features.electricaltools.domain.TechnicalConductorMaterial
import com.matiasdev.elecapp.features.electricaltools.domain.TemperatureMode
import com.matiasdev.elecapp.features.electricaltools.domain.VoltageDropCurrentMode
import com.matiasdev.elecapp.features.electricaltools.domain.VoltageDropInput
import com.matiasdev.elecapp.features.electricaltools.domain.VoltageDropResult
import kotlin.math.sqrt

object VoltageDropCalculator {
    const val FORMULA_VERSION = "voltage-drop-resistive-v1"
    const val TECHNICAL_DISCLAIMER =
        "Cálculo aproximado basado en resistencia del conductor. No incluye todas las variables posibles de una instalación real."

    fun calculate(input: VoltageDropInput): CalculationValidationResult<VoltageDropResult> {
        val errors = validate(input).toMutableList()
        val assumptions = mutableListOf<String>()
        val current = resolveCurrent(input, errors, assumptions)
        if (errors.isNotEmpty() || current == null) return CalculationValidationResult(null, errors)

        return runCatching {
            val resistivity = VoltageDropConstants.resistivity(input.conductorMaterial, input.temperatureMode, input.conductorTemperatureCelsius)
            val factor = when (input.systemType) {
                ElectricalSystemType.DC, ElectricalSystemType.AC_SINGLE_PHASE -> 2.0
                ElectricalSystemType.AC_THREE_PHASE -> sqrt(3.0)
            }
            val voltageDrop = factor * input.conductorLengthMeters * current * resistivity / input.conductorSectionMm2
            val percent = voltageDrop / input.nominalVoltageVolts * 100.0
            val classification = VoltageDropAssessmentConfig.classify(percent)
            listOf(voltageDrop, percent).forEach { require(it.isFinite() && it >= 0.0) }
            CalculationValidationResult(
                VoltageDropResult(
                    voltageDropVolts = voltageDrop,
                    voltageDropPercent = percent,
                    estimatedEndVoltageVolts = input.nominalVoltageVolts - voltageDrop,
                    currentUsedAmps = current,
                    derivedCurrent = input.currentMode == VoltageDropCurrentMode.DERIVED_FROM_POWER,
                    systemType = input.systemType,
                    nominalVoltageVolts = input.nominalVoltageVolts,
                    conductorLengthMeters = input.conductorLengthMeters,
                    conductorSectionMm2 = input.conductorSectionMm2,
                    conductorMaterial = input.conductorMaterial,
                    resistivityOhmMm2PerMeter = resistivity,
                    temperatureMode = input.temperatureMode,
                    conductorTemperatureCelsius = input.conductorTemperatureCelsius,
                    classification = classification,
                    assessmentThresholdPercent = VoltageDropAssessmentConfig.thresholdFor(classification),
                    assessmentConfigVersion = VoltageDropAssessmentConfig.VERSION,
                    assumptionsUsed = assumptions + listOfNotNull(input.context.assumptions?.takeIf(String::isNotBlank)),
                    formulaVersion = FORMULA_VERSION,
                ),
            )
        }.getOrElse { CalculationValidationResult(null, listOf("No se pudo calcular la caída de tensión con los valores ingresados.")) }
    }

    private fun resolveCurrent(
        input: VoltageDropInput,
        errors: MutableList<String>,
        assumptions: MutableList<String>,
    ): Double? = when (input.currentMode) {
        VoltageDropCurrentMode.DIRECT_CURRENT -> input.currentAmps
        VoltageDropCurrentMode.DERIVED_FROM_POWER -> {
            val powerResult = PowerCurrentVoltageCalculator.calculate(
                PowerCurrentVoltageInput(
                    systemType = input.systemType,
                    variableToCalculate = ElectricalVariable.CURRENT,
                    voltageVolts = input.nominalVoltageVolts,
                    currentAmps = null,
                    activePowerWatts = input.activePowerWatts,
                    powerFactor = input.powerFactor,
                    efficiency = input.efficiency,
                    source = input.source,
                    context = input.context,
                ),
            )
            if (!powerResult.isValid) {
                errors += powerResult.errors
                null
            } else {
                assumptions += "Corriente derivada desde potencia activa."
                assumptions += powerResult.value!!.assumptionsUsed
                powerResult.value.currentAmps
            }
        }
    }

    private fun validate(input: VoltageDropInput): List<String> = buildList {
        checkPositive(input.nominalVoltageVolts, "La tensión nominal")
        if (input.currentMode == VoltageDropCurrentMode.DIRECT_CURRENT) checkPositive(input.currentAmps, "La corriente")
        if (input.currentMode == VoltageDropCurrentMode.DERIVED_FROM_POWER) checkPositive(input.activePowerWatts, "La potencia")
        checkPositive(input.conductorLengthMeters, "La longitud")
        checkPositive(input.conductorSectionMm2, "La sección del conductor")
        input.powerFactor?.let { if (!it.isFinite() || it <= 0.0 || it > 1.0) add("El factor de potencia debe ser mayor que cero y menor o igual a 1.") }
        input.efficiency?.let { if (!it.isFinite() || it <= 0.0 || it > 1.0) add("La eficiencia debe ser mayor que cero y menor o igual a 1.") }
        if (input.temperatureMode == TemperatureMode.CUSTOM) checkPositive(input.conductorTemperatureCelsius, "La temperatura")
    }

    private fun MutableList<String>.checkPositive(value: Double?, label: String) {
        if (value == null) add("$label es requerida.") else if (!value.isFinite() || value <= 0.0) add("$label debe ser mayor que cero.")
    }
}

object VoltageDropConstants {
    const val VERSION = "resistivity-20c-v1"
    private const val COPPER_RHO_20 = 0.017241
    private const val ALUMINUM_RHO_20 = 0.028264
    private const val COPPER_ALPHA = 0.00393
    private const val ALUMINUM_ALPHA = 0.00403

    fun resistivity(material: TechnicalConductorMaterial, mode: TemperatureMode, temperatureCelsius: Double?): Double {
        val rho20 = when (material) {
            TechnicalConductorMaterial.COPPER -> COPPER_RHO_20
            TechnicalConductorMaterial.ALUMINUM -> ALUMINUM_RHO_20
        }
        if (mode != TemperatureMode.CUSTOM || temperatureCelsius == null) return rho20
        val alpha = when (material) {
            TechnicalConductorMaterial.COPPER -> COPPER_ALPHA
            TechnicalConductorMaterial.ALUMINUM -> ALUMINUM_ALPHA
        }
        return rho20 * (1 + alpha * (temperatureCelsius - 20.0))
    }
}

object VoltageDropAssessmentConfig {
    const val VERSION = "voltage-drop-orientative-thresholds-v1"
    private const val ACCEPTABLE_LIMIT_PERCENT = 3.0
    private const val REVIEW_LIMIT_PERCENT = 5.0

    fun classify(percent: Double): TechnicalClassification = when {
        !percent.isFinite() -> TechnicalClassification.NOT_CLASSIFIED
        percent <= ACCEPTABLE_LIMIT_PERCENT -> TechnicalClassification.ACCEPTABLE
        percent <= REVIEW_LIMIT_PERCENT -> TechnicalClassification.REQUIRES_REVIEW
        else -> TechnicalClassification.CRITICAL_REVIEW
    }

    fun thresholdFor(classification: TechnicalClassification): Double? = when (classification) {
        TechnicalClassification.ACCEPTABLE -> ACCEPTABLE_LIMIT_PERCENT
        TechnicalClassification.REQUIRES_REVIEW -> REVIEW_LIMIT_PERCENT
        TechnicalClassification.CRITICAL_REVIEW -> REVIEW_LIMIT_PERCENT
        else -> null
    }
}
