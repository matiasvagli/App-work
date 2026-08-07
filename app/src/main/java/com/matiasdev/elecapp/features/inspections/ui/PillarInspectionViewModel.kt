package com.matiasdev.elecapp.features.inspections.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.matiasdev.elecapp.features.inspections.data.InspectionRepository
import com.matiasdev.elecapp.features.inspections.domain.AccessStatus
import com.matiasdev.elecapp.features.inspections.domain.ConductorCondition
import com.matiasdev.elecapp.features.inspections.domain.ConductorMaterial
import com.matiasdev.elecapp.features.inspections.domain.DifferentialTestResult
import com.matiasdev.elecapp.features.inspections.domain.GeneralCondition
import com.matiasdev.elecapp.features.inspections.domain.InspectionSectionReviewStatus
import com.matiasdev.elecapp.features.inspections.domain.InspectionScope
import com.matiasdev.elecapp.features.inspections.domain.InspectionStatus
import com.matiasdev.elecapp.features.inspections.domain.InspectionUnverifiedItem
import com.matiasdev.elecapp.features.inspections.domain.InspectionValidation
import com.matiasdev.elecapp.features.inspections.domain.MeasurementOrigin
import com.matiasdev.elecapp.features.inspections.domain.PillarMeasurement
import com.matiasdev.elecapp.features.inspections.domain.PillarMeasurementType
import com.matiasdev.elecapp.features.inspections.domain.PillarInspection
import com.matiasdev.elecapp.features.inspections.domain.PropertyType
import com.matiasdev.elecapp.features.inspections.domain.ProtectionCompatibility
import com.matiasdev.elecapp.features.inspections.domain.SupplyType
import com.matiasdev.elecapp.features.inspections.domain.UnverifiedItemType
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

data class PillarInspectionUiState(
    val isLoading: Boolean = true,
    val scope: InspectionScope = InspectionScope.GENERAL_ASSESSMENT,
    val reviewStatus: InspectionSectionReviewStatus = InspectionSectionReviewStatus.REVIEWED,
    val addToUnverified: Boolean = false,
    val exists: Boolean? = null,
    val propertyType: PropertyType = PropertyType.UNKNOWN,
    val propertyTypeOther: String = "",
    val supplyType: SupplyType = SupplyType.UNKNOWN,
    val accessible: AccessStatus = AccessStatus.PARTIAL,
    val generalCondition: GeneralCondition = GeneralCondition.NOT_ASSESSED,
    val mainBreakerPresent: YesNoUnknown = YesNoUnknown.UNKNOWN,
    val mainBreakerAmps: String = "",
    val mainBreakerOtherAmps: String = "",
    val differentialPresent: YesNoUnknown = YesNoUnknown.UNKNOWN,
    val differentialRatedAmps: String = "",
    val differentialOtherRatedAmps: String = "",
    val differentialSensitivityMa: String = "",
    val differentialOtherSensitivityMa: String = "",
    val differentialTestResult: DifferentialTestResult = DifferentialTestResult.NOT_TESTED,
    val conductorSectionMm2: String = "",
    val conductorOtherSectionMm2: String = "",
    val conductorMaterial: ConductorMaterial = ConductorMaterial.UNKNOWN,
    val conductorMaterialOther: String = "",
    val conductorCondition: ConductorCondition = ConductorCondition.NOT_ASSESSED,
    val neutralIdentified: YesNoUnknown = YesNoUnknown.UNKNOWN,
    val groundingVisible: YesNoUnknown = YesNoUnknown.UNKNOWN,
    val protectionCompatibility: ProtectionCompatibility = ProtectionCompatibility.NOT_ASSESSED,
    val protectionCompatibilityNotes: String = "",
    val notes: String = "",
    val measurements: List<PillarMeasurement> = emptyList(),
    val editingMeasurementId: String? = null,
    val measurementType: PillarMeasurementType = PillarMeasurementType.SINGLE_PHASE_VOLTAGE_LN,
    val measurementValue: String = "",
    val measurementOrigin: MeasurementOrigin = MeasurementOrigin.MEASURED,
    val status: InspectionStatus = InspectionStatus.DRAFT,
    val ampError: String? = null,
    val sectionError: String? = null,
    val measurementError: String? = null,
    val saved: Boolean = false,
)

