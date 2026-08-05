package com.matiasdev.elecapp.features.electricaltools.summary

import com.matiasdev.elecapp.features.electricaltools.calculators.VoltageDropCalculator
import com.matiasdev.elecapp.features.electricaltools.data.CalculationJson
import com.matiasdev.elecapp.features.electricaltools.domain.PowerCurrentVoltageInput
import com.matiasdev.elecapp.features.electricaltools.domain.PowerCurrentVoltageResult
import com.matiasdev.elecapp.features.electricaltools.domain.TechnicalCalculation
import com.matiasdev.elecapp.features.electricaltools.domain.TechnicalCalculationType
import com.matiasdev.elecapp.features.electricaltools.domain.VoltageDropInput
import com.matiasdev.elecapp.features.electricaltools.domain.VoltageDropResult

object TechnicalCalculationTextGenerator {
    fun generate(calculation: TechnicalCalculation, includeFormulaVersion: Boolean = false): String {
        return when (calculation.type) {
            TechnicalCalculationType.POWER_CURRENT_VOLTAGE -> powerText(calculation, includeFormulaVersion)
            TechnicalCalculationType.VOLTAGE_DROP -> voltageDropText(calculation, includeFormulaVersion)
            else -> genericText(calculation, includeFormulaVersion)
        }
    }

    private fun powerText(calculation: TechnicalCalculation, includeFormulaVersion: Boolean): String {
        val input = CalculationJson.decodePowerInput(calculation.inputDataJson)
        val result = CalculationJson.decodePowerResult(calculation.resultDataJson)
        return buildString {
            appendLine("CÁLCULO DE POTENCIA, CORRIENTE Y TENSIÓN")
            appendCommon(calculation, includeFormulaVersion)
            if (input != null) appendPowerInput(input)
            if (result != null) appendPowerResult(result)
            appendDisclaimer()
        }.trimEnd()
    }

    private fun voltageDropText(calculation: TechnicalCalculation, includeFormulaVersion: Boolean): String {
        val input = CalculationJson.decodeVoltageDropInput(calculation.inputDataJson)
        val result = CalculationJson.decodeVoltageDropResult(calculation.resultDataJson)
        return buildString {
            appendLine("CÁLCULO DE CAÍDA DE TENSIÓN")
            appendCommon(calculation, includeFormulaVersion)
            if (input != null) appendVoltageDropInput(input)
            if (result != null) appendVoltageDropResult(result)
            appendLine()
            appendLine("Aclaración:")
            appendLine(VoltageDropCalculator.TECHNICAL_DISCLAIMER)
            appendDisclaimer()
        }.trimEnd()
    }

    private fun genericText(calculation: TechnicalCalculation, includeFormulaVersion: Boolean): String {
        return buildString {
            appendLine(calculation.type.label().uppercase())
            appendCommon(calculation, includeFormulaVersion)
            appendLine("Resultado: ${TechnicalValueFormatter.withUnit(calculation.primaryResultValue, calculation.primaryResultUnit)}")
            appendDisclaimer()
        }.trimEnd()
    }

    private fun StringBuilder.appendCommon(calculation: TechnicalCalculation, includeFormulaVersion: Boolean) {
        appendLine()
        appendLine("Origen: ${calculation.source.label()}")
        appendLine("Título: ${calculation.title}")
        appendLineIf("Descripción", calculation.description)
        appendLine("Clasificación orientativa: ${calculation.classification.label()}")
        appendLine("Conclusión del técnico: ${calculation.technicianConclusion.label()}")
        appendLineIf("Notas del técnico", calculation.technicianNotes)
        if (includeFormulaVersion) appendLine("Versión de fórmula: ${calculation.formulaVersion}")
    }

    private fun StringBuilder.appendPowerInput(input: PowerCurrentVoltageInput) {
        appendLine()
        appendLine("Entradas:")
        appendLine("- Sistema: ${input.systemType.label()}")
        appendLine("- Variable calculada: ${input.variableToCalculate.label()}")
        input.voltageVolts?.let { appendLine("- Tensión: ${TechnicalValueFormatter.withUnit(it, "V")}") }
        input.currentAmps?.let { appendLine("- Corriente: ${TechnicalValueFormatter.withUnit(it, "A")}") }
        input.activePowerWatts?.let { appendLine("- Potencia: ${TechnicalValueFormatter.withUnit(it, "W")}") }
        input.powerFactor?.let { appendLine("- Factor de potencia: ${TechnicalValueFormatter.format(it, 3)}") }
        input.efficiency?.let { appendLine("- Eficiencia: ${TechnicalValueFormatter.format(it * 100, 2)} %") }
        appendContext(input.context.instrumentName, input.context.measurementContext, input.context.assumptions, input.context.dataProvidedByClient)
    }

