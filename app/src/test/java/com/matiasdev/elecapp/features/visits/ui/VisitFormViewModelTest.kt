package com.matiasdev.elecapp.features.visits.ui

import com.matiasdev.elecapp.features.clients.domain.Client
import com.matiasdev.elecapp.features.clients.ui.FakeClientRepository
import com.matiasdev.elecapp.features.clients.ui.MainDispatcherRule
import com.matiasdev.elecapp.features.reminders.data.FakeVisitReminderRepository
import com.matiasdev.elecapp.features.reminders.scheduling.FakeReminderScheduler
import com.matiasdev.elecapp.features.reminders.scheduling.ReminderCoordinator
import com.matiasdev.elecapp.features.settings.data.FakeReminderSettingsRepository
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class VisitFormViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `creates visit for selected client`() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val client = client()
        val visitRepository = FakeVisitRepository()
        val clientRepository = FakeClientRepository(listOf(client))
        val reminderRepository = FakeVisitReminderRepository()
        val viewModel = VisitFormViewModel(
            clientRepository = clientRepository,
            visitRepository = visitRepository,
            reminderRepository = reminderRepository,
            settingsRepository = FakeReminderSettingsRepository(),
            reminderCoordinator = ReminderCoordinator(clientRepository, reminderRepository, FakeReminderScheduler()),
            initialClientId = client.id,
            ioDispatcher = testDispatcher,
        )
        advanceUntilIdle()

        viewModel.onDateChange(LocalDate.now().plusDays(1))
        viewModel.onTimeChange(LocalTime.of(10, 30))
        viewModel.onReasonChange("Cambio de térmica")
        viewModel.save()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.saved)
        assertEquals("Cambio de térmica", visitRepository.currentVisits().first().reason)
    }

    @Test
    fun `does not save visit without selected client`() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val visitRepository = FakeVisitRepository()
        val viewModel = VisitFormViewModel(
            clientRepository = FakeClientRepository(),
            visitRepository = visitRepository,
            reminderRepository = FakeVisitReminderRepository(),
            settingsRepository = FakeReminderSettingsRepository(),
            reminderCoordinator = ReminderCoordinator(FakeClientRepository(), FakeVisitReminderRepository(), FakeReminderScheduler()),
            ioDispatcher = testDispatcher,
        )
        advanceUntilIdle()

        viewModel.onReasonChange("Revisión")
        viewModel.save()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.saved)
        assertTrue(visitRepository.currentVisits().isEmpty())
    }

    @Test
    fun `searches clients by name or phone`() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val clientRepository = FakeClientRepository(
            listOf(
                client(id = "client-1", fullName = "Ana Perez", phone = "111"),
                client(id = "client-2", fullName = "Luis Gomez", phone = "555444"),
            ),
        )
        val viewModel = VisitFormViewModel(
            clientRepository = clientRepository,
            visitRepository = FakeVisitRepository(),
            reminderRepository = FakeVisitReminderRepository(),
            settingsRepository = FakeReminderSettingsRepository(),
            reminderCoordinator = ReminderCoordinator(clientRepository, FakeVisitReminderRepository(), FakeReminderScheduler()),
            ioDispatcher = testDispatcher,
        )
        advanceUntilIdle()

        viewModel.onClientSearchChange("555")
        advanceTimeBy(300)
        advanceUntilIdle()

        assertEquals(listOf("Luis Gomez"), viewModel.uiState.value.clientSearchResults.map { it.fullName })
    }

    @Test
    fun `selects client returned from quick create and keeps visit draft`() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val clientRepository = FakeClientRepository(listOf(client(id = "new-client", fullName = "Nuevo Cliente")))
        val viewModel = VisitFormViewModel(
            clientRepository = clientRepository,
            visitRepository = FakeVisitRepository(),
            reminderRepository = FakeVisitReminderRepository(),
            settingsRepository = FakeReminderSettingsRepository(),
            reminderCoordinator = ReminderCoordinator(clientRepository, FakeVisitReminderRepository(), FakeReminderScheduler()),
            ioDispatcher = testDispatcher,
        )
        advanceUntilIdle()

        viewModel.onDateChange(LocalDate.now().plusDays(2))
        viewModel.onTimeChange(LocalTime.of(14, 0))
        viewModel.onReasonChange("Instalación")
        viewModel.onNotesChange("Traer disyuntor")
        viewModel.selectClientById("new-client")
        advanceUntilIdle()

        assertEquals("new-client", viewModel.uiState.value.client?.id)
        assertEquals("Instalación", viewModel.uiState.value.reason)
        assertEquals("Traer disyuntor", viewModel.uiState.value.notes)
    }

    private fun client(
        id: String = "client-1",
        fullName: String = "Ana Perez",
        phone: String = "111111",
    ): Client {
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
