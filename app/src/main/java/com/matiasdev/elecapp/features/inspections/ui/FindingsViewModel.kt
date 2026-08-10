package com.matiasdev.elecapp.features.inspections.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.matiasdev.elecapp.features.electricalrules.domain.DefaultElectricalRuleConfigs
import com.matiasdev.elecapp.features.electricalrules.domain.ElectricalRuleConfigRepository
import com.matiasdev.elecapp.features.inspections.data.InspectionRepository
import com.matiasdev.elecapp.features.inspections.domain.FindingCategory
import com.matiasdev.elecapp.features.inspections.domain.FindingReviewStatus
import com.matiasdev.elecapp.features.inspections.domain.FindingSeverity
import com.matiasdev.elecapp.features.inspections.domain.FindingSourceType
import com.matiasdev.elecapp.features.inspections.domain.InspectionFindingGroups
import com.matiasdev.elecapp.features.inspections.domain.InspectionFindingProposalBuilder
import com.matiasdev.elecapp.features.inspections.domain.InspectionFinding
import com.matiasdev.elecapp.features.inspections.domain.InspectionSection
import com.matiasdev.elecapp.features.inspections.domain.InspectionScope
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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
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
    val isEditorOpen: Boolean = false,
    val filter: FindingsFilter = FindingsFilter.ALL,
    val category: FindingCategory = FindingCategory.MAIN_PANEL,
    val severity: FindingSeverity = FindingSeverity.RECOMMENDED,
    val title: String = "",
    val description: String = "",
    val technicianNotes: String = "",
    val exclusionReason: String = "",
    val excludeDialogFindingId: String? = null,
    val descriptionError: String? = null,
    val status: InspectionStatus = InspectionStatus.DRAFT,
    /** Define cual es la seccion siguiente: en relevamiento visual no es la misma. */
    val scope: InspectionScope = InspectionScope.GENERAL_ASSESSMENT,
    val saved: Boolean = false,
    val errorMessage: String? = null,
) {
    val allFindings: List<InspectionFinding> = confirmed + suggested + dataReview + notVerified + manual
}

class FindingsViewModel(
    private val repository: InspectionRepository,
    private val inspectionId: String,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    /**
     * Los umbrales configurables entran acá porque hay hallazgos que salen de una regla
     * (caída de tensión, rango de tensión, resistencia de tierra). Sin esto la pantalla
     * usaría los defaults y podría mostrar una lista distinta a la del informe, que sí
     * lee los umbrales guardados.
     */
    private val ruleConfigRepository: ElectricalRuleConfigRepository? = null,
) : ViewModel() {
    private val _uiState = MutableStateFlow(FindingsUiState())
    val uiState: StateFlow<FindingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch(ioDispatcher) {
            val rulesFlow = ruleConfigRepository?.observeAll() ?: flowOf(DefaultElectricalRuleConfigs.all)
            combine(repository.observeAggregate(inspectionId), rulesFlow) { aggregate, rules -> aggregate to rules }
                .catch { error -> _uiState.update { it.copy(isLoading = false, errorMessage = error.message) } }
                .collect { (aggregate, rules) ->
                    val groups = aggregate?.let { InspectionFindingProposalBuilder.buildGroups(it, rules) }
                        ?: InspectionFindingGroups()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            confirmed = groups.confirmed,
                            suggested = groups.suggested,
                            dataReview = groups.dataReview,
                            notVerified = groups.notVerified,
                            manual = groups.manual,
                            status = aggregate?.inspection?.status ?: it.status,
                            scope = aggregate?.inspection?.scope ?: it.scope,
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
                isEditorOpen = true,
                category = finding.category,
                severity = finding.severity,
                title = finding.title,
                description = finding.description,
                technicianNotes = finding.technicianNotes.orEmpty(),
                saved = false,
            )
        }
    }

    fun openManualEditor() {
        _uiState.update {
            it.copy(
                editingId = null,
                isEditorOpen = true,
                category = FindingCategory.MAIN_PANEL,
                severity = FindingSeverity.RECOMMENDED,
                title = "",
                description = "",
                technicianNotes = "",
                descriptionError = null,
                saved = false,
            )
        }
    }

    fun updateFilter(filter: FindingsFilter) {
        _uiState.update { it.copy(filter = filter) }
    }

    fun requestExclude(finding: InspectionFinding) {
        _uiState.update { it.copy(excludeDialogFindingId = finding.id, exclusionReason = "") }
    }

    fun updateExclusionReason(value: String) {
        _uiState.update { it.copy(exclusionReason = value) }
    }

    fun dismissExcludeDialog() {
        _uiState.update { it.copy(excludeDialogFindingId = null, exclusionReason = "") }
    }

    fun confirmExclude() {
        val state = _uiState.value
        val finding = state.allFindings.firstOrNull { it.id == state.excludeDialogFindingId } ?: return
        excludeFromReport(finding, state.exclusionReason)
        dismissExcludeDialog()
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
                filter = state.filter,
            )
        }
    }

    /**
     * Deja registrado que el técnico pasó por hallazgos. Sin esto, salir sin agregar
     * ninguno se veía igual que no haber entrado nunca: la sección quedaba incompleta,
     * aunque "no encontré nada" es un resultado válido.
     */
    fun markFindingsReviewed() {
        viewModelScope.launch(ioDispatcher) {
            val inspection = repository.findAggregate(inspectionId)?.inspection ?: return@launch
            if (inspection.findingsReviewedAt != null) return@launch
            val now = Instant.now()
            repository.saveInspection(inspection.copy(findingsReviewedAt = now, updatedAt = now))
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
                    title = state.title.trim().ifBlank { state.category.labelForTitle() },
                    description = state.description.trim(),
                    recommendation = null,
                    sourceType = existing?.sourceType ?: FindingSourceType.MANUAL,
                    sourceSection = existing?.sourceSection ?: InspectionSection.FINDINGS,
                    sourceEntityId = existing?.sourceEntityId,
                    sourceValue = existing?.sourceValue,
                    sourceUnit = existing?.sourceUnit,
                    ruleCode = existing?.ruleCode,
                    reviewStatus = existing?.reviewStatus?.takeUnless { it == FindingReviewStatus.PENDING } ?: FindingReviewStatus.CONFIRMED,
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

    fun restoreInReport(finding: InspectionFinding) = saveFindingDecision(
        finding.copy(
            includeInReport = true,
            reviewStatus = if (finding.sourceType == FindingSourceType.RULE_SUGGESTION || finding.sourceType == FindingSourceType.DATA_REVIEW) {
                FindingReviewStatus.PENDING
            } else {
                FindingReviewStatus.CONFIRMED
            },
        ),
    )

    fun excludeFromReport(finding: InspectionFinding, reason: String = "") = saveFindingDecision(
        finding.copy(
            includeInReport = false,
            reviewStatus = FindingReviewStatus.EXCLUDED,
            technicianNotes = reason.trim().ifBlank { finding.technicianNotes },
        ),
    )

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

enum class FindingsFilter {
    ALL,
    IMPORTANT,
    EXCLUDED,
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
    private val ruleConfigRepository: ElectricalRuleConfigRepository? = null,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        FindingsViewModel(
            repository = repository,
            inspectionId = inspectionId,
            ruleConfigRepository = ruleConfigRepository,
        ) as T
}
