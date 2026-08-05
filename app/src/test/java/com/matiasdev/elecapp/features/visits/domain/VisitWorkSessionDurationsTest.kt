package com.matiasdev.elecapp.features.visits.domain

import java.time.Duration
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VisitWorkSessionDurationsTest {
    @Test
    fun `running session duration uses now`() {
        val duration = VisitWorkSessionDurations.sessionDuration(
            session(start.plusSeconds(60), null, VisitWorkSessionStatus.RUNNING),
            start.plusSeconds(180),
        )

        assertEquals(Duration.ofMinutes(2), duration)
    }

    @Test
    fun `summary adds closed and running sessions`() {
        val visit = visit(start)
        val sessions = listOf(
            session(start, start.plusSeconds(3600), VisitWorkSessionStatus.PAUSED),
            session(start.plusSeconds(5400), null, VisitWorkSessionStatus.RUNNING),
        )

        val summary = VisitWorkSessionDurations.summarize(visit, sessions, start.plusSeconds(7200))

        assertEquals(Duration.ofMinutes(90), summary.totalWorkedDuration)
        assertEquals(Duration.ofHours(2), summary.totalElapsedDuration)
        assertEquals(Duration.ofMinutes(30), summary.totalPausedDuration)
        assertEquals(2, summary.sessionCount)
        assertEquals(1, summary.pauseCount)
    }

    @Test
    fun `paused duration is never negative`() {
        val visit = visit(start)
        val sessions = listOf(session(start, start.plusSeconds(120), VisitWorkSessionStatus.COMPLETED))

        val summary = VisitWorkSessionDurations.summarize(visit, sessions, start.plusSeconds(60))

        assertEquals(Duration.ZERO, summary.totalPausedDuration)
    }

    @Test
    fun `manual overlap detects intersecting ranges`() {
        val sessions = listOf(session(start, start.plusSeconds(600), VisitWorkSessionStatus.COMPLETED))

        assertTrue(VisitWorkSessionDurations.overlaps(start.plusSeconds(300), start.plusSeconds(900), sessions))
        assertFalse(VisitWorkSessionDurations.overlaps(start.plusSeconds(600), start.plusSeconds(900), sessions))
    }

    private fun visit(startedAt: Instant): Visit {
        return Visit("visit", "client", startedAt, null, "Revisión", null, VisitStatus.IN_PROGRESS, startedAt, startedAt, false, startedAt = startedAt)
    }

    private fun session(startedAt: Instant, endedAt: Instant?, status: VisitWorkSessionStatus): VisitWorkSession {
        return VisitWorkSession("session-$startedAt", "visit", startedAt, endedAt, status, null, start, start, false)
    }

    private companion object {
        val start: Instant = Instant.parse("2026-08-05T12:00:00Z")
    }
}
