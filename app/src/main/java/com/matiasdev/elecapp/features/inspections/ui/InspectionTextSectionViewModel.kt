package com.matiasdev.elecapp.features.inspections.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.matiasdev.elecapp.features.inspections.data.InspectionRepository
import com.matiasdev.elecapp.features.inspections.domain.InspectionStatus
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class InspectionTextSection {
    TECHNICAL_COMMENT,
    FINAL_REPORT,
}

data class InspectionTextSectionUiState(
    val isLoading: Boolean = true,
    val text: String = "",
    val status: InspectionStatus = InspectionStatus.DRAFT,
    val saved: Boolean = false,
    val errorMessage: String? = null,
) {
    val characterCount: Int = text.length
}

class InspectionTextSectionViewModel(
    private val repository: InspectionRepository,
    private val inspectionId: String,
    private val section: InspectionTextSection,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {
    private val _uiState = MutableStateFlow(InspectionTextSectionUiState())
    val uiState: StateFlow<InspectionTextSectionUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch(ioDispatcher) {
            val inspection = repository.findAggregate(inspectionId)?.inspection
            val text = when (section) {
                InspectionTextSection.TECHNICAL_COMMENT -> inspection?.originalTechnicalComment
                InspectionTextSection.FINAL_REPORT -> inspection?.finalClientReport
            }.orEmpty()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    text = text,
                    status = inspection?.status ?: it.status,
                    errorMessage = if (inspection == null) "Relevamiento no encontrado" else null,
                )
            }
        }
    }

    fun onTextChange(value: String) {
        _uiState.update { it.copy(text = value, saved = false) }
    }

    fun save() {
        viewModelScope.launch(ioDispatcher) {
            val aggregate = repository.findAggregate(inspectionId) ?: return@launch
            val trimmed = _uiState.value.text.trim().ifBlank { null }
            val next = when (section) {
                InspectionTextSection.TECHNICAL_COMMENT -> aggregate.inspection.copy(originalTechnicalComment = trimmed)
                InspectionTextSection.FINAL_REPORT -> aggregate.inspection.copy(finalClientReport = trimmed)
            }
            repository.saveInspection(next)
            _uiState.update { it.copy(saved = true) }
        }
    }
}

class InspectionTextSectionViewModelFactory(
    private val repository: InspectionRepository,
    private val inspectionId: String,
    private val section: InspectionTextSection,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return InspectionTextSectionViewModel(repository, inspectionId, section) as T
    }
}
