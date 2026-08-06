package com.matiasdev.elecapp.features.electricalrules.domain

import com.matiasdev.elecapp.features.electricaltools.domain.CalculationSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SupplyVoltageRuleEvaluatorTest {
    private val evaluator = SupplyVoltageRuleEvaluator()

    @Test
    fun `187 V with minimum 190 V fails`() {
        val result = evaluator.evaluateMinimum(input(voltage = 187.0), minConfig())

        assertEquals(ElectricalRuleEvaluationStatus.FAILED, result.status)
        assertNotNull(result.finding)
    }

    @Test
    fun `190 V with minimum 190 V passes`() {
        val result = evaluator.evaluateMinimum(input(voltage = 190.0), minConfig())

        assertEquals(ElectricalRuleEvaluationStatus.PASSED, result.status)
        assertNull(result.finding)
    }

    @Test
    fun `220 V with minimum 190 V passes`() {
        val result = evaluator.evaluateMinimum(input(voltage = 220.0), minConfig())

        assertEquals(ElectricalRuleEvaluationStatus.PASSED, result.status)
        assertNull(result.finding)
    }

    @Test
    fun `254 V with maximum 250 V fails`() {
        val result = evaluator.evaluateMaximum(input(voltage = 254.0), maxConfig())

        assertEquals(ElectricalRuleEvaluationStatus.FAILED, result.status)
        assertNotNull(result.finding)
    }

    @Test
    fun `250 V with maximum 250 V passes`() {
        val result = evaluator.evaluateMaximum(input(voltage = 250.0), maxConfig())

        assertEquals(ElectricalRuleEvaluationStatus.PASSED, result.status)
        assertNull(result.finding)
    }

    @Test
    fun `disabled rule returns disabled without finding`() {
        val result = evaluator.evaluateMinimum(input(voltage = 187.0), minConfig().copy(enabled = false))

        assertEquals(ElectricalRuleEvaluationStatus.DISABLED, result.status)
        assertNull(result.finding)
    }

    @Test
    fun `config without numeric value is not evaluated`() {
        val result = evaluator.evaluateMinimum(input(voltage = 187.0), minConfig().copy(numericValue = null))

        assertEquals(ElectricalRuleEvaluationStatus.NOT_EVALUATED, result.status)
        assertNull(result.finding)
        assertTrue(result.explanation!!.contains("valor numérico"))
    }

    @Test
    fun `zero voltage is not evaluated`() {
        val result = evaluator.evaluateMinimum(input(voltage = 0.0), minConfig())

        assertEquals(ElectricalRuleEvaluationStatus.NOT_EVALUATED, result.status)
        assertNull(result.finding)
    }

    @Test
    fun `negative voltage is not evaluated`() {
        val result = evaluator.evaluateMinimum(input(voltage = -1.0), minConfig())

        assertEquals(ElectricalRuleEvaluationStatus.NOT_EVALUATED, result.status)
        assertNull(result.finding)
    }

    @Test
    fun `NaN voltage is not evaluated`() {
        val result = evaluator.evaluateMinimum(input(voltage = Double.NaN), minConfig())

        assertEquals(ElectricalRuleEvaluationStatus.NOT_EVALUATED, result.status)
        assertNull(result.finding)
        assertNull(result.measuredValue)
    }

    @Test
    fun `infinite voltage is not evaluated`() {
        val result = evaluator.evaluateMaximum(input(voltage = Double.POSITIVE_INFINITY), maxConfig())

        assertEquals(ElectricalRuleEvaluationStatus.NOT_EVALUATED, result.status)
        assertNull(result.finding)
        assertNull(result.measuredValue)
    }

    @Test
    fun `generated finding contains measured value limit location and configured severity`() {
        val config = minConfig().copy(severity = ElectricalRuleSeverity.CRITICAL)
        val result = evaluator.evaluateMinimum(input(voltage = 187.5, location = "pilar"), config)
        val finding = result.finding!!

        assertEquals(ElectricalRuleSeverity.CRITICAL, finding.severity)
        assertTrue(finding.description.contains("187,5 V"))
        assertTrue(finding.description.contains("190 V"))
        assertTrue(finding.description.contains("en el pilar"))
        assertEquals("calculation-id", finding.sourceCalculationId)
        assertEquals("inspection-id", finding.inspectionId)
    }

    private fun input(
        voltage: Double,
        location: String? = "pilar",
    ): SupplyVoltageInput = SupplyVoltageInput(
        voltage = voltage,
        location = location,
        origin = CalculationSource.MEASURED,
        sourceCalculationId = "calculation-id",
        inspectionId = "inspection-id",
    )

    private fun minConfig(): ElectricalRuleConfig {
        return DefaultElectricalRuleConfigs.all.first { it.code == ElectricalRuleCode.MIN_SUPPLY_VOLTAGE }
    }

    private fun maxConfig(): ElectricalRuleConfig {
        return DefaultElectricalRuleConfigs.all.first { it.code == ElectricalRuleCode.MAX_SUPPLY_VOLTAGE }
    }
}
