package com.matiasdev.elecapp.features.inspections.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.matiasdev.elecapp.features.inspections.data.InspectionRepository
import com.matiasdev.elecapp.features.inspections.domain.FindingCategory
import com.matiasdev.elecapp.features.inspections.domain.FindingSeverity
import com.matiasdev.elecapp.features.inspections.domain.InspectionFinding
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
    val findings: List<InspectionFinding> = emptyList(),
    val editingId: String? = null,
    val category: FindingCategory = FindingCategory.GENERAL,
    val severity: FindingSeverity = FindingSeverity.RECOMMENDED,
    val title: String = "",
    val description: String = "",
    val recommendation: String = "",
    val titleError: String? = null,
    val descriptionError: String? = null,
    val status: InspectionStatus = InspectionStatus.DRAFT,
    val saved: Boolean = false,
    val errorMessage: String? = null,
)

data class FindingTemplate(
    val title: String,
    val category: FindingCategory,
    val severity: FindingSeverity,
    val description: String,
    val recommendation: String = "",
)

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
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            findings = aggregate?.findings.orEmpty(),
                            status = aggregate?.inspection?.status ?: it.status,
                        )
                    }
                }
        }
    }

    fun update(transform: FindingsUiState.() -> FindingsUiState) {
        _uiState.update { it.transform().copy(saved = false, titleError = null, descriptionError = null) }
    }

    fun edit(finding: InspectionFinding) {
        _uiState.update {
            it.copy(
                editingId = finding.id,
                category = finding.category,
                severity = finding.severity,
                title = finding.title,
                description = finding.description,
                recommendation = finding.recommendation.orEmpty(),
                saved = false,
            )
        }
    }

    fun applyTemplate(template: FindingTemplate) {
        _uiState.update {
            it.copy(
                editingId = null,
                category = template.category,
                severity = template.severity,
                title = template.title,
                description = template.description,
                recommendation = template.recommendation,
                saved = false,
            )
        }
    }

    fun clearForm() {
        _uiState.update { FindingsUiState(isLoading = false, findings = it.findings, status = it.status) }
    }

    fun save() {
        val state = _uiState.value
        val titleError = InspectionValidation.validateRequiredText(state.title, "El título")
        val descriptionError = InspectionValidation.validateRequiredText(state.description, "La descripción")
        if (titleError != null || descriptionError != null) {
            _uiState.update { it.copy(titleError = titleError, descriptionError = descriptionError) }
            return
        }
        viewModelScope.launch(ioDispatcher) {
            val now = Instant.now()
            val existing = state.findings.firstOrNull { it.id == state.editingId }
            repository.saveFinding(
                InspectionFinding(
                    id = existing?.id ?: UUID.randomUUID().toString(),
                    inspectionId = inspectionId,
                    category = state.category,
                    severity = state.severity,
                    title = state.title.trim(),
                    description = state.description.trim(),
                    recommendation = state.recommendation.trim().ifBlank { null },
                    sortOrder = existing?.sortOrder ?: state.findings.size,
                    createdAt = existing?.createdAt ?: now,
                    updatedAt = now,
                    isDeleted = false,
                ),
            )
            clearForm()
            _uiState.update { it.copy(saved = true) }
        }
    }

    fun delete(finding: InspectionFinding) {
        viewModelScope.launch(ioDispatcher) {
            repository.softDeleteFinding(finding.id)
        }
    }
}

val findingTemplates = listOf(
    FindingTemplate("Conductores deteriorados", FindingCategory.PILLAR, FindingSeverity.URGENT, "Se observaron conductores deteriorados."),
    FindingTemplate("Protección posiblemente sobredimensionada", FindingCategory.GENERAL, FindingSeverity.RECOMMENDED, "La protección podría no corresponder con la sección observada."),
    FindingTemplate("Falta interruptor diferencial", FindingCategory.MAIN_PANEL, FindingSeverity.URGENT, "No se verificó interruptor diferencial en el tablero principal."),
    FindingTemplate("Circuitos sin identificar", FindingCategory.MAIN_PANEL, FindingSeverity.RECOMMENDED, "Los circuitos no se encuentran identificados."),
    FindingTemplate("Colores de conductores incorrectos", FindingCategory.MAIN_PANEL, FindingSeverity.RECOMMENDED, "Se observaron colores de conductores incorrectos o mezclados."),
    FindingTemplate("Empalmes improvisados", FindingCategory.MAIN_PANEL, FindingSeverity.URGENT, "Se observaron empalmes o conexiones improvisadas."),
    FindingTemplate("Signos de recalentamiento", FindingCategory.MAIN_PANEL, FindingSeverity.URGENT, "Se observaron signos compatibles con recalentamiento."),
    FindingTemplate("Puesta a tierra no verificada", FindingCategory.GENERAL, FindingSeverity.RECOMMENDED, "No se pudo verificar la puesta a tierra."),
    FindingTemplate("Sector inaccesible", FindingCategory.OTHER, FindingSeverity.RECOMMENDED, "Un sector no pudo ser verificado durante la visita."),
)

class FindingsViewModelFactory(
    private val repository: InspectionRepository,
    private val inspectionId: String,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = FindingsViewModel(repository, inspectionId) as T
}
