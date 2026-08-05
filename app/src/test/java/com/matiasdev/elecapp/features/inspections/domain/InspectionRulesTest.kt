package com.matiasdev.elecapp.features.inspections.domain

import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class InspectionRulesTest {
    @Test
    fun `validates positive amperage and conductor section`() {
        assertEquals("El amperaje debe ser mayor a cero", InspectionValidation.validatePositiveInt(0, "El amperaje"))
        assertEquals("La sección debe ser mayor a cero", InspectionValidation.validatePositiveDouble(-1.0, "La sección"))
        assertNull(InspectionValidation.validatePositiveInt(null, "El amperaje"))
        assertNull(InspectionValidation.validatePositiveDouble(4.0, "La sección"))
    }

    @Test
    fun `progress marks completed sections with simple MVP rules`() {
        val aggregate = completeAggregate()
        val progress = InspectionProgressCalculator.calculate(aggregate)

        assertEquals(7, progress.totalCount)
        assertTrue(progress.completedCount >= 5)
        assertEquals(InspectionSectionStatus.COMPLETE, progress.sections.first { it.section == InspectionSection.PILLAR }.status)
        assertEquals(InspectionSectionStatus.COMPLETE, progress.sections.first { it.section == InspectionSection.MAIN_PANEL }.status)
    }

    @Test
    fun `completion requires general data pillar panel and comment or finding`() {
        val incomplete = InspectionAggregate(testInspection().copy(originalTechnicalComment = null), null, null, emptyList(), emptyList())

        val result = InspectionCompletionRules.validate(incomplete)

        assertFalse(result.canComplete)
        assertTrue(result.missingItems.any { it.contains("Pilar") || it.contains("pilar") })
        assertTrue(result.missingItems.any { it.contains("tablero") })
        assertTrue(result.missingItems.any { it.contains("Comentario") })
    }

    @Test
    fun `completion allows completed MVP aggregate`() {
        val result = InspectionCompletionRules.validate(completeAggregate())

        assertTrue(result.canComplete)
        assertEquals(emptyList<String>(), result.missingItems)
    }

    @Test
    fun `reopen decision clears completed timestamp`() {
        val completed = testInspection().copy(status = InspectionStatus.COMPLETED, completedAt = Instant.parse("2026-08-04T15:00:00Z"))
        val reopened = completed.copy(status = InspectionStatus.DRAFT, completedAt = null)

        assertEquals(InspectionStatus.DRAFT, reopened.status)
        assertNull(reopened.completedAt)
    }
}

fun completeAggregate(): InspectionAggregate {
    val now = Instant.parse("2026-08-04T14:30:00Z")
    return InspectionAggregate(
        inspection = testInspection(),
        pillar = PillarInspection(
            inspectionId = "inspection-1",
            exists = true,
            accessible = AccessStatus.PARTIAL,
            generalCondition = GeneralCondition.POOR,
            mainBreakerPresent = YesNoUnknown.YES,
            mainBreakerAmps = 40,
            conductorSectionMm2 = 4.0,
            conductorMaterial = ConductorMaterial.COPPER,
            conductorCondition = ConductorCondition.DETERIORATED,
            neutralIdentified = YesNoUnknown.UNKNOWN,
            groundingVisible = YesNoUnknown.UNKNOWN,
            protectionCompatibility = ProtectionCompatibility.REQUIRES_VERIFICATION,
            notes = "Pilar con acceso parcial.",
            createdAt = now,
            updatedAt = now,
        ),
        mainPanel = MainPanelInspection(
            inspectionId = "inspection-1",
            accessible = AccessStatus.YES,
            generalCondition = GeneralCondition.FAIR,
            differentialPresent = YesNoUnknown.YES,
            differentialRatedAmps = null,
            differentialSensitivityMa = 30,
            differentialTestResult = DifferentialTestResult.PASSED,
            circuitCount = null,
            circuitsIdentified = YesNoPartialUnknown.NO,
            neutralBarPresent = YesNoUnknown.YES,
            groundBarPresent = YesNoUnknown.YES,
            neutralAndGroundSeparated = YesNoUnknown.UNKNOWN,
            improvisedConnections = YesNoUnknown.NO,
            mixedOrIncorrectColors = YesNoUnknown.UNKNOWN,
            overheatingSigns = YesNoUnknown.NO,
            protectionCompatibility = ProtectionCompatibility.REQUIRES_VERIFICATION,
            notes = null,
            createdAt = now,
            updatedAt = now,
        ),
        findings = listOf(
            InspectionFinding(
                id = "finding-1",
                inspectionId = "inspection-1",
                category = FindingCategory.PILLAR,
                severity = FindingSeverity.URGENT,
                title = "Conductores deteriorados",
                description = "Se observaron conductores deteriorados.",
                recommendation = "Verificar y reemplazar.",
                sortOrder = 0,
                createdAt = now,
                updatedAt = now,
                isDeleted = false,
            ),
        ),
        unverifiedItems = listOf(
            InspectionUnverifiedItem(
                id = "unverified-1",
                inspectionId = "inspection-1",
                type = UnverifiedItemType.HIDDEN_WIRING,
                description = null,
                createdAt = now,
                updatedAt = now,
                isDeleted = false,
            ),
        ),
    )
}
