package com.matiasdev.elecapp.features.electricaltools.calculators

import com.matiasdev.elecapp.features.electricaltools.domain.CalculationSource
import com.matiasdev.elecapp.features.electricaltools.domain.CalculationValidationResult
import com.matiasdev.elecapp.features.electricaltools.domain.ElectricalSystemType
import com.matiasdev.elecapp.features.electricaltools.domain.ElectricalVariable
import com.matiasdev.elecapp.features.electricaltools.domain.PowerCurrentVoltageInput
import com.matiasdev.elecapp.features.electricaltools.domain.PowerCurrentVoltageResult
import kotlin.math.sqrt

object PowerCurrentVoltageCalculator {
    const val FORMULA_VERSION = "power-current-voltage-resistive-v1"

    fun calculate(input: PowerCurrentVoltageInput): CalculationValidationResult<PowerCurrentVoltageResult> {
        val errors = mutableListOf<String>()
        val assumptions = mutableListOf<String>()
        validateRequired(input, errors)
        val powerFactor = resolvePowerFactor(input, errors, assumptions)
        val efficiency = resolveEfficiency(input, errors, assumptions)
        if (errors.isNotEmpty()) return CalculationValidationResult(null, errors)

        return runCatching {
            val multiplier = systemMultiplier(input.systemType) * (powerFactor ?: 1.0) * efficiency
            val power = when (input.variableToCalculate) {
                ElectricalVariable.POWER -> input.voltageVolts!! * input.currentAmps!! * multiplier
                else -> input.activePowerWatts
            }
            val current = when (input.variableToCalculate) {
                ElectricalVariable.CURRENT -> input.activePowerWatts!! / (input.voltageVolts!! * multiplier)
                else -> input.currentAmps
            }
            val voltage = when (input.variableToCalculate) {
                ElectricalVariable.VOLTAGE -> input.activePowerWatts!! / (input.currentAmps!! * multiplier)
                else -> input.voltageVolts
            }
            listOfNotNull(power, current, voltage).forEach { requireValid(it) }
            CalculationValidationResult(
                PowerCurrentVoltageResult(
                    calculatedVariable = input.variableToCalculate,
                    powerWatts = power,
                    powerKilowatts = power?.div(1000.0),
                    currentAmps = current,
                    voltageVolts = voltage,
                    systemType = input.systemType,
                    powerFactorUsed = powerFactor,
                    efficiencyUsed = efficiency,
                    assumptionsUsed = assumptions + listOfNotNull(input.context.assumptions?.takeIf(String::isNotBlank)),
                    formulaVersion = FORMULA_VERSION,
                ),
            )
        }.getOrElse { CalculationValidationResult(null, listOf("No se pudo calcular con los valores ingresados")) }
    }

    private fun resolvePowerFactor(
        input: PowerCurrentVoltageInput,
        errors: MutableList<String>,
        assumptions: MutableList<String>,
    ): Double? {
        if (input.systemType == ElectricalSystemType.DC) return null
        val value = input.powerFactor
        if (value == null) {
            return if (input.source == CalculationSource.ESTIMATED) {
                assumptions += "Factor de potencia asumido: 0.9"
                0.9
            } else {
                errors += "Ingresá el factor de potencia para sistemas AC, o marcá el cálculo como estimado e indicá el supuesto."
                null
            }
        }
        validateUnitFactor(value, "El factor de potencia", errors)
        if (input.source == CalculationSource.ESTIMATED) assumptions += "Factor de potencia utilizado: $value"
        return value
    }

    private fun resolveEfficiency(
        input: PowerCurrentVoltageInput,
        errors: MutableList<String>,
        assumptions: MutableList<String>,
    ): Double {
        val value = input.efficiency ?: 1.0
        validateUnitFactor(value, "La eficiencia", errors)
        if (input.efficiency == null) assumptions += "Eficiencia asumida: 100 %"
        return value
    }

    private fun validatePositive(value: Double?, label: String, errors: MutableList<String>) {
        value ?: return
        if (!value.isFinite() || value <= 0.0) errors += "$label debe ser mayor que cero."
    }

    private fun validateRequired(input: PowerCurrentVoltageInput, errors: MutableList<String>) {
        when (input.variableToCalculate) {
            ElectricalVariable.POWER -> {
                if (input.voltageVolts == null) errors += "Ingresá la tensión."
                if (input.currentAmps == null) errors += "Ingresá la corriente."
            }
            ElectricalVariable.CURRENT -> {
                if (input.voltageVolts == null) errors += "Ingresá la tensión."
                if (input.activePowerWatts == null) errors += "Ingresá la potencia."
            }
            ElectricalVariable.VOLTAGE -> {
                if (input.currentAmps == null) errors += "Ingresá la corriente."
                if (input.activePowerWatts == null) errors += "Ingresá la potencia."
            }
        }
        validatePositive(input.voltageVolts, "La tensión", errors)
        validatePositive(input.currentAmps, "La corriente", errors)
        validatePositive(input.activePowerWatts, "La potencia", errors)
    }

    private fun validateUnitFactor(value: Double, label: String, errors: MutableList<String>) {
        if (!value.isFinite() || value <= 0.0 || value > 1.0) errors += "$label debe ser mayor que cero y menor o igual a 1."
    }

    private fun systemMultiplier(systemType: ElectricalSystemType): Double = when (systemType) {
        ElectricalSystemType.DC -> 1.0
        ElectricalSystemType.AC_SINGLE_PHASE -> 1.0
        ElectricalSystemType.AC_THREE_PHASE -> sqrt(3.0)
    }

    private fun requireValid(value: Double) {
        require(value.isFinite() && value > 0.0)
    }
}
