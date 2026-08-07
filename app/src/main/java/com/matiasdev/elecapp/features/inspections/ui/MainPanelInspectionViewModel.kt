package com.matiasdev.elecapp.features.inspections.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.matiasdev.elecapp.features.inspections.data.InspectionRepository
import com.matiasdev.elecapp.features.inspections.domain.AccessStatus
import com.matiasdev.elecapp.features.inspections.domain.BreakerCurve
import com.matiasdev.elecapp.features.inspections.domain.CircuitDestination
import com.matiasdev.elecapp.features.inspections.domain.ConductorColorStatus
import com.matiasdev.elecapp.features.inspections.domain.ConductorMaterial
import com.matiasdev.elecapp.features.inspections.domain.DifferentialTestResult
import com.matiasdev.elecapp.features.inspections.domain.GeneralCondition
import com.matiasdev.elecapp.features.inspections.domain.InspectionSectionReviewStatus
import com.matiasdev.elecapp.features.inspections.domain.InspectionScope
import com.matiasdev.elecapp.features.inspections.domain.InspectionStatus
import com.matiasdev.elecapp.features.inspections.domain.InspectionUnverifiedItem
import com.matiasdev.elecapp.features.inspections.domain.InspectionValidation
import com.matiasdev.elecapp.features.inspections.domain.MainPanelCircuit
import com.matiasdev.elecapp.features.inspections.domain.MainPanelInspection
import com.matiasdev.elecapp.features.inspections.domain.MainPanelMeasurement
import com.matiasdev.elecapp.features.inspections.domain.MainPanelMeasurementSection
import com.matiasdev.elecapp.features.inspections.domain.MainPanelMeasurementType
import com.matiasdev.elecapp.features.inspections.domain.MeasurementOrigin
import com.matiasdev.elecapp.features.inspections.domain.ProtectionCompatibility
import com.matiasdev.elecapp.features.inspections.domain.ProtectionConductorCheckResult
import com.matiasdev.elecapp.features.inspections.domain.SupplyType
import com.matiasdev.elecapp.features.inspections.domain.UnverifiedItemType
import com.matiasdev.elecapp.features.inspections.domain.YesNoPartialUnknown
import com.matiasdev.elecapp.features.inspections.domain.YesNoUnknown
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MainPanelInspectionUiState(
    val isLoading: Boolean = true,
    val scope: InspectionScope = InspectionScope.GENERAL_ASSESSMENT,
    val supplyType: SupplyType = SupplyType.UNKNOWN,
    val reviewStatus: InspectionSectionReviewStatus = InspectionSectionReviewStatus.REVIEWED,
    val addToUnverified: Boolean = false,
    val accessible: AccessStatus = AccessStatus.PARTIAL,
    val generalCondition: GeneralCondition = GeneralCondition.NOT_ASSESSED,
    val differentialPresent: YesNoUnknown = YesNoUnknown.UNKNOWN,
    val differentialRatedAmps: String = "",
    val differentialOtherRatedAmps: String = "",
    val differentialSensitivityMa: String = "",
    val differentialOtherSensitivityMa: String = "",
    val differentialTestResult: DifferentialTestResult = DifferentialTestResult.NOT_TESTED,
    val circuitCount: String = "",
    val circuitsIdentified: YesNoPartialUnknown = YesNoPartialUnknown.UNKNOWN,
    val neutralBarPresent: YesNoUnknown = YesNoUnknown.UNKNOWN,
    val groundBarPresent: YesNoUnknown = YesNoUnknown.UNKNOWN,
    val neutralAndGroundSeparated: YesNoUnknown = YesNoUnknown.UNKNOWN,
    val protectionConductorsPresent: YesNoPartialUnknown = YesNoPartialUnknown.UNKNOWN,
    val improvisedConnections: YesNoUnknown = YesNoUnknown.UNKNOWN,
    val conductorColorStatus: ConductorColorStatus = ConductorColorStatus.UNKNOWN,
    val mixedOrIncorrectColors: YesNoUnknown = YesNoUnknown.UNKNOWN,
    val overheatingSigns: YesNoUnknown = YesNoUnknown.UNKNOWN,
    val exposedPartsOrDamagedInsulation: YesNoUnknown = YesNoUnknown.UNKNOWN,
    val protectionCompatibility: ProtectionCompatibility = ProtectionCompatibility.NOT_ASSESSED,
    val wiringRisksNotes: String = "",
    val protectionConductorCheckResult: ProtectionConductorCheckResult = ProtectionConductorCheckResult.NOT_VERIFIED,
    val feederDistanceMeters: String = "",
    val feederConductorSectionMm2: String = "",
    val feederConductorMaterial: ConductorMaterial = ConductorMaterial.UNKNOWN,
    val feederDataOrigin: MeasurementOrigin = MeasurementOrigin.NOT_VERIFIED,
    val notes: String = "",
    val measurements: List<MainPanelMeasurement> = emptyList(),
    val circuits: List<MainPanelCircuit> = emptyList(),
    val circuitConsumptionInputs: Map<String, String> = emptyMap(),
    val expandedCircuitIds: Set<String> = emptySet(),
    val editingMeasurementId: String? = null,
    val measurementSection: MainPanelMeasurementSection = MainPanelMeasurementSection.INPUT_VOLTAGE,
    val measurementType: MainPanelMeasurementType = MainPanelMeasurementType.INPUT_VOLTAGE_LN,
    val measurementValue: String = "",
    val measurementOrigin: MeasurementOrigin = MeasurementOrigin.MEASURED,
    val status: InspectionStatus = InspectionStatus.DRAFT,
    val ratedAmpsError: String? = null,
    val sensitivityError: String? = null,
    val circuitCountError: String? = null,
    val feederDistanceError: String? = null,
    val feederSectionError: String? = null,
    val measurementError: String? = null,
    val saved: Boolean = false,
)

