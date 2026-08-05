package com.matiasdev.elecapp.features.reminders.scheduling

import com.matiasdev.elecapp.features.reminders.domain.VisitReminder
import com.matiasdev.elecapp.features.visits.domain.Visit
import com.matiasdev.elecapp.features.visits.domain.VisitStatus
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReminderScheduleRulesTest {
    @Test
    fun `calculates trigger at from scheduledAt and minutesBefore`() {
        val triggerAt = reminderTriggerAt(visit(), reminder(minutes = 30))

        assertEquals("2026-08-04T11:30:00Z", triggerAt.toString())
    }

    @Test
    fun `does not schedule past reminders`() {
        assertFalse(shouldScheduleReminder(visit(), reminder(minutes = 180), Instant.parse("2026-08-04T10:00:00Z")))
        assertTrue(shouldScheduleReminder(visit(), reminder(minutes = 30), Instant.parse("2026-08-04T10:00:00Z")))
    }

    @Test
    fun `builds stable pending intent request code`() {
        assertEquals(pendingIntentRequestCode("reminder-1"), pendingIntentRequestCode("reminder-1"))
    }

    private fun visit(): Visit {
        val now = Instant.parse("2026-01-01T00:00:00Z")
        return Visit("visit", "client", Instant.parse("2026-08-04T12:00:00Z"), null, "Motivo", null, VisitStatus.PENDING, now, now, false)
    }

    private fun reminder(minutes: Int): VisitReminder {
        val now = Instant.parse("2026-01-01T00:00:00Z")
        return VisitReminder("reminder", "visit", minutes, true, now, now)
    }
}
