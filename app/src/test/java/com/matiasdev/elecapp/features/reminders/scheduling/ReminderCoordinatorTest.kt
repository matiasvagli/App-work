package com.matiasdev.elecapp.features.reminders.scheduling

import com.matiasdev.elecapp.features.clients.domain.Client
import com.matiasdev.elecapp.features.clients.ui.FakeClientRepository
import com.matiasdev.elecapp.features.reminders.data.FakeVisitReminderRepository
import com.matiasdev.elecapp.features.reminders.domain.VisitReminder
import java.time.Instant
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ReminderCoordinatorTest {
    @Test
    fun `cancels enabled reminders for visit`() = runTest {
        val reminder = reminder()
        val scheduler = FakeReminderScheduler()
        val coordinator = ReminderCoordinator(
            clientRepository = FakeClientRepository(listOf(client())),
            reminderRepository = FakeVisitReminderRepository(listOf(reminder)),
            scheduler = scheduler,
        )

        coordinator.cancelForVisit("visit")

        assertEquals(listOf("reminder"), scheduler.cancelledReminderIds)
    }

    private fun client(): Client {
        val now = Instant.parse("2026-01-01T00:00:00Z")
        return Client("client", "Ana", "111", null, null, null, null, now, now, false)
    }

    private fun reminder(): VisitReminder {
        val now = Instant.parse("2026-01-01T00:00:00Z")
        return VisitReminder("reminder", "visit", 30, true, now, now)
    }
}
