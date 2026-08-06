package com.matiasdev.elecapp.features.electricalrules.domain

import com.matiasdev.elecapp.features.electricaltools.domain.CalculationContext
import com.matiasdev.elecapp.features.electricaltools.domain.CalculationSource
import com.matiasdev.elecapp.features.electricaltools.domain.ElectricalSystemType
import com.matiasdev.elecapp.features.electricaltools.domain.ElectricalVariable
import com.matiasdev.elecapp.features.electricaltools.domain.PowerCurrentVoltageInput
import com.matiasdev.elecapp.features.electricaltools.domain.PowerCurrentVoltageResult
import com.matiasdev.elecapp.features.electricaltools.ui.CalculationAssociationDraft
import com.matiasdev.elecapp.features.electricaltools.ui.buildPowerCalculation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ElectricalMeasurementReviewEvaluatorTest {
    @Test
    fun `measured supply voltage inside configured range has no anomaly`() = runTest {
        val summary = evaluate(220.0)

        assertFalse(summary.hasAnomalies)
        assertEquals(0, summary.anomalyCount)
        assertEquals("", summary.indicatorText)
    }

    @Test
    fun `measured supply voltage below minimum creates anomaly`() = runTest {
        val summary = evaluate(180.0)

        assertTrue(summary.hasAnomalies)
        assertEquals(1, summary.anomalyCount)
        assertEquals("1 medición para revisar", summary.indicatorText)
        assertEquals(180.0, summary.items.single().measuredValue, 0.0)
        assertEquals(190.0, summary.items.single().minimumAllowed)
        assertEquals(250.0, summary.items.single().maximumAllowed)
    }

    @Test
    fun `measured supply voltage above maximum creates anomaly`() = runTest {
        val summary = evaluate(255.0)

        assertTrue(summary.hasAnomalies)
        assertEquals(1, summary.anomalyCount)
        assertEquals(255.0, summary.items.single().measuredValue, 0.0)
    }

    @Test
    fun `disabled supply voltage rules do not create anomalies`() = runTest {
        val disabledConfigs = defaultSupplyVoltageConfigs().map { it.copy(enabled = false) }
        val summary = evaluate(255.0, disabledConfigs)

        assertFalse(summary.hasAnomalies)
        assertEquals(0, summary.anomalyCount)
    }

    @Test
    fun `indicator text handles several anomalies`() = runTest {
        val summary = ElectricalMeasurementReviewEvaluator.evaluateSupplyVoltage(
            calculations = listOf(calculation("one", 180.0), calculation("two", 255.0)),
            useCase = EvaluateSupplyVoltageUseCase(ReviewFakeElectricalRuleConfigRepository(defaultSupplyVoltageConfigs())),
        )

        assertEquals(2, summary.anomalyCount)
        assertEquals("2 mediciones para revisar", summary.indicatorText)
    }

    private suspend fun evaluate(
        voltage: Double,
        configs: List<ElectricalRuleConfig> = defaultSupplyVoltageConfigs(),
    ): ElectricalMeasurementReviewSummary {
        return ElectricalMeasurementReviewEvaluator.evaluateSupplyVoltage(
            calculations = listOf(calculation("calculation", voltage)),
            useCase = EvaluateSupplyVoltageUseCase(ReviewFakeElectricalRuleConfigRepository(configs)),
        )
    }

    private fun calculation(id: String, voltage: Double) = buildPowerCalculation(
        existingId = id,
        input = PowerCurrentVoltageInput(
            systemType = ElectricalSystemType.AC_SINGLE_PHASE,
            variableToCalculate = ElectricalVariable.CURRENT,
            voltageVolts = voltage,
            currentAmps = null,
            activePowerWatts = 1000.0,
            powerFactor = 0.9,
            efficiency = null,
            source = CalculationSource.MEASURED,
            context = CalculationContext(measurementContext = "pilar"),
        ),
        result = PowerCurrentVoltageResult(
            calculatedVariable = ElectricalVariable.CURRENT,
            powerWatts = 1000.0,
            powerKilowatts = 1.0,
            currentAmps = 5.05,
            voltageVolts = voltage,
            systemType = ElectricalSystemType.AC_SINGLE_PHASE,
            powerFactorUsed = 0.9,
            efficiencyUsed = null,
            assumptionsUsed = emptyList(),
            formulaVersion = "test",
        ),
        association = CalculationAssociationDraft(inspectionId = "inspection"),
    )

    private fun defaultSupplyVoltageConfigs(): List<ElectricalRuleConfig> {
        return DefaultElectricalRuleConfigs.all.filter {
            it.code == ElectricalRuleCode.MIN_SUPPLY_VOLTAGE || it.code == ElectricalRuleCode.MAX_SUPPLY_VOLTAGE
        }
    }
}

private class ReviewFakeElectricalRuleConfigRepository(
    initialConfigs: List<ElectricalRuleConfig>,
) : ElectricalRuleConfigRepository {
    private val configs = MutableStateFlow(initialConfigs)

    override fun observeAll(): Flow<List<ElectricalRuleConfig>> = configs

    override fun observeByCode(code: ElectricalRuleCode): Flow<ElectricalRuleConfig?> {
        return configs.map { values -> values.firstOrNull { it.code == code } }
    }

    override suspend fun getByCode(code: ElectricalRuleCode): ElectricalRuleConfig? {
        return configs.value.firstOrNull { it.code == code }
    }

    override suspend fun save(config: ElectricalRuleConfig) = Unit
    override suspend fun saveAll(configs: List<ElectricalRuleConfig>) = Unit
    override suspend fun restoreDefaults() = Unit
}
