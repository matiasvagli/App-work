package com.matiasdev.elecapp.features.inspections.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.matiasdev.elecapp.features.inspections.data.InspectionRepository
import com.matiasdev.elecapp.features.inspections.domain.GeneralCondition
import com.matiasdev.elecapp.features.inspections.domain.InspectionScope
import com.matiasdev.elecapp.features.inspections.domain.InspectionStatus
import com.matiasdev.elecapp.features.inspections.domain.InspectionType
import com.matiasdev.elecapp.features.inspections.domain.PropertyType
import com.matiasdev.elecapp.features.inspections.domain.SupplyType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class InspectionGeneralUiState(
    val isLoading: Boolean = true,
    val inspectionType: InspectionType = InspectionType.VISUAL,
    val generalCondition: GeneralCondition = GeneralCondition.NOT_ASSESSED,
    val supplyType: SupplyType = SupplyType.UNKNOWN,
    val propertyType: PropertyType = PropertyType.HOUSE,
    val scope: InspectionScope = InspectionScope.GENERAL_ASSESSMENT,
    val reviewReason: String = "",
    val reviewedElement: String = "",
    val taskDescription: String = "",
    val technicianName: String = "",
    val accessLimitations: String = "",
    val status: InspectionStatus = InspectionStatus.DRAFT,
    val saved: Boolean = false,
    val errorMessage: String? = null,
)

class InspectionGeneralViewModel(
    private val repository: InspectionRepository,
    private val inspectionId: String,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {
    private val _uiState = MutableStateFlow(InspectionGeneralUiState())
    val uiState: StateFlow<InspectionGeneralUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch(ioDispatcher) {
            val inspection = repository.findAggregate(inspectionId)?.inspection
            _uiState.update {
                if (inspection == null) {
                    it.copy(isLoading = false, errorMessage = "Relevamiento no encontrado")
                } else {
                    it.copy(
                        isLoading = false,
                        scope = inspection.scope,
                        inspectionType = inspection.inspectionType,
                        generalCondition = inspection.generalCondition,
                        supplyType = inspection.supplyType,
                        propertyType = inspection.propertyType,
                        reviewReason = inspection.reviewReason ?: inspection.visitReasonSnapshot,
                        reviewedElement = inspection.reviewedElement.orEmpty(),
                        taskDescription = inspection.taskDescription.orEmpty(),
                        technicianName = inspection.technicianName.orEmpty(),
                        accessLimitations = inspection.accessLimitations.orEmpty(),
                        status = inspection.status,
                    )
                }
            }
        }
    }

    fun onInspectionTypeChange(value: InspectionType) = _uiState.update { it.copy(inspectionType = value, saved = false) }
    fun onGeneralConditionChange(value: GeneralCondition) = _uiState.update { it.copy(generalCondition = value, saved = false) }
    fun onSupplyTypeChange(value: SupplyType) = _uiState.update { it.copy(supplyType = value, saved = false) }
    fun onPropertyTypeChange(value: PropertyType) = _uiState.update { it.copy(propertyType = value, saved = false) }
    fun onReviewReasonChange(value: String) = _uiState.update { it.copy(reviewReason = value, saved = false) }
    fun onReviewedElementChange(value: String) = _uiState.update { it.copy(reviewedElement = value, saved = false) }
    fun onTaskDescriptionChange(value: String) = _uiState.update { it.copy(taskDescription = value, saved = false) }
    fun onTechnicianNameChange(value: String) = _uiState.update { it.copy(technicianName = value, saved = false) }
    fun onAccessLimitationsChange(value: String) = _uiState.update { it.copy(accessLimitations = value, saved = false) }

    fun save() {
        viewModelScope.launch(ioDispatcher) {
            val aggregate = repository.findAggregate(inspectionId) ?: return@launch
            val state = _uiState.value
            repository.saveInspection(
                aggregate.inspection.copy(
                    inspectionType = state.inspectionType,
                    generalCondition = state.generalCondition,
                    supplyType = state.supplyType,
                    propertyType = state.propertyType,
                    reviewReason = state.reviewReason.trim().ifBlank { null },
                    reviewedElement = state.reviewedElement.trim().ifBlank { null },
                    taskDescription = state.taskDescription.trim().ifBlank { null },
                    technicianName = state.technicianName.trim().ifBlank { null },
                    accessLimitations = state.accessLimitations.trim().ifBlank { null },
                ),
            )
            _uiState.update { it.copy(saved = true) }
        }
    }
}

class InspectionGeneralViewModelFactory(
    private val repository: InspectionRepository,
    private val inspectionId: String,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return InspectionGeneralViewModel(repository, inspectionId) as T
    }
}
