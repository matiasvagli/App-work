package com.matiasdev.elecapp.features.visits.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.matiasdev.elecapp.features.clients.data.ClientRepository
import com.matiasdev.elecapp.features.inspections.data.InspectionRepository
import com.matiasdev.elecapp.features.materials.data.MaterialRepository
import com.matiasdev.elecapp.features.quotes.data.QuoteRepository
import com.matiasdev.elecapp.features.reminders.scheduling.ReminderCoordinator
import com.matiasdev.elecapp.features.visits.data.VisitRepository
import com.matiasdev.elecapp.features.visits.domain.Visit
import com.matiasdev.elecapp.features.visits.domain.VisitStatus
import com.matiasdev.elecapp.features.visits.domain.VisitStatusTransitions
import java.time.Duration
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VisitDetailViewModel(
    private val clientRepository: ClientRepository,
    private val visitRepository: VisitRepository,
    private val inspectionRepository: InspectionRepository,
    private val quoteRepository: QuoteRepository,
    private val materialRepository: MaterialRepository,
    private val reminderCoordinator: ReminderCoordinator,
    private val visitId: String,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {
    private val _uiState = MutableStateFlow(VisitDetailUiState())
    val uiState: StateFlow<VisitDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch(ioDispatcher) {
            visitRepository.observeActiveVisitById(visitId)
                .catch { error ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = error.message ?: "No se pudo cargar la visita")
                    }
                }
                .collect { visit ->
                    val client = visit?.let { clientRepository.findById(it.clientId) }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            visit = visit,
                            client = client,
                            errorMessage = if (visit == null) "Visita no encontrada" else null,
                        )
                    }
                }
        }
        viewModelScope.launch(ioDispatcher) {
            inspectionRepository.observeActiveInspectionForVisit(visitId)
                .collect { inspection -> _uiState.update { it.copy(inspection = inspection) } }
        }
        viewModelScope.launch(ioDispatcher) {
            quoteRepository.observeLatestForVisit(visitId)
                .collect { quote -> _uiState.update { it.copy(quote = quote) } }
        }
        viewModelScope.launch(ioDispatcher) {
            materialRepository.observeLatestForVisit(visitId)
                .collect { materialList -> _uiState.update { it.copy(materialList = materialList) } }
        }
    }

    fun allowedNextStatuses(): List<VisitStatus> {
        return _uiState.value.visit?.status?.let(VisitStatusTransitions::allowedNextStatuses).orEmpty()
    }

    fun askStatusChange(status: VisitStatus) {
        _uiState.update { it.copy(statusPendingChange = status) }
    }

    fun dismissStatusChange() {
        _uiState.update { it.copy(statusPendingChange = null) }
    }

    fun confirmStatusChange() {
        val visit = _uiState.value.visit ?: return
        val status = _uiState.value.statusPendingChange ?: return
        if (status !in VisitStatusTransitions.allowedNextStatuses(visit.status)) return
        viewModelScope.launch(ioDispatcher) {
            runCatching {
                visitRepository.updateStatus(visit.id, status)
                if (status == VisitStatus.COMPLETED || status == VisitStatus.CANCELLED) {
                    reminderCoordinator.cancelForVisit(visit.id)
                }
            }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(errorMessage = error.message ?: "No se pudo cambiar el estado")
                    }
                }
            dismissStatusChange()
        }
    }

    fun requestStartVisit() {
        val visit = _uiState.value.visit ?: return
        if (visit.status !in listOf(VisitStatus.PENDING, VisitStatus.CONFIRMED)) return
        val hoursUntilVisit = Duration.between(java.time.Instant.now(), visit.scheduledAt).toHours()
        _uiState.update {
            if (hoursUntilVisit > 2) {
                it.copy(showDistantStartWarning = true)
            } else {
                it.copy(showStartVisitConfirmation = true)
            }
        }
    }

    fun dismissStartVisit() {
        _uiState.update { it.copy(showStartVisitConfirmation = false, showDistantStartWarning = false) }
    }

    fun confirmStartVisit() {
        val visit = _uiState.value.visit ?: return
        if (visit.status !in listOf(VisitStatus.PENDING, VisitStatus.CONFIRMED)) return
        viewModelScope.launch(ioDispatcher) {
            runCatching {
                visitRepository.startVisit(visit.id)
                reminderCoordinator.cancelForVisit(visit.id)
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        showStartVisitConfirmation = false,
                        showDistantStartWarning = false,
                        snackbarMessage = "Visita iniciada",
                    )
                }
            }.onFailure { error ->
                _uiState.update { it.copy(errorMessage = error.message ?: "No se pudo iniciar la visita") }
            }
        }
    }

    fun requestCompleteVisit() {
        _uiState.update { it.copy(showCompletionDialog = true) }
    }

    fun dismissCompleteVisit() {
        _uiState.update { it.copy(showCompletionDialog = false) }
    }

    fun onCompletionNotesChange(value: String) {
        _uiState.update { it.copy(completionNotes = value) }
    }

    fun onPendingWorkNotesChange(value: String) {
        _uiState.update { it.copy(pendingWorkNotes = value) }
    }

    fun confirmCompleteVisit() {
        val visit = _uiState.value.visit ?: return
        val state = _uiState.value
        if (visit.status != VisitStatus.IN_PROGRESS) return
        viewModelScope.launch(ioDispatcher) {
            runCatching {
                visitRepository.completeVisit(visit.id, state.completionNotes, state.pendingWorkNotes)
                reminderCoordinator.cancelForVisit(visit.id)
            }.onSuccess {
                _uiState.update {
                    it.copy(showCompletionDialog = false, snackbarMessage = "Visita finalizada")
                }
            }.onFailure { error ->
                _uiState.update { it.copy(errorMessage = error.message ?: "No se pudo finalizar la visita") }
            }
        }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    fun askDelete(visit: Visit) {
        _uiState.update { it.copy(visitPendingDelete = visit) }
    }

    fun dismissDelete() {
        _uiState.update { it.copy(visitPendingDelete = null) }
    }

    fun confirmDelete(onDeleted: () -> Unit) {
        val visit = _uiState.value.visitPendingDelete ?: return
        viewModelScope.launch {
            runCatching {
                withContext(ioDispatcher) {
                    reminderCoordinator.cancelForVisit(visit.id)
                    visitRepository.softDelete(visit.id)
                }
            }
                .onSuccess { onDeleted() }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(errorMessage = error.message ?: "No se pudo eliminar la visita")
                    }
                }
        }
    }

    fun requestOpenInspection(onInspectionReady: (String) -> Unit) {
        val visit = _uiState.value.visit ?: return
        val inspection = _uiState.value.inspection
        if (inspection != null) {
            onInspectionReady(inspection.id)
            return
        }
        if (visit.status == VisitStatus.CANCELLED) {
            _uiState.update { it.copy(showCancelledInspectionWarning = true) }
            return
        }
        startInspection(onInspectionReady)
    }

    fun dismissCancelledInspectionWarning() {
        _uiState.update { it.copy(showCancelledInspectionWarning = false) }
    }

    fun confirmStartCancelledInspection(onInspectionReady: (String) -> Unit) {
        _uiState.update { it.copy(showCancelledInspectionWarning = false) }
        startInspection(onInspectionReady)
    }

    private fun startInspection(onInspectionReady: (String) -> Unit) {
        val visit = _uiState.value.visit ?: return
        val client = _uiState.value.client ?: return
        viewModelScope.launch {
            runCatching {
                withContext(ioDispatcher) { inspectionRepository.startOrGetInspection(visit, client) }
            }
                .onSuccess { inspection -> onInspectionReady(inspection.id) }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(errorMessage = error.message ?: "No se pudo iniciar el relevamiento")
                    }
                }
        }
    }
}

class VisitDetailViewModelFactory(
    private val clientRepository: ClientRepository,
    private val visitRepository: VisitRepository,
    private val inspectionRepository: InspectionRepository,
    private val quoteRepository: QuoteRepository,
    private val materialRepository: MaterialRepository,
    private val reminderCoordinator: ReminderCoordinator,
    private val visitId: String,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return VisitDetailViewModel(
            clientRepository,
            visitRepository,
            inspectionRepository,
            quoteRepository,
            materialRepository,
            reminderCoordinator,
            visitId,
        ) as T
    }
}
