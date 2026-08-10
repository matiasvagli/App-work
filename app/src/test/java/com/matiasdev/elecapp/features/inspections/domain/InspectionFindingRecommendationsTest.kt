package com.matiasdev.elecapp.features.inspections.domain

import com.matiasdev.elecapp.features.inspections.summary.InspectionSummaryGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import java.time.Instant
import java.time.ZoneId
import org.junit.Test

/**
 * La recomendación de cada hallazgo forma parte del informe técnico que firma el
 * electricista, así que se decide acá y no en el prompt de la IA.
 */
class InspectionFindingRecommendationsTest {

    @Test
    fun `un diferencial que falla la prueba es urgente y se reemplaza, no se revisa`() {
        val finding = confirmedFinding("auto:obs:panel:differential_failed") {
            it.copy(differentialTestResult = DifferentialTestResult.FAILED)
        }

        assertEquals(FindingSeverity.URGENT, finding.severity)
        assertEquals(InspectionFindingRecommendations.PANEL_DIFFERENTIAL_FAILED, finding.recommendation)
        assertTrue(finding.recommendation!!.startsWith("Reemplazar el interruptor diferencial"))
    }

    @Test
    fun `un tablero sin diferencial es urgente y hay que instalarlo`() {
        val finding = confirmedFinding("auto:obs:panel:differential_missing") {
            it.copy(differentialPresent = YesNoUnknown.NO)
        }

        assertEquals(FindingSeverity.URGENT, finding.severity)
        assertTrue(finding.recommendation!!.startsWith("Instalar un interruptor diferencial"))
    }

    @Test
    fun `todo hallazgo confirmado automatico trae una accion concreta`() {
        val groups = InspectionFindingProposalBuilder.buildGroups(aggregateWithEveryObservation())

        assertTrue("no se generó ningún hallazgo confirmado", groups.confirmed.isNotEmpty())
        val sinRecomendacion = groups.confirmed.filter { it.recommendation.isNullOrBlank() }
        assertTrue(
            "hallazgos confirmados sin recomendación: ${sinRecomendacion.map { it.id }}",
            sinRecomendacion.isEmpty(),
        )
    }

    @Test
    fun `un hallazgo ya guardado recibe la recomendacion nueva pero conserva la severidad del tecnico`() {
        val base = aggregateWithEveryObservation()
        val guardado = InspectionFinding(
            id = "auto:obs:panel:differential_failed",
            inspectionId = "inspection-1",
            category = FindingCategory.PROTECTIONS,
            severity = FindingSeverity.RECOMMENDED,
            title = "Tablero principal",
            description = "La prueba manual del interruptor diferencial fue fallida.",
            recommendation = null,
            sourceType = FindingSourceType.OBSERVATION_CONFIRMED,
            sortOrder = 0,
            createdAt = Instant.parse("2026-08-04T14:30:00Z"),
            updatedAt = Instant.parse("2026-08-04T14:30:00Z"),
            isDeleted = false,
        )

        val groups = InspectionFindingProposalBuilder.buildGroups(
            base.copy(findings = base.findings + guardado),
        )
        val finding = groups.confirmed.first { it.id == "auto:obs:panel:differential_failed" }

        assertEquals(InspectionFindingRecommendations.PANEL_DIFFERENTIAL_FAILED, finding.recommendation)
        assertEquals(FindingSeverity.RECOMMENDED, finding.severity)
    }

    @Test
    fun `una sugerencia pendiente de validacion recomienda verificar, no accionar`() {
        val base = aggregateWithEveryObservation()
        val aggregate = base.copy(
            grounding = base.grounding?.copy(
                resistanceOhms = 999.0,
                resistanceOrigin = MeasurementOrigin.MEASURED,
            ),
        )

        val finding = InspectionFindingProposalBuilder.buildGroups(aggregate)
            .suggested
            .first { it.id == "auto:rule:grounding:resistance" }

        assertEquals(InspectionFindingRecommendations.GROUND_RESISTANCE_ABOVE_LIMIT, finding.recommendation)
        assertEquals(FindingReviewStatus.PENDING, finding.reviewStatus)
    }

    @Test
    fun `la recomendacion viaja al informe tecnico, que es lo que despues lee la IA`() {
        val summary = InspectionSummaryGenerator.generate(
            aggregateWithEveryObservation(),
            testVisit(),
            ZoneId.of("UTC"),
        )

        assertTrue(summary.contains("[URGENTE] Tablero principal"))
        assertTrue(summary.contains("Recomendación: ${InspectionFindingRecommendations.PANEL_DIFFERENTIAL_FAILED}"))
    }

    @Test
    fun `el overview cuenta los hallazgos automaticos aunque no esten guardados en Room`() {
        val base = aggregateWithEveryObservation()
        val aggregate = base.copy(
            inspection = base.inspection.copy(findingsReviewedAt = Instant.parse("2026-08-04T15:00:00Z")),
        )

        val findings = InspectionProgressCalculator.calculate(aggregate)
            .sections
            .first { it.section == InspectionSection.FINDINGS }

        assertEquals(InspectionSectionStatus.COMPLETE, findings.status)
        assertTrue(
            "el overview seguía diciendo '${findings.summary}' con la pantalla de hallazgos llena",
            findings.summary.endsWith("hallazgo(s)"),
        )
    }

    private fun confirmedFinding(
        id: String,
        panel: (MainPanelInspection) -> MainPanelInspection,
    ): InspectionFinding {
        val base = completeAggregate()
        val aggregate = base.copy(mainPanel = panel(base.mainPanel!!), findings = emptyList())
        return InspectionFindingProposalBuilder.buildGroups(aggregate).confirmed.first { it.id == id }
    }

    /** Dispara de una todas las observaciones confirmadas que el builder sabe generar. */
    private fun aggregateWithEveryObservation(): InspectionAggregate {
        val base = completeAggregate()
        return base.copy(
            findings = emptyList(),
            pillar = base.pillar!!.copy(
                conductorCondition = ConductorCondition.VISIBLE_RISK,
                protectionCompatibility = ProtectionCompatibility.INCOMPATIBLE,
            ),
            mainPanel = base.mainPanel!!.copy(
                differentialPresent = YesNoUnknown.NO,
                differentialTestResult = DifferentialTestResult.FAILED,
                improvisedConnections = YesNoUnknown.YES,
                overheatingSigns = YesNoUnknown.YES,
                exposedPartsOrDamagedInsulation = YesNoUnknown.YES,
                conductorColorStatus = ConductorColorStatus.INCORRECT_OR_MIXED,
                groundBarPresent = YesNoUnknown.NO,
                neutralAndGroundSeparated = YesNoUnknown.NO,
                protectionConductorsPresent = YesNoPartialUnknown.NO,
            ),
        )
    }
}