class PillarInspectionViewModel(
    private val repository: InspectionRepository,
    private val inspectionId: String,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {
    private val _uiState = MutableStateFlow(PillarInspectionUiState())
    val uiState: StateFlow<PillarInspectionUiState> = _uiState.asStateFlow()
    private var createdAt: Instant = Instant.now()

    init {
        viewModelScope.launch(ioDispatcher) {
            val aggregate = repository.findAggregate(inspectionId)
            val pillar = aggregate?.pillar
            createdAt = pillar?.createdAt ?: Instant.now()
            val scope = aggregate?.inspection?.scope ?: InspectionScope.GENERAL_ASSESSMENT
            _uiState.update {
                it.copy(
                    isLoading = false,
                    scope = scope,
                    reviewStatus = pillar?.reviewStatus ?: InspectionSectionReviewStatus.REVIEWED,
                    exists = pillar?.exists,
                    propertyType = pillar?.propertyType ?: aggregate?.inspection?.propertyType ?: it.propertyType,
                    propertyTypeOther = pillar?.propertyTypeOther.orEmpty(),
                    supplyType = pillar?.supplyType ?: aggregate?.inspection?.supplyType ?: it.supplyType,
                    accessible = pillar?.accessible ?: if (scope == InspectionScope.VISUAL_INSPECTION) AccessStatus.UNKNOWN else it.accessible,
                    generalCondition = pillar?.generalCondition ?: it.generalCondition,
                    mainBreakerPresent = pillar?.mainBreakerPresent ?: it.mainBreakerPresent,
                    mainBreakerAmps = pillar?.mainBreakerAmps?.toString().orEmpty(),
                    mainBreakerOtherAmps = pillar?.mainBreakerOtherAmps?.toString().orEmpty(),
                    differentialPresent = pillar?.differentialPresent ?: it.differentialPresent,
                    differentialRatedAmps = pillar?.differentialRatedAmps?.toString().orEmpty(),
                    differentialOtherRatedAmps = pillar?.differentialOtherRatedAmps?.toString().orEmpty(),
                    differentialSensitivityMa = pillar?.differentialSensitivityMa?.toString().orEmpty(),
                    differentialOtherSensitivityMa = pillar?.differentialOtherSensitivityMa?.toString().orEmpty(),
                    differentialTestResult = pillar?.differentialTestResult ?: it.differentialTestResult,
                    conductorSectionMm2 = pillar?.conductorSectionMm2?.toString().orEmpty(),
                    conductorOtherSectionMm2 = pillar?.conductorOtherSectionMm2?.toString().orEmpty(),
                    conductorMaterial = pillar?.conductorMaterial ?: it.conductorMaterial,
                    conductorMaterialOther = pillar?.conductorMaterialOther.orEmpty(),
                    conductorCondition = pillar?.conductorCondition ?: it.conductorCondition,
                    neutralIdentified = pillar?.neutralIdentified ?: it.neutralIdentified,
                    groundingVisible = pillar?.groundingVisible ?: it.groundingVisible,
                    protectionCompatibility = pillar?.protectionCompatibility ?: it.protectionCompatibility,
                    protectionCompatibilityNotes = pillar?.protectionCompatibilityNotes.orEmpty(),
                    notes = pillar?.notes.orEmpty(),
                    measurements = aggregate?.pillarMeasurements.orEmpty(),
                    status = aggregate?.inspection?.status ?: it.status,
                )
            }
        }
    }

    fun update(transform: PillarInspectionUiState.() -> PillarInspectionUiState) {
        _uiState.update { it.transform().normalized().copy(saved = false, ampError = null, sectionError = null, measurementError = null) }
        if (!_uiState.value.isLoading && _uiState.value.status == InspectionStatus.DRAFT) save()
    }

    fun save() {
        val state = _uiState.value
        val amps = state.mainBreakerAmps.toIntOrNull()
        val otherAmps = state.mainBreakerOtherAmps.toIntOrNull()
        val differentialRatedAmps = state.differentialRatedAmps.toIntOrNull()
        val differentialOtherRatedAmps = state.differentialOtherRatedAmps.toIntOrNull()
        val differentialSensitivityMa = state.differentialSensitivityMa.toIntOrNull()
        val differentialOtherSensitivityMa = state.differentialOtherSensitivityMa.toIntOrNull()
        val section = state.conductorSectionMm2.replace(",", ".").toDoubleOrNull()
        val otherSection = state.conductorOtherSectionMm2.replace(",", ".").toDoubleOrNull()
        val ampError = InspectionValidation.validatePositiveInt(amps ?: otherAmps, "El amperaje")
        val sectionError = InspectionValidation.validatePositiveDouble(section ?: otherSection, "La sección")
        if (ampError != null || sectionError != null) {
            _uiState.update { it.copy(ampError = ampError, sectionError = sectionError) }
            return
        }
        viewModelScope.launch(ioDispatcher) {
            val now = Instant.now()
            repository.savePillar(
                PillarInspection(
                    inspectionId = inspectionId,
                    reviewStatus = state.reviewStatus,
                    exists = if (state.reviewStatus == InspectionSectionReviewStatus.REVIEWED) true else null,
                    propertyType = state.propertyType,
                    propertyTypeOther = state.propertyTypeOther.trim().ifBlank { null },
                    supplyType = state.supplyType,
                    accessible = state.accessible,
                    generalCondition = state.generalCondition,
                    mainBreakerPresent = state.mainBreakerPresent,
                    mainBreakerAmps = amps,
                    mainBreakerOtherAmps = otherAmps,
                    differentialPresent = state.differentialPresent,
                    differentialRatedAmps = differentialRatedAmps,
                    differentialOtherRatedAmps = differentialOtherRatedAmps,
                    differentialSensitivityMa = differentialSensitivityMa,
                    differentialOtherSensitivityMa = differentialOtherSensitivityMa,
                    differentialTestResult = state.differentialTestResult,
                    conductorSectionMm2 = section,
                    conductorOtherSectionMm2 = otherSection,
                    conductorMaterial = state.conductorMaterial,
                    conductorMaterialOther = state.conductorMaterialOther.trim().ifBlank { null },
                    conductorCondition = state.conductorCondition,
                    neutralIdentified = YesNoUnknown.UNKNOWN,
                    groundingVisible = YesNoUnknown.UNKNOWN,
                    protectionCompatibility = state.protectionCompatibility,
                    protectionCompatibilityNotes = state.protectionCompatibilityNotes.trim().ifBlank { null },
                    notes = state.notes.trim().ifBlank { null },
                    createdAt = createdAt,
                    updatedAt = now,
                ),
            )
            saveUnverifiedReferenceIfRequested(state)
            _uiState.update { it.copy(saved = true) }
        }
    }

    fun editMeasurement(measurement: PillarMeasurement) {
        _uiState.update {
            it.copy(
                editingMeasurementId = measurement.id,
                measurementType = measurement.type,
                measurementValue = measurement.value?.toString().orEmpty(),
                measurementOrigin = measurement.origin,
                measurementError = null,
            )
        }
    }

    fun updateMeasurementDraft(
        type: PillarMeasurementType = _uiState.value.measurementType,
        value: String = _uiState.value.measurementValue,
        origin: MeasurementOrigin = _uiState.value.measurementOrigin,
    ) {
        _uiState.update {
            it.copy(
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
            val measurement = PillarMeasurement(
                id = state.editingMeasurementId ?: UUID.randomUUID().toString(),
                inspectionId = inspectionId,
                type = state.measurementType,
                value = if (state.measurementOrigin == MeasurementOrigin.NOT_VERIFIED) null else value,
                unit = state.measurementType.unit,
                origin = state.measurementOrigin,
                sortOrder = state.measurements.maxOfOrNull { it.sortOrder }?.plus(1) ?: 0,
                createdAt = state.measurements.firstOrNull { it.id == state.editingMeasurementId }?.createdAt ?: now,
                updatedAt = now,
                isDeleted = false,
            )
            repository.savePillarMeasurement(measurement)
            val aggregate = repository.findAggregate(inspectionId)
            _uiState.update {
                it.copy(
                    measurements = aggregate?.pillarMeasurements.orEmpty(),
                    editingMeasurementId = null,
                    measurementValue = "",
                    measurementError = null,
                    saved = true,
                )
            }
        }
    }

    fun deleteMeasurement(id: String) {
        viewModelScope.launch(ioDispatcher) {
            repository.softDeletePillarMeasurement(id)
            val aggregate = repository.findAggregate(inspectionId)
            _uiState.update { it.copy(measurements = aggregate?.pillarMeasurements.orEmpty(), saved = true) }
        }
    }

    fun cancelMeasurementEdit() {
        _uiState.update { it.copy(editingMeasurementId = null, measurementValue = "", measurementError = null) }
    }

    private suspend fun saveUnverifiedReferenceIfRequested(state: PillarInspectionUiState) {
        if (
            state.scope != InspectionScope.VISUAL_INSPECTION ||
            state.reviewStatus != InspectionSectionReviewStatus.NOT_VERIFIED ||
            !state.addToUnverified
        ) {
            return
        }
        val aggregate = repository.findAggregate(inspectionId) ?: return
        val existing = aggregate.unverifiedItems
        if (existing.any { it.type == UnverifiedItemType.PILLAR_NOT_ACCESSIBLE }) return
        val now = Instant.now()
        repository.saveUnverifiedItems(
            inspectionId,
            existing + InspectionUnverifiedItem(
                id = UUID.randomUUID().toString(),
                inspectionId = inspectionId,
                type = UnverifiedItemType.PILLAR_NOT_ACCESSIBLE,
                description = "Pilar o acometida no verificados.",
                createdAt = now,
                updatedAt = now,
                isDeleted = false,
            ),
        )
    }
}

private val PillarMeasurementType.unit: String
    get() = when (this) {
        PillarMeasurementType.SINGLE_PHASE_VOLTAGE_LN,
        PillarMeasurementType.VOLTAGE_L1_N,
        PillarMeasurementType.VOLTAGE_L2_N,
        PillarMeasurementType.VOLTAGE_L3_N,
        PillarMeasurementType.VOLTAGE_L1_L2,
        PillarMeasurementType.VOLTAGE_L2_L3,
        PillarMeasurementType.VOLTAGE_L3_L1 -> "V"
        PillarMeasurementType.SINGLE_PHASE_CURRENT,
        PillarMeasurementType.CURRENT_L1,
        PillarMeasurementType.CURRENT_L2,
        PillarMeasurementType.CURRENT_L3,
        PillarMeasurementType.CURRENT_NEUTRAL -> "A"
    }

private fun PillarInspectionUiState.normalized(): PillarInspectionUiState {
    if (reviewStatus != InspectionSectionReviewStatus.REVIEWED) {
        return copy(
            exists = null,
            accessible = AccessStatus.UNKNOWN,
            generalCondition = GeneralCondition.NOT_ASSESSED,
            mainBreakerPresent = YesNoUnknown.UNKNOWN,
            mainBreakerAmps = "",
            mainBreakerOtherAmps = "",
            differentialPresent = YesNoUnknown.UNKNOWN,
            differentialRatedAmps = "",
            differentialOtherRatedAmps = "",
            differentialSensitivityMa = "",
            differentialOtherSensitivityMa = "",
            differentialTestResult = DifferentialTestResult.NOT_TESTED,
            conductorSectionMm2 = "",
            conductorOtherSectionMm2 = "",
            conductorMaterial = ConductorMaterial.UNKNOWN,
            conductorMaterialOther = "",
            conductorCondition = ConductorCondition.NOT_ASSESSED,
            neutralIdentified = YesNoUnknown.UNKNOWN,
            groundingVisible = YesNoUnknown.UNKNOWN,
            protectionCompatibility = ProtectionCompatibility.NOT_ASSESSED,
            protectionCompatibilityNotes = "",
        )
    }
    return copy(
        exists = true,
        propertyTypeOther = if (propertyType == PropertyType.OTHER) propertyTypeOther else "",
        mainBreakerAmps = if (mainBreakerPresent == YesNoUnknown.YES) mainBreakerAmps else "",
        mainBreakerOtherAmps = if (mainBreakerPresent == YesNoUnknown.YES) mainBreakerOtherAmps else "",
        differentialRatedAmps = if (differentialPresent == YesNoUnknown.YES) differentialRatedAmps else "",
        differentialOtherRatedAmps = if (differentialPresent == YesNoUnknown.YES) differentialOtherRatedAmps else "",
        differentialSensitivityMa = if (differentialPresent == YesNoUnknown.YES) differentialSensitivityMa else "",
        differentialOtherSensitivityMa = if (differentialPresent == YesNoUnknown.YES) differentialOtherSensitivityMa else "",
        differentialTestResult = if (differentialPresent == YesNoUnknown.YES) differentialTestResult else DifferentialTestResult.NOT_APPLICABLE,
        conductorOtherSectionMm2 = conductorOtherSectionMm2.takeIf { conductorSectionMm2 == OTHER_VALUE }.orEmpty(),
        conductorMaterialOther = if (conductorMaterial == ConductorMaterial.OTHER) conductorMaterialOther else "",
    )
}

const val OTHER_VALUE = "OTHER"

class PillarInspectionViewModelFactory(
    private val repository: InspectionRepository,
    private val inspectionId: String,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PillarInspectionViewModel(repository, inspectionId) as T
    }
}