    private fun StringBuilder.appendPowerResult(result: PowerCurrentVoltageResult) {
        appendLine()
        appendLine("Resultado:")
        result.powerWatts?.let { appendLine("- Potencia: ${TechnicalValueFormatter.withUnit(it, "W")} (${TechnicalValueFormatter.withUnit(result.powerKilowatts, "kW", 4)})") }
        result.currentAmps?.let { appendLine("- Corriente: ${TechnicalValueFormatter.withUnit(it, "A")}") }
        result.voltageVolts?.let { appendLine("- Tensión: ${TechnicalValueFormatter.withUnit(it, "V")}") }
        appendAssumptions(result.assumptionsUsed)
    }

    private fun StringBuilder.appendVoltageDropInput(input: VoltageDropInput) {
        appendLine()
        appendLine("Entradas:")
        appendLine("- Sistema: ${input.systemType.label()}")
        appendLine("- Tensión nominal: ${TechnicalValueFormatter.withUnit(input.nominalVoltageVolts, "V")}")
        input.currentAmps?.let { appendLine("- Corriente: ${TechnicalValueFormatter.withUnit(it, "A")}") }
        input.activePowerWatts?.let { appendLine("- Potencia activa: ${TechnicalValueFormatter.withUnit(it, "W")}") }
        input.powerFactor?.let { appendLine("- Factor de potencia: ${TechnicalValueFormatter.format(it, 3)}") }
        appendLine("- Longitud de ida: ${TechnicalValueFormatter.withUnit(input.conductorLengthMeters, "m")}")
        appendLine("- Conductor: ${input.conductorMaterial.label()}")
        appendLine("- Sección: ${TechnicalValueFormatter.withUnit(input.conductorSectionMm2, "mm²")}")
        appendLine("- Temperatura: ${input.temperatureMode.label()}")
        input.conductorTemperatureCelsius?.let { appendLine("- Temperatura del conductor: ${TechnicalValueFormatter.withUnit(it, "°C")}") }
        appendContext(input.context.instrumentName, input.context.measurementContext, input.context.assumptions, input.context.dataProvidedByClient)
    }

    private fun StringBuilder.appendVoltageDropResult(result: VoltageDropResult) {
        appendLine()
        appendLine("Resultado:")
        appendLine("- Caída aproximada: ${TechnicalValueFormatter.withUnit(result.voltageDropVolts, "V")}")
        appendLine("- Caída porcentual: ${TechnicalValueFormatter.withUnit(result.voltageDropPercent, "%")}")
        appendLine("- Tensión estimada al final: ${TechnicalValueFormatter.withUnit(result.estimatedEndVoltageVolts, "V")}")
        appendLine("- Corriente utilizada: ${TechnicalValueFormatter.withUnit(result.currentUsedAmps, "A")}")
        appendLine("- Clasificación orientativa: ${result.classification.label()}")
        appendAssumptions(result.assumptionsUsed)
    }

    private fun StringBuilder.appendContext(instrument: String?, measurementContext: String?, assumptions: String?, providedByClient: Boolean) {
        appendLineIf("- Instrumento", instrument)
        appendLineIf("- Contexto", measurementContext)
        appendLineIf("- Supuestos", assumptions)
        if (providedByClient) appendLine("- Datos declarados por el cliente")
    }

    private fun StringBuilder.appendAssumptions(assumptions: List<String>) {
        val clean = assumptions.map(String::trim).filter(String::isNotBlank).distinct()
        if (clean.isEmpty()) return
        appendLine()
        appendLine("Supuestos:")
        clean.forEach { appendLine("- $it") }
    }

    private fun StringBuilder.appendDisclaimer() {
        appendLine()
        appendLine("Resultado orientativo basado en los datos ingresados. El técnico debe verificar datos, supuestos y condiciones reales antes de definir una corrección.")
    }

    private fun StringBuilder.appendLineIf(label: String, value: String?) {
        if (!value.isNullOrBlank()) appendLine("$label: $value")
    }
}
