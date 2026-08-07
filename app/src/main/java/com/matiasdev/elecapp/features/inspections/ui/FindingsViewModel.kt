package com.matiasdev.elecapp.features.inspections.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.matiasdev.elecapp.features.inspections.data.InspectionRepository
import com.matiasdev.elecapp.features.inspections.domain.FindingCategory
import com.matiasdev.elecapp.features.inspections.domain.FindingReviewStatus
import com.matiasdev.elecapp.features.inspections.domain.FindingSeverity
import com.matiasdev.elecapp.features.inspections.domain.FindingSourceType
import com.matiasdev.elecapp.features.inspections.domain.InspectionFindingGroups
import com.matiasdev.elecapp.features.inspections.domain.InspectionFindingProposalBuilder
import com.matiasdev.elecapp.features.inspections.domain.InspectionFinding
import com.matiasdev.elecapp.features.inspections.domain.InspectionSection
import com.matiasdev.elecapp.features.inspections.domain.InspectionStatus
import com.matiasdev.elecapp.features.inspections.domain.InspectionValidation
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FindingsUiState(
    val isLoading: Boolean = true,
    val confirmed: List<InspectionFinding> = emptyList(),
    val suggested: List<InspectionFinding> = emptyList(),
    val dataReview: List<InspectionFinding> = emptyList(),
    val notVerified: List<InspectionFinding> = emptyList(),
    val manual: List<InspectionFinding> = emptyList(),
    val editingId: String? = null,
    val category: FindingCategory = FindingCategory.MAIN_PANEL,
    val severity: FindingSeverity = FindingSeverity.RECOMMENDED,
    val description: String = "",
    val technicianNotes: String = "",
    val descriptionError: String? = null,
    val status: InspectionStatus = InspectionStatus.DRAFT,
    val saved: Boolean = false,
    val errorMessage: String? = null,
) {
    val allFindings: List<InspectionFinding> = confirmed + suggested + dataReview + notVerified + manual
}

class FindingsViewModel(
    private val repository: InspectionRepository,
    private val inspectionId: String,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {
    private val _uiState = MutableStateFlow(FindingsUiState())
    val uiState: StateFlow<FindingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch(ioDispatcher) {
            repository.observeAggregate(inspectionId)
                .catch { error -> _uiState.update { it.copy(isLoading = false, errorMessage = error.message) } }
                .collect { aggregate ->
                    val groups = aggregate?.let(InspectionFindingProposalBuilder::buildGroups) ?: InspectionFindingGroups()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            confirmed = groups.confirmed,
                            suggested = groups.suggested,
                            dataReview = groups.dataReview,
                            notVerified = groups.notVerified,
                            manual = groups.manual,
                            status = aggregate?.inspection?.status ?: it.status,
                        )
                    }
                }
        }
    }

    fun update(transform: FindingsUiState.() -> FindingsUiState) {
        _uiState.update { it.transform().copy(saved = false, descriptionError = null) }
    }

    fun edit(finding: InspectionFinding) {
        _uiState.update {
            it.copy(
                editingId = finding.id,
                category = finding.category,
                severity = finding.severity,
                description = finding.description,
                technicianNotes = finding.technicianNotes.orEmpty(),
                saved = false,
            )
        }
    }

    fun clearForm() {
        _uiState.update { state ->
            FindingsUiState(
                isLoading = false,
                confirmed = state.confirmed,
                suggested = state.suggested,
                dataReview = state.dataReview,
                notVerified = state.notVerified,
                manual = state.manual,
                status = state.status,
            )
        }
    }

    fun saveManual() {
        val state = _uiState.value
        val descriptionError = InspectionValidation.validateRequiredText(state.description, "La descripción")
        if (descriptionError != null) {
            _uiState.update { it.copy(descriptionError = descriptionError) }
            return
        }
        viewModelScope.launch(ioDispatcher) {
            val now = Instant.now()
            val existing = state.allFindings.firstOrNull { it.id == state.editingId }
            repository.saveFinding(
                InspectionFinding(
                    id = existing?.id ?: UUID.randomUUID().toString(),
                    inspectionId = inspectionId,
                    category = state.category,
                    severity = state.severity,
                    title = state.category.labelForTitle(),
                    description = state.description.trim(),
                    recommendation = null,
                    sourceType = existing?.sourceType ?: FindingSourceType.MANUAL,
                    sourceSection = existing?.sourceSection ?: InspectionSection.FINDINGS,
                    sourceEntityId = existing?.sourceEntityId,
                    sourceValue = existing?.sourceValue,
                    sourceUnit = existing?.sourceUnit,
                    ruleCode = existing?.ruleCode,
                    reviewStatus = existing?.reviewStatus ?: FindingReviewStatus.CONFIRMED,
                    includeInReport = existing?.includeInReport ?: true,
                    technicianNotes = state.technicianNotes.trim().ifBlank { null },
                    sortOrder = existing?.sortOrder ?: state.manual.size,
                    createdAt = existing?.createdAt ?: now,
                    updatedAt = now,
                    isDeleted = false,
                ),
            )
            clearForm()
            _uiState.update { it.copy(saved = true) }
        }
    }

    fun includeInReport(finding: InspectionFinding) = saveFindingDecision(finding.copy(includeInReport = true))

    fun excludeFromReport(finding: InspectionFinding) = saveFindingDecision(finding.copy(includeInReport = false, reviewStatus = FindingReviewStatus.EXCLUDED))

    fun changePriority(finding: InspectionFinding, severity: FindingSeverity) = saveFindingDecision(finding.copy(severity = severity))

    fun confirmSuggestion(finding: InspectionFinding) = saveFindingDecision(
        finding.copy(
            sourceType = if (finding.sourceType == FindingSourceType.RULE_SUGGESTION) FindingSourceType.OBSERVATION_CONFIRMED else finding.sourceType,
            reviewStatus = FindingReviewStatus.CONFIRMED,
            includeInReport = true,
        ),
    )

    fun delete(finding: InspectionFinding) {
        if (finding.sourceType != FindingSourceType.MANUAL) return
        viewModelScope.launch(ioDispatcher) {
            repository.softDeleteFinding(finding.id)
        }
    }

    private fun saveFindingDecision(finding: InspectionFinding) {
        viewModelScope.launch(ioDispatcher) {
            repository.saveFinding(finding.copy(updatedAt = Instant.now()))
        }
    }
}

private fun FindingCategory.labelForTitle(): String = when (this) {
    FindingCategory.PILLAR -> "Pilar y acometida"
    FindingCategory.MAIN_PANEL -> "Tablero principal"
    FindingCategory.CIRCUITS -> "Circuitos"
    FindingCategory.CONDUCTORS -> "Conductores"
    FindingCategory.PROTECTIONS -> "Protecciones"
    FindingCategory.GROUNDING -> "Puesta a tierra"
    FindingCategory.EQUIPMENT -> "Artefactos o equipos"
    FindingCategory.VISIBLE_RISK -> "Riesgo visible"
    FindingCategory.GENERAL -> "General"
    FindingCategory.OTHER -> "Otro"
}

class FindingsViewModelFactory(
    private val repository: InspectionRepository,
    private val inspectionId: String,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = FindingsViewModel(repository, inspectionId) as T
}
