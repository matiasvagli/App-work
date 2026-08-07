package com.matiasdev.elecapp.features.inspections.domain

import com.matiasdev.elecapp.features.electricaltools.domain.TechnicalClassification
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class InspectionVoltageDropEvaluatorsTest {
    @Test
    fun measuredVoltageDropUsesUpstreamVoltageAsReference() {
        val result = VoltageDropMeasuredEvaluator.evaluate(
            sourceVoltageVolts = 205.0,
            destinationVoltageVolts = 192.0,
            maxAllowedPercent = 3.0,
        )

        assertEquals(13.0, result!!.differenceVolts, 0.001)
        assertEquals(6.34, result.percent, 0.01)
        assertEquals(TechnicalClassification.CRITICAL_REVIEW, result.classification)
    }

    @Test
    fun measuredVoltageDropCalculatesPercentFromMeasuredSourceVoltage() {
        val result = VoltageDropMeasuredEvaluator.evaluate(
            sourceVoltageVolts = 220.0,
            destinationVoltageVolts = 200.0,
            maxAllowedPercent = 3.0,
        )

        assertEquals(20.0, result!!.differenceVolts, 0.001)
        assertEquals(9.09, result.percent, 0.01)
    }

    @Test
    fun measuredVoltageDropRequiresBothEndpointVoltages() {
        assertNull(VoltageDropMeasuredEvaluator.evaluate(220.0, null, 3.0))
        assertNull(VoltageDropMeasuredEvaluator.evaluate(null, 200.0, 3.0))
    }
}
