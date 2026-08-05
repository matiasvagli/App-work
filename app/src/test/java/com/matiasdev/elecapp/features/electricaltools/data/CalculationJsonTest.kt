package com.matiasdev.elecapp.features.electricaltools.data

import com.matiasdev.elecapp.features.electricaltools.domain.CalculationContext
import com.matiasdev.elecapp.features.electricaltools.domain.CalculationSource
import com.matiasdev.elecapp.features.electricaltools.domain.ElectricalSystemType
import com.matiasdev.elecapp.features.electricaltools.domain.ElectricalVariable
import com.matiasdev.elecapp.features.electricaltools.domain.PowerCurrentVoltageInput
import com.matiasdev.elecapp.features.electricaltools.domain.TechnicalConductorMaterial
import com.matiasdev.elecapp.features.electricaltools.domain.TemperatureMode
import com.matiasdev.elecapp.features.electricaltools.domain.VoltageDropCurrentMode
import com.matiasdev.elecapp.features.electricaltools.domain.VoltageDropInput
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CalculationJsonTest {
    @Test
    fun powerInputRoundTripIsStable() {
        val input = PowerCurrentVoltageInput(
            systemType = ElectricalSystemType.AC_SINGLE_PHASE,
            variableToCalculate = ElectricalVariable.CURRENT,
            voltageVolts = 220.0,
            currentAmps = null,
            activePowerWatts = 4500.0,
            powerFactor = 0.9,
            efficiency = 1.0,
            source = CalculationSource.CALCULATED,
            context = CalculationContext(instrumentName = "Tester", measurementContext = "Extremo de línea", assumptions = "Carga conectada", dataProvidedByClient = true),
        )
        val json = CalculationJson.encode(input)
        assertEquals(input, CalculationJson.decodePowerInput(json))
        assertEquals(json, CalculationJson.encode(CalculationJson.decodePowerInput(json)!!))
    }

    @Test
    fun voltageDropInputRoundTripPreservesEnumsAndNumbers() {
        val input = VoltageDropInput(
            systemType = ElectricalSystemType.AC_THREE_PHASE,
            nominalVoltageVolts = 380.0,
            currentMode = VoltageDropCurrentMode.DERIVED_FROM_POWER,
            currentAmps = null,
            activePowerWatts = 9000.0,
            powerFactor = 0.92,
            efficiency = 0.98,
            conductorLengthMeters = 44.5,
            conductorSectionMm2 = 6.0,
            conductorMaterial = TechnicalConductorMaterial.ALUMINUM,
            temperatureMode = TemperatureMode.CUSTOM,
            conductorTemperatureCelsius = 60.0,
            source = CalculationSource.ESTIMATED,
        )
        val decoded = CalculationJson.decodeVoltageDropInput(CalculationJson.encode(input))
        assertEquals(input, decoded)
    }

    @Test
    fun invalidJsonFailsControlled() {
        assertNull(CalculationJson.decodeVoltageDropResult("{bad json"))
    }
}
