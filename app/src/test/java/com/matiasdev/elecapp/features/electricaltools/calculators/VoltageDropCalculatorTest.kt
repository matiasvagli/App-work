package com.matiasdev.elecapp.features.electricaltools.calculators

import com.matiasdev.elecapp.features.electricaltools.domain.CalculationSource
import com.matiasdev.elecapp.features.electricaltools.domain.ElectricalSystemType
import com.matiasdev.elecapp.features.electricaltools.domain.TechnicalClassification
import com.matiasdev.elecapp.features.electricaltools.domain.TechnicalConductorMaterial
import com.matiasdev.elecapp.features.electricaltools.domain.TemperatureMode
import com.matiasdev.elecapp.features.electricaltools.domain.VoltageDropCurrentMode
import com.matiasdev.elecapp.features.electricaltools.domain.VoltageDropInput
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VoltageDropCalculatorTest {
    @Test
    fun dcCopperDirectCurrent() {
        val result = calculate(ElectricalSystemType.DC, TechnicalConductorMaterial.COPPER)
        assertEquals(5.51712, result.voltageDropVolts, 0.0001)
        assertEquals(2.50778, result.voltageDropPercent, 0.0001)
        assertEquals(214.48288, result.estimatedEndVoltageVolts, 0.0001)
    }

    @Test
    fun singlePhaseUsesRoundTripFactor() {
        val result = calculate(ElectricalSystemType.AC_SINGLE_PHASE, TechnicalConductorMaterial.COPPER)
        assertEquals(5.51712, result.voltageDropVolts, 0.0001)
    }

    @Test
    fun threePhaseUsesSquareRootFactor() {
        val result = calculate(ElectricalSystemType.AC_THREE_PHASE, TechnicalConductorMaterial.COPPER, voltage = 380.0)
        assertEquals(4.77809, result.voltageDropVolts, 0.0002)
    }

    @Test
    fun aluminumUsesDifferentResistivity() {
        val result = calculate(ElectricalSystemType.AC_SINGLE_PHASE, TechnicalConductorMaterial.ALUMINUM)
        assertEquals(9.04448, result.voltageDropVolts, 0.0001)
    }

    @Test
    fun derivedCurrentReusesPowerCalculator() {
        val result = VoltageDropCalculator.calculate(
            baseInput(
                currentMode = VoltageDropCurrentMode.DERIVED_FROM_POWER,
                current = null,
                power = 4500.0,
                pf = 1.0,
            ),
        )
        assertTrue(result.isValid)
        assertEquals(20.4545, result.value!!.currentUsedAmps, 0.0001)
        assertTrue(result.value.derivedCurrent)
    }

    @Test
    fun customTemperatureIncreasesCopperDrop() {
        val reference = calculate(ElectricalSystemType.AC_SINGLE_PHASE, TechnicalConductorMaterial.COPPER)
        val hot = VoltageDropCalculator.calculate(
            baseInput(temperatureMode = TemperatureMode.CUSTOM, temperature = 70.0),
        ).value!!
        assertTrue(hot.voltageDropVolts > reference.voltageDropVolts)
    }

    @Test
    fun classificationIsOrientative() {
        val acceptable = calculate(ElectricalSystemType.AC_SINGLE_PHASE, TechnicalConductorMaterial.COPPER, length = 10.0)
        val review = calculate(ElectricalSystemType.AC_SINGLE_PHASE, TechnicalConductorMaterial.COPPER, length = 30.0)
        val critical = calculate(ElectricalSystemType.AC_SINGLE_PHASE, TechnicalConductorMaterial.COPPER, length = 50.0)
        assertEquals(TechnicalClassification.ACCEPTABLE, acceptable.classification)
        assertEquals(TechnicalClassification.REQUIRES_REVIEW, review.classification)
        assertEquals(TechnicalClassification.CRITICAL_REVIEW, critical.classification)
    }

    @Test
    fun invalidInputsAreRejected() {
        assertFalse(VoltageDropCalculator.calculate(baseInput(section = 0.0)).isValid)
        assertFalse(VoltageDropCalculator.calculate(baseInput(length = Double.NaN)).isValid)
        assertFalse(VoltageDropCalculator.calculate(baseInput(current = Double.POSITIVE_INFINITY)).isValid)
        assertFalse(VoltageDropCalculator.calculate(baseInput(pf = 1.4)).isValid)
    }

    private fun calculate(
        system: ElectricalSystemType,
        material: TechnicalConductorMaterial,
        voltage: Double = 220.0,
        length: Double = 20.0,
    ) = VoltageDropCalculator.calculate(baseInput(systemType = system, material = material, voltage = voltage, length = length)).value!!

    private fun baseInput(
        systemType: ElectricalSystemType = ElectricalSystemType.AC_SINGLE_PHASE,
        voltage: Double = 220.0,
        currentMode: VoltageDropCurrentMode = VoltageDropCurrentMode.DIRECT_CURRENT,
        current: Double? = 20.0,
        power: Double? = null,
        pf: Double? = null,
        length: Double = 20.0,
        section: Double = 2.5,
        material: TechnicalConductorMaterial = TechnicalConductorMaterial.COPPER,
        temperatureMode: TemperatureMode = TemperatureMode.NOT_CONSIDERED,
        temperature: Double? = null,
    ) = VoltageDropInput(
        systemType = systemType,
        nominalVoltageVolts = voltage,
        currentMode = currentMode,
        currentAmps = current,
        activePowerWatts = power,
        powerFactor = pf,
        efficiency = 1.0,
        conductorLengthMeters = length,
        conductorSectionMm2 = section,
        conductorMaterial = material,
        temperatureMode = temperatureMode,
        conductorTemperatureCelsius = temperature,
        source = CalculationSource.CALCULATED,
    )
}
