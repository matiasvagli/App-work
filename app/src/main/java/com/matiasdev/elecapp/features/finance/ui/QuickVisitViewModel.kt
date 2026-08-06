package com.matiasdev.elecapp.features.finance.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.matiasdev.elecapp.features.clients.data.ClientRepository
import com.matiasdev.elecapp.features.clients.domain.Client
import com.matiasdev.elecapp.features.finance.data.FinanceRepository
import com.matiasdev.elecapp.features.finance.domain.QuickVisitClientMode
import com.matiasdev.elecapp.features.finance.domain.QuickVisitDraft
import com.matiasdev.elecapp.features.finance.domain.QuickVisitValidation
import com.matiasdev.elecapp.features.finance.domain.QuickVisitValidator
import com.matiasdev.elecapp.features.finance.domain.VisitAttentionType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class QuickVisitUiState(
    val clients: List<Client> = emptyList(),
    val draft: QuickVisitDraft = QuickVisitDraft(),
    val validation: QuickVisitValidation = QuickVisitValidation(isValid = true),
    val isSaving: Boolean = false,
    val showActiveVisitWarning: Boolean = false,
    val errorMessage: String? = null,
) {
    val canStart: Boolean get() = QuickVisitValidator.validate(draft).isValid
}

sealed interface QuickVisitEvent {
    data class VisitStarted(val visitId: String) : QuickVisitEvent
    data object ContinueCurrentVisit : QuickVisitEvent
    data class Message(val text: String) : QuickVisitEvent
}

class QuickVisitViewModel(
    private val clientRepository: ClientRepository,
    private val financeRepository: FinanceRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {
    private val _uiState = MutableStateFlow(QuickVisitUiState())
    val uiState: StateFlow<QuickVisitUiState> = _uiState.asStateFlow()
    private val _events = MutableSharedFlow<QuickVisitEvent>()
    val events = _events.asSharedFlow()

    init {
        viewModelScope.launch(ioDispatcher) {
            clientRepository.observeActiveClients().collect { clients ->
                _uiState.update { it.copy(clients = clients) }
            }
        }
    }

    fun updateDraft(transform: (QuickVisitDraft) -> QuickVisitDraft) {
        _uiState.update { it.copy(draft = transform(it.draft), validation = QuickVisitValidation(isValid = true), errorMessage = null) }
    }

    fun selectClient(id: String) = updateDraft { it.copy(clientMode = QuickVisitClientMode.EXISTING, selectedClientId = id) }

    fun selectMode(mode: QuickVisitClientMode) = updateDraft { it.copy(clientMode = mode) }

    fun selectType(type: VisitAttentionType) = updateDraft { it.copy(attentionType = type) }

    fun start() {
        val validation = QuickVisitValidator.validate(_uiState.value.draft)
        if (!validation.isValid) {
            _uiState.update { it.copy(validation = validation) }
            return
        }
        viewModelScope.launch {
            val hasRunning = withContext(ioDispatcher) { financeRepository.hasAnotherRunningVisit() }
            if (hasRunning) {
                _uiState.update { it.copy(showActiveVisitWarning = true) }
            } else {
                save(pauseRunning = false)
            }
        }
    }

    fun continueCurrentVisit() {
        _uiState.update { it.copy(showActiveVisitWarning = false) }
        viewModelScope.launch { _events.emit(QuickVisitEvent.ContinueCurrentVisit) }
    }

    fun pauseAndStart() {
        _uiState.update { it.copy(showActiveVisitWarning = false) }
        save(pauseRunning = true)
    }

    fun dismissActiveVisitWarning() {
        _uiState.update { it.copy(showActiveVisitWarning = false) }
    }

    private fun save(pauseRunning: Boolean) {
        if (_uiState.value.isSaving) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            runCatching {
                withContext(ioDispatcher) { financeRepository.startQuickVisit(_uiState.value.draft, pauseRunning) }
            }.onSuccess { visitId ->
                _events.emit(QuickVisitEvent.Message("Visita iniciada"))
                _events.emit(QuickVisitEvent.VisitStarted(visitId))
            }.onFailure { error ->
                _uiState.update { it.copy(errorMessage = error.message ?: "No se pudo iniciar la visita") }
            }
            _uiState.update { it.copy(isSaving = false) }
        }
    }
}

class QuickVisitViewModelFactory(
    private val clientRepository: ClientRepository,
    private val financeRepository: FinanceRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return QuickVisitViewModel(clientRepository, financeRepository) as T
    }
}
