package com.matiasdev.elecapp.features.visits.ui

import com.matiasdev.elecapp.features.clients.domain.Client
import com.matiasdev.elecapp.features.clients.ui.FakeClientRepository
import com.matiasdev.elecapp.features.clients.ui.MainDispatcherRule
import com.matiasdev.elecapp.features.inspections.data.FakeInspectionRepository
import com.matiasdev.elecapp.features.materials.data.FakeMaterialRepository
import com.matiasdev.elecapp.features.quotes.data.FakeQuoteRepository
import com.matiasdev.elecapp.features.reminders.data.FakeVisitReminderRepository
import com.matiasdev.elecapp.features.reminders.domain.VisitReminder
import com.matiasdev.elecapp.features.reminders.scheduling.FakeReminderScheduler
import com.matiasdev.elecapp.features.reminders.scheduling.ReminderCoordinator
import com.matiasdev.elecapp.features.visits.domain.Visit
import com.matiasdev.elecapp.features.visits.domain.VisitStatus
import java.time.Instant
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class VisitOperationalViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(dispatcher)

    @Test
    fun `starting visit moves to in progress stores startedAt and cancels reminders`() = runTest(dispatcher) {
        val reminder = VisitReminder("reminder", "visit", 30, true, now, now)
        val scheduler = FakeReminderScheduler()
        val visitRepository = FakeVisitRepository(listOf(visit(VisitStatus.CONFIRMED)))
        val viewModel = viewModel(visitRepository, FakeVisitReminderRepository(listOf(reminder)), scheduler)

        viewModel.requestStartVisit()
        viewModel.confirmStartVisit()

        val updated = visitRepository.currentVisits().first()
        assertEquals(VisitStatus.IN_PROGRESS, updated.status)
        assertNotNull(updated.startedAt)
        assertEquals(listOf("reminder"), scheduler.cancelledReminderIds)
    }

    @Test
    fun `completed and cancelled visits are not started`() = runTest(dispatcher) {
        listOf(VisitStatus.COMPLETED, VisitStatus.CANCELLED).forEach { status ->
            val visitRepository = FakeVisitRepository(listOf(visit(status)))
            val viewModel = viewModel(visitRepository)

            viewModel.requestStartVisit()
            viewModel.confirmStartVisit()

            assertEquals(status, visitRepository.currentVisits().first().status)
        }
    }

    @Test
    fun `completing visit stores closing notes`() = runTest(dispatcher) {
        val visitRepository = FakeVisitRepository(listOf(visit(VisitStatus.IN_PROGRESS).copy(startedAt = now)))
        val viewModel = viewModel(visitRepository)

        viewModel.requestCompleteVisit()
        viewModel.onCompletionNotesChange("Se revisó tablero")
        viewModel.onPendingWorkNotesChange("Queda identificar circuitos")
        viewModel.confirmCompleteVisit()

        val updated = visitRepository.currentVisits().first()
        assertEquals(VisitStatus.COMPLETED, updated.status)
        assertNotNull(updated.completedAt)
        assertEquals("Se revisó tablero", updated.completionNotes)
        assertEquals("Queda identificar circuitos", updated.pendingWorkNotes)
    }

    @Test
    fun `opening inspection reuses existing one`() = runTest(dispatcher) {
        val inspectionRepository = FakeInspectionRepository()
        val visitRepository = FakeVisitRepository(listOf(visit(VisitStatus.IN_PROGRESS)))
        val viewModel = viewModel(visitRepository, inspectionRepository = inspectionRepository)
        val opened = mutableListOf<String>()

        viewModel.requestOpenInspection { opened += it }
        viewModel.requestOpenInspection { opened += it }

        assertEquals(2, opened.size)
        assertTrue(opened.distinct().size == 1)
    }

    private fun viewModel(
        visitRepository: FakeVisitRepository,
        reminderRepository: FakeVisitReminderRepository = FakeVisitReminderRepository(),
        scheduler: FakeReminderScheduler = FakeReminderScheduler(),
        inspectionRepository: FakeInspectionRepository = FakeInspectionRepository(),
    ): VisitDetailViewModel {
        val clientRepository = FakeClientRepository(listOf(client))
        return VisitDetailViewModel(
            clientRepository = clientRepository,
            visitRepository = visitRepository,
            inspectionRepository = inspectionRepository,
            quoteRepository = FakeQuoteRepository(),
            materialRepository = FakeMaterialRepository(),
            reminderCoordinator = ReminderCoordinator(clientRepository, reminderRepository, scheduler),
            visitId = "visit",
            ioDispatcher = dispatcher,
        )
    }

    private fun visit(status: VisitStatus): Visit {
        return Visit("visit", "client", now.plusSeconds(3600), null, "Revisión", null, status, now, now, false)
    }

    private companion object {
        val now: Instant = Instant.parse("2026-08-04T12:00:00Z")
        val client: Client = Client("client", "Ana", "111", null, "Calle 1", "Lanús", null, now, now, false)
    }
}
