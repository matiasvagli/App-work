package com.matiasdev.elecapp.features.electricaltools.data

import com.matiasdev.elecapp.features.electricaltools.domain.CalculationContext
import com.matiasdev.elecapp.features.electricaltools.domain.CalculationSource
import com.matiasdev.elecapp.features.electricaltools.domain.ElectricalSystemType
import com.matiasdev.elecapp.features.electricaltools.domain.ElectricalVariable
import com.matiasdev.elecapp.features.electricaltools.domain.PowerCurrentVoltageInput
import com.matiasdev.elecapp.features.electricaltools.domain.PowerCurrentVoltageResult
import com.matiasdev.elecapp.features.electricaltools.domain.TechnicalClassification
import com.matiasdev.elecapp.features.electricaltools.domain.TechnicalConductorMaterial
import com.matiasdev.elecapp.features.electricaltools.domain.TemperatureMode
import com.matiasdev.elecapp.features.electricaltools.domain.VoltageDropCurrentMode
import com.matiasdev.elecapp.features.electricaltools.domain.VoltageDropInput
import com.matiasdev.elecapp.features.electricaltools.domain.VoltageDropResult

object CalculationJson {
    fun encode(input: PowerCurrentVoltageInput): String = jsonObject(
        "schemaVersion" to input.schemaVersion,
        "systemType" to input.systemType.name,
        "variableToCalculate" to input.variableToCalculate.name,
        "voltageVolts" to input.voltageVolts,
        "currentAmps" to input.currentAmps,
        "activePowerWatts" to input.activePowerWatts,
        "powerFactor" to input.powerFactor,
        "efficiency" to input.efficiency,
        "source" to input.source.name,
        "context" to encodeContextObject(input.context),
    )

    fun encode(result: PowerCurrentVoltageResult): String = jsonObject(
        "schemaVersion" to result.schemaVersion,
        "calculatedVariable" to result.calculatedVariable.name,
        "powerWatts" to result.powerWatts,
        "powerKilowatts" to result.powerKilowatts,
        "currentAmps" to result.currentAmps,
        "voltageVolts" to result.voltageVolts,
        "systemType" to result.systemType.name,
        "powerFactorUsed" to result.powerFactorUsed,
        "efficiencyUsed" to result.efficiencyUsed,
        "assumptionsUsed" to result.assumptionsUsed,
        "formulaVersion" to result.formulaVersion,
    )

    fun encode(input: VoltageDropInput): String = jsonObject(
        "schemaVersion" to input.schemaVersion,
        "systemType" to input.systemType.name,
        "nominalVoltageVolts" to input.nominalVoltageVolts,
        "currentMode" to input.currentMode.name,
        "currentAmps" to input.currentAmps,
        "activePowerWatts" to input.activePowerWatts,
        "powerFactor" to input.powerFactor,
        "efficiency" to input.efficiency,
        "conductorLengthMeters" to input.conductorLengthMeters,
        "conductorSectionMm2" to input.conductorSectionMm2,
        "conductorMaterial" to input.conductorMaterial.name,
        "temperatureMode" to input.temperatureMode.name,
        "conductorTemperatureCelsius" to input.conductorTemperatureCelsius,
        "source" to input.source.name,
        "context" to encodeContextObject(input.context),
    )

    fun encode(result: VoltageDropResult): String = jsonObject(
        "schemaVersion" to result.schemaVersion,
        "voltageDropVolts" to result.voltageDropVolts,
        "voltageDropPercent" to result.voltageDropPercent,
        "estimatedEndVoltageVolts" to result.estimatedEndVoltageVolts,
        "currentUsedAmps" to result.currentUsedAmps,
        "derivedCurrent" to result.derivedCurrent,
        "systemType" to result.systemType.name,
        "nominalVoltageVolts" to result.nominalVoltageVolts,
        "conductorLengthMeters" to result.conductorLengthMeters,
        "conductorSectionMm2" to result.conductorSectionMm2,
        "conductorMaterial" to result.conductorMaterial.name,
        "resistivityOhmMm2PerMeter" to result.resistivityOhmMm2PerMeter,
        "temperatureMode" to result.temperatureMode.name,
        "conductorTemperatureCelsius" to result.conductorTemperatureCelsius,
        "classification" to result.classification.name,
        "assessmentThresholdPercent" to result.assessmentThresholdPercent,
        "assessmentConfigVersion" to result.assessmentConfigVersion,
        "assumptionsUsed" to result.assumptionsUsed,
        "formulaVersion" to result.formulaVersion,
    )

