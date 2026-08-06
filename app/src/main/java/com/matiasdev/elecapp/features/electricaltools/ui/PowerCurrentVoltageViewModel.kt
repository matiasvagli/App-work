package com.matiasdev.elecapp.features.electricaltools.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.matiasdev.elecapp.features.clients.data.ClientRepository
import com.matiasdev.elecapp.features.electricalrules.domain.ElectricalMeasurementReviewEvaluator
import com.matiasdev.elecapp.features.electricalrules.domain.ElectricalRuleConfigRepository
import com.matiasdev.elecapp.features.electricalrules.domain.EvaluateSupplyVoltageUseCase
import com.matiasdev.elecapp.features.electricaltools.calculators.PowerCurrentVoltageCalculator
import com.matiasdev.elecapp.features.electricaltools.data.CalculationJson
import com.matiasdev.elecapp.features.electricaltools.data.TechnicalCalculationRepository
import com.matiasdev.elecapp.features.electricaltools.domain.CalculationSource
import com.matiasdev.elecapp.features.electricaltools.domain.ElectricalSystemType
import com.matiasdev.elecapp.features.electricaltools.domain.ElectricalVariable
import com.matiasdev.elecapp.features.electricaltools.domain.PowerCurrentVoltageInput
import com.matiasdev.elecapp.features.electricaltools.domain.PowerCurrentVoltageResult
import com.matiasdev.elecapp.features.inspections.data.InspectionRepository
import com.matiasdev.elecapp.features.visits.data.VisitRepository
import com.matiasdev.elecapp.features.visits.ui.formatVisitDateTime
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PowerCurrentVoltageUiState(
    val systemType: ElectricalSystemType = ElectricalSystemType.AC_SINGLE_PHASE,
    val variableToCalculate: ElectricalVariable = ElectricalVariable.CURRENT,
    val voltage: String = "",
    val current: String = "",
    val power: String = "",
    val powerInKilowatts: Boolean = false,
    val powerFactor: String = "",
    val efficiency: String = "",
    val source: CalculationSource = CalculationSource.CALCULATED,
    val instrumentName: String = "",
    val measurementContext: String = "",
    val assumptions: String = "",
    val dataProvidedByClient: Boolean = false,
    val result: PowerCurrentVoltageResult? = null,
    val errors: List<String> = emptyList(),
    val savedCalculationId: String? = null,
    val association: CalculationAssociationDraft = CalculationAssociationDraft(),
    val snackbarMessage: String? = null,
)

