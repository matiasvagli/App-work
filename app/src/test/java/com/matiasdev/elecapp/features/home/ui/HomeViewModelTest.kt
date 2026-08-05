package com.matiasdev.elecapp.features.home.ui

import com.matiasdev.elecapp.features.clients.domain.Client
import com.matiasdev.elecapp.features.clients.ui.FakeClientRepository
import com.matiasdev.elecapp.features.clients.ui.MainDispatcherRule
import com.matiasdev.elecapp.features.inspections.data.FakeInspectionRepository
import com.matiasdev.elecapp.features.materials.data.FakeMaterialRepository
import com.matiasdev.elecapp.features.quotes.data.FakeQuoteRepository
import com.matiasdev.elecapp.features.visits.domain.Visit
import com.matiasdev.elecapp.features.visits.domain.VisitStatus
import com.matiasdev.elecapp.features.visits.ui.FakeVisitRepository
import java.time.Instant
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(dispatcher)

    @Test
    fun `in progress visit is prioritized over next scheduled visit`() = runTest(dispatcher) {
        val viewModel = HomeViewModel(
            clientRepository = FakeClientRepository(listOf(client)),
            visitRepository = FakeVisitRepository(
                listOf(
                    visit("current", VisitStatus.IN_PROGRESS, now.minusSeconds(600)),
                    visit("next", VisitStatus.CONFIRMED, Instant.now().plusSeconds(3600)),
                ),
            ),
            inspectionRepository = FakeInspectionRepository(),
            quoteRepository = FakeQuoteRepository(),
            materialRepository = FakeMaterialRepository(),
            ioDispatcher = dispatcher,
        )

        assertEquals("current", viewModel.uiState.value.currentVisit?.visit?.id)
        assertEquals("next", viewModel.uiState.value.nextVisit?.visit?.id)
    }

    private fun visit(id: String, status: VisitStatus, scheduledAt: Instant): Visit {
        return Visit(id, "client", scheduledAt, null, "Revisión", null, status, now, now, false, startedAt = now)
    }

    private companion object {
        val now: Instant = Instant.parse("2026-08-04T12:00:00Z")
        val client = Client("client", "Ana", "111", null, "Calle 1", "Lanús", null, now, now, false)
    }
}
