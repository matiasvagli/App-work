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

        assertEquals(5, progress.totalCount)
        assertFalse(progress.sections.any { it.section == InspectionSection.GENERAL })
        assertFalse(progress.sections.any { it.section == InspectionSection.UNVERIFIED })
        // El informe al cliente se redacta en la atención, no dentro del relevamiento.
        assertFalse(progress.sections.any { it.section == InspectionSection.FINAL_REPORT })
        assertTrue(progress.sections.indexOfFirst { it.section == InspectionSection.GROUNDING } < progress.sections.indexOfFirst { it.section == InspectionSection.FINDINGS })
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
    fun `visual completion does not require pillar panel measurements or findings`() {
        val aggregate = InspectionAggregate(
            inspection = testInspection().copy(
                scope = InspectionScope.VISUAL_INSPECTION,
                supplyType = SupplyType.UNKNOWN,
                propertyType = PropertyType.UNKNOWN,
                reviewReason = "Revisar reflector exterior",
                originalTechnicalComment = null,
            ),
            pillar = null,
            mainPanel = null,
            findings = emptyList(),
            unverifiedItems = emptyList(),
        )

        val result = InspectionCompletionRules.validate(aggregate, hasCalculations = false)

        assertTrue(result.canComplete)
        assertEquals(emptyList<String>(), result.missingItems)
    }

    @Test
    fun `visual progress opens complementary flow and keeps legacy unverified out of steps`() {
        val aggregate = InspectionAggregate(
            inspection = testInspection().copy(scope = InspectionScope.VISUAL_INSPECTION),
            pillar = null,
            mainPanel = null,
            findings = emptyList(),
            unverifiedItems = emptyList(),
        )

        val progress = InspectionProgressCalculator.calculate(aggregate)

        assertTrue(progress.sections.any { it.section == InspectionSection.VISUAL_COMPLEMENTARY })
        assertFalse(progress.sections.any { it.section == InspectionSection.GENERAL })
        assertTrue(progress.sections.any { it.section == InspectionSection.GROUNDING })
        assertFalse(progress.sections.any { it.section == InspectionSection.UNVERIFIED })
    }

    @Test
    fun `grounding soon card does not block completion`() {
        val result = InspectionCompletionRules.validate(completeAggregate())

        assertTrue(result.canComplete)
        assertFalse(result.missingItems.any { it.contains("Puesta a tierra") })
    }

    @Test
    fun `sector assessment only includes pillar when reviewed sector is relevant`() {
        val unrelated = completeAggregate().copy(
            inspection = testInspection().copy(
                scope = InspectionScope.SECTOR_ASSESSMENT,
                reviewedElement = "Circuito de cocina",
                taskDescription = "Se revisa toma y térmica del sector.",
            ),
        )
        val related = unrelated.copy(
            inspection = unrelated.inspection.copy(reviewedElement = "Pilar y medidor"),
        )

        assertFalse(InspectionProgressCalculator.calculate(unrelated).sections.any { it.section == InspectionSection.PILLAR })
        assertTrue(InspectionProgressCalculator.calculate(related).sections.any { it.section == InspectionSection.PILLAR })
    }

    @Test
    fun `sector assessment only includes main panel when reviewed sector is relevant`() {
        val unrelated = completeAggregate().copy(
            inspection = testInspection().copy(
                scope = InspectionScope.SECTOR_ASSESSMENT,
                reviewedElement = "Reflector exterior",
                taskDescription = "Se revisa el artefacto.",
            ),
        )
        val related = unrelated.copy(
            inspection = unrelated.inspection.copy(reviewedElement = "Circuito de cocina"),
        )

        assertFalse(InspectionProgressCalculator.calculate(unrelated).sections.any { it.section == InspectionSection.MAIN_PANEL })
        assertTrue(InspectionProgressCalculator.calculate(related).sections.any { it.section == InspectionSection.MAIN_PANEL })
    }

    @Test
    fun `reopen decision clears completed timestamp`() {
        val completed = testInspection().copy(status = InspectionStatus.COMPLETED, completedAt = Instant.parse("2026-08-04T15:00:00Z"))
        val reopened = completed.copy(status = InspectionStatus.DRAFT, completedAt = null)

        assertEquals(InspectionStatus.DRAFT, reopened.status)
        assertNull(reopened.completedAt)
    }

    @Test
    fun `voltage range suggestion ignores pillar current measurements`() {
        val now = Instant.parse("2026-08-04T14:30:00Z")
        val aggregate = completeAggregate().copy(
            pillarMeasurements = listOf(
                PillarMeasurement(
                    id = "current-1",
                    inspectionId = "inspection-1",
                    type = PillarMeasurementType.SINGLE_PHASE_CURRENT,
                    value = 17.0,
                    unit = "A",
                    origin = MeasurementOrigin.MEASURED,
                    sortOrder = 0,
                    createdAt = now,
                    updatedAt = now,
                    isDeleted = false,
                ),
            ),
        )

        val groups = InspectionFindingProposalBuilder.buildGroups(aggregate)

        assertFalse(groups.suggested.any { it.ruleCode == "SUPPLY_VOLTAGE_RANGE" && it.sourceUnit == "A" })
    }
}

