package com.matiasdev.elecapp.features.finance.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.matiasdev.elecapp.features.clients.data.ClientRepository
import com.matiasdev.elecapp.features.finance.data.FinanceRepository
import com.matiasdev.elecapp.features.finance.domain.AttentionReportCoordinator
import com.matiasdev.elecapp.features.finance.domain.AttentionReportState
import com.matiasdev.elecapp.features.finance.domain.AttentionReportStatus
import com.matiasdev.elecapp.features.inspections.data.InspectionRepository
import com.matiasdev.elecapp.features.inspections.summary.ClientReportPromptGenerator
import com.matiasdev.elecapp.features.visits.data.VisitRepository
import java.time.Instant
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AttentionReportsViewModel(
    private val visitId: String,
    private val financeRepository: FinanceRepository,
    private val visitRepository: VisitRepository,
    private val clientRepository: ClientRepository,
    private val inspectionRepository: InspectionRepository,
    private val reportCoordinator: AttentionReportCoordinator,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AttentionReportsUiState())
    val uiState: StateFlow<AttentionReportsUiState> = _uiState.asStateFlow()
    private val _events = MutableSharedFlow<AttentionReportsEvent>()
    val events = _events.asSharedFlow()

    init {
        refresh()
    }

    private fun refresh() {
        viewModelScope.launch(ioDispatcher) {
            runCatching { load() }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = error.message ?: "No se pudieron cargar los informes")
                    }
                }
        }
    }

    private suspend fun load() {
        val completion = financeRepository.observeVisitCompletion(visitId).first()
        val visit = visitRepository.findActiveById(visitId)
        val clientName = visit?.let { clientRepository.findById(it.clientId)?.fullName }.orEmpty()
        _uiState.update {
            it.copy(
                isLoading = false,
                clientName = clientName,
                technicalReport = completion?.technicalReportSnapshot,
                clientReport = completion?.clientReport.orEmpty(),
                reportState = AttentionReportStatus.evaluate(completion, collectSourceTimestamps()),
                errorMessage = null,
            )
        }
    }

    /** `updatedAt` de todo lo que alimenta el informe, para saber si quedó viejo. */
    private suspend fun collectSourceTimestamps(): List<Instant> {
        val inspection = inspectionRepository.findActiveInspectionForVisit(visitId) ?: return emptyList()
        val aggregate = inspectionRepository.findAggregate(inspection.id) ?: return listOf(inspection.updatedAt)
        return buildList {
            add(aggregate.inspection.updatedAt)
            aggregate.pillar?.let { add(it.updatedAt) }
            aggregate.mainPanel?.let { add(it.updatedAt) }
            aggregate.grounding?.let { add(it.updatedAt) }
            addAll(aggregate.pillarMeasurements.map { it.updatedAt })
            addAll(aggregate.mainPanelMeasurements.map { it.updatedAt })
            addAll(aggregate.mainPanelCircuits.map { it.updatedAt })
            addAll(aggregate.findings.map { it.updatedAt })
        }
    }

    fun copyTechnicalReport() {
        val report = _uiState.value.technicalReport ?: return
        viewModelScope.launch { _events.emit(AttentionReportsEvent.CopyToClipboard("Informe técnico", report)) }
    }

    fun shareTechnicalReport() {
        val report = _uiState.value.technicalReport ?: return
        viewModelScope.launch { _events.emit(AttentionReportsEvent.Share(report)) }
    }

    /** Copia el informe técnico envuelto en las instrucciones para la IA externa. */
    fun copyAiPrompt() {
        val report = _uiState.value.technicalReport ?: return
        val prompt = ClientReportPromptGenerator.generate(report)
        viewModelScope.launch {
            _events.emit(AttentionReportsEvent.CopyToClipboard("Plantilla para IA", prompt))
            _events.emit(AttentionReportsEvent.Message("Plantilla copiada. Pegala en tu IA y traé la respuesta."))
        }
    }

    /**
     * Manda la plantilla al Sharesheet en vez del portapapeles, para abrirla directo en la
     * app de IA. Mismo texto que [copyAiPrompt]: cambia el canal, no el contenido.
     */
    fun shareAiPrompt() {
        val report = _uiState.value.technicalReport ?: return
        viewModelScope.launch { _events.emit(AttentionReportsEvent.Share(ClientReportPromptGenerator.generate(report))) }
    }

    fun updateClientReport(value: String) {
        _uiState.update { it.copy(clientReport = value) }
    }

    fun saveClientReport() {
        val report = _uiState.value.clientReport
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            runCatching { withContext(ioDispatcher) { financeRepository.saveClientReport(visitId, report) } }
                .onSuccess { _events.emit(AttentionReportsEvent.Message("Informe del cliente guardado")) }
                .onFailure { _events.emit(AttentionReportsEvent.Message(it.message ?: "No se pudo guardar")) }
            _uiState.update { it.copy(isSaving = false) }
        }
    }

    fun shareClientReport() {
        val report = _uiState.value.clientReport.takeIf(String::isNotBlank) ?: return
        viewModelScope.launch { _events.emit(AttentionReportsEvent.Share(report)) }
    }

    /**
     * Rehace el informe técnico con los datos actuales. Solo por acción explícita: el
     * informe entregado nunca se reemplaza solo. No toca el informe del cliente.
     */
    fun regenerateTechnicalReport() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val ok = withContext(ioDispatcher) { reportCoordinator.generateForClosedVisit(visitId) }
            _uiState.update { it.copy(isSaving = false) }
            _events.emit(
                AttentionReportsEvent.Message(
                    if (ok) "Informe técnico regenerado" else "No se pudo regenerar el informe",
                ),
            )
            if (ok) refresh()
        }
    }
}

class AttentionReportsViewModelFactory(
    private val visitId: String,
    private val financeRepository: FinanceRepository,
    private val visitRepository: VisitRepository,
    private val clientRepository: ClientRepository,
    private val inspectionRepository: InspectionRepository,
    private val reportCoordinator: AttentionReportCoordinator,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AttentionReportsViewModel(
            visitId = visitId,
            financeRepository = financeRepository,
            visitRepository = visitRepository,
            clientRepository = clientRepository,
            inspectionRepository = inspectionRepository,
            reportCoordinator = reportCoordinator,
        ) as T
    }
}
