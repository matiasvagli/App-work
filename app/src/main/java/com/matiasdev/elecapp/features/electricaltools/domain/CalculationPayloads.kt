package com.matiasdev.elecapp.features.electricaltools.domain

data class PowerCurrentVoltageInput(
    val schemaVersion: Int = 1,
    val systemType: ElectricalSystemType,
    val variableToCalculate: ElectricalVariable,
    val voltageVolts: Double?,
    val currentAmps: Double?,
    val activePowerWatts: Double?,
    val powerFactor: Double?,
    val efficiency: Double?,
    val source: CalculationSource,
    val context: CalculationContext = CalculationContext(),
)

data class PowerCurrentVoltageResult(
    val schemaVersion: Int = 1,
    val calculatedVariable: ElectricalVariable,
    val powerWatts: Double?,
    val powerKilowatts: Double?,
    val currentAmps: Double?,
    val voltageVolts: Double?,
    val systemType: ElectricalSystemType,
    val powerFactorUsed: Double?,
    val efficiencyUsed: Double?,
    val assumptionsUsed: List<String>,
    val formulaVersion: String,
)

data class VoltageDropInput(
    val schemaVersion: Int = 1,
    val systemType: ElectricalSystemType,
    val nominalVoltageVolts: Double,
    val currentMode: VoltageDropCurrentMode,
    val currentAmps: Double?,
    val activePowerWatts: Double?,
    val powerFactor: Double?,
    val efficiency: Double?,
    val conductorLengthMeters: Double,
    val conductorSectionMm2: Double,
    val conductorMaterial: TechnicalConductorMaterial,
    val temperatureMode: TemperatureMode,
    val conductorTemperatureCelsius: Double?,
    val source: CalculationSource,
    val context: CalculationContext = CalculationContext(),
)

data class VoltageDropResult(
    val schemaVersion: Int = 1,
    val voltageDropVolts: Double,
    val voltageDropPercent: Double,
    val estimatedEndVoltageVolts: Double,
    val currentUsedAmps: Double,
    val derivedCurrent: Boolean,
    val systemType: ElectricalSystemType,
    val nominalVoltageVolts: Double,
    val conductorLengthMeters: Double,
    val conductorSectionMm2: Double,
    val conductorMaterial: TechnicalConductorMaterial,
    val resistivityOhmMm2PerMeter: Double,
    val temperatureMode: TemperatureMode,
    val conductorTemperatureCelsius: Double?,
    val classification: TechnicalClassification,
    val assessmentThresholdPercent: Double?,
    val assessmentConfigVersion: String,
    val assumptionsUsed: List<String>,
    val formulaVersion: String,
)

data class CalculationValidationResult<T>(
    val value: T?,
    val errors: List<String> = emptyList(),
) {
    val isValid: Boolean get() = value != null && errors.isEmpty()
}
