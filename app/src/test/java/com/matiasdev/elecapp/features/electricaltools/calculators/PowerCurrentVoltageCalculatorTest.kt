package com.matiasdev.elecapp.features.electricaltools.calculators

import com.matiasdev.elecapp.features.electricaltools.domain.CalculationSource
import com.matiasdev.elecapp.features.electricaltools.domain.ElectricalSystemType
import com.matiasdev.elecapp.features.electricaltools.domain.ElectricalVariable
import com.matiasdev.elecapp.features.electricaltools.domain.PowerCurrentVoltageInput
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PowerCurrentVoltageCalculatorTest {
    @Test
    fun dcPower() {
        val result = calculate(ElectricalSystemType.DC, ElectricalVariable.POWER, voltage = 12.0, current = 10.0)
        assertEquals(120.0, result.powerWatts!!, 0.0001)
    }

    @Test
    fun dcCurrent() {
        val result = calculate(ElectricalSystemType.DC, ElectricalVariable.CURRENT, voltage = 12.0, power = 120.0)
        assertEquals(10.0, result.currentAmps!!, 0.0001)
    }

    @Test
    fun dcVoltage() {
        val result = calculate(ElectricalSystemType.DC, ElectricalVariable.VOLTAGE, current = 10.0, power = 120.0)
        assertEquals(12.0, result.voltageVolts!!, 0.0001)
    }

    @Test
    fun singlePhasePower() {
        val result = calculate(ElectricalSystemType.AC_SINGLE_PHASE, ElectricalVariable.POWER, voltage = 220.0, current = 10.0, pf = 0.9, efficiency = 0.95)
        assertEquals(1881.0, result.powerWatts!!, 0.0001)
    }

    @Test
    fun singlePhaseCurrent() {
        val result = calculate(ElectricalSystemType.AC_SINGLE_PHASE, ElectricalVariable.CURRENT, voltage = 220.0, power = 1881.0, pf = 0.9, efficiency = 0.95)
        assertEquals(10.0, result.currentAmps!!, 0.0001)
    }

    @Test
    fun singlePhaseVoltage() {
        val result = calculate(ElectricalSystemType.AC_SINGLE_PHASE, ElectricalVariable.VOLTAGE, current = 10.0, power = 1881.0, pf = 0.9, efficiency = 0.95)
        assertEquals(220.0, result.voltageVolts!!, 0.0001)
    }

    @Test
    fun threePhasePower() {
        val result = calculate(ElectricalSystemType.AC_THREE_PHASE, ElectricalVariable.POWER, voltage = 380.0, current = 10.0, pf = 0.9, efficiency = 1.0)
        assertEquals(5923.61376, result.powerWatts!!, 0.001)
    }

    @Test
    fun threePhaseCurrentAndVoltage() {
        val current = calculate(ElectricalSystemType.AC_THREE_PHASE, ElectricalVariable.CURRENT, voltage = 380.0, power = 5923.61376, pf = 0.9)
        val voltage = calculate(ElectricalSystemType.AC_THREE_PHASE, ElectricalVariable.VOLTAGE, current = 10.0, power = 5923.61376, pf = 0.9)
        assertEquals(10.0, current.currentAmps!!, 0.001)
        assertEquals(380.0, voltage.voltageVolts!!, 0.001)
    }

    @Test
    fun estimatedAcCanUseVisiblePowerFactorAssumption() {
        val result = PowerCurrentVoltageCalculator.calculate(
            PowerCurrentVoltageInput(
                systemType = ElectricalSystemType.AC_SINGLE_PHASE,
                variableToCalculate = ElectricalVariable.CURRENT,
                voltageVolts = 220.0,
                currentAmps = null,
                activePowerWatts = 1980.0,
                powerFactor = null,
                efficiency = null,
                source = CalculationSource.ESTIMATED,
            ),
        )
        assertTrue(result.isValid)
        assertTrue(result.value!!.assumptionsUsed.any { it.contains("Factor de potencia asumido") })
        assertTrue(result.value.assumptionsUsed.any { it.contains("Eficiencia asumida") })
    }

    @Test
    fun invalidValuesAreRejected() {
        assertFalse(calculateRaw(voltage = 0.0, current = 1.0).isValid)
        assertFalse(calculateRaw(voltage = Double.NaN, current = 1.0).isValid)
        assertFalse(calculateRaw(voltage = Double.POSITIVE_INFINITY, current = 1.0).isValid)
        assertFalse(calculateRaw(voltage = 220.0, current = 1.0, pf = 1.2).isValid)
    }

    @Test
    fun kilowattConversionIsExternalToBaseFormula() {
        val watts = 4.5 * 1000.0
        val result = calculate(ElectricalSystemType.DC, ElectricalVariable.CURRENT, voltage = 220.0, power = watts)
        assertEquals(20.4545, result.currentAmps!!, 0.0001)
    }

    private fun calculate(
        system: ElectricalSystemType,
        variable: ElectricalVariable,
        voltage: Double? = null,
        current: Double? = null,
        power: Double? = null,
        pf: Double? = null,
        efficiency: Double? = null,
    ) = PowerCurrentVoltageCalculator.calculate(
        PowerCurrentVoltageInput(systemType = system, variableToCalculate = variable, voltageVolts = voltage, currentAmps = current, activePowerWatts = power, powerFactor = pf, efficiency = efficiency, source = CalculationSource.CALCULATED),
    ).value!!

    private fun calculateRaw(voltage: Double?, current: Double?, pf: Double? = null) = PowerCurrentVoltageCalculator.calculate(
        PowerCurrentVoltageInput(
            systemType = ElectricalSystemType.AC_SINGLE_PHASE,
            variableToCalculate = ElectricalVariable.POWER,
            voltageVolts = voltage,
            currentAmps = current,
            activePowerWatts = null,
            powerFactor = pf,
            efficiency = 1.0,
            source = CalculationSource.CALCULATED,
        ),
    )
}
