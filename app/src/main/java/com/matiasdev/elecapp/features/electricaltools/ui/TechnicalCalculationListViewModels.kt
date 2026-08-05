package com.matiasdev.elecapp.features.electricaltools.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.matiasdev.elecapp.features.clients.data.ClientRepository
import com.matiasdev.elecapp.features.electricaltools.data.TechnicalCalculationFilters
import com.matiasdev.elecapp.features.electricaltools.data.TechnicalCalculationRepository
import com.matiasdev.elecapp.features.electricaltools.domain.CalculationSource
import com.matiasdev.elecapp.features.electricaltools.domain.TechnicalCalculation
import com.matiasdev.elecapp.features.electricaltools.domain.TechnicalCalculationType
import com.matiasdev.elecapp.features.electricaltools.domain.TechnicalClassification
import com.matiasdev.elecapp.features.electricaltools.summary.SuggestedFindingFactory
import com.matiasdev.elecapp.features.inspections.data.InspectionRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class CalculationHistoryFilter {
    ALL,
    POWER_CURRENT_VOLTAGE,
    VOLTAGE_DROP,
    MEASURED,
    CALCULATED,
    ESTIMATED,
    REQUIRES_REVIEW,
    ASSOCIATED_TO_INSPECTION,
    UNASSOCIATED,
}

data class ElectricalToolsHistoryUiState(
    val isLoading: Boolean = true,
    val query: String = "",
    val filter: CalculationHistoryFilter = CalculationHistoryFilter.ALL,
    val calculations: List<TechnicalCalculation> = emptyList(),
    val errorMessage: String? = null,
)

@OptIn(ExperimentalCoroutinesApi::class)
class ElectricalToolsHistoryViewModel(
    private val repository: TechnicalCalculationRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {
    private val filters = MutableStateFlow(TechnicalCalculationFilters())
    private val _uiState = MutableStateFlow(ElectricalToolsHistoryUiState())
    val uiState: StateFlow<ElectricalToolsHistoryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch(ioDispatcher) {
            filters.flatMapLatest { repository.search(it).map { rows -> it to rows } }
                .catch { error -> _uiState.update { it.copy(isLoading = false, errorMessage = error.message) } }
                .collect { (filterSet, rows) ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            query = filterSet.query,
                            filter = filterSet.toHistoryFilter(),
                            calculations = rows,
                        )
                    }
                }
        }
    }

    fun updateQuery(query: String) {
        filters.update { it.copy(query = query) }
    }

    fun selectFilter(filter: CalculationHistoryFilter) {
        filters.update { filter.toRepositoryFilters(it.query) }
    }

    fun delete(calculation: TechnicalCalculation) {
        viewModelScope.launch(ioDispatcher) {
            repository.softDelete(calculation.id)
        }
    }
}

private fun CalculationHistoryFilter.toRepositoryFilters(query: String): TechnicalCalculationFilters = when (this) {
    CalculationHistoryFilter.ALL -> TechnicalCalculationFilters(query = query)
    CalculationHistoryFilter.POWER_CURRENT_VOLTAGE -> TechnicalCalculationFilters(query = query, type = TechnicalCalculationType.POWER_CURRENT_VOLTAGE)
    CalculationHistoryFilter.VOLTAGE_DROP -> TechnicalCalculationFilters(query = query, type = TechnicalCalculationType.VOLTAGE_DROP)
    CalculationHistoryFilter.MEASURED -> TechnicalCalculationFilters(query = query, source = CalculationSource.MEASURED)
    CalculationHistoryFilter.CALCULATED -> TechnicalCalculationFilters(query = query, source = CalculationSource.CALCULATED)
    CalculationHistoryFilter.ESTIMATED -> TechnicalCalculationFilters(query = query, source = CalculationSource.ESTIMATED)
    CalculationHistoryFilter.REQUIRES_REVIEW -> TechnicalCalculationFilters(query = query, classification = TechnicalClassification.REQUIRES_REVIEW)
    CalculationHistoryFilter.ASSOCIATED_TO_INSPECTION -> TechnicalCalculationFilters(query = query, associatedToInspection = true)
    CalculationHistoryFilter.UNASSOCIATED -> TechnicalCalculationFilters(query = query, unassociated = true)
}

private fun TechnicalCalculationFilters.toHistoryFilter(): CalculationHistoryFilter = when {
    type == TechnicalCalculationType.POWER_CURRENT_VOLTAGE -> CalculationHistoryFilter.POWER_CURRENT_VOLTAGE
    type == TechnicalCalculationType.VOLTAGE_DROP -> CalculationHistoryFilter.VOLTAGE_DROP
    source == CalculationSource.MEASURED -> CalculationHistoryFilter.MEASURED
    source == CalculationSource.CALCULATED -> CalculationHistoryFilter.CALCULATED
    source == CalculationSource.ESTIMATED -> CalculationHistoryFilter.ESTIMATED
    classification == TechnicalClassification.REQUIRES_REVIEW -> CalculationHistoryFilter.REQUIRES_REVIEW
    associatedToInspection == true -> CalculationHistoryFilter.ASSOCIATED_TO_INSPECTION
    unassociated == true -> CalculationHistoryFilter.UNASSOCIATED
    else -> CalculationHistoryFilter.ALL
}

