package com.matiasdev.elecapp.features.clients.ui

import com.matiasdev.elecapp.features.clients.domain.Client
import java.time.Instant
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ClientListViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    @Test
    fun `filters clients by name or phone`() = runTest {
        val repository = FakeClientRepository(
            listOf(
                client(id = "1", fullName = "Ana Perez", phone = "111111"),
                client(id = "2", fullName = "Luis Gomez", phone = "222999"),
            ),
        )
        val viewModel = ClientListViewModel(repository, testDispatcher)

        viewModel.onSearchQueryChange("999")

        assertEquals(listOf("Luis Gomez"), viewModel.uiState.value.clients.map { it.fullName })
    }

    @Test
    fun `soft delete removes client from visible list`() = runTest {
        val client = client(id = "1", fullName = "Ana Perez", phone = "111111")
        val repository = FakeClientRepository(listOf(client))
        val viewModel = ClientListViewModel(repository, testDispatcher)

        viewModel.askDelete(client)
        viewModel.confirmDelete()

        assertEquals(emptyList<Client>(), viewModel.uiState.value.clients)
    }

    private fun client(id: String, fullName: String, phone: String): Client {
        val now = Instant.parse("2026-01-01T00:00:00Z")
        return Client(
            id = id,
            fullName = fullName,
            phone = phone,
            email = null,
            address = null,
            locality = null,
            notes = null,
            createdAt = now,
            updatedAt = now,
            isDeleted = false,
        )
    }
}
