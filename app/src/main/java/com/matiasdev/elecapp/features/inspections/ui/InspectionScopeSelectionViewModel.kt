package com.matiasdev.elecapp.features.inspections.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.matiasdev.elecapp.features.clients.data.ClientRepository
import com.matiasdev.elecapp.features.inspections.data.InspectionRepository
import com.matiasdev.elecapp.features.inspections.domain.InspectionScope
import com.matiasdev.elecapp.features.visits.data.VisitRepository
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

data class InspectionScopeSelectionUiState(
    val selectedScope: InspectionScope? = null,
    val isCreating: Boolean = false,
    val errorMessage: String? = null,
)

sealed interface InspectionScopeSelectionEvent {
    data class InspectionReady(val inspectionId: String) : InspectionScopeSelectionEvent
}

class InspectionScopeSelectionViewModel(
    private val clientRepository: ClientRepository,
    private val visitRepository: VisitRepository,
    private val inspectionRepository: InspectionRepository,
    private val visitId: String,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {
    private val _uiState = MutableStateFlow(InspectionScopeSelectionUiState())
    val uiState: StateFlow<InspectionScopeSelectionUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<InspectionScopeSelectionEvent>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    fun onScopeSelected(scope: InspectionScope) {
        if (_uiState.value.isCreating) return
        _uiState.update { it.copy(selectedScope = scope, isCreating = true, errorMessage = null) }
        viewModelScope.launch {
            runCatching {
                withContext(ioDispatcher) {
                    val existing = inspectionRepository.findActiveInspectionForVisit(visitId)
                    if (existing != null) {
                        existing
                    } else {
                        val visit = visitRepository.findActiveById(visitId) ?: error("Visita no encontrada")
                        val client = clientRepository.findById(visit.clientId) ?: error("Cliente no encontrado")
                        inspectionRepository.startOrGetInspection(visit, client, scope)
                    }
                }
            }.onSuccess { inspection ->
                _uiState.update { it.copy(isCreating = false) }
                _events.emit(InspectionScopeSelectionEvent.InspectionReady(inspection.id))
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isCreating = false,
                        errorMessage = error.message ?: "No se pudo iniciar el relevamiento",
                    )
                }
            }
        }
    }
}

class InspectionScopeSelectionViewModelFactory(
    private val clientRepository: ClientRepository,
    private val visitRepository: VisitRepository,
    private val inspectionRepository: InspectionRepository,
    private val visitId: String,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return InspectionScopeSelectionViewModel(
            clientRepository = clientRepository,
            visitRepository = visitRepository,
            inspectionRepository = inspectionRepository,
            visitId = visitId,
        ) as T
    }
}
