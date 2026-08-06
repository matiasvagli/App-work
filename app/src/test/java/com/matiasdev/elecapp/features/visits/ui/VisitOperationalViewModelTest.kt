package com.matiasdev.elecapp.features.visits.ui

import com.matiasdev.elecapp.features.clients.domain.Client
import com.matiasdev.elecapp.features.clients.ui.FakeClientRepository
import com.matiasdev.elecapp.features.clients.ui.MainDispatcherRule
import com.matiasdev.elecapp.core.time.TimeProvider
import com.matiasdev.elecapp.features.finance.data.FakeFinanceRepository
import com.matiasdev.elecapp.features.inspections.data.FakeInspectionRepository
import com.matiasdev.elecapp.features.materials.data.FakeMaterialRepository
import com.matiasdev.elecapp.features.quotes.data.FakeQuoteRepository
import com.matiasdev.elecapp.features.reminders.data.FakeVisitReminderRepository
import com.matiasdev.elecapp.features.reminders.domain.VisitReminder
import com.matiasdev.elecapp.features.reminders.scheduling.FakeReminderScheduler
import com.matiasdev.elecapp.features.reminders.scheduling.ReminderCoordinator
import com.matiasdev.elecapp.features.visits.domain.Visit
import com.matiasdev.elecapp.features.visits.domain.VisitStatus
import com.matiasdev.elecapp.features.visits.domain.VisitWorkSessionStatus
import java.time.Instant
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
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
        val workRepository = FakeVisitWorkSessionRepository(visitRepository = visitRepository)
        val viewModel = viewModel(
            visitRepository = visitRepository,
            workSessionRepository = workRepository,
            reminderRepository = FakeVisitReminderRepository(listOf(reminder)),
            scheduler = scheduler,
        )

        viewModel.requestStartVisit()
        viewModel.confirmStartVisit()

        val updated = visitRepository.currentVisits().first()
        assertEquals(VisitStatus.IN_PROGRESS, updated.status)
        assertNotNull(updated.startedAt)
        assertEquals(1, workRepository.currentSessions().size)
        assertEquals(VisitWorkSessionStatus.RUNNING, workRepository.currentSessions().first().status)
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
    fun `starting twice keeps one running session`() = runTest(dispatcher) {
        val visitRepository = FakeVisitRepository(listOf(visit(VisitStatus.CONFIRMED)))
        val workRepository = FakeVisitWorkSessionRepository(visitRepository = visitRepository)
        val viewModel = viewModel(visitRepository, workSessionRepository = workRepository)

        viewModel.requestStartVisit()
        viewModel.confirmStartVisit()
        viewModel.confirmStartVisit()

        assertEquals(1, workRepository.currentSessions().count { it.status == VisitWorkSessionStatus.RUNNING })
    }

    @Test
    fun `pause and resume creates a new running session`() = runTest(dispatcher) {
        val visitRepository = FakeVisitRepository(listOf(visit(VisitStatus.IN_PROGRESS).copy(startedAt = now)))
        val workRepository = FakeVisitWorkSessionRepository(visitRepository = visitRepository)
        workRepository.startVisitWork("visit")
        val viewModel = viewModel(visitRepository, workSessionRepository = workRepository)

        viewModel.pauseWork()
        viewModel.resumeWork()

        assertEquals(2, workRepository.currentSessions().size)
        assertEquals(1, workRepository.currentSessions().count { it.status == VisitWorkSessionStatus.PAUSED })
        assertEquals(1, workRepository.currentSessions().count { it.status == VisitWorkSessionStatus.RUNNING })
    }

    @Test
    fun `completing visit stores closing notes`() = runTest(dispatcher) {
        val visitRepository = FakeVisitRepository(listOf(visit(VisitStatus.IN_PROGRESS).copy(startedAt = now)))
        val workRepository = FakeVisitWorkSessionRepository(visitRepository = visitRepository)
        workRepository.startVisitWork("visit")
        val viewModel = viewModel(visitRepository, workSessionRepository = workRepository)

        viewModel.requestCompleteVisit()
        viewModel.onCompletionNotesChange("Se revisó tablero")
        viewModel.onPendingWorkNotesChange("Queda identificar circuitos")
        viewModel.confirmCompleteVisit()

        val updated = visitRepository.currentVisits().first()
        assertEquals(VisitStatus.COMPLETED, updated.status)
        assertNotNull(updated.completedAt)
        assertEquals("Se revisó tablero", updated.completionNotes)
        assertEquals("Queda identificar circuitos", updated.pendingWorkNotes)
        assertEquals(VisitWorkSessionStatus.COMPLETED, workRepository.currentSessions().first().status)
    }

    @Test
    fun `opening inspection reuses existing one`() = runTest(dispatcher) {
        val inspectionRepository = FakeInspectionRepository()
        val visitRepository = FakeVisitRepository(listOf(visit(VisitStatus.IN_PROGRESS)))
        val viewModel = viewModel(visitRepository, inspectionRepository = inspectionRepository)
        inspectionRepository.startOrGetInspection(visitRepository.currentVisits().first(), client)
        val opened = mutableListOf<String>()
        val requestedScope = mutableListOf<String>()

        viewModel.requestOpenInspection({ opened += it }, { requestedScope += it })
        viewModel.requestOpenInspection({ opened += it }, { requestedScope += it })

        assertEquals(2, opened.size)
        assertTrue(opened.distinct().size == 1)
        assertEquals(emptyList<String>(), requestedScope)
    }

    @Test
    fun `opening inspection without existing one requests scope without creating inspection`() = runTest(dispatcher) {
        val inspectionRepository = FakeInspectionRepository()
        val visitRepository = FakeVisitRepository(listOf(visit(VisitStatus.IN_PROGRESS)))
        val viewModel = viewModel(visitRepository, inspectionRepository = inspectionRepository)
        val opened = mutableListOf<String>()
        val requestedScope = mutableListOf<String>()

        viewModel.requestOpenInspection({ opened += it }, { requestedScope += it })

        assertEquals(emptyList<String>(), opened)
        assertEquals(listOf("visit"), requestedScope)
        assertNull(inspectionRepository.findActiveInspectionForVisit("visit"))
    }

    @Test
    fun `manual session with overlap remains editable and is not saved`() = runTest(dispatcher) {
        val visitRepository = FakeVisitRepository(listOf(visit(VisitStatus.IN_PROGRESS).copy(startedAt = now)))
        val workRepository = FakeVisitWorkSessionRepository(
            visitRepository = visitRepository,
            timeProvider = TimeProvider { now.plusSeconds(10800) },
        )
        workRepository.addManualSession("visit", now.minusSeconds(3600), now, null)
        val viewModel = viewModel(visitRepository, workSessionRepository = workRepository)

        viewModel.requestManualSession()
        viewModel.onManualStartChange("04/08/2026 08:30")
        viewModel.onManualEndChange("04/08/2026 09:30")
        viewModel.confirmManualSession()

        assertEquals(1, workRepository.currentSessions().size)
        assertTrue(viewModel.uiState.value.showManualSessionDialog)
        assertNotNull(viewModel.uiState.value.manualSessionError)
    }

    private fun viewModel(
        visitRepository: FakeVisitRepository,
        workSessionRepository: FakeVisitWorkSessionRepository = FakeVisitWorkSessionRepository(visitRepository = visitRepository),
        reminderRepository: FakeVisitReminderRepository = FakeVisitReminderRepository(),
        scheduler: FakeReminderScheduler = FakeReminderScheduler(),
        inspectionRepository: FakeInspectionRepository = FakeInspectionRepository(),
    ): VisitDetailViewModel {
        val clientRepository = FakeClientRepository(listOf(client))
        return VisitDetailViewModel(
            clientRepository = clientRepository,
            visitRepository = visitRepository,
            workSessionRepository = workSessionRepository,
            financeRepository = FakeFinanceRepository(),
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