class MainPanelInspectionViewModel(
    private val repository: InspectionRepository,
    private val inspectionId: String,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MainPanelInspectionUiState())
    val uiState: StateFlow<MainPanelInspectionUiState> = _uiState.asStateFlow()
    private var createdAt: Instant = Instant.now()

    init {
        viewModelScope.launch(ioDispatcher) {
            val aggregate = repository.findAggregate(inspectionId)
            val panel = aggregate?.mainPanel
            createdAt = panel?.createdAt ?: Instant.now()
            val scope = aggregate?.inspection?.scope ?: InspectionScope.GENERAL_ASSESSMENT
            _uiState.update {
                it.copy(
                    isLoading = false,
                    scope = scope,
                    supplyType = aggregate?.inspection?.supplyType ?: SupplyType.UNKNOWN,
                    reviewStatus = panel?.reviewStatus ?: InspectionSectionReviewStatus.REVIEWED,
                    accessible = panel?.accessible ?: if (scope == InspectionScope.VISUAL_INSPECTION) AccessStatus.UNKNOWN else it.accessible,
                    generalCondition = panel?.generalCondition ?: it.generalCondition,
                    differentialPresent = panel?.differentialPresent ?: it.differentialPresent,
                    differentialRatedAmps = panel?.differentialRatedAmps?.toString().orEmpty(),
                    differentialOtherRatedAmps = panel?.differentialOtherRatedAmps?.toString().orEmpty(),
                    differentialSensitivityMa = panel?.differentialSensitivityMa?.toString().orEmpty(),
                    differentialOtherSensitivityMa = panel?.differentialOtherSensitivityMa?.toString().orEmpty(),
                    differentialTestResult = panel?.differentialTestResult ?: it.differentialTestResult,
                    circuitCount = panel?.circuitCount?.toString().orEmpty(),
                    circuitsIdentified = panel?.circuitsIdentified ?: it.circuitsIdentified,
                    neutralBarPresent = panel?.neutralBarPresent ?: it.neutralBarPresent,
                    groundBarPresent = panel?.groundBarPresent ?: it.groundBarPresent,
                    neutralAndGroundSeparated = panel?.neutralAndGroundSeparated ?: it.neutralAndGroundSeparated,
                    protectionConductorsPresent = panel?.protectionConductorsPresent ?: it.protectionConductorsPresent,
                    improvisedConnections = panel?.improvisedConnections ?: it.improvisedConnections,
                    conductorColorStatus = panel?.conductorColorStatus ?: it.conductorColorStatus,
                    mixedOrIncorrectColors = panel?.mixedOrIncorrectColors ?: it.mixedOrIncorrectColors,
                    overheatingSigns = panel?.overheatingSigns ?: it.overheatingSigns,
                    exposedPartsOrDamagedInsulation = panel?.exposedPartsOrDamagedInsulation ?: it.exposedPartsOrDamagedInsulation,
                    protectionCompatibility = panel?.protectionCompatibility ?: it.protectionCompatibility,
                    wiringRisksNotes = panel?.wiringRisksNotes.orEmpty(),
                    protectionConductorCheckResult = panel?.protectionConductorCheckResult ?: it.protectionConductorCheckResult,
                    feederDistanceMeters = panel?.feederDistanceMeters?.toInputText().orEmpty(),
                    feederConductorSectionMm2 = panel?.feederConductorSectionMm2?.toInputText().orEmpty(),
                    feederConductorMaterial = panel?.feederConductorMaterial ?: it.feederConductorMaterial,
                    feederDataOrigin = panel?.feederDataOrigin ?: it.feederDataOrigin,
                    notes = panel?.notes.orEmpty(),
                    measurements = aggregate?.mainPanelMeasurements.orEmpty(),
                    circuits = aggregate?.mainPanelCircuits.orEmpty(),
                    status = aggregate?.inspection?.status ?: it.status,
                )
            }
        }
    }

    fun update(transform: MainPanelInspectionUiState.() -> MainPanelInspectionUiState) {
        _uiState.update {
            it.transform().normalized().copy(
                saved = false,
                ratedAmpsError = null,
                sensitivityError = null,
                circuitCountError = null,
                feederDistanceError = null,
                feederSectionError = null,
                measurementError = null,
            )
        }
        if (!_uiState.value.isLoading && _uiState.value.status == InspectionStatus.DRAFT) save()
    }

    fun save() {
        val state = _uiState.value
        val ratedAmps = state.differentialRatedAmps.toIntOrNull()
        val otherRatedAmps = state.differentialOtherRatedAmps.toIntOrNull()
        val sensitivity = state.differentialSensitivityMa.toIntOrNull()
        val otherSensitivity = state.differentialOtherSensitivityMa.toIntOrNull()
        val circuits = state.circuitCount.toIntOrNull()
        val feederDistance = state.feederDistanceMeters.parseDecimalInput()
        val feederSection = state.feederConductorSectionMm2.parseDecimalInput()
        val ratedError = InspectionValidation.validatePositiveInt(ratedAmps ?: otherRatedAmps, "La corriente nominal")
        val sensitivityError = InspectionValidation.validatePositiveInt(sensitivity ?: otherSensitivity, "La sensibilidad")
        val circuitsError = InspectionValidation.validatePositiveInt(circuits, "La cantidad de circuitos")
        val feederDistanceError = InspectionValidation.validatePositiveDouble(feederDistance, "La distancia")
        val feederSectionError = InspectionValidation.validatePositiveDouble(feederSection, "La sección del conductor")
        if (ratedError != null || sensitivityError != null || circuitsError != null || feederDistanceError != null || feederSectionError != null) {
            _uiState.update {
                it.copy(
                    ratedAmpsError = ratedError,
                    sensitivityError = sensitivityError,
                    circuitCountError = circuitsError,
                    feederDistanceError = feederDistanceError,
                    feederSectionError = feederSectionError,
                )
            }
            return
        }
        viewModelScope.launch(ioDispatcher) {
            val now = Instant.now()
            repository.saveMainPanel(
                MainPanelInspection(
                    inspectionId = inspectionId,
                    reviewStatus = state.reviewStatus,
                    accessible = state.accessible,
                    generalCondition = state.generalCondition,
                    differentialPresent = state.differentialPresent,
                    differentialRatedAmps = ratedAmps,
                    differentialOtherRatedAmps = otherRatedAmps,
                    differentialSensitivityMa = sensitivity,
                    differentialOtherSensitivityMa = otherSensitivity,
                    differentialTestResult = state.differentialTestResult,
                    circuitCount = circuits,
                    circuitsIdentified = state.circuitsIdentified,
                    neutralBarPresent = state.neutralBarPresent,
                    groundBarPresent = state.groundBarPresent,
                    neutralAndGroundSeparated = state.neutralAndGroundSeparated,
                    protectionConductorsPresent = state.protectionConductorsPresent,
                    improvisedConnections = state.improvisedConnections,
                    conductorColorStatus = state.conductorColorStatus,
                    mixedOrIncorrectColors = state.mixedOrIncorrectColors,
                    overheatingSigns = state.overheatingSigns,
                    exposedPartsOrDamagedInsulation = state.exposedPartsOrDamagedInsulation,
                    protectionCompatibility = state.protectionCompatibility,
                    wiringRisksNotes = state.wiringRisksNotes.trim().ifBlank { null },
                    protectionConductorCheckResult = state.protectionConductorCheckResult,
                    feederDistanceMeters = feederDistance,
                    feederConductorSectionMm2 = feederSection,
                    feederConductorMaterial = state.feederConductorMaterial,
                    feederDataOrigin = state.feederDataOrigin,
                    notes = state.notes.trim().ifBlank { null },
                    createdAt = createdAt,
                    updatedAt = now,
                ),
            )
            saveUnverifiedReferenceIfRequested(state)
            _uiState.update { it.copy(saved = true) }
        }
    }

    fun updateCircuitCount(value: String) {
        val digits = value.filter(Char::isDigit)
        update { copy(circuitCount = digits) }
        val count = digits.toIntOrNull() ?: return
        viewModelScope.launch(ioDispatcher) {
            syncCircuitCount(count)
            refreshCollections()
        }
    }

    fun toggleCircuitExpanded(id: String) {
        _uiState.update {
            val ids = if (id in it.expandedCircuitIds) it.expandedCircuitIds - id else it.expandedCircuitIds + id
            it.copy(expandedCircuitIds = ids)
        }
    }

    fun updateCircuit(circuit: MainPanelCircuit) {
        viewModelScope.launch(ioDispatcher) {
            repository.saveMainPanelCircuit(circuit.copy(updatedAt = Instant.now()))
            refreshCollections()
        }
    }

    fun updateCircuitConsumption(circuit: MainPanelCircuit, value: String) {
        val filtered = value.filter { it.isDigit() || it == '.' || it == ',' }
        _uiState.update {
            it.copy(circuitConsumptionInputs = it.circuitConsumptionInputs + (circuit.id to filtered))
        }
        updateCircuit(circuit.copy(consumptionAmps = filtered.replace(",", ".").toDoubleOrNull()))
    }

    fun editMeasurement(measurement: MainPanelMeasurement) {
        _uiState.update {
            it.copy(
                editingMeasurementId = measurement.id,
                measurementSection = measurement.section,
                measurementType = measurement.type,
                measurementValue = measurement.value?.toString().orEmpty(),
                measurementOrigin = measurement.origin,
                measurementError = null,
            )
        }
    }

    fun updateMeasurementDraft(
        section: MainPanelMeasurementSection = _uiState.value.measurementSection,
        type: MainPanelMeasurementType = _uiState.value.measurementType,
        value: String = _uiState.value.measurementValue,
        origin: MeasurementOrigin = _uiState.value.measurementOrigin,
    ) {
        _uiState.update {
            it.copy(
                measurementSection = section,
                measurementType = type,
                measurementValue = value.filter { char -> char.isDigit() || char == '.' || char == ',' },
                measurementOrigin = origin,
                measurementError = null,
            )
        }
    }

    fun saveMeasurement() {
        val state = _uiState.value
        val value = state.measurementValue.replace(",", ".").toDoubleOrNull()
        val error = if (state.measurementOrigin == MeasurementOrigin.NOT_VERIFIED) null else InspectionValidation.validatePositiveDouble(value, "La medición")
        if (error != null) {
            _uiState.update { it.copy(measurementError = error) }
            return
        }
        viewModelScope.launch(ioDispatcher) {
            val now = Instant.now()
            val existing = state.measurements.firstOrNull { it.id == state.editingMeasurementId }
            repository.saveMainPanelMeasurement(
                MainPanelMeasurement(
                    id = existing?.id ?: UUID.randomUUID().toString(),
                    inspectionId = inspectionId,
                    section = state.measurementSection,
                    type = state.measurementType,
                    value = if (state.measurementOrigin == MeasurementOrigin.NOT_VERIFIED) null else value,
                    unit = "V",
                    origin = state.measurementOrigin,
                    sortOrder = existing?.sortOrder ?: (state.measurements.maxOfOrNull { it.sortOrder }?.plus(1) ?: 0),
                    createdAt = existing?.createdAt ?: now,
                    updatedAt = now,
                    isDeleted = false,
                ),
            )
            refreshCollections()
            _uiState.update { it.copy(editingMeasurementId = null, measurementValue = "", measurementError = null, saved = true) }
        }
    }

    fun deleteMeasurement(id: String) {
        viewModelScope.launch(ioDispatcher) {
            repository.softDeleteMainPanelMeasurement(id)
            refreshCollections()
        }
    }

    fun cancelMeasurementEdit() {
        _uiState.update { it.copy(editingMeasurementId = null, measurementValue = "", measurementError = null) }
    }

    private suspend fun syncCircuitCount(count: Int) {
        val now = Instant.now()
        val current = repository.findAggregate(inspectionId)?.mainPanelCircuits.orEmpty()
        current.filter { it.sortOrder >= count }.forEach { repository.softDeleteMainPanelCircuit(it.id) }
        (0 until count).forEach { index ->
            if (current.none { it.sortOrder == index }) {
                repository.saveMainPanelCircuit(
                    MainPanelCircuit(
                        id = UUID.randomUUID().toString(),
                        inspectionId = inspectionId,
                        sortOrder = index,
                        destination = CircuitDestination.UNIDENTIFIED,
                        destinationOther = null,
                        breakerAmps = null,
                        breakerOtherAmps = null,
                        breakerCurve = BreakerCurve.UNKNOWN,
                        conductorSectionMm2 = null,
                        conductorOtherSectionMm2 = null,
                        conductorMaterial = ConductorMaterial.UNKNOWN,
                        conductorMaterialOther = null,
                        consumptionAmps = null,
                        consumptionOrigin = MeasurementOrigin.NOT_VERIFIED,
                        notes = null,
                        createdAt = now,
                        updatedAt = now,
                        isDeleted = false,
                    ),
                )
            }
        }
    }

    private suspend fun refreshCollections() {
        val aggregate = repository.findAggregate(inspectionId)
        _uiState.update {
            val circuits = aggregate?.mainPanelCircuits.orEmpty()
            it.copy(
                measurements = aggregate?.mainPanelMeasurements.orEmpty(),
                circuits = circuits,
                circuitConsumptionInputs = circuits.associate { circuit ->
                    circuit.id to (it.circuitConsumptionInputs[circuit.id] ?: circuit.consumptionAmps?.toInputText().orEmpty())
                },
                saved = true,
            )
        }
    }

    private suspend fun saveUnverifiedReferenceIfRequested(state: MainPanelInspectionUiState) {
        if (
            state.scope != InspectionScope.VISUAL_INSPECTION ||
            state.reviewStatus != InspectionSectionReviewStatus.NOT_VERIFIED ||
            !state.addToUnverified
        ) {
            return
        }
        val aggregate = repository.findAggregate(inspectionId) ?: return
        val existing = aggregate.unverifiedItems
        if (existing.any { it.type == UnverifiedItemType.PANEL_NOT_OPENED }) return
        val now = Instant.now()
        repository.saveUnverifiedItems(
            inspectionId,
            existing + InspectionUnverifiedItem(
                id = UUID.randomUUID().toString(),
                inspectionId = inspectionId,
                type = UnverifiedItemType.PANEL_NOT_OPENED,
                description = "Tablero principal no verificado.",
                createdAt = now,
                updatedAt = now,
                isDeleted = false,
            ),
        )
    }
}

