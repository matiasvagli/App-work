package com.matiasdev.elecapp.features.agenda.ui

import com.matiasdev.elecapp.features.clients.domain.Client
import com.matiasdev.elecapp.features.clients.ui.FakeClientRepository
import com.matiasdev.elecapp.features.clients.ui.MainDispatcherRule
import com.matiasdev.elecapp.features.inspections.data.FakeInspectionRepository
import com.matiasdev.elecapp.features.visits.domain.Visit
import com.matiasdev.elecapp.features.visits.domain.VisitStatus
import com.matiasdev.elecapp.features.visits.ui.FakeVisitRepository
import com.matiasdev.elecapp.features.visits.ui.FakeVisitWorkSessionRepository
import java.time.Instant
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AgendaViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `loads upcoming visits ordered with client data`() = runTest {
        val client = client()
        val viewModel = AgendaViewModel(
            clientRepository = FakeClientRepository(listOf(client)),
            visitRepository = FakeVisitRepository(
                listOf(
                    visit("2", client.id, Instant.now().plusSeconds(7200)),
                    visit("1", client.id, Instant.now().plusSeconds(3600)),
                ),
            ),
            workSessionRepository = FakeVisitWorkSessionRepository(),
            inspectionRepository = FakeInspectionRepository(),
            ioDispatcher = UnconfinedTestDispatcher(),
        )

        assertEquals(listOf("1", "2"), viewModel.uiState.value.upcomingItemsByVisitId.keys.toList())
    }

    private fun client(): Client {
        val now = Instant.parse("2026-01-01T00:00:00Z")
        return Client("client", "Ana", "111", null, "Calle 1", "Lanús", null, now, now, false)
    }

    private fun visit(id: String, clientId: String, scheduledAt: Instant): Visit {
        val now = Instant.parse("2026-01-01T00:00:00Z")
        return Visit(id, clientId, scheduledAt, null, "Revisión", null, VisitStatus.PENDING, now, now, false)
    }
}
