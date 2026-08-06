package com.matiasdev.elecapp.features.inspections.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.matiasdev.elecapp.features.inspections.data.InspectionRepository
import com.matiasdev.elecapp.features.inspections.domain.InspectionStatus
import com.matiasdev.elecapp.features.inspections.domain.InspectionUnverifiedItem
import com.matiasdev.elecapp.features.inspections.domain.UnverifiedItemType
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class VisualInspectionComplementaryUiState(
    val isLoading: Boolean = true,
    val observation: String = "",
    val selectedTypes: Set<UnverifiedItemType> = emptySet(),
    val descriptions: Map<UnverifiedItemType, String> = emptyMap(),
    val existingIds: Map<UnverifiedItemType, String> = emptyMap(),
    val status: InspectionStatus = InspectionStatus.DRAFT,
    val saved: Boolean = false,
    val errorMessage: String? = null,
)

class VisualInspectionComplementaryViewModel(
    private val repository: InspectionRepository,
    private val inspectionId: String,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {
    private val _uiState = MutableStateFlow(VisualInspectionComplementaryUiState())
    val uiState: StateFlow<VisualInspectionComplementaryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch(ioDispatcher) {
            val aggregate = repository.findAggregate(inspectionId)
            val items = aggregate?.unverifiedItems.orEmpty()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    observation = aggregate?.inspection?.originalTechnicalComment.orEmpty(),
                    selectedTypes = items.map { item -> item.type }.toSet(),
                    descriptions = items.associate { item -> item.type to item.description.orEmpty() },
                    existingIds = items.associate { item -> item.type to item.id },
                    status = aggregate?.inspection?.status ?: it.status,
                    errorMessage = if (aggregate == null) "Relevamiento no encontrado" else null,
                )
            }
        }
    }

    fun onObservationChange(value: String) {
        _uiState.update { it.copy(observation = value, saved = false) }
    }

    fun toggle(type: UnverifiedItemType) {
        _uiState.update {
            val selected = if (type in it.selectedTypes) it.selectedTypes - type else it.selectedTypes + type
            it.copy(selectedTypes = selected, saved = false)
        }
    }

    fun onDescriptionChange(type: UnverifiedItemType, value: String) {
        _uiState.update { it.copy(descriptions = it.descriptions + (type to value), saved = false) }
    }

    fun save() {
        val state = _uiState.value
        viewModelScope.launch(ioDispatcher) {
            val aggregate = repository.findAggregate(inspectionId) ?: return@launch
            repository.saveInspection(
                aggregate.inspection.copy(
                    originalTechnicalComment = state.observation.trim().ifBlank { null },
                ),
            )
            val now = Instant.now()
            val items = state.selectedTypes.sortedBy(UnverifiedItemType::name).map { type ->
                InspectionUnverifiedItem(
                    id = state.existingIds[type] ?: UUID.randomUUID().toString(),
                    inspectionId = inspectionId,
                    type = type,
                    description = state.descriptions[type]?.trim()?.ifBlank { null },
                    createdAt = now,
                    updatedAt = now,
                    isDeleted = false,
                )
            }
            repository.saveUnverifiedItems(inspectionId, items)
            _uiState.update { it.copy(saved = true) }
        }
    }
}

class VisualInspectionComplementaryViewModelFactory(
    private val repository: InspectionRepository,
    private val inspectionId: String,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return VisualInspectionComplementaryViewModel(repository, inspectionId) as T
    }
}
