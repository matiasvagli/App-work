package com.matiasdev.elecapp.features.finance.domain

import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test

class AttentionReportStatusTest {

    private val generatedAt: Instant = Instant.parse("2026-08-09T12:00:00Z")

    private fun completion(
        snapshot: String? = "INFORME TÉCNICO",
        reportsGeneratedAt: Instant? = generatedAt,
    ) = VisitCompletion(
        id = "completion-1",
        visitId = "visit-1",
        diagnosis = null,
        workType = null,
        workPerformed = "Se instaló un reflector exterior",
        workSectors = null,
        workItems = null,
        workTests = null,
        workObservations = null,
        technicalResult = null,
        pendingWork = null,
        requiresFollowUp = false,
        followUpSuggestedAt = null,
        internalNotes = null,
        customerNotes = null,
        completedAt = generatedAt,
        technicalReportSnapshot = snapshot,
        clientReport = null,
        reportsGeneratedAt = reportsGeneratedAt,
        createdAt = generatedAt,
        updatedAt = generatedAt,
        isDeleted = false,
    )

    @Test
    fun `sin cierre no hay informe generado`() {
        assertEquals(
            AttentionReportState.NOT_GENERATED,
            AttentionReportStatus.evaluate(completion = null, sourceUpdatedAt = listOf(generatedAt)),
        )
    }

    @Test
    fun `cierre sin snapshot cuenta como no generado`() {
        assertEquals(
            AttentionReportState.NOT_GENERATED,
            AttentionReportStatus.evaluate(completion(snapshot = null), listOf(generatedAt)),
        )
        assertEquals(
            AttentionReportState.NOT_GENERATED,
            AttentionReportStatus.evaluate(completion(snapshot = "   "), listOf(generatedAt)),
        )
    }

    @Test
    fun `snapshot sin fecha de generacion cuenta como no generado`() {
        assertEquals(
            AttentionReportState.NOT_GENERATED,
            AttentionReportStatus.evaluate(completion(reportsGeneratedAt = null), listOf(generatedAt)),
        )
    }

    @Test
    fun `una fuente editada despues de generar deja el informe viejo`() {
        val corregidoDespues = generatedAt.plusSeconds(60)
        assertEquals(
            AttentionReportState.STALE,
            AttentionReportStatus.evaluate(
                completion(),
                sourceUpdatedAt = listOf(generatedAt.minusSeconds(3600), corregidoDespues),
            ),
        )
    }

    @Test
    fun `fuentes anteriores o iguales dejan el informe vigente`() {
        assertEquals(
            AttentionReportState.UP_TO_DATE,
            AttentionReportStatus.evaluate(
                completion(),
                sourceUpdatedAt = listOf(generatedAt.minusSeconds(1), generatedAt),
            ),
        )
    }

    @Test
    fun `sin fuentes que comparar el informe se considera vigente`() {
        assertEquals(
            AttentionReportState.UP_TO_DATE,
            AttentionReportStatus.evaluate(completion(), sourceUpdatedAt = emptyList()),
        )
    }
}
