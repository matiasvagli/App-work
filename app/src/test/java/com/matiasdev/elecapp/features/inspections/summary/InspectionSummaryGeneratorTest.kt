package com.matiasdev.elecapp.features.inspections.summary

import com.matiasdev.elecapp.features.electricaltools.domain.CalculationSource
import com.matiasdev.elecapp.features.electricaltools.domain.TechnicalCalculation
import com.matiasdev.elecapp.features.electricaltools.domain.TechnicalCalculationType
import com.matiasdev.elecapp.features.electricaltools.domain.TechnicalClassification
import com.matiasdev.elecapp.features.electricaltools.domain.TechnicianConclusion
import com.matiasdev.elecapp.features.inspections.domain.AccessStatus
import com.matiasdev.elecapp.features.inspections.domain.DifferentialTestResult
import com.matiasdev.elecapp.features.inspections.domain.FindingCategory
import com.matiasdev.elecapp.features.inspections.domain.FindingSeverity
import com.matiasdev.elecapp.features.inspections.domain.GeneralCondition
import com.matiasdev.elecapp.features.inspections.domain.InspectionAggregate
import com.matiasdev.elecapp.features.inspections.domain.InspectionFinding
import com.matiasdev.elecapp.features.inspections.domain.InspectionScope
import com.matiasdev.elecapp.features.inspections.domain.InspectionSectionReviewStatus
import com.matiasdev.elecapp.features.inspections.domain.InspectionUnverifiedItem
import com.matiasdev.elecapp.features.inspections.domain.MainPanelInspection
import com.matiasdev.elecapp.features.inspections.domain.MeasurementOrigin
import com.matiasdev.elecapp.features.inspections.domain.PillarMeasurement
import com.matiasdev.elecapp.features.inspections.domain.PillarMeasurementType
import com.matiasdev.elecapp.features.inspections.domain.PropertyType
import com.matiasdev.elecapp.features.inspections.domain.SupplyType
import com.matiasdev.elecapp.features.inspections.domain.UnverifiedItemType
import com.matiasdev.elecapp.features.inspections.domain.YesNoPartialUnknown
import com.matiasdev.elecapp.features.inspections.domain.YesNoUnknown
import com.matiasdev.elecapp.features.inspections.domain.completeAggregate
import com.matiasdev.elecapp.features.inspections.domain.testVisit
import com.matiasdev.elecapp.features.inspections.domain.testInspection
import java.time.ZoneId
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InspectionSummaryGeneratorTest {
    @Test
    fun `generates deterministic structured summary in Spanish`() {
        val summary = InspectionSummaryGenerator.generate(completeAggregate(), testVisit(), ZoneId.of("UTC"))

        assertTrue(summary.contains("VISITA TÉCNICA"))
        assertTrue(summary.contains("Cliente: Carlos López"))
        assertTrue(summary.contains("Fecha: 04/08/2026"))
        assertTrue(summary.contains("[URGENTE] Conductores deteriorados"))
        assertTrue(summary.contains("Estos elementos no fueron verificados durante la visita."))
        assertEquals(summary, InspectionSummaryGenerator.generate(completeAggregate(), testVisit(), ZoneId.of("UTC")))
    }

    @Test
    fun `omits empty fields and does not invent values`() {
        val summary = InspectionSummaryGenerator.generate(completeAggregate(), testVisit(), ZoneId.of("UTC"))

        assertFalse(summary.contains("Corriente diferencial nominal"))
        assertFalse(summary.contains("Cantidad de circuitos"))
        assertTrue(summary.contains("No constituye una certificación integral"))
    }

    @Test
    fun `preserves original technical comment exactly`() {
        val summary = InspectionSummaryGenerator.generate(completeAggregate(), testVisit(), ZoneId.of("UTC"))

        assertTrue(summary.contains("COMENTARIO ORIGINAL DEL ELECTRICISTA\nComentario técnico exacto."))
    }

    @Test
    fun `visual summary omits empty pillar panel calculations and findings`() {
        val summary = InspectionSummaryGenerator.generate(visualAggregate(), testVisit(), ZoneId.of("UTC"), calculations = emptyList())

        assertTrue(summary.startsWith("INSPECCIÓN VISUAL"))
        assertFalse(summary.contains("PILAR Y ACOMETIDA"))
        assertFalse(summary.contains("TABLERO PRINCIPAL"))
        assertFalse(summary.contains("MEDICIONES Y CÁLCULOS"))
        assertFalse(summary.contains("HALLAZGOS"))
        assertFalse(summary.contains("NO VERIFICADO"))
        assertFalse(summary.contains(": \n"))
        assertTrue(summary.contains("La revisión se limitó al sector"))
    }

    @Test
    fun `visual summary prints only real data and differentiates panel fields`() {
        val summary = InspectionSummaryGenerator.generate(
            visualAggregate(
                mainPanel = visualMainPanel(),
                findings = listOf(visualFinding()),
                unverifiedItems = listOf(visualUnverified()),
            ),
            testVisit(),
            ZoneId.of("UTC"),
            calculations = listOf(visualCalculation()),
        )

        assertFalse(summary.contains("PILAR Y ACOMETIDA"))
        assertTrue(summary.contains("TABLERO PRINCIPAL"))
        assertTrue(summary.contains("Interruptor diferencial visible: Sí"))
        assertTrue(summary.contains("Corriente nominal: 20 A"))
        assertTrue(summary.contains("Sensibilidad: 30 mA"))
        assertTrue(summary.contains("Prueba manual: No realizada"))
        assertTrue(summary.contains("MEDICIONES Y CÁLCULOS"))
        assertTrue(summary.contains("[RECOMENDADO] Toma deteriorada"))
        assertTrue(summary.contains("NO VERIFICADO"))
        assertTrue(summary.contains("OBSERVACIONES"))
    }

    @Test
    fun `visual summary can show explicit not verified status`() {
        val summary = InspectionSummaryGenerator.generate(
            visualAggregate(
                mainPanel = MainPanelInspection(
                    inspectionId = "inspection-1",
                    reviewStatus = InspectionSectionReviewStatus.NOT_VERIFIED,
                    accessible = AccessStatus.UNKNOWN,
                    generalCondition = GeneralCondition.NOT_ASSESSED,
                    differentialPresent = YesNoUnknown.UNKNOWN,
                    differentialRatedAmps = null,
                    differentialSensitivityMa = null,
                    differentialTestResult = DifferentialTestResult.NOT_TESTED,
                    circuitCount = null,
                    circuitsIdentified = YesNoPartialUnknown.UNKNOWN,
                    neutralBarPresent = YesNoUnknown.UNKNOWN,
                    groundBarPresent = YesNoUnknown.UNKNOWN,
                    neutralAndGroundSeparated = YesNoUnknown.UNKNOWN,
                    improvisedConnections = YesNoUnknown.UNKNOWN,
                    mixedOrIncorrectColors = YesNoUnknown.UNKNOWN,
                    overheatingSigns = YesNoUnknown.UNKNOWN,
                    protectionCompatibility = com.matiasdev.elecapp.features.inspections.domain.ProtectionCompatibility.NOT_ASSESSED,
                    notes = null,
                    createdAt = Instant.parse("2026-08-04T14:30:00Z"),
                    updatedAt = Instant.parse("2026-08-04T14:30:00Z"),
                ),
            ),
            testVisit(),
            ZoneId.of("UTC"),
        )

        assertTrue(summary.contains("Estado de revisión: No se verificó"))
    }

    @Test
    fun `pillar summary omits contradictory dependencies and duplicate breaker rows`() {
        val summary = InspectionSummaryGenerator.generate(
            completeAggregate().copy(
                pillar = completeAggregate().pillar?.copy(
                    mainBreakerPresent = YesNoUnknown.YES,
                    mainBreakerAmps = 25,
                    differentialPresent = YesNoUnknown.NO,
                    differentialRatedAmps = null,
                    differentialSensitivityMa = null,
                ),
                mainPanel = null,
            ),
            testVisit(),
            ZoneId.of("UTC"),
        )

        assertTrue(summary.contains("Térmica principal: 25 A"))
        assertFalse(summary.contains("Térmica principal: sí"))
        assertTrue(summary.contains("Interruptor diferencial en pilar: no"))
        assertFalse(summary.contains("Sensibilidad: 30 mA"))
        assertFalse(summary.contains("Neutro identificado"))
        assertFalse(summary.contains("Puesta a tierra visible"))
    }

    @Test
    fun `pillar summary lists only performed three phase measurements with units and origin`() {
        val now = Instant.parse("2026-08-04T14:30:00Z")
        val summary = InspectionSummaryGenerator.generate(
            completeAggregate().copy(
                pillar = completeAggregate().pillar?.copy(supplyType = SupplyType.THREE_PHASE),
                pillarMeasurements = listOf(
                    PillarMeasurement(
                        id = "measurement-1",
                        inspectionId = "inspection-1",
                        type = PillarMeasurementType.VOLTAGE_L1_L2,
                        value = 381.0,
                        unit = "V",
                        origin = MeasurementOrigin.MEASURED,
                        sortOrder = 0,
                        createdAt = now,
                        updatedAt = now,
                        isDeleted = false,
                    ),
                    PillarMeasurement(
                        id = "measurement-2",
                        inspectionId = "inspection-1",
                        type = PillarMeasurementType.CURRENT_L1,
                        value = 16.5,
                        unit = "A",
                        origin = MeasurementOrigin.DECLARED_BY_CLIENT,
                        sortOrder = 1,
                        createdAt = now,
                        updatedAt = now,
                        isDeleted = false,
                    ),
                ),
            ),
            testVisit(),
            ZoneId.of("UTC"),
        )

        assertTrue(summary.contains("L1-L2: 381 V (medido)"))
        assertTrue(summary.contains("Corriente L1: 16,5 A (declarado por el cliente)"))
        assertFalse(summary.contains("L2-L3"))
    }

    private fun visualAggregate(
        mainPanel: MainPanelInspection? = null,
        findings: List<InspectionFinding> = emptyList(),
        unverifiedItems: List<InspectionUnverifiedItem> = emptyList(),
    ): InspectionAggregate {
        return InspectionAggregate(
            inspection = testInspection().copy(
                scope = InspectionScope.VISUAL_INSPECTION,
                supplyType = SupplyType.UNKNOWN,
                propertyType = PropertyType.UNKNOWN,
                reviewReason = "Revisar toma del dormitorio",
                reviewedElement = "Toma del dormitorio",
                taskDescription = "Se observa calentamiento en la tapa.",
                originalTechnicalComment = "Se revisó únicamente el punto indicado.",
            ),
            pillar = null,
            mainPanel = mainPanel,
            findings = findings,
            unverifiedItems = unverifiedItems,
        )
    }

    private fun visualMainPanel(): MainPanelInspection {
        val now = Instant.parse("2026-08-04T14:30:00Z")
        return MainPanelInspection(
            inspectionId = "inspection-1",
            reviewStatus = InspectionSectionReviewStatus.REVIEWED,
            accessible = AccessStatus.YES,
            generalCondition = GeneralCondition.FAIR,
            differentialPresent = YesNoUnknown.YES,
            differentialRatedAmps = 20,
            differentialSensitivityMa = 30,
            differentialTestResult = DifferentialTestResult.NOT_TESTED,
            circuitCount = null,
            circuitsIdentified = YesNoPartialUnknown.UNKNOWN,
            neutralBarPresent = YesNoUnknown.UNKNOWN,
            groundBarPresent = YesNoUnknown.UNKNOWN,
            neutralAndGroundSeparated = YesNoUnknown.UNKNOWN,
            improvisedConnections = YesNoUnknown.NO,
            mixedOrIncorrectColors = YesNoUnknown.UNKNOWN,
            overheatingSigns = YesNoUnknown.NO,
            protectionCompatibility = com.matiasdev.elecapp.features.inspections.domain.ProtectionCompatibility.NOT_ASSESSED,
            notes = null,
            createdAt = now,
            updatedAt = now,
        )
    }

    private fun visualFinding(): InspectionFinding {
        val now = Instant.parse("2026-08-04T14:30:00Z")
        return InspectionFinding(
            id = "finding-visual",
            inspectionId = "inspection-1",
            category = FindingCategory.GENERAL,
            severity = FindingSeverity.RECOMMENDED,
            title = "Toma deteriorada",
            description = "Se observó deterioro visible en la toma.",
            recommendation = "Reemplazar la toma y verificar conexiones visibles.",
            sortOrder = 0,
            createdAt = now,
            updatedAt = now,
            isDeleted = false,
        )
    }

    private fun visualUnverified(): InspectionUnverifiedItem {
        val now = Instant.parse("2026-08-04T14:30:00Z")
        return InspectionUnverifiedItem(
            id = "unverified-visual",
            inspectionId = "inspection-1",
            type = UnverifiedItemType.PANEL_NOT_OPENED,
            description = "No fue necesario abrirlo para la urgencia.",
            createdAt = now,
            updatedAt = now,
            isDeleted = false,
        )
    }

    private fun visualCalculation(): TechnicalCalculation {
        val now = Instant.parse("2026-08-04T14:30:00Z")
        return TechnicalCalculation(
            id = "calc-visual",
            type = TechnicalCalculationType.OTHER,
            source = CalculationSource.MEASURED,
            clientId = "client-1",
            visitId = "visit-1",
            inspectionId = "inspection-1",
            title = "Medición de tensión",
            description = null,
            inputDataJson = "{}",
            resultDataJson = "{}",
            primaryResultValue = 221.0,
            primaryResultUnit = "V",
            classification = TechnicalClassification.INFORMATIONAL,
            technicianConclusion = TechnicianConclusion.NOT_REVIEWED,
            technicianNotes = null,
            formulaVersion = "manual-1",
            createdAt = now,
            updatedAt = now,
            isDeleted = false,
        )
    }
}