class ElectricalToolsHistoryViewModelFactory(
    private val repository: TechnicalCalculationRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = ElectricalToolsHistoryViewModel(repository) as T
}

data class TechnicalCalculationDetailUiState(
    val isLoading: Boolean = true,
    val calculation: TechnicalCalculation? = null,
    val clientName: String? = null,
    val title: String = "",
    val description: String = "",
    val technicianConclusion: com.matiasdev.elecapp.features.electricaltools.domain.TechnicianConclusion =
        com.matiasdev.elecapp.features.electricaltools.domain.TechnicianConclusion.NOT_REVIEWED,
    val technicianNotes: String = "",
    val associationClientId: String = "",
    val associationVisitId: String = "",
    val associationInspectionId: String = "",
    val snackbarMessage: String? = null,
    val errorMessage: String? = null,
)

class TechnicalCalculationDetailViewModel(
    private val repository: TechnicalCalculationRepository,
    private val clientRepository: ClientRepository,
    private val inspectionRepository: InspectionRepository,
    private val calculationId: String,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {
    private val _uiState = MutableStateFlow(TechnicalCalculationDetailUiState())
    val uiState: StateFlow<TechnicalCalculationDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch(ioDispatcher) {
            repository.observeById(calculationId)
                .catch { error -> _uiState.update { it.copy(isLoading = false, errorMessage = error.message) } }
                .collect { calculation ->
                    val client = calculation?.clientId?.let { clientRepository.findById(it) }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            calculation = calculation,
                            clientName = client?.fullName,
                            title = calculation?.title.orEmpty(),
                            description = calculation?.description.orEmpty(),
                            technicianConclusion = calculation?.technicianConclusion ?: it.technicianConclusion,
                            technicianNotes = calculation?.technicianNotes.orEmpty(),
                            associationClientId = calculation?.clientId.orEmpty(),
                            associationVisitId = calculation?.visitId.orEmpty(),
                            associationInspectionId = calculation?.inspectionId.orEmpty(),
                        )
                    }
                }
        }
    }

    fun update(transform: TechnicalCalculationDetailUiState.() -> TechnicalCalculationDetailUiState) {
        _uiState.update { it.transform().copy(snackbarMessage = null) }
    }

    fun saveEditableFields() {
        val state = _uiState.value
        viewModelScope.launch(ioDispatcher) {
            repository.updateEditableFields(
                id = calculationId,
                title = state.title,
                description = state.description,
                technicianConclusion = state.technicianConclusion,
                technicianNotes = state.technicianNotes,
            )
            _uiState.update { it.copy(snackbarMessage = "Cambios guardados") }
        }
    }

    fun saveAssociation() {
        val state = _uiState.value
        viewModelScope.launch(ioDispatcher) {
            repository.associate(
                calculationId,
                state.associationClientId.takeIf(String::isNotBlank),
                state.associationVisitId.takeIf(String::isNotBlank),
                state.associationInspectionId.takeIf(String::isNotBlank),
            )
            _uiState.update { it.copy(snackbarMessage = "Asociación actualizada") }
        }
    }

    fun unlinkInspection() {
        viewModelScope.launch(ioDispatcher) {
            repository.unlinkInspection(calculationId)
            _uiState.update { it.copy(snackbarMessage = "Cálculo desvinculado del relevamiento") }
        }
    }

    fun createSuggestedFinding() {
        val calculation = _uiState.value.calculation ?: return
        val draft = SuggestedFindingFactory.fromCalculation(calculation)
        if (draft == null) {
            _uiState.update { it.copy(snackbarMessage = "Este cálculo no requiere un hallazgo sugerido.") }
            return
        }
        viewModelScope.launch(ioDispatcher) {
            val aggregate = inspectionRepository.findAggregate(draft.inspectionId)
            val duplicate = aggregate?.findings.orEmpty().any {
                it.title == draft.title && it.description == draft.description && !it.isDeleted
            }
            if (duplicate) {
                _uiState.update { it.copy(snackbarMessage = "Ya existe un hallazgo sugerido igual.") }
            } else {
                inspectionRepository.saveFinding(draft)
                _uiState.update { it.copy(snackbarMessage = "Hallazgo sugerido guardado") }
            }
        }
    }

    fun delete(onDeleted: () -> Unit) {
        viewModelScope.launch(ioDispatcher) {
            repository.softDelete(calculationId)
            onDeleted()
        }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }
}

class TechnicalCalculationDetailViewModelFactory(
    private val repository: TechnicalCalculationRepository,
    private val clientRepository: ClientRepository,
    private val inspectionRepository: InspectionRepository,
    private val calculationId: String,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return TechnicalCalculationDetailViewModel(repository, clientRepository, inspectionRepository, calculationId) as T
    }
}
