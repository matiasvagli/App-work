package com.matiasdev.elecapp.features.inspections.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.matiasdev.elecapp.features.inspections.data.InspectionRepository
import com.matiasdev.elecapp.features.inspections.domain.AccessStatus
import com.matiasdev.elecapp.features.inspections.domain.ConductorCondition
import com.matiasdev.elecapp.features.inspections.domain.ConductorMaterial
import com.matiasdev.elecapp.features.inspections.domain.GeneralCondition
import com.matiasdev.elecapp.features.inspections.domain.InspectionSectionReviewStatus
import com.matiasdev.elecapp.features.inspections.domain.InspectionScope
import com.matiasdev.elecapp.features.inspections.domain.InspectionStatus
import com.matiasdev.elecapp.features.inspections.domain.InspectionUnverifiedItem
import com.matiasdev.elecapp.features.inspections.domain.InspectionValidation
import com.matiasdev.elecapp.features.inspections.domain.PillarInspection
import com.matiasdev.elecapp.features.inspections.domain.ProtectionCompatibility
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
    val accessible: AccessStatus = AccessStatus.PARTIAL,
    val generalCondition: GeneralCondition = GeneralCondition.NOT_ASSESSED,
    val mainBreakerPresent: YesNoUnknown = YesNoUnknown.UNKNOWN,
    val mainBreakerAmps: String = "",
    val conductorSectionMm2: String = "",
    val conductorMaterial: ConductorMaterial = ConductorMaterial.UNKNOWN,
    val conductorCondition: ConductorCondition = ConductorCondition.NOT_ASSESSED,
    val neutralIdentified: YesNoUnknown = YesNoUnknown.UNKNOWN,
    val groundingVisible: YesNoUnknown = YesNoUnknown.UNKNOWN,
    val protectionCompatibility: ProtectionCompatibility = ProtectionCompatibility.NOT_ASSESSED,
    val notes: String = "",
    val status: InspectionStatus = InspectionStatus.DRAFT,
    val ampError: String? = null,
    val sectionError: String? = null,
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
                    accessible = pillar?.accessible ?: if (scope == InspectionScope.VISUAL_INSPECTION) AccessStatus.UNKNOWN else it.accessible,
                    generalCondition = pillar?.generalCondition ?: it.generalCondition,
                    mainBreakerPresent = pillar?.mainBreakerPresent ?: it.mainBreakerPresent,
                    mainBreakerAmps = pillar?.mainBreakerAmps?.toString().orEmpty(),
                    conductorSectionMm2 = pillar?.conductorSectionMm2?.toString().orEmpty(),
                    conductorMaterial = pillar?.conductorMaterial ?: it.conductorMaterial,
                    conductorCondition = pillar?.conductorCondition ?: it.conductorCondition,
                    neutralIdentified = pillar?.neutralIdentified ?: it.neutralIdentified,
                    groundingVisible = pillar?.groundingVisible ?: it.groundingVisible,
                    protectionCompatibility = pillar?.protectionCompatibility ?: it.protectionCompatibility,
                    notes = pillar?.notes.orEmpty(),
                    status = aggregate?.inspection?.status ?: it.status,
                )
            }
        }
    }

    fun update(transform: PillarInspectionUiState.() -> PillarInspectionUiState) {
        _uiState.update { it.transform().copy(saved = false, ampError = null, sectionError = null) }
    }

    fun save() {
        val state = _uiState.value
        val amps = state.mainBreakerAmps.toIntOrNull()
        val section = state.conductorSectionMm2.replace(",", ".").toDoubleOrNull()
        val ampError = InspectionValidation.validatePositiveInt(amps, "El amperaje")
        val sectionError = InspectionValidation.validatePositiveDouble(section, "La sección")
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
                    exists = state.exists,
                    accessible = state.accessible,
                    generalCondition = state.generalCondition,
                    mainBreakerPresent = state.mainBreakerPresent,
                    mainBreakerAmps = amps,
                    conductorSectionMm2 = section,
                    conductorMaterial = state.conductorMaterial,
                    conductorCondition = state.conductorCondition,
                    neutralIdentified = state.neutralIdentified,
                    groundingVisible = state.groundingVisible,
                    protectionCompatibility = state.protectionCompatibility,
                    notes = state.notes.trim().ifBlank { null },
                    createdAt = createdAt,
                    updatedAt = now,
                ),
            )
            saveUnverifiedReferenceIfRequested(state)
            _uiState.update { it.copy(saved = true) }
        }
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

class PillarInspectionViewModelFactory(
    private val repository: InspectionRepository,
    private val inspectionId: String,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PillarInspectionViewModel(repository, inspectionId) as T
    }
}