private fun Double.toInputText(): String {
    val whole = toLong()
    return if (this == whole.toDouble()) whole.toString() else toString()
}

private fun MainPanelInspectionUiState.normalized(): MainPanelInspectionUiState {
    if (reviewStatus != InspectionSectionReviewStatus.REVIEWED || accessible == AccessStatus.NO) {
        return copy(
            differentialPresent = YesNoUnknown.UNKNOWN,
            differentialRatedAmps = "",
            differentialOtherRatedAmps = "",
            differentialSensitivityMa = "",
            differentialOtherSensitivityMa = "",
            differentialTestResult = DifferentialTestResult.NOT_APPLICABLE,
            circuitCount = "",
            circuitsIdentified = YesNoPartialUnknown.UNKNOWN,
            neutralBarPresent = YesNoUnknown.UNKNOWN,
            groundBarPresent = YesNoUnknown.UNKNOWN,
            neutralAndGroundSeparated = YesNoUnknown.UNKNOWN,
            protectionConductorsPresent = YesNoPartialUnknown.UNKNOWN,
            improvisedConnections = YesNoUnknown.UNKNOWN,
            conductorColorStatus = ConductorColorStatus.UNKNOWN,
            mixedOrIncorrectColors = YesNoUnknown.UNKNOWN,
            overheatingSigns = YesNoUnknown.UNKNOWN,
            exposedPartsOrDamagedInsulation = YesNoUnknown.UNKNOWN,
            protectionCompatibility = ProtectionCompatibility.NOT_ASSESSED,
            wiringRisksNotes = "",
            protectionConductorCheckResult = ProtectionConductorCheckResult.NOT_VERIFIED,
            feederDistanceMeters = "",
            feederConductorSectionMm2 = "",
            feederConductorMaterial = ConductorMaterial.UNKNOWN,
            feederDataOrigin = MeasurementOrigin.NOT_VERIFIED,
        )
    }
    return copy(
        differentialRatedAmps = if (differentialPresent == YesNoUnknown.YES) differentialRatedAmps else "",
        differentialOtherRatedAmps = if (differentialPresent == YesNoUnknown.YES) differentialOtherRatedAmps else "",
        differentialSensitivityMa = if (differentialPresent == YesNoUnknown.YES) differentialSensitivityMa else "",
        differentialOtherSensitivityMa = if (differentialPresent == YesNoUnknown.YES) differentialOtherSensitivityMa else "",
        differentialTestResult = if (differentialPresent == YesNoUnknown.YES) differentialTestResult else DifferentialTestResult.NOT_APPLICABLE,
    )
}

private fun String.parseDecimalInput(): Double? = trim().replace(",", ".").takeIf(String::isNotBlank)?.toDoubleOrNull()

const val MAIN_PANEL_OTHER_VALUE = "OTHER"

class MainPanelInspectionViewModelFactory(
    private val repository: InspectionRepository,
    private val inspectionId: String,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainPanelInspectionViewModel(repository, inspectionId) as T
    }
}
