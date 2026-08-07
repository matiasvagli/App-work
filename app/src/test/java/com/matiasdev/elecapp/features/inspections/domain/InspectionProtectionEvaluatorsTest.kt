package com.matiasdev.elecapp.features.inspections.domain

import com.matiasdev.elecapp.features.electricalrules.domain.DefaultElectricalRuleConfigs
import com.matiasdev.elecapp.features.electricaltools.domain.TechnicalClassification
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InspectionProtectionEvaluatorsTest {
    @Test
    fun `protection load classifies measured current percentage`() {
        assertLoad(12.0, 16, 75.0, ProtectionLoadStatus.ACCEPTABLE, TechnicalClassification.ACCEPTABLE)
        assertLoad(8.0, 10, 80.0, ProtectionLoadStatus.ELEVATED_LOAD, TechnicalClassification.REQUIRES_REVIEW)
        assertLoad(9.0, 10, 90.0, ProtectionLoadStatus.ELEVATED_LOAD, TechnicalClassification.REQUIRES_REVIEW)
        assertLoad(9.5, 10, 95.0, ProtectionLoadStatus.NEAR_LIMIT, TechnicalClassification.REQUIRES_REVIEW)
        assertLoad(9.9, 10, 99.0, ProtectionLoadStatus.NEAR_LIMIT, TechnicalClassification.REQUIRES_REVIEW)
        assertLoad(10.0, 10, 100.0, ProtectionLoadStatus.REQUIRES_REVIEW, TechnicalClassification.REQUIRES_REVIEW)
        assertLoad(12.0, 10, 120.0, ProtectionLoadStatus.REQUIRES_REVIEW, TechnicalClassification.REQUIRES_REVIEW)
    }

    @Test
    fun `protection load at nominal current asks for review without overload wording`() {
        val result = ProtectionLoadEvaluator.evaluate(10.0, 10)!!

        assertEquals(ProtectionLoadStatus.REQUIRES_REVIEW, result.status)
        assertTrue(result.detailText().contains("alcanza la corriente nominal"))
        assertTrue(!result.detailText().contains("sobrecarga", ignoreCase = true))
    }

    @Test
    fun `protection conductor compatibility detects mismatch and keeps alternatives orientative`() {
        val result = ProtectionConductorCompatibilityEvaluator.evaluate(
            breakerAmps = 32,
            sectionMm2 = 2.5,
            material = ConductorMaterial.COPPER,
            rules = DefaultElectricalRuleConfigs.all,
        )!!

        assertEquals(TechnicalClassification.CRITICAL_REVIEW, result.classification)
        assertEquals(16.0, result.observedSectionReferenceAmps ?: 0.0, 0.0)
        assertEquals(6.0, result.sectionReferenceForBreaker?.sectionMm2 ?: 0.0, 0.0)
        assertTrue(result.detailText().contains("requiere revisión"))
        assertTrue(result.detailText().contains("2,5 mm² -> protección de referencia 16 A"))
        assertTrue(result.detailText().contains("Para 32 A -> sección de referencia 6 mm²"))
        assertTrue(!result.detailText().contains("cambiar térmica", ignoreCase = true))
        assertTrue(!result.detailText().contains("cambiar cable", ignoreCase = true))
        assertTrue(!result.detailText().contains("corresponde colocar", ignoreCase = true))
    }

    @Test
    fun `protection conductor compatibility accepts breaker within configured reference`() {
        val result = ProtectionConductorCompatibilityEvaluator.evaluate(
            breakerAmps = 10,
            sectionMm2 = 2.5,
            material = ConductorMaterial.COPPER,
            rules = DefaultElectricalRuleConfigs.all,
        )!!

        assertEquals(TechnicalClassification.ACCEPTABLE, result.classification)
    }

    private fun assertLoad(
        measuredCurrentAmps: Double,
        breakerAmps: Int,
        expectedPercent: Double,
        expectedStatus: ProtectionLoadStatus,
        expectedClassification: TechnicalClassification,
    ) {
        val result = ProtectionLoadEvaluator.evaluate(measuredCurrentAmps, breakerAmps)!!

        assertEquals(expectedPercent, result.loadPercent, 0.001)
        assertEquals(expectedStatus, result.status)
        assertEquals(expectedClassification, result.classification)
    }
}
