package com.matiasdev.elecapp.features.visits.domain

import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test

class VisitWorkActionsTest {
    @Test
    fun `pending and confirmed visits can start`() {
        assertEquals(VisitWorkPrimaryAction.START, VisitWorkActions.primaryAction(VisitStatus.PENDING, null))
        assertEquals(VisitWorkPrimaryAction.START, VisitWorkActions.primaryAction(VisitStatus.CONFIRMED, null))
    }

    @Test
    fun `in progress visit pauses when session is running`() {
        assertEquals(VisitWorkPrimaryAction.PAUSE, VisitWorkActions.primaryAction(VisitStatus.IN_PROGRESS, runningSession()))
    }

    @Test
    fun `in progress visit resumes when no session is running`() {
        assertEquals(VisitWorkPrimaryAction.RESUME, VisitWorkActions.primaryAction(VisitStatus.IN_PROGRESS, null))
    }

    @Test
    fun `closed visits have no work action`() {
        assertEquals(VisitWorkPrimaryAction.NONE, VisitWorkActions.primaryAction(VisitStatus.COMPLETED, null))
        assertEquals(VisitWorkPrimaryAction.NONE, VisitWorkActions.primaryAction(VisitStatus.CANCELLED, null))
    }

    private fun runningSession(): VisitWorkSession {
        val now = Instant.parse("2026-08-05T12:00:00Z")
        return VisitWorkSession("session", "visit", now, null, VisitWorkSessionStatus.RUNNING, null, now, now, false)
    }
}
