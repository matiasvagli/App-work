package com.matiasdev.elecapp.features.electricaltools.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.matiasdev.elecapp.features.clients.data.ClientRepository
import com.matiasdev.elecapp.features.electricaltools.calculators.VoltageDropCalculator
import com.matiasdev.elecapp.features.electricaltools.data.CalculationJson
import com.matiasdev.elecapp.features.electricaltools.data.TechnicalCalculationRepository
import com.matiasdev.elecapp.features.electricaltools.domain.CalculationSource
import com.matiasdev.elecapp.features.electricaltools.domain.ElectricalSystemType
import com.matiasdev.elecapp.features.electricaltools.domain.TechnicalConductorMaterial
import com.matiasdev.elecapp.features.electricaltools.domain.TemperatureMode
import com.matiasdev.elecapp.features.electricaltools.domain.VoltageDropCurrentMode
import com.matiasdev.elecapp.features.electricaltools.domain.VoltageDropInput
import com.matiasdev.elecapp.features.electricaltools.domain.VoltageDropResult
import com.matiasdev.elecapp.features.inspections.data.InspectionRepository
import com.matiasdev.elecapp.features.inspections.domain.ConductorMaterial
import com.matiasdev.elecapp.features.inspections.domain.InspectionAggregate
import com.matiasdev.elecapp.features.inspections.domain.MainPanelMeasurementSection
import com.matiasdev.elecapp.features.inspections.domain.MeasurementOrigin
import com.matiasdev.elecapp.features.inspections.domain.PillarMeasurementType
import com.matiasdev.elecapp.features.inspections.domain.SupplyType
import com.matiasdev.elecapp.features.visits.data.VisitRepository
import com.matiasdev.elecapp.features.visits.ui.formatVisitDateTime
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class VoltageDropUiState(
    val systemType: ElectricalSystemType = ElectricalSystemType.AC_SINGLE_PHASE,
    val nominalVoltage: String = "220",
    val currentMode: VoltageDropCurrentMode = VoltageDropCurrentMode.DIRECT_CURRENT,
    val current: String = "",
    val power: String = "",
    val powerInKilowatts: Boolean = false,
    val powerFactor: String = "",
    val efficiency: String = "",
    val length: String = "",
    val section: String = "",
    val material: TechnicalConductorMaterial = TechnicalConductorMaterial.COPPER,
    val temperatureMode: TemperatureMode = TemperatureMode.NOT_CONSIDERED,
    val temperature: String = "",
    val source: CalculationSource = CalculationSource.CALCULATED,
    val instrumentName: String = "",
    val measurementContext: String = "",
    val assumptions: String = "",
    val dataProvidedByClient: Boolean = false,
    val result: VoltageDropResult? = null,
    val errors: List<String> = emptyList(),
    val savedCalculationId: String? = null,
    val association: CalculationAssociationDraft = CalculationAssociationDraft(),
    val snackbarMessage: String? = null,
)

