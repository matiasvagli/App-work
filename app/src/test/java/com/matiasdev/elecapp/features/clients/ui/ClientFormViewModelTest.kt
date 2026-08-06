package com.matiasdev.elecapp.features.clients.ui

import com.matiasdev.elecapp.core.external.ImportedContact
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

    @Test
    fun `contact with one phone fills name and phone without saving`() = runTest {
        val viewModel = newViewModel()

        viewModel.applyImportedContact(ImportedContact(fullName = "Ana Garcia", phones = listOf("+5491122334455")))

        assertEquals("Ana Garcia", viewModel.uiState.value.fullName)
        assertEquals("+5491122334455", viewModel.uiState.value.phone)
        assertTrue(viewModel.uiState.value.phoneChoices.isEmpty())
        assertEquals(false, viewModel.uiState.value.saved)
    }

    @Test
    fun `contact with multiple phones fills name and waits for phone choice`() = runTest {
        val viewModel = newViewModel()

        viewModel.applyImportedContact(
            ImportedContact(
                fullName = "Ana Garcia",
                phones = listOf("+5491122334455", "01155556666"),
            ),
        )

        assertEquals("Ana Garcia", viewModel.uiState.value.fullName)
        assertEquals("", viewModel.uiState.value.phone)
        assertEquals(listOf("+5491122334455", "01155556666"), viewModel.uiState.value.phoneChoices)

        viewModel.selectImportedPhone("01155556666")

        assertEquals("01155556666", viewModel.uiState.value.phone)
        assertTrue(viewModel.uiState.value.phoneChoices.isEmpty())
        assertEquals(false, viewModel.uiState.value.saved)
    }

    @Test
    fun `contact without phone fills name and shows manual completion message`() = runTest {
        val viewModel = newViewModel()

        viewModel.applyImportedContact(ImportedContact(fullName = "Ana Garcia"))

        assertEquals("Ana Garcia", viewModel.uiState.value.fullName)
        assertEquals("", viewModel.uiState.value.phone)
        assertEquals("El contacto no tiene teléfono. Podés completarlo manualmente.", viewModel.uiState.value.errorMessage)
        assertEquals(false, viewModel.uiState.value.saved)
    }

    @Test
    fun `selection cancelled shows clear message`() = runTest {
        val viewModel = newViewModel()

        viewModel.onContactSelectionCancelled()

        assertEquals("Selección de contacto cancelada.", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `invalid contact uri read shows clear message`() = runTest {
        val viewModel = newViewModel()

        viewModel.onContactReadFailed()

        assertEquals("No se pudo leer el contacto seleccionado.", viewModel.uiState.value.errorMessage)
    }

    private fun newViewModel(): ClientFormViewModel {
        return ClientFormViewModel(
            repository = FakeClientRepository(),
            clientId = null,
            ioDispatcher = testDispatcher,
        )
    }
}
