package com.matiasdev.elecapp.features.inspections.summary

import com.matiasdev.elecapp.features.electricaltools.domain.CalculationSource
import com.matiasdev.elecapp.features.electricaltools.domain.TechnicalCalculation
import com.matiasdev.elecapp.features.electricaltools.domain.TechnicalCalculationType
import com.matiasdev.elecapp.features.electricaltools.domain.TechnicalClassification
import com.matiasdev.elecapp.features.electricaltools.domain.TechnicianConclusion
import com.matiasdev.elecapp.features.inspections.domain.AccessStatus
import com.matiasdev.elecapp.features.inspections.domain.AutoInspectionCalculationBuilder
import com.matiasdev.elecapp.features.inspections.domain.DifferentialTestResult
import com.matiasdev.elecapp.features.inspections.domain.FindingCategory
import com.matiasdev.elecapp.features.inspections.domain.FindingReviewStatus
import com.matiasdev.elecapp.features.inspections.domain.FindingSeverity
import com.matiasdev.elecapp.features.inspections.domain.FindingSourceType
import com.matiasdev.elecapp.features.inspections.domain.GeneralCondition
import com.matiasdev.elecapp.features.inspections.domain.InspectionAggregate
import com.matiasdev.elecapp.features.inspections.domain.InspectionFinding
import com.matiasdev.elecapp.features.inspections.domain.InspectionScope
import com.matiasdev.elecapp.features.inspections.domain.InspectionSection
import com.matiasdev.elecapp.features.inspections.domain.InspectionSectionReviewStatus
import com.matiasdev.elecapp.features.inspections.domain.InspectionUnverifiedItem
import com.matiasdev.elecapp.features.inspections.domain.MainPanelCircuit
import com.matiasdev.elecapp.features.inspections.domain.MainPanelInspection
import com.matiasdev.elecapp.features.inspections.domain.MainPanelMeasurement
import com.matiasdev.elecapp.features.inspections.domain.MainPanelMeasurementSection
import com.matiasdev.elecapp.features.inspections.domain.MainPanelMeasurementType
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
        assertTrue(summary.contains("Interruptor diferencial: 20 A / 30 mA / prueba no realizada"))
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
                    differentialOtherRatedAmps = null,
                    differentialSensitivityMa = null,
                    differentialOtherSensitivityMa = null,
                    differentialTestResult = DifferentialTestResult.NOT_TESTED,
                    circuitCount = null,
                    circuitsIdentified = YesNoPartialUnknown.UNKNOWN,
                    neutralBarPresent = YesNoUnknown.UNKNOWN,
                    groundBarPresent = YesNoUnknown.UNKNOWN,
                    neutralAndGroundSeparated = YesNoUnknown.UNKNOWN,
                    protectionConductorsPresent = YesNoPartialUnknown.UNKNOWN,
                    improvisedConnections = YesNoUnknown.UNKNOWN,
                    conductorColorStatus = com.matiasdev.elecapp.features.inspections.domain.ConductorColorStatus.UNKNOWN,
                    mixedOrIncorrectColors = YesNoUnknown.UNKNOWN,
                    overheatingSigns = YesNoUnknown.UNKNOWN,
                    exposedPartsOrDamagedInsulation = YesNoUnknown.UNKNOWN,
                    protectionCompatibility = com.matiasdev.elecapp.features.inspections.domain.ProtectionCompatibility.NOT_ASSESSED,
                    wiringRisksNotes = null,
                    protectionConductorCheckResult = com.matiasdev.elecapp.features.inspections.domain.ProtectionConductorCheckResult.NOT_VERIFIED,
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

    @Test
    fun `main panel summary combines differential and omits dependent fields when absent`() {
        val summary = InspectionSummaryGenerator.generate(
            completeAggregate().copy(
                mainPanel = completeAggregate().mainPanel?.copy(
                    differentialPresent = YesNoUnknown.YES,
                    differentialRatedAmps = 40,
                    differentialSensitivityMa = 30,
                    differentialTestResult = DifferentialTestResult.PASSED,
                ),
            ),
            testVisit(),
            ZoneId.of("UTC"),
        )

        assertTrue(summary.contains("Interruptor diferencial: 40 A / 30 mA / prueba correcta"))
        assertFalse(summary.contains("Corriente diferencial nominal"))
        assertFalse(summary.contains("Sensibilidad: 30 mA"))
    }

    @Test
    fun `main panel summary lists circuit details without empty fields`() {
        val now = Instant.parse("2026-08-04T14:30:00Z")
        val summary = InspectionSummaryGenerator.generate(
            completeAggregate().copy(
                mainPanelCircuits = listOf(
                    MainPanelCircuit(
                        id = "circuit-1",
                        inspectionId = "inspection-1",
                        sortOrder = 0,
                        destination = com.matiasdev.elecapp.features.inspections.domain.CircuitDestination.LIGHTING,
                        destinationOther = null,
                        breakerAmps = 10,
                        breakerOtherAmps = null,
                        breakerCurve = com.matiasdev.elecapp.features.inspections.domain.BreakerCurve.UNKNOWN,
                        conductorSectionMm2 = 1.5,
                        conductorOtherSectionMm2 = null,
                        conductorMaterial = com.matiasdev.elecapp.features.inspections.domain.ConductorMaterial.COPPER,
                        conductorMaterialOther = null,
                        consumptionAmps = 4.2,
                        consumptionOrigin = MeasurementOrigin.MEASURED,
                        notes = null,
                        createdAt = now,
                        updatedAt = now,
                        isDeleted = false,
                    ),
                ),
            ),
            testVisit(),
            ZoneId.of("UTC"),
        )

        assertTrue(summary.contains("Circuito 1 (iluminación): térmica 10 A"))
        assertTrue(summary.contains("conductor cobre 1,5 mm²"))
        assertTrue(summary.contains("consumo medido 4,2 A"))
    }

    @Test
    fun `measurements and calculations section collects inspection measurements by source`() {
        val now = Instant.parse("2026-08-04T14:30:00Z")
        val summary = InspectionSummaryGenerator.generate(
            completeAggregate().copy(
                pillarMeasurements = listOf(
                    PillarMeasurement(
                        id = "pillar-voltage",
                        inspectionId = "inspection-1",
                        type = PillarMeasurementType.SINGLE_PHASE_VOLTAGE_LN,
                        value = 205.0,
                        unit = "V",
                        origin = MeasurementOrigin.MEASURED,
                        sortOrder = 0,
                        createdAt = now,
                        updatedAt = now,
                        isDeleted = false,
                    ),
                ),
                mainPanelMeasurements = listOf(
                    MainPanelMeasurement(
                        id = "panel-voltage",
                        inspectionId = "inspection-1",
                        section = MainPanelMeasurementSection.INPUT_VOLTAGE,
                        type = MainPanelMeasurementType.INPUT_VOLTAGE_LN,
                        value = 192.0,
                        unit = "V",
                        origin = MeasurementOrigin.MEASURED,
                        sortOrder = 0,
                        createdAt = now,
                        updatedAt = now,
                        isDeleted = false,
                    ),
                    MainPanelMeasurement(
                        id = "panel-phase-ground",
                        inspectionId = "inspection-1",
                        section = MainPanelMeasurementSection.PROTECTION_CONDUCTOR_CHECK,
                        type = MainPanelMeasurementType.PROTECTION_VOLTAGE_PHASE_GROUND,
                        value = 185.0,
                        unit = "V",
                        origin = MeasurementOrigin.MEASURED,
                        sortOrder = 1,
                        createdAt = now,
                        updatedAt = now,
                        isDeleted = false,
                    ),
                ),
                mainPanelCircuits = listOf(
                    MainPanelCircuit(
                        id = "circuit-1",
                        inspectionId = "inspection-1",
                        sortOrder = 0,
                        destination = com.matiasdev.elecapp.features.inspections.domain.CircuitDestination.LIGHTING,
                        destinationOther = null,
                        breakerAmps = 10,
                        breakerOtherAmps = null,
                        breakerCurve = com.matiasdev.elecapp.features.inspections.domain.BreakerCurve.UNKNOWN,
                        conductorSectionMm2 = 1.5,
                        conductorOtherSectionMm2 = null,
                        conductorMaterial = com.matiasdev.elecapp.features.inspections.domain.ConductorMaterial.COPPER,
                        conductorMaterialOther = null,
                        consumptionAmps = 12.0,
                        consumptionOrigin = MeasurementOrigin.MEASURED,
                        notes = null,
                        createdAt = now,
                        updatedAt = now,
                        isDeleted = false,
                    ),
                    MainPanelCircuit(
                        id = "circuit-2",
                        inspectionId = "inspection-1",
                        sortOrder = 1,
                        destination = com.matiasdev.elecapp.features.inspections.domain.CircuitDestination.UNIDENTIFIED,
                        destinationOther = null,
                        breakerAmps = 16,
                        breakerOtherAmps = null,
                        breakerCurve = com.matiasdev.elecapp.features.inspections.domain.BreakerCurve.UNKNOWN,
                        conductorSectionMm2 = 2.5,
                        conductorOtherSectionMm2 = null,
                        conductorMaterial = com.matiasdev.elecapp.features.inspections.domain.ConductorMaterial.COPPER,
                        conductorMaterialOther = null,
                        consumptionAmps = 8.0,
                        consumptionOrigin = MeasurementOrigin.MEASURED,
                        notes = null,
                        createdAt = now,
                        updatedAt = now,
                        isDeleted = false,
                    ),
                ),
                calculations = emptyList(),
            ),
            testVisit(),
            ZoneId.of("UTC"),
            calculations = emptyList(),
        )

        assertTrue(summary.contains("MEDICIONES Y CÁLCULOS"))
        assertTrue(summary.contains("Pilar y acometida"))
        assertTrue(summary.contains("- Tensión fase-neutro: 205 V (medido)"))
        assertTrue(summary.contains("Tablero principal"))
        assertTrue(summary.contains("- Tensión fase-neutro: 192 V (medido)"))
        assertTrue(summary.contains("- Fase-tierra: 185 V (medido)"))
        assertTrue(summary.contains("Circuitos"))
        assertTrue(summary.contains("- Circuito 1 (iluminación): consumo 12 A (medido)"))
        assertTrue(summary.contains("- Circuito 2 sin identificar: consumo 8 A (medido)"))
        assertFalse(summary.contains("unidentified"))
        assertTrue(summary.contains("Cálculos técnicos"))
        assertTrue(summary.contains("- Sin cálculos registrados"))
        assertFalse(summary.contains("- Sin mediciones ni cálculos asociados"))
    }

    @Test
    fun `automatic protection calculations are reported with neutral review text`() {
        val now = Instant.parse("2026-08-04T14:30:00Z")
        val base = completeAggregate()
        val aggregate = base.copy(
            pillar = base.pillar?.copy(
                mainBreakerAmps = 32,
                conductorSectionMm2 = 2.5,
                conductorMaterial = com.matiasdev.elecapp.features.inspections.domain.ConductorMaterial.COPPER,
            ),
            mainPanelCircuits = listOf(
                MainPanelCircuit(
                    id = "circuit-1",
                    inspectionId = "inspection-1",
                    sortOrder = 0,
                    destination = com.matiasdev.elecapp.features.inspections.domain.CircuitDestination.UNIDENTIFIED,
                    destinationOther = null,
                    breakerAmps = 10,
                    breakerOtherAmps = null,
                    breakerCurve = com.matiasdev.elecapp.features.inspections.domain.BreakerCurve.UNKNOWN,
                    conductorSectionMm2 = 2.5,
                    conductorOtherSectionMm2 = null,
                    conductorMaterial = com.matiasdev.elecapp.features.inspections.domain.ConductorMaterial.COPPER,
                    conductorMaterialOther = null,
                    consumptionAmps = 10.0,
                    consumptionOrigin = MeasurementOrigin.MEASURED,
                    notes = null,
                    createdAt = now,
                    updatedAt = now,
                    isDeleted = false,
                ),
            ),
        )
        val summary = InspectionSummaryGenerator.generate(
            aggregate,
            testVisit(),
            ZoneId.of("UTC"),
            autoCalculations = AutoInspectionCalculationBuilder.build(aggregate),
        )

        assertTrue(summary.contains("[AUTO] Pilar: compatibilidad térmica-conductor: crítico"))
        assertTrue(summary.contains("Protección de 32 A asociada a conductor observado de cobre de 2,5 mm². La combinación requiere revisión."))
        assertTrue(summary.contains("  * 2,5 mm² -> protección de referencia 16 A."))
        assertTrue(summary.contains("  * Para 32 A -> sección de referencia 6 mm²."))
        assertTrue(summary.contains("[AUTO] Circuito 1 sin identificar: consumo y térmica: 10 A sobre 10 A · requiere revisión"))
        assertFalse(summary.contains("sobrecarga", ignoreCase = true))
        assertFalse(summary.contains("bajar", ignoreCase = true))
        assertFalse(summary.contains("cambiar térmica", ignoreCase = true))
    }

    @Test
    fun `main panel quick protection check does not assert grounding correctness`() {
        val now = Instant.parse("2026-08-04T14:30:00Z")
        val summary = InspectionSummaryGenerator.generate(
            completeAggregate().copy(
                mainPanel = completeAggregate().mainPanel?.copy(
                    protectionConductorCheckResult = com.matiasdev.elecapp.features.inspections.domain.ProtectionConductorCheckResult.REQUIRES_REVIEW,
                ),
                mainPanelMeasurements = listOf(
                    MainPanelMeasurement(
                        id = "measurement-panel-1",
                        inspectionId = "inspection-1",
                        section = MainPanelMeasurementSection.PROTECTION_CONDUCTOR_CHECK,
                        type = MainPanelMeasurementType.PROTECTION_VOLTAGE_PHASE_GROUND,
                        value = 219.0,
                        unit = "V",
                        origin = MeasurementOrigin.MEASURED,
                        sortOrder = 0,
                        createdAt = now,
                        updatedAt = now,
                        isDeleted = false,
                    ),
                ),
            ),
            testVisit(),
            ZoneId.of("UTC"),
        )

        assertTrue(summary.contains("Verificación rápida del conductor de protección"))
        assertTrue(summary.contains("Fase-tierra: 219 V"))
        assertTrue(summary.contains("Resultado orientativo: requiere revisión"))
        assertFalse(summary.contains("puesta a tierra correcta"))
    }

    @Test
    fun `summary separates not verified and avoids definitive text for unconfirmed suggestions`() {
        val now = Instant.parse("2026-08-04T14:30:00Z")
        val summary = InspectionSummaryGenerator.generate(
            completeAggregate().copy(
                findings = listOf(
                    InspectionFinding(
                        id = "suggested-1",
                        inspectionId = "inspection-1",
                        category = FindingCategory.PROTECTIONS,
                        severity = FindingSeverity.RECOMMENDED,
                        title = "Tablero principal",
                        description = "La tensión se encuentra fuera del rango configurado.",
                        recommendation = null,
                        sourceType = FindingSourceType.RULE_SUGGESTION,
                        sourceSection = InspectionSection.MAIN_PANEL,
                        sourceEntityId = "measurement-1",
                        sourceValue = 260.0,
                        sourceUnit = "V",
                        ruleCode = "SUPPLY_VOLTAGE_RANGE",
                        reviewStatus = FindingReviewStatus.PENDING,
                        includeInReport = true,
                        technicianNotes = null,
                        sortOrder = 0,
                        createdAt = now,
                        updatedAt = now,
                        isDeleted = false,
                    ),
                    InspectionFinding(
                        id = "review-1",
                        inspectionId = "inspection-1",
                        category = FindingCategory.CIRCUITS,
                        severity = FindingSeverity.OK,
                        title = "Tablero principal",
                        description = "Revisar el valor ingresado antes de incluirlo en el informe.",
                        recommendation = null,
                        sourceType = FindingSourceType.DATA_REVIEW,
                        sourceSection = InspectionSection.MAIN_PANEL,
                        sourceEntityId = "circuit-1",
                        sourceValue = 198.0,
                        sourceUnit = "A",
                        ruleCode = null,
                        reviewStatus = FindingReviewStatus.PENDING,
                        includeInReport = true,
                        technicianNotes = null,
                        sortOrder = 1,
                        createdAt = now,
                        updatedAt = now,
                        isDeleted = false,
                    ),
                    InspectionFinding(
                        id = "not-verified-1",
                        inspectionId = "inspection-1",
                        category = FindingCategory.GROUNDING,
                        severity = FindingSeverity.OK,
                        title = "Tablero principal",
                        description = "No se verificó la existencia de bornera de tierra.",
                        recommendation = null,
                        sourceType = FindingSourceType.NOT_VERIFIED,
                        sourceSection = InspectionSection.MAIN_PANEL,
                        sourceEntityId = "panel-ground-bar",
                        sourceValue = null,
                        sourceUnit = null,
                        ruleCode = null,
                        reviewStatus = FindingReviewStatus.PENDING,
                        includeInReport = true,
                        technicianNotes = null,
                        sortOrder = 2,
                        createdAt = now,
                        updatedAt = now,
                        isDeleted = false,
                    ),
                ),
            ),
            testVisit(),
            ZoneId.of("UTC"),
        )

        assertTrue(summary.contains("Sugerencia de la app, requiere validación del técnico"))
        assertTrue(summary.contains("NO VERIFICADO"))
        assertTrue(summary.contains("No se verificó la existencia de bornera de tierra."))
        assertFalse(summary.contains("Revisar el valor ingresado antes de incluirlo en el informe."))
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
            differentialOtherRatedAmps = null,
            differentialSensitivityMa = 30,
            differentialOtherSensitivityMa = null,
            differentialTestResult = DifferentialTestResult.NOT_TESTED,
            circuitCount = null,
            circuitsIdentified = YesNoPartialUnknown.UNKNOWN,
            neutralBarPresent = YesNoUnknown.UNKNOWN,
            groundBarPresent = YesNoUnknown.UNKNOWN,
            neutralAndGroundSeparated = YesNoUnknown.UNKNOWN,
            protectionConductorsPresent = YesNoPartialUnknown.UNKNOWN,
            improvisedConnections = YesNoUnknown.NO,
            conductorColorStatus = com.matiasdev.elecapp.features.inspections.domain.ConductorColorStatus.UNKNOWN,
            mixedOrIncorrectColors = YesNoUnknown.UNKNOWN,
            overheatingSigns = YesNoUnknown.NO,
            exposedPartsOrDamagedInsulation = YesNoUnknown.UNKNOWN,
            protectionCompatibility = com.matiasdev.elecapp.features.inspections.domain.ProtectionCompatibility.NOT_ASSESSED,
            wiringRisksNotes = null,
            protectionConductorCheckResult = com.matiasdev.elecapp.features.inspections.domain.ProtectionConductorCheckResult.NOT_VERIFIED,
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
