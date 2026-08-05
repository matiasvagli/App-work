package com.matiasdev.elecapp.features.visits.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.matiasdev.elecapp.features.clients.data.ClientRepository
import com.matiasdev.elecapp.features.clients.domain.Client
import com.matiasdev.elecapp.features.reminders.data.VisitReminderRepository
import com.matiasdev.elecapp.features.reminders.domain.ReminderInput
import com.matiasdev.elecapp.features.reminders.domain.ReminderOption
import com.matiasdev.elecapp.features.reminders.domain.ReminderRules
import com.matiasdev.elecapp.features.reminders.domain.ReminderUnit
import com.matiasdev.elecapp.features.reminders.domain.VisitReminder
import com.matiasdev.elecapp.features.reminders.domain.reminderInputFromMinutes
import com.matiasdev.elecapp.features.reminders.scheduling.ReminderCoordinator
import com.matiasdev.elecapp.features.settings.data.ReminderSettingsRepository
import com.matiasdev.elecapp.features.visits.data.VisitRepository
import com.matiasdev.elecapp.features.visits.domain.Visit
import com.matiasdev.elecapp.features.visits.domain.VisitStatus
import com.matiasdev.elecapp.features.visits.domain.VisitValidator
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.UUID
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class VisitFormViewModel(
    private val clientRepository: ClientRepository,
    private val visitRepository: VisitRepository,
    private val reminderRepository: VisitReminderRepository,
    private val settingsRepository: ReminderSettingsRepository,
    private val reminderCoordinator: ReminderCoordinator,
    private val initialClientId: String? = null,
    private val visitId: String? = null,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {
    private val _uiState = MutableStateFlow(VisitFormUiState())
    val uiState: StateFlow<VisitFormUiState> = _uiState.asStateFlow()
    private val searchQuery = MutableStateFlow("")
    private var existingVisit: Visit? = null

    init {
        loadInitialData()
        observeClientSearch()
    }

    fun selectClient(client: Client) {
        _uiState.update { it.copy(client = client, clientSearchQuery = "", clientSearchResults = emptyList()) }
        searchQuery.value = ""
    }

    fun selectClientById(clientId: String) {
        viewModelScope.launch(ioDispatcher) {
            clientRepository.findById(clientId)?.takeUnless { it.isDeleted }?.let(::selectClient)
        }
    }

    fun clearClientSelection() {
        _uiState.update { it.copy(client = null, clientSearchQuery = "", isClientSearchLoading = true) }
        viewModelScope.launch(ioDispatcher) {
            val clients = clientRepository.observeActiveClientsMatching("", limit = 20).first()
            _uiState.update { it.copy(clientSearchResults = clients, isClientSearchLoading = false) }
        }
    }

    fun onClientSearchChange(value: String) {
        _uiState.update { it.copy(clientSearchQuery = value, isClientSearchLoading = value.isNotBlank()) }
        searchQuery.value = value
    }

    fun onDateChange(date: LocalDate) {
        _uiState.update { it.copy(date = date, dateTimeError = null, reminderWarning = null) }
    }

    fun onTimeChange(time: LocalTime) {
        _uiState.update { it.copy(time = time, dateTimeError = null, reminderWarning = null) }
    }

    fun onDurationChange(value: String) {
        _uiState.update { it.copy(durationMinutes = value.filter(Char::isDigit), durationError = null) }
    }

    fun onReasonChange(value: String) {
        _uiState.update { it.copy(reason = value, reasonError = null) }
    }

    fun onNotesChange(value: String) {
        _uiState.update { it.copy(notes = value) }
    }

    fun onFirstReminderOptionChange(option: ReminderOption) {
        _uiState.update { it.copy(firstReminder = it.firstReminder.copy(option = option), reminderError = null) }
    }

    fun onFirstReminderCustomValueChange(value: String) {
        _uiState.update { it.copy(firstReminder = it.firstReminder.copy(customValue = value.filter(Char::isDigit))) }
    }

    fun onFirstReminderUnitChange(unit: ReminderUnit) {
        _uiState.update { it.copy(firstReminder = it.firstReminder.copy(customUnit = unit)) }
    }

    fun addSecondReminder() {
        _uiState.update { it.copy(secondReminderEnabled = true, secondReminder = ReminderInput(ReminderOption.ONE_HOUR)) }
    }

    fun removeSecondReminder() {
        _uiState.update { it.copy(secondReminderEnabled = false, secondReminder = ReminderInput(), reminderError = null) }
    }

    fun onSecondReminderOptionChange(option: ReminderOption) {
        _uiState.update { it.copy(secondReminder = it.secondReminder.copy(option = option), reminderError = null) }
    }

    fun onSecondReminderCustomValueChange(value: String) {
        _uiState.update { it.copy(secondReminder = it.secondReminder.copy(customValue = value.filter(Char::isDigit))) }
    }

    fun onSecondReminderUnitChange(unit: ReminderUnit) {
        _uiState.update { it.copy(secondReminder = it.secondReminder.copy(customUnit = unit)) }
    }

    fun save() {
        val state = _uiState.value
        val client = state.client ?: return
        val scheduledAt = LocalDateTime.of(state.date, state.time).atZone(ZoneId.systemDefault()).toInstant()
        val duration = state.durationMinutes.toIntOrNull()
        val visitValidation = VisitValidator.validate(state.reason, scheduledAt, duration)
        val reminderValidation = ReminderRules.validate(scheduledAt, state.reminderMinutes())
        if (!visitValidation.isValid || !reminderValidation.isValid) {
            _uiState.update {
                it.copy(
                    reasonError = visitValidation.reasonError,
                    dateTimeError = visitValidation.dateTimeError,
                    durationError = visitValidation.durationError,
                    reminderError = reminderValidation.errorMessage,
                )
            }
            return
        }

        viewModelScope.launch(ioDispatcher) {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            runCatching { saveVisitAndReminders(client, scheduledAt, duration, reminderValidation.validMinutes) }
                .onSuccess {
                    _uiState.update { current ->
                        current.copy(
                            isSaving = false,
                            saved = true,
                            reminderWarning = reminderValidation.skippedPastMinutes.takeIf(List<Int>::isNotEmpty)
                                ?.joinToString(prefix = "Se omitieron recordatorios vencidos: ") { "$it min" },
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isSaving = false, errorMessage = error.message ?: "No se pudo guardar la visita")
                    }
                }
        }
    }

    private fun loadInitialData() {
        viewModelScope.launch(ioDispatcher) {
            runCatching {
                val visit = visitId?.let { visitRepository.findActiveById(it) }
                existingVisit = visit
                val reminders = visit?.let { reminderRepository.findEnabledForVisit(it.id) }.orEmpty()
                val settings = settingsRepository.settings.first()
                val resolvedClientId = visit?.clientId ?: initialClientId
                LoadedVisitForm(
                    client = resolvedClientId?.let { clientRepository.findById(it)?.takeUnless(Client::isDeleted) },
                    visit = visit,
                    firstReminder = reminderInputFromMinutes(reminders.getOrNull(0)?.minutesBefore ?: settings.defaultFirstReminderMinutes),
                    secondReminder = reminderInputFromMinutes(reminders.getOrNull(1)?.minutesBefore ?: settings.defaultSecondReminderMinutes),
                    hasSecondReminder = reminders.size > 1 || settings.defaultSecondReminderMinutes != null,
                )
            }.onSuccess { loaded ->
                val scheduledAt = loaded.visit?.scheduledAt?.atZone(ZoneId.systemDefault())
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        client = loaded.client,
                        date = scheduledAt?.toLocalDate() ?: it.date,
                        time = scheduledAt?.toLocalTime()?.withSecond(0)?.withNano(0) ?: it.time,
                        durationMinutes = loaded.visit?.estimatedDurationMinutes?.toString().orEmpty(),
                        reason = loaded.visit?.reason.orEmpty(),
                        notes = loaded.visit?.notes.orEmpty(),
                        firstReminder = loaded.firstReminder,
                        secondReminderEnabled = loaded.hasSecondReminder,
                        secondReminder = loaded.secondReminder,
                    )
                }
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, errorMessage = error.message ?: "No se pudo cargar la visita") }
            }
        }
    }

    private fun observeClientSearch() {
        viewModelScope.launch(ioDispatcher) {
            searchQuery
                .debounce(250)
                .distinctUntilChanged()
                .flatMapLatest { query -> clientRepository.observeActiveClientsMatching(query, limit = 20) }
                .collect { clients ->
                    _uiState.update { it.copy(clientSearchResults = clients, isClientSearchLoading = false) }
                }
        }
    }

    private suspend fun saveVisitAndReminders(
        client: Client,
        scheduledAt: Instant,
        duration: Int?,
        reminderMinutes: List<Int>,
    ) {
        val now = Instant.now()
        val existing = existingVisit
        val visit = Visit(
            id = existing?.id ?: UUID.randomUUID().toString(),
            clientId = client.id,
            scheduledAt = scheduledAt,
            estimatedDurationMinutes = duration,
            reason = _uiState.value.reason.trim(),
            notes = _uiState.value.notes.trim().ifBlank { null },
            status = existing?.status ?: VisitStatus.PENDING,
            createdAt = existing?.createdAt ?: now,
            updatedAt = now,
            isDeleted = false,
        )
        visitRepository.save(visit)
        reminderCoordinator.replaceAndSchedule(
            visit = visit,
            reminders = reminderMinutes.map { minutes ->
                VisitReminder(UUID.randomUUID().toString(), visit.id, minutes, true, now, now)
            },
        )
    }

    private fun VisitFormUiState.reminderMinutes(): List<Int> {
        return listOfNotNull(
            firstReminder.toMinutesOrNull(),
            secondReminder.toMinutesOrNull().takeIf { secondReminderEnabled },
        )
    }
}

class VisitFormViewModelFactory(
    private val clientRepository: ClientRepository,
    private val visitRepository: VisitRepository,
    private val reminderRepository: VisitReminderRepository,
    private val settingsRepository: ReminderSettingsRepository,
    private val reminderCoordinator: ReminderCoordinator,
    private val clientId: String?,
    private val visitId: String? = null,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return VisitFormViewModel(
            clientRepository,
            visitRepository,
            reminderRepository,
            settingsRepository,
            reminderCoordinator,
            clientId,
            visitId,
        ) as T
    }
}

private data class LoadedVisitForm(
    val client: Client?,
    val visit: Visit?,
    val firstReminder: ReminderInput,
    val secondReminder: ReminderInput,
    val hasSecondReminder: Boolean,
)
