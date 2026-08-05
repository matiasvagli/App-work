package com.matiasdev.elecapp.features.clients.ui

import com.matiasdev.elecapp.features.clients.domain.Client
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
class ClientDetailViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    @Test
    fun `orders upcoming visits by scheduled date`() = runTest {
        val client = client()
        val viewModel = ClientDetailViewModel(
            clientRepository = FakeClientRepository(listOf(client)),
            visitRepository = FakeVisitRepository(
                listOf(
                    visit("2", client.id, Instant.now().plusSeconds(7200)),
                    visit("1", client.id, Instant.now().plusSeconds(3600)),
                ),
            ),
            clientId = client.id,
            ioDispatcher = testDispatcher,
        )

        assertEquals(listOf("1", "2"), viewModel.uiState.value.upcomingVisits.map { it.id })
    }

    private fun client(): Client {
        val now = Instant.parse("2026-01-01T00:00:00Z")
        return Client("client-1", "Ana Perez", "111111", null, null, null, null, now, now, false)
    }

    private fun visit(id: String, clientId: String, scheduledAt: Instant): Visit {
        val now = Instant.parse("2026-01-01T00:00:00Z")
        return Visit(id, clientId, scheduledAt, null, "Revisión", null, VisitStatus.PENDING, now, now, false)
    }
}