class VoltageDropViewModel(
    private val repository: TechnicalCalculationRepository,
    private val clientRepository: ClientRepository,
    private val visitRepository: VisitRepository,
    private val inspectionRepository: InspectionRepository,
    initialClientId: String?,
    initialVisitId: String?,
    initialInspectionId: String?,
    duplicateId: String?,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        VoltageDropUiState(
            association = CalculationAssociationDraft(initialClientId, initialVisitId, initialInspectionId, "Resolviendo asociación..."),
        ),
    )
    val uiState: StateFlow<VoltageDropUiState> = _uiState.asStateFlow()

    init {
        resolveAssociation(initialClientId, initialVisitId, initialInspectionId, shouldPrefillFromInspection = duplicateId == null)
        if (duplicateId != null) loadDuplicate(duplicateId)
    }

    fun update(transform: VoltageDropUiState.() -> VoltageDropUiState) {
        _uiState.update { it.transform().copy(errors = emptyList(), snackbarMessage = null) }
    }

    fun calculate() {
        val state = _uiState.value
        val calculation = VoltageDropCalculator.calculate(state.toInput())
        _uiState.update { it.copy(result = calculation.value, errors = calculation.errors, savedCalculationId = null) }
    }

    fun save() {
        val state = _uiState.value
        val result = state.result ?: return _uiState.update { it.copy(errors = listOf("Calculá un resultado válido antes de guardar.")) }
        viewModelScope.launch(ioDispatcher) {
            val calculation = buildVoltageDropCalculation(state.savedCalculationId, state.toInput(), result, state.association)
            repository.save(calculation)
            _uiState.update { it.copy(savedCalculationId = calculation.id, snackbarMessage = "Cálculo guardado") }
        }
    }

    fun clearAssociation() {
        _uiState.update { it.copy(association = CalculationAssociationDraft()) }
    }

    fun newCalculation() {
        _uiState.update { VoltageDropUiState(association = it.association) }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    private fun resolveAssociation(clientId: String?, visitId: String?, inspectionId: String?, shouldPrefillFromInspection: Boolean) {
        viewModelScope.launch(ioDispatcher) {
            var resolvedClientId = clientId
            var resolvedVisitId = visitId
            val aggregate = inspectionId?.let { inspectionRepository.findAggregate(it) }
            if (aggregate?.inspection != null) resolvedVisitId = aggregate.inspection.visitId
            val visit = resolvedVisitId?.let { visitRepository.findActiveById(it) }
            if (resolvedClientId == null) resolvedClientId = visit?.clientId
            val client = resolvedClientId?.let { clientRepository.findById(it) }
            val label = listOfNotNull(
                client?.fullName,
                visit?.scheduledAt?.formatVisitDateTime()?.let { "Visita del $it" },
                inspectionId?.let { "Relevamiento en curso" },
            ).joinToString(" · ").ifBlank { "Sin asociación" }
            _uiState.update {
                val associated = it.copy(association = CalculationAssociationDraft(resolvedClientId, resolvedVisitId, inspectionId, label))
                if (shouldPrefillFromInspection && aggregate != null) associated.prefilledFromInspection(aggregate) else associated
            }
        }
    }

    private fun loadDuplicate(duplicateId: String) {
        viewModelScope.launch(ioDispatcher) {
            val input = repository.findById(duplicateId)?.let { CalculationJson.decodeVoltageDropInput(it.inputDataJson) } ?: return@launch
            _uiState.update {
                it.copy(
                    systemType = input.systemType,
                    nominalVoltage = input.nominalVoltageVolts.toString(),
                    currentMode = input.currentMode,
                    current = input.currentAmps?.toString().orEmpty(),
                    power = input.activePowerWatts?.toString().orEmpty(),
                    powerInKilowatts = false,
                    powerFactor = input.powerFactor?.toString().orEmpty(),
                    efficiency = input.efficiency?.toString().orEmpty(),
                    length = input.conductorLengthMeters.toString(),
                    section = input.conductorSectionMm2.toString(),
                    material = input.conductorMaterial,
                    temperatureMode = input.temperatureMode,
                    temperature = input.conductorTemperatureCelsius?.toString().orEmpty(),
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

private fun VoltageDropUiState.prefilledFromInspection(aggregate: InspectionAggregate): VoltageDropUiState {
    val panel = aggregate.mainPanel
    return copy(
        systemType = when (aggregate.pillar?.supplyType ?: aggregate.inspection.supplyType) {
            SupplyType.THREE_PHASE -> ElectricalSystemType.AC_THREE_PHASE
            SupplyType.SINGLE_PHASE,
            SupplyType.UNKNOWN,
            -> ElectricalSystemType.AC_SINGLE_PHASE
        },
        nominalVoltage = aggregate.preferredVoltage()?.toInputText() ?: nominalVoltage,
        current = aggregate.preferredCurrent()?.toInputText().orEmpty(),
        length = panel?.feederDistanceMeters?.toInputText().orEmpty(),
        section = panel?.feederConductorSectionMm2?.toInputText().orEmpty(),
        material = when (panel?.feederConductorMaterial) {
            ConductorMaterial.ALUMINUM -> TechnicalConductorMaterial.ALUMINUM
            else -> TechnicalConductorMaterial.COPPER
        },
        source = panel?.feederDataOrigin?.toCalculationSourceOrNull() ?: source,
        measurementContext = "Alimentación principal desde pilar/acometida a tablero principal.",
        dataProvidedByClient = panel?.feederDataOrigin == MeasurementOrigin.DECLARED_BY_CLIENT,
        result = null,
        savedCalculationId = null,
    )
}

private fun InspectionAggregate.preferredVoltage(): Double? {
    return mainPanelMeasurements
        .filterNot { it.isDeleted }
        .filter { it.section == MainPanelMeasurementSection.INPUT_VOLTAGE }
        .firstNotNullOfOrNull { it.value }
        ?: pillarMeasurements.filterNot { it.isDeleted }.firstNotNullOfOrNull { it.value?.takeIf { _ -> it.type.name.contains("VOLTAGE") } }
}

private fun InspectionAggregate.preferredCurrent(): Double? {
    return mainPanelCircuits
        .filterNot { it.isDeleted }
        .firstNotNullOfOrNull { it.consumptionAmps }
        ?: pillarMeasurements
            .filterNot { it.isDeleted }
            .filter { it.type == PillarMeasurementType.SINGLE_PHASE_CURRENT || it.type.name.startsWith("CURRENT") }
            .firstNotNullOfOrNull { it.value }
}

private fun MeasurementOrigin.toCalculationSourceOrNull(): CalculationSource? = when (this) {
    MeasurementOrigin.MEASURED -> CalculationSource.MEASURED
    MeasurementOrigin.CALCULATED -> CalculationSource.CALCULATED
    MeasurementOrigin.ESTIMATED,
    MeasurementOrigin.DECLARED_BY_CLIENT,
    -> CalculationSource.ESTIMATED
    MeasurementOrigin.NOT_VERIFIED -> null
}

private fun VoltageDropUiState.toInput(): VoltageDropInput {
    val powerWatts = power.parseDouble()?.let { if (powerInKilowatts) it * 1000.0 else it }
    return VoltageDropInput(
        systemType = systemType,
        nominalVoltageVolts = nominalVoltage.parseDouble() ?: Double.NaN,
        currentMode = currentMode,
        currentAmps = current.parseDouble(),
        activePowerWatts = powerWatts,
        powerFactor = powerFactor.parseDouble(),
        efficiency = efficiency.parseDouble()?.let { if (it > 1.0) it / 100.0 else it },
        conductorLengthMeters = length.parseDouble() ?: Double.NaN,
        conductorSectionMm2 = section.parseDouble() ?: Double.NaN,
        conductorMaterial = material,
        temperatureMode = temperatureMode,
        conductorTemperatureCelsius = temperature.parseDouble(),
        source = source,
        context = contextFromFields(source, instrumentName, measurementContext, assumptions, dataProvidedByClient),
    )
}

private fun Double.toInputText(): String {
    val whole = toLong()
    return if (this == whole.toDouble()) whole.toString() else toString()
}

class VoltageDropViewModelFactory(
    private val repository: TechnicalCalculationRepository,
    private val clientRepository: ClientRepository,
    private val visitRepository: VisitRepository,
    private val inspectionRepository: InspectionRepository,
    private val clientId: String?,
    private val visitId: String?,
    private val inspectionId: String?,
    private val duplicateId: String?,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return VoltageDropViewModel(repository, clientRepository, visitRepository, inspectionRepository, clientId, visitId, inspectionId, duplicateId) as T
    }
}
