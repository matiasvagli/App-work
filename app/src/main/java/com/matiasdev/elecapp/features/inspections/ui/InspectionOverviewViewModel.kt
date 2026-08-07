package com.matiasdev.elecapp.features.inspections.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.matiasdev.elecapp.features.electricalrules.domain.DefaultElectricalRuleConfigs
import com.matiasdev.elecapp.features.electricalrules.domain.ElectricalMeasurementReviewEvaluator
import com.matiasdev.elecapp.features.electricalrules.domain.ElectricalRuleCode
import com.matiasdev.elecapp.features.electricalrules.domain.ElectricalRuleConfig
import com.matiasdev.elecapp.features.electricalrules.domain.ElectricalRuleConfigRepository
import com.matiasdev.elecapp.features.electricalrules.domain.EvaluateSupplyVoltageUseCase
import com.matiasdev.elecapp.features.electricaltools.data.TechnicalCalculationRepository
import com.matiasdev.elecapp.features.electricaltools.data.TechnicalCalculationFilters
import com.matiasdev.elecapp.features.electricaltools.domain.CalculationSource
import com.matiasdev.elecapp.features.electricaltools.domain.TechnicalCalculation
import com.matiasdev.elecapp.features.electricaltools.domain.TechnicalCalculationType
import com.matiasdev.elecapp.features.electricaltools.domain.TechnicalClassification
import com.matiasdev.elecapp.features.electricaltools.domain.TechnicianConclusion
import com.matiasdev.elecapp.features.finance.data.FinanceRepository
import com.matiasdev.elecapp.features.inspections.data.InspectionRepository
import com.matiasdev.elecapp.features.inspections.domain.AutoInspectionCalculationBuilder
import com.matiasdev.elecapp.features.inspections.domain.InspectionCompletionRules
import com.matiasdev.elecapp.features.inspections.domain.InspectionProgressCalculator
import com.matiasdev.elecapp.features.inspections.domain.InspectionStatus
import com.matiasdev.elecapp.features.visits.data.VisitRepository
import java.time.Instant
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class InspectionOverviewViewModel(
    private val inspectionRepository: InspectionRepository,
    private val visitRepository: VisitRepository,
    private val inspectionId: String,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val financeRepository: FinanceRepository? = null,
    private val technicalCalculationRepository: TechnicalCalculationRepository = EmptyTechnicalCalculationRepository,
    private val electricalRuleConfigRepository: ElectricalRuleConfigRepository = DefaultElectricalRuleConfigRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(InspectionOverviewUiState())
    val uiState: StateFlow<InspectionOverviewUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch(ioDispatcher) {
            combine(
                inspectionRepository.observeAggregate(inspectionId),
                technicalCalculationRepository.observeByInspection(inspectionId),
                electricalRuleConfigRepository.observeAll(),
            ) { aggregate, calculations, rules -> Triple(aggregate, calculations, rules) }
                .catch { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.message ?: "No se pudo cargar") }
                }
                .collect { (aggregate, calculations, rules) ->
                    val visit = aggregate?.inspection?.visitId?.let { visitRepository.findActiveById(it) }
                    val measurementReviewSummary = ElectricalMeasurementReviewEvaluator.evaluateSupplyVoltage(
                        calculations = calculations,
                        useCase = EvaluateSupplyVoltageUseCase(electricalRuleConfigRepository),
                    )
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            aggregate = aggregate,
                            visit = visit,
                            calculations = calculations,
                            autoCalculations = aggregate?.let { AutoInspectionCalculationBuilder.build(it, rules) }.orEmpty(),
                            measurementReviewSummary = measurementReviewSummary,
                            measurementReviewExpanded = if (measurementReviewSummary.hasAnomalies) {
                                it.measurementReviewExpanded
                            } else {
                                false
                            },
                            progress = aggregate?.let(InspectionProgressCalculator::calculate),
                            errorMessage = if (aggregate == null) "Relevamiento no encontrado" else null,
                        )
                    }
                }
        }
        viewModelScope.launch(ioDispatcher) {
            inspectionRepository.observeAggregate(inspectionId)
                .map { it?.inspection?.visitId }
                .distinctUntilChanged()
                .flatMapLatest { visitId ->
                    if (visitId == null || financeRepository == null) flowOf(null) else financeRepository.observeVisitCompletion(visitId)
                }
                .collect { completion -> _uiState.update { it.copy(visitCompletion = completion) } }
        }
    }

    fun requestComplete() {
        val aggregate = _uiState.value.aggregate ?: return
        val result = InspectionCompletionRules.validate(aggregate, hasCalculations = _uiState.value.calculations.isNotEmpty())
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
                    navigateHomeAfterCompletion = true,
                    snackbarMessage = "Relevamiento finalizado",
                )
            }
        }
    }

    fun onCompletionNavigationHandled() {
        _uiState.update { it.copy(navigateHomeAfterCompletion = false) }
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

    fun toggleMeasurementReview() {
        _uiState.update { it.copy(measurementReviewExpanded = !it.measurementReviewExpanded) }
    }

    fun notifySummaryCopied() {
        _uiState.update {
            it.copy(snackbarMessage = "Informe copiado")
        }
    }
}

class InspectionOverviewViewModelFactory(
    private val inspectionRepository: InspectionRepository,
    private val visitRepository: VisitRepository,
    private val inspectionId: String,
    private val financeRepository: FinanceRepository? = null,
    private val technicalCalculationRepository: TechnicalCalculationRepository = EmptyTechnicalCalculationRepository,
    private val electricalRuleConfigRepository: ElectricalRuleConfigRepository = DefaultElectricalRuleConfigRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return InspectionOverviewViewModel(
            inspectionRepository = inspectionRepository,
            visitRepository = visitRepository,
            inspectionId = inspectionId,
            financeRepository = financeRepository,
            technicalCalculationRepository = technicalCalculationRepository,
            electricalRuleConfigRepository = electricalRuleConfigRepository,
        ) as T
    }
}

private object DefaultElectricalRuleConfigRepository : ElectricalRuleConfigRepository {
    override fun observeAll() = flowOf(DefaultElectricalRuleConfigs.all)
    override fun observeByCode(code: ElectricalRuleCode) = flowOf(DefaultElectricalRuleConfigs.all.firstOrNull { it.code == code })
    override suspend fun getByCode(code: ElectricalRuleCode): ElectricalRuleConfig? = DefaultElectricalRuleConfigs.all.firstOrNull { it.code == code }
    override suspend fun save(config: ElectricalRuleConfig) = Unit
    override suspend fun saveAll(configs: List<ElectricalRuleConfig>) = Unit
    override suspend fun restoreDefaults() = Unit
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
