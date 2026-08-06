package com.matiasdev.elecapp.features.visits.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.matiasdev.elecapp.core.time.SystemTimeProvider
import com.matiasdev.elecapp.core.time.TimeProvider
import com.matiasdev.elecapp.features.clients.data.ClientRepository
import com.matiasdev.elecapp.features.finance.data.FinanceRepository
import com.matiasdev.elecapp.features.inspections.data.InspectionRepository
import com.matiasdev.elecapp.features.materials.data.MaterialRepository
import com.matiasdev.elecapp.features.quotes.data.QuoteRepository
import com.matiasdev.elecapp.features.reminders.scheduling.ReminderCoordinator
import com.matiasdev.elecapp.features.visits.data.VisitRepository
import com.matiasdev.elecapp.features.visits.data.VisitWorkSessionRepository
import com.matiasdev.elecapp.features.visits.domain.Visit
import com.matiasdev.elecapp.features.visits.domain.VisitStatus
import com.matiasdev.elecapp.features.visits.domain.VisitStatusTransitions
import com.matiasdev.elecapp.features.visits.domain.VisitWorkSessionDurations
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalCoroutinesApi::class)
class VisitDetailViewModel(
    private val clientRepository: ClientRepository,
    private val visitRepository: VisitRepository,
    private val workSessionRepository: VisitWorkSessionRepository,
    private val financeRepository: FinanceRepository,
    private val inspectionRepository: InspectionRepository,
    private val quoteRepository: QuoteRepository,
    private val materialRepository: MaterialRepository,
    private val reminderCoordinator: ReminderCoordinator,
    private val visitId: String,
    private val timeProvider: TimeProvider = SystemTimeProvider,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {
    private val _uiState = MutableStateFlow(VisitDetailUiState(now = timeProvider.now()))
    val uiState: StateFlow<VisitDetailUiState> = _uiState.asStateFlow()
    private val _events = MutableSharedFlow<VisitDetailEvent>()
    val events = _events.asSharedFlow()

    init {
        viewModelScope.launch(ioDispatcher) {
            visitRepository.observeActiveVisitById(visitId)
                .catch { error -> setLoadError(error.message ?: "No se pudo cargar la visita") }
                .collect { visit ->
                    val client = visit?.let { clientRepository.findById(it.clientId) }
                    _uiState.update {
                        recalculate(
                            it.copy(
                                isLoading = false,
                                visit = visit,
                                client = client,
                                errorMessage = if (visit == null) "Visita no encontrada" else null,
                            ),
                        )
                    }
                }
        }
        viewModelScope.launch(ioDispatcher) {
            workSessionRepository.observeByVisitId(visitId).collect { sessions ->
                _uiState.update { recalculate(it.copy(sessions = sessions)) }
            }
        }
        viewModelScope.launch(ioDispatcher) {
            inspectionRepository.observeActiveInspectionForVisit(visitId)
                .collect { inspection -> _uiState.update { it.copy(inspection = inspection) } }
        }
        viewModelScope.launch(ioDispatcher) {
            quoteRepository.observeLatestForVisit(visitId).collect { quote -> _uiState.update { it.copy(quote = quote) } }
        }
        viewModelScope.launch(ioDispatcher) {
            materialRepository.observeLatestForVisit(visitId)
                .collect { materialList -> _uiState.update { it.copy(materialList = materialList) } }
        }
        viewModelScope.launch(ioDispatcher) {
            financeRepository.observeVisitCompletion(visitId).collect { completion ->
                _uiState.update { it.copy(completion = completion) }
            }
        }
        viewModelScope.launch(ioDispatcher) {
            financeRepository.observeReceiptByVisitId(visitId).collect { receipt ->
                _uiState.update { it.copy(receipt = receipt) }
            }
        }
        viewModelScope.launch(ioDispatcher) {
            financeRepository.observeReceiptByVisitId(visitId)
                .flatMapLatest { receipt -> receipt?.let { financeRepository.observePayments(it.id) } ?: flowOf(emptyList()) }
                .collect { payments -> _uiState.update { it.copy(payments = payments) } }
        }
    }

    fun refreshNow() {
        _uiState.update { recalculate(it.copy(now = timeProvider.now())) }
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
        launchOperation {
            if (status == VisitStatus.COMPLETED) {
                workSessionRepository.completeVisitWork(visit.id, null, null)
            } else {
                visitRepository.updateStatus(visit.id, status)
            }
            if (status == VisitStatus.COMPLETED || status == VisitStatus.CANCELLED) reminderCoordinator.cancelForVisit(visit.id)
            _uiState.update { it.copy(statusPendingChange = null) }
            _events.emit(VisitDetailEvent.Message("Estado actualizado"))
        }
    }

    fun requestStartVisit() {
        val visit = _uiState.value.visit ?: return
        if (visit.status !in listOf(VisitStatus.PENDING, VisitStatus.CONFIRMED)) return
        val hoursUntilVisit = Duration.between(timeProvider.now(), visit.scheduledAt).toHours()
        _uiState.update {
            if (hoursUntilVisit > 2) it.copy(showDistantStartWarning = true) else it.copy(showStartVisitConfirmation = true)
        }
    }

    fun dismissStartVisit() {
        _uiState.update { it.copy(showStartVisitConfirmation = false, showDistantStartWarning = false) }
    }

    fun confirmStartVisit() {
        val visit = _uiState.value.visit ?: return
        if (visit.status !in listOf(VisitStatus.PENDING, VisitStatus.CONFIRMED, VisitStatus.IN_PROGRESS)) return
        launchOperation {
            workSessionRepository.startVisitWork(visit.id)
            reminderCoordinator.cancelForVisit(visit.id)
            _uiState.update { it.copy(showStartVisitConfirmation = false, showDistantStartWarning = false) }
            _events.emit(VisitDetailEvent.Message("Visita iniciada"))
        }
    }

    fun pauseWork() = launchOperation {
        workSessionRepository.pauseWork(visitId)
        _events.emit(VisitDetailEvent.Message("Trabajo pausado"))
    }

    fun resumeWork() = launchOperation {
        workSessionRepository.resumeWork(visitId)
        _events.emit(VisitDetailEvent.Message("Trabajo reanudado"))
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
        launchOperation {
            workSessionRepository.completeVisitWork(visit.id, state.completionNotes, state.pendingWorkNotes)
            reminderCoordinator.cancelForVisit(visit.id)
            _uiState.update { it.copy(showCompletionDialog = false) }
            _events.emit(VisitDetailEvent.Message("Visita finalizada"))
        }
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
            }.onSuccess { onDeleted() }
                .onFailure { error -> setLoadError(error.message ?: "No se pudo eliminar la visita") }
        }
    }

    fun requestManualSession() {
        _uiState.update { it.copy(showManualSessionDialog = true, manualSessionError = null) }
    }

    fun dismissManualSession() {
        _uiState.update { it.copy(showManualSessionDialog = false, manualSessionError = null) }
    }

    fun onManualStartChange(value: String) {
        _uiState.update { it.copy(manualSessionStart = value, manualSessionError = null) }
    }

    fun onManualEndChange(value: String) {
        _uiState.update { it.copy(manualSessionEnd = value, manualSessionError = null) }
    }

    fun onManualNotesChange(value: String) {
        _uiState.update { it.copy(manualSessionNotes = value) }
    }

    fun confirmManualSession() {
        val state = _uiState.value
        val start = parseLocalDateTime(state.manualSessionStart)
        val end = parseLocalDateTime(state.manualSessionEnd)
        if (start == null || end == null) {
            _uiState.update { it.copy(manualSessionError = "Usá el formato dd/MM/yyyy HH:mm") }
            return
        }
        launchOperation(onFailure = { message -> _uiState.update { it.copy(manualSessionError = message) } }) {
            workSessionRepository.addManualSession(
                visitId = visitId,
                startedAt = start,
                endedAt = end,
                notes = state.manualSessionNotes,
            )
            _uiState.update {
                it.copy(showManualSessionDialog = false, manualSessionStart = "", manualSessionEnd = "", manualSessionNotes = "")
            }
            _events.emit(VisitDetailEvent.Message("Tiempo manual registrado"))
        }
    }

    fun requestEditSessionNotes(sessionId: String) {
        val session = _uiState.value.sessions.firstOrNull { it.id == sessionId } ?: return
        _uiState.update { it.copy(sessionNoteTarget = session, sessionNoteText = session.notes.orEmpty()) }
    }

    fun onSessionNoteChange(value: String) {
        _uiState.update { it.copy(sessionNoteText = value) }
    }

    fun dismissSessionNote() {
        _uiState.update { it.copy(sessionNoteTarget = null, sessionNoteText = "") }
    }

    fun saveSessionNote() = launchOperation {
        val target = _uiState.value.sessionNoteTarget ?: return@launchOperation
        workSessionRepository.updateNotes(target.id, _uiState.value.sessionNoteText)
        _uiState.update { it.copy(sessionNoteTarget = null, sessionNoteText = "") }
        _events.emit(VisitDetailEvent.Message("Nota actualizada"))
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
            runCatching { withContext(ioDispatcher) { inspectionRepository.startOrGetInspection(visit, client) } }
                .onSuccess { inspection -> onInspectionReady(inspection.id) }
                .onFailure { error -> setLoadError(error.message ?: "No se pudo iniciar el relevamiento") }
        }
    }

    private fun launchOperation(onFailure: (String) -> Unit = ::setLoadError, block: suspend () -> Unit) {
        if (_uiState.value.isOperationInProgress) return
        viewModelScope.launch(ioDispatcher) {
            _uiState.update { it.copy(isOperationInProgress = true) }
            runCatching { block() }
                .onFailure { error -> onFailure(error.message ?: "No se pudo completar la operación") }
            _uiState.update { it.copy(isOperationInProgress = false) }
        }
    }

    private fun setLoadError(message: String) {
        _uiState.update { it.copy(isLoading = false, errorMessage = message, isOperationInProgress = false) }
    }

    private fun recalculate(state: VisitDetailUiState): VisitDetailUiState {
        val visit = state.visit ?: return state.copy(workSummary = null)
        return state.copy(workSummary = VisitWorkSessionDurations.summarize(visit, state.sessions, state.now))
    }

    private fun parseLocalDateTime(value: String): java.time.Instant? {
        return runCatching {
            LocalDateTime.parse(value.trim(), ManualDateTimeFormatter).atZone(ZoneId.systemDefault()).toInstant()
        }.getOrNull()
    }

    private companion object {
        val ManualDateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.forLanguageTag("es-AR"))
    }
}

class VisitDetailViewModelFactory(
    private val clientRepository: ClientRepository,
    private val visitRepository: VisitRepository,
    private val workSessionRepository: VisitWorkSessionRepository,
    private val financeRepository: FinanceRepository,
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
            workSessionRepository,
            financeRepository,
            inspectionRepository,
            quoteRepository,
            materialRepository,
            reminderCoordinator,
            visitId,
        ) as T
    }
}
