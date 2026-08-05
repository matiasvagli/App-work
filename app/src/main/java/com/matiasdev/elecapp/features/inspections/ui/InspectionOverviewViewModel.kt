package com.matiasdev.elecapp.features.inspections.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.matiasdev.elecapp.features.electricaltools.data.TechnicalCalculationRepository
import com.matiasdev.elecapp.features.electricaltools.data.TechnicalCalculationFilters
import com.matiasdev.elecapp.features.electricaltools.domain.CalculationSource
import com.matiasdev.elecapp.features.electricaltools.domain.TechnicalCalculation
import com.matiasdev.elecapp.features.electricaltools.domain.TechnicalCalculationType
import com.matiasdev.elecapp.features.electricaltools.domain.TechnicalClassification
import com.matiasdev.elecapp.features.electricaltools.domain.TechnicianConclusion
import com.matiasdev.elecapp.features.inspections.data.InspectionRepository
import com.matiasdev.elecapp.features.inspections.domain.InspectionCompletionRules
import com.matiasdev.elecapp.features.inspections.domain.InspectionProgressCalculator
import com.matiasdev.elecapp.features.inspections.domain.InspectionStatus
import com.matiasdev.elecapp.features.visits.data.VisitRepository
import java.time.Instant
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

class InspectionOverviewViewModel(
    private val inspectionRepository: InspectionRepository,
    private val visitRepository: VisitRepository,
    private val inspectionId: String,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val technicalCalculationRepository: TechnicalCalculationRepository = EmptyTechnicalCalculationRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(InspectionOverviewUiState())
    val uiState: StateFlow<InspectionOverviewUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch(ioDispatcher) {
            combine(
                inspectionRepository.observeAggregate(inspectionId),
                technicalCalculationRepository.observeByInspection(inspectionId),
            ) { aggregate, calculations -> aggregate to calculations }
                .catch { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.message ?: "No se pudo cargar") }
                }
                .collect { (aggregate, calculations) ->
                    val visit = aggregate?.inspection?.visitId?.let { visitRepository.findActiveById(it) }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            aggregate = aggregate,
                            visit = visit,
                            calculations = calculations,
                            progress = aggregate?.let(InspectionProgressCalculator::calculate),
                            errorMessage = if (aggregate == null) "Relevamiento no encontrado" else null,
                        )
                    }
                }
        }
    }

    fun requestComplete() {
        val aggregate = _uiState.value.aggregate ?: return
        val result = InspectionCompletionRules.validate(aggregate)
        _uiState.update {
            it.copy(
                completionMissingItems = result.missingItems,
                showCompleteConfirmation = result.missingItems.isEmpty(),
            )
        }
    }

    fun dismissCompletionMessages() {
        _uiState.update { it.copy(completionMissingItems = emptyList(), showCompleteConfirmation = false) }
    }

    fun confirmComplete() {
        val inspection = _uiState.value.aggregate?.inspection ?: return
        viewModelScope.launch(ioDispatcher) {
            val now = Instant.now()
            inspectionRepository.saveInspection(
                inspection.copy(status = InspectionStatus.COMPLETED, completedAt = now, updatedAt = now),
            )
            _uiState.update {
                it.copy(
                    completionMissingItems = emptyList(),
                    showCompleteConfirmation = false,
                    snackbarMessage = "Relevamiento finalizado",
                )
            }
        }
    }

    fun requestReopen() {
        _uiState.update { it.copy(showReopenConfirmation = true) }
    }

    fun dismissReopen() {
        _uiState.update { it.copy(showReopenConfirmation = false) }
    }

    fun confirmReopen() {
        val inspection = _uiState.value.aggregate?.inspection ?: return
        viewModelScope.launch(ioDispatcher) {
            inspectionRepository.saveInspection(
                inspection.copy(status = InspectionStatus.DRAFT, completedAt = null, updatedAt = Instant.now()),
            )
            _uiState.update {
                it.copy(showReopenConfirmation = false, snackbarMessage = "Relevamiento reabierto")
            }
        }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    fun notifySummaryCopied() {
        _uiState.update {
            it.copy(snackbarMessage = "Resumen copiado. Pegalo en ChatGPT para redactar el informe.")
        }
    }
}

class InspectionOverviewViewModelFactory(
    private val inspectionRepository: InspectionRepository,
    private val visitRepository: VisitRepository,
    private val inspectionId: String,
    private val technicalCalculationRepository: TechnicalCalculationRepository = EmptyTechnicalCalculationRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return InspectionOverviewViewModel(
            inspectionRepository = inspectionRepository,
            visitRepository = visitRepository,
            inspectionId = inspectionId,
            technicalCalculationRepository = technicalCalculationRepository,
        ) as T
    }
}

private object EmptyTechnicalCalculationRepository : TechnicalCalculationRepository {
    override fun observeAll() = flowOf(emptyList<TechnicalCalculation>())
    override fun observeById(id: String) = flowOf<TechnicalCalculation?>(null)
    override fun observeByClient(clientId: String) = flowOf(emptyList<TechnicalCalculation>())
    override fun observeByVisit(visitId: String) = flowOf(emptyList<TechnicalCalculation>())
    override fun observeByInspection(inspectionId: String) = flowOf(emptyList<TechnicalCalculation>())
    override fun observeByType(type: TechnicalCalculationType) = flowOf(emptyList<TechnicalCalculation>())
    override fun observeBySource(source: CalculationSource) = flowOf(emptyList<TechnicalCalculation>())
    override fun observeByClassification(classification: TechnicalClassification) = flowOf(emptyList<TechnicalCalculation>())
    override fun search(filters: TechnicalCalculationFilters) = flowOf(emptyList<TechnicalCalculation>())
    override suspend fun findById(id: String): TechnicalCalculation? = null
    override suspend fun save(calculation: TechnicalCalculation) = Unit
    override suspend fun updateEditableFields(
        id: String,
        title: String,
        description: String?,
        technicianConclusion: TechnicianConclusion,
        technicianNotes: String?,
    ) = Unit
    override suspend fun associate(id: String, clientId: String?, visitId: String?, inspectionId: String?) = Unit
    override suspend fun unlinkInspection(id: String) = Unit
    override suspend fun softDelete(id: String) = Unit
}