    fun decodePowerInput(json: String): PowerCurrentVoltageInput? = runCatching {
        PowerCurrentVoltageInput(
            schemaVersion = int(json, "schemaVersion") ?: 1,
            systemType = enumValueOf(string(json, "systemType")!!),
            variableToCalculate = enumValueOf(string(json, "variableToCalculate")!!),
            voltageVolts = double(json, "voltageVolts"),
            currentAmps = double(json, "currentAmps"),
            activePowerWatts = double(json, "activePowerWatts"),
            powerFactor = double(json, "powerFactor"),
            efficiency = double(json, "efficiency"),
            source = enumValueOf(string(json, "source")!!),
            context = context(json),
        )
    }.getOrNull()

    fun decodePowerResult(json: String): PowerCurrentVoltageResult? = runCatching {
        PowerCurrentVoltageResult(
            schemaVersion = int(json, "schemaVersion") ?: 1,
            calculatedVariable = enumValueOf(string(json, "calculatedVariable")!!),
            powerWatts = double(json, "powerWatts"),
            powerKilowatts = double(json, "powerKilowatts"),
            currentAmps = double(json, "currentAmps"),
            voltageVolts = double(json, "voltageVolts"),
            systemType = enumValueOf(string(json, "systemType")!!),
            powerFactorUsed = double(json, "powerFactorUsed"),
            efficiencyUsed = double(json, "efficiencyUsed"),
            assumptionsUsed = stringArray(json, "assumptionsUsed"),
            formulaVersion = string(json, "formulaVersion")!!,
        )
    }.getOrNull()

    fun decodeVoltageDropInput(json: String): VoltageDropInput? = runCatching {
        VoltageDropInput(
            schemaVersion = int(json, "schemaVersion") ?: 1,
            systemType = enumValueOf(string(json, "systemType")!!),
            nominalVoltageVolts = double(json, "nominalVoltageVolts")!!,
            currentMode = enumValueOf(string(json, "currentMode")!!),
            currentAmps = double(json, "currentAmps"),
            activePowerWatts = double(json, "activePowerWatts"),
            powerFactor = double(json, "powerFactor"),
            efficiency = double(json, "efficiency"),
            conductorLengthMeters = double(json, "conductorLengthMeters")!!,
            conductorSectionMm2 = double(json, "conductorSectionMm2")!!,
            conductorMaterial = enumValueOf(string(json, "conductorMaterial")!!),
            temperatureMode = enumValueOf(string(json, "temperatureMode")!!),
            conductorTemperatureCelsius = double(json, "conductorTemperatureCelsius"),
            source = enumValueOf(string(json, "source")!!),
            context = context(json),
        )
    }.getOrNull()

