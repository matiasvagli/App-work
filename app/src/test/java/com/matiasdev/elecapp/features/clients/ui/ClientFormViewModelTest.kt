package com.matiasdev.elecapp.features.clients.ui

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ClientFormViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    @Test
    fun `shows validation errors when name and phone are invalid`() = runTest {
        val viewModel = ClientFormViewModel(
            repository = FakeClientRepository(),
            clientId = null,
            ioDispatcher = testDispatcher,
        )

        viewModel.save()

        assertEquals("El nombre es obligatorio", viewModel.uiState.value.fullNameError)
        assertEquals("El teléfono es obligatorio", viewModel.uiState.value.phoneError)
    }

    @Test
    fun `saves a valid new client`() = runTest {
        val repository = FakeClientRepository()
        val viewModel = ClientFormViewModel(
            repository = repository,
            clientId = null,
            ioDispatcher = testDispatcher,
        )

        viewModel.onFullNameChange("Carlos Lopez")
        viewModel.onPhoneChange("1155554444")
        viewModel.onAddressChange("San Martín 123")
        viewModel.onLocalityChange("Lanús")
        viewModel.save()

        val savedClient = viewModel.uiState.value
        assertTrue(savedClient.saved)
        assertNotNull(savedClient.savedClientId)
        val client = repository.currentClients().firstOrNull { it.fullName == "Carlos Lopez" }
        assertNotNull(client)
        assertEquals(client?.id, savedClient.savedClientId)
        assertEquals("San Martín 123", client?.address)
        assertEquals("Lanús", client?.locality)
    }
}