fun completeAggregate(): InspectionAggregate {
    val now = Instant.parse("2026-08-04T14:30:00Z")
    return InspectionAggregate(
        inspection = testInspection(),
        pillar = PillarInspection(
            inspectionId = "inspection-1",
            reviewStatus = InspectionSectionReviewStatus.REVIEWED,
            exists = true,
            propertyType = PropertyType.HOUSE,
            propertyTypeOther = null,
            supplyType = SupplyType.SINGLE_PHASE,
            accessible = AccessStatus.PARTIAL,
            generalCondition = GeneralCondition.POOR,
            mainBreakerPresent = YesNoUnknown.YES,
            mainBreakerAmps = 40,
            mainBreakerOtherAmps = null,
            differentialPresent = YesNoUnknown.UNKNOWN,
            differentialRatedAmps = null,
            differentialOtherRatedAmps = null,
            differentialSensitivityMa = null,
            differentialOtherSensitivityMa = null,
            differentialTestResult = DifferentialTestResult.NOT_TESTED,
            conductorSectionMm2 = 4.0,
            conductorOtherSectionMm2 = null,
            conductorMaterial = ConductorMaterial.COPPER,
            conductorMaterialOther = null,
            conductorCondition = ConductorCondition.DETERIORATED,
            neutralIdentified = YesNoUnknown.UNKNOWN,
            groundingVisible = YesNoUnknown.UNKNOWN,
            protectionCompatibility = ProtectionCompatibility.REQUIRES_VERIFICATION,
            protectionCompatibilityNotes = null,
            notes = "Pilar con acceso parcial.",
            createdAt = now,
            updatedAt = now,
        ),
        mainPanel = MainPanelInspection(
            inspectionId = "inspection-1",
            reviewStatus = InspectionSectionReviewStatus.REVIEWED,
            accessible = AccessStatus.YES,
            generalCondition = GeneralCondition.FAIR,
            differentialPresent = YesNoUnknown.YES,
            differentialRatedAmps = null,
            differentialOtherRatedAmps = null,
            differentialSensitivityMa = 30,
            differentialOtherSensitivityMa = null,
            differentialTestResult = DifferentialTestResult.PASSED,
            circuitCount = null,
            circuitsIdentified = YesNoPartialUnknown.NO,
            neutralBarPresent = YesNoUnknown.YES,
            groundBarPresent = YesNoUnknown.YES,
            neutralAndGroundSeparated = YesNoUnknown.UNKNOWN,
            protectionConductorsPresent = YesNoPartialUnknown.UNKNOWN,
            improvisedConnections = YesNoUnknown.NO,
            conductorColorStatus = ConductorColorStatus.UNKNOWN,
            mixedOrIncorrectColors = YesNoUnknown.UNKNOWN,
            overheatingSigns = YesNoUnknown.NO,
            exposedPartsOrDamagedInsulation = YesNoUnknown.UNKNOWN,
            protectionCompatibility = ProtectionCompatibility.REQUIRES_VERIFICATION,
            wiringRisksNotes = null,
            protectionConductorCheckResult = ProtectionConductorCheckResult.NOT_VERIFIED,
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
        grounding = GroundingInspection(
            inspectionId = "inspection-1",
            electrodePresent = YesNoUnknown.YES,
            inspectionChamberAccessible = YesNoUnknown.YES,
            mainGroundConductorPresent = YesNoUnknown.YES,
            protectiveConductorContinuity = YesNoUnknown.YES,
            resistanceOhms = 18.0,
            resistanceOrigin = MeasurementOrigin.MEASURED,
            notes = null,
            createdAt = now,
            updatedAt = now,
        ),
    )
}