    fun decodeVoltageDropResult(json: String): VoltageDropResult? = runCatching {
        VoltageDropResult(
            schemaVersion = int(json, "schemaVersion") ?: 1,
            voltageDropVolts = double(json, "voltageDropVolts")!!,
            voltageDropPercent = double(json, "voltageDropPercent")!!,
            estimatedEndVoltageVolts = double(json, "estimatedEndVoltageVolts")!!,
            currentUsedAmps = double(json, "currentUsedAmps")!!,
            derivedCurrent = bool(json, "derivedCurrent") == true,
            systemType = enumValueOf(string(json, "systemType")!!),
            nominalVoltageVolts = double(json, "nominalVoltageVolts")!!,
            conductorLengthMeters = double(json, "conductorLengthMeters")!!,
            conductorSectionMm2 = double(json, "conductorSectionMm2")!!,
            conductorMaterial = enumValueOf(string(json, "conductorMaterial")!!),
            resistivityOhmMm2PerMeter = double(json, "resistivityOhmMm2PerMeter")!!,
            temperatureMode = enumValueOf(string(json, "temperatureMode")!!),
            conductorTemperatureCelsius = double(json, "conductorTemperatureCelsius"),
            classification = enumValueOf(string(json, "classification")!!),
            assessmentThresholdPercent = double(json, "assessmentThresholdPercent"),
            assessmentConfigVersion = string(json, "assessmentConfigVersion")!!,
            assumptionsUsed = stringArray(json, "assumptionsUsed"),
            formulaVersion = string(json, "formulaVersion")!!,
        )
    }.getOrNull()

    private fun encodeContextObject(context: CalculationContext): JsonRaw = JsonRaw(
        jsonObject(
            "instrumentName" to context.instrumentName,
            "measurementContext" to context.measurementContext,
            "assumptions" to context.assumptions,
            "dataProvidedByClient" to context.dataProvidedByClient,
        ),
    )

    private fun context(json: String) = CalculationContext(
        instrumentName = string(json, "instrumentName"),
        measurementContext = string(json, "measurementContext"),
        assumptions = string(json, "assumptions"),
        dataProvidedByClient = bool(json, "dataProvidedByClient") == true,
    )

    private fun jsonObject(vararg entries: Pair<String, Any?>): String {
        return entries.joinToString(prefix = "{", postfix = "}") { (key, value) ->
            "\"${escape(key)}\":${encodeValue(value)}"
        }
    }

    private fun encodeValue(value: Any?): String = when (value) {
        null -> "null"
        is JsonRaw -> value.value
        is String -> "\"${escape(value)}\""
        is Number -> value.toString()
        is Boolean -> value.toString()
        is List<*> -> value.joinToString(prefix = "[", postfix = "]") { encodeValue(it) }
        else -> "\"${escape(value.toString())}\""
    }

    private fun string(json: String, key: String): String? {
        val match = Regex("\"${Regex.escape(key)}\"\\s*:\\s*(null|\"((?:\\\\.|[^\"])*)\")").find(json) ?: return null
        if (match.groupValues[1] == "null") return null
        return unescape(match.groupValues[2])
    }

    private fun double(json: String, key: String): Double? {
        val match = Regex("\"${Regex.escape(key)}\"\\s*:\\s*(null|-?\\d+(?:\\.\\d+)?(?:[eE][+-]?\\d+)?)").find(json) ?: return null
        return match.groupValues[1].takeUnless { it == "null" }?.toDoubleOrNull()
    }

    private fun int(json: String, key: String): Int? = double(json, key)?.toInt()

    private fun bool(json: String, key: String): Boolean? {
        val match = Regex("\"${Regex.escape(key)}\"\\s*:\\s*(true|false|null)").find(json) ?: return null
        return match.groupValues[1].takeUnless { it == "null" }?.toBooleanStrictOrNull()
    }

    private fun stringArray(json: String, key: String): List<String> {
        val match = Regex("\"${Regex.escape(key)}\"\\s*:\\s*\\[(.*?)]").find(json) ?: return emptyList()
        return Regex("\"((?:\\\\.|[^\"])*)\"").findAll(match.groupValues[1]).map { unescape(it.groupValues[1]) }.toList()
    }

    private fun escape(value: String): String = buildString {
        value.forEach {
            when (it) {
                '\\' -> append("\\\\")
                '"' -> append("\\\"")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> append(it)
            }
        }
    }

    private fun unescape(value: String): String = value
        .replace("\\n", "\n")
        .replace("\\r", "\r")
        .replace("\\t", "\t")
        .replace("\\\"", "\"")
        .replace("\\\\", "\\")
}

private data class JsonRaw(val value: String)