class PowerCurrentVoltageViewModel(
    private val repository: TechnicalCalculationRepository,
    private val clientRepository: ClientRepository,
    private val visitRepository: VisitRepository,
    private val inspectionRepository: InspectionRepository,
    private val electricalRuleConfigRepository: ElectricalRuleConfigRepository,
    initialClientId: String?,
    initialVisitId: String?,
    initialInspectionId: String?,
    duplicateId: String?,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        PowerCurrentVoltageUiState(
            association = CalculationAssociationDraft(initialClientId, initialVisitId, initialInspectionId, "Resolviendo asociación..."),
        ),
    )
    val uiState: StateFlow<PowerCurrentVoltageUiState> = _uiState.asStateFlow()

    init {
        resolveAssociation(initialClientId, initialVisitId, initialInspectionId)
        if (duplicateId != null) loadDuplicate(duplicateId)
    }

    fun update(transform: PowerCurrentVoltageUiState.() -> PowerCurrentVoltageUiState) {
        _uiState.update { it.transform().copy(errors = emptyList(), snackbarMessage = null) }
    }

    fun calculate() {
        val state = _uiState.value
        val input = state.toInput()
        val calculation = PowerCurrentVoltageCalculator.calculate(input)
        _uiState.update { it.copy(result = calculation.value, errors = calculation.errors, savedCalculationId = null) }
    }

    fun save() {
        val state = _uiState.value
        val result = state.result ?: return _uiState.update { it.copy(errors = listOf("Calculá un resultado válido antes de guardar.")) }
        viewModelScope.launch(ioDispatcher) {
            val calculation = buildPowerCalculation(state.savedCalculationId, state.toInput(), result, state.association)
            repository.save(calculation)
            ElectricalMeasurementReviewEvaluator.evaluateSupplyVoltage(
                calculation = calculation,
                useCase = EvaluateSupplyVoltageUseCase(electricalRuleConfigRepository),
            )
            _uiState.update { it.copy(savedCalculationId = calculation.id, snackbarMessage = "Cálculo guardado") }
        }
    }

    fun clearAssociation() {
        _uiState.update { it.copy(association = CalculationAssociationDraft()) }
    }

    fun newCalculation() {
        _uiState.update { PowerCurrentVoltageUiState(association = it.association) }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    private fun resolveAssociation(clientId: String?, visitId: String?, inspectionId: String?) {
        viewModelScope.launch(ioDispatcher) {
            var resolvedClientId = clientId
            var resolvedVisitId = visitId
            val inspection = inspectionId?.let { inspectionRepository.findAggregate(it)?.inspection }
            if (inspection != null) resolvedVisitId = inspection.visitId
            val visit = resolvedVisitId?.let { visitRepository.findActiveById(it) }
            if (resolvedClientId == null) resolvedClientId = visit?.clientId
            val client = resolvedClientId?.let { clientRepository.findById(it) }
            val label = listOfNotNull(
                client?.fullName,
                visit?.scheduledAt?.formatVisitDateTime()?.let { "Visita del $it" },
                inspectionId?.let { "Relevamiento en curso" },
            ).joinToString(" · ").ifBlank { "Sin asociación" }
            _uiState.update { it.copy(association = CalculationAssociationDraft(resolvedClientId, resolvedVisitId, inspectionId, label)) }
        }
    }

    private fun loadDuplicate(duplicateId: String) {
        viewModelScope.launch(ioDispatcher) {
            val input = repository.findById(duplicateId)?.let { CalculationJson.decodePowerInput(it.inputDataJson) } ?: return@launch
            _uiState.update {
                it.copy(
                    systemType = input.systemType,
                    variableToCalculate = input.variableToCalculate,
                    voltage = input.voltageVolts?.toString().orEmpty(),
                    current = input.currentAmps?.toString().orEmpty(),
                    power = input.activePowerWatts?.toString().orEmpty(),
                    powerInKilowatts = false,
                    powerFactor = input.powerFactor?.toString().orEmpty(),
                    efficiency = input.efficiency?.toString().orEmpty(),
                    source = input.source,
                    instrumentName = input.context.instrumentName.orEmpty(),
                    measurementContext = input.context.measurementContext.orEmpty(),
                    assumptions = input.context.assumptions.orEmpty(),
                    dataProvidedByClient = input.context.dataProvidedByClient,
                    result = null,
                    savedCalculationId = null,
                    snackbarMessage = "Entradas duplicadas. Recalculá antes de guardar.",
                )
            }
        }
    }
}

private fun PowerCurrentVoltageUiState.toInput(): PowerCurrentVoltageInput {
    val powerWatts = power.parseDouble()?.let { if (powerInKilowatts) it * 1000.0 else it }
    return PowerCurrentVoltageInput(
        systemType = systemType,
        variableToCalculate = variableToCalculate,
        voltageVolts = voltage.parseDouble(),
        currentAmps = current.parseDouble(),
        activePowerWatts = powerWatts,
        powerFactor = powerFactor.parseDouble(),
        efficiency = efficiency.parseDouble()?.let { if (it > 1.0) it / 100.0 else it },
        source = source,
        context = contextFromFields(source, instrumentName, measurementContext, assumptions, dataProvidedByClient),
    )
}

fun String.parseDouble(): Double? = trim().replace(",", ".").toDoubleOrNull()

class PowerCurrentVoltageViewModelFactory(
    private val repository: TechnicalCalculationRepository,
    private val clientRepository: ClientRepository,
    private val visitRepository: VisitRepository,
    private val inspectionRepository: InspectionRepository,
    private val clientId: String?,
    private val visitId: String?,
    private val inspectionId: String?,
    private val duplicateId: String?,
    private val electricalRuleConfigRepository: ElectricalRuleConfigRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PowerCurrentVoltageViewModel(
            repository,
            clientRepository,
            visitRepository,
            inspectionRepository,
            electricalRuleConfigRepository,
            clientId,
            visitId,
            inspectionId,
            duplicateId,
        ) as T
    }
}
