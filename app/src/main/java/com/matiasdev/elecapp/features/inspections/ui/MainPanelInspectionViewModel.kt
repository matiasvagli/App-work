package com.matiasdev.elecapp.features.inspections.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.matiasdev.elecapp.features.inspections.data.InspectionRepository
import com.matiasdev.elecapp.features.inspections.domain.AccessStatus
import com.matiasdev.elecapp.features.inspections.domain.DifferentialTestResult
import com.matiasdev.elecapp.features.inspections.domain.GeneralCondition
import com.matiasdev.elecapp.features.inspections.domain.InspectionStatus
import com.matiasdev.elecapp.features.inspections.domain.InspectionValidation
import com.matiasdev.elecapp.features.inspections.domain.MainPanelInspection
import com.matiasdev.elecapp.features.inspections.domain.ProtectionCompatibility
import com.matiasdev.elecapp.features.inspections.domain.YesNoPartialUnknown
import com.matiasdev.elecapp.features.inspections.domain.YesNoUnknown
import java.time.Instant
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MainPanelInspectionUiState(
    val isLoading: Boolean = true,
    val accessible: AccessStatus = AccessStatus.PARTIAL,
    val generalCondition: GeneralCondition = GeneralCondition.NOT_ASSESSED,
    val differentialPresent: YesNoUnknown = YesNoUnknown.UNKNOWN,
    val differentialRatedAmps: String = "",
    val differentialSensitivityMa: String = "",
    val differentialTestResult: DifferentialTestResult = DifferentialTestResult.NOT_TESTED,
    val circuitCount: String = "",
    val circuitsIdentified: YesNoPartialUnknown = YesNoPartialUnknown.UNKNOWN,
    val neutralBarPresent: YesNoUnknown = YesNoUnknown.UNKNOWN,
    val groundBarPresent: YesNoUnknown = YesNoUnknown.UNKNOWN,
    val neutralAndGroundSeparated: YesNoUnknown = YesNoUnknown.UNKNOWN,
    val improvisedConnections: YesNoUnknown = YesNoUnknown.UNKNOWN,
    val mixedOrIncorrectColors: YesNoUnknown = YesNoUnknown.UNKNOWN,
    val overheatingSigns: YesNoUnknown = YesNoUnknown.UNKNOWN,
    val protectionCompatibility: ProtectionCompatibility = ProtectionCompatibility.NOT_ASSESSED,
    val notes: String = "",
    val status: InspectionStatus = InspectionStatus.DRAFT,
    val ratedAmpsError: String? = null,
    val sensitivityError: String? = null,
    val circuitCountError: String? = null,
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
            _uiState.update {
                it.copy(
                    isLoading = false,
                    accessible = panel?.accessible ?: it.accessible,
                    generalCondition = panel?.generalCondition ?: it.generalCondition,
                    differentialPresent = panel?.differentialPresent ?: it.differentialPresent,
                    differentialRatedAmps = panel?.differentialRatedAmps?.toString().orEmpty(),
                    differentialSensitivityMa = panel?.differentialSensitivityMa?.toString().orEmpty(),
                    differentialTestResult = panel?.differentialTestResult ?: it.differentialTestResult,
                    circuitCount = panel?.circuitCount?.toString().orEmpty(),
                    circuitsIdentified = panel?.circuitsIdentified ?: it.circuitsIdentified,
                    neutralBarPresent = panel?.neutralBarPresent ?: it.neutralBarPresent,
                    groundBarPresent = panel?.groundBarPresent ?: it.groundBarPresent,
                    neutralAndGroundSeparated = panel?.neutralAndGroundSeparated ?: it.neutralAndGroundSeparated,
                    improvisedConnections = panel?.improvisedConnections ?: it.improvisedConnections,
                    mixedOrIncorrectColors = panel?.mixedOrIncorrectColors ?: it.mixedOrIncorrectColors,
                    overheatingSigns = panel?.overheatingSigns ?: it.overheatingSigns,
                    protectionCompatibility = panel?.protectionCompatibility ?: it.protectionCompatibility,
                    notes = panel?.notes.orEmpty(),
                    status = aggregate?.inspection?.status ?: it.status,
                )
            }
        }
    }

    fun update(transform: MainPanelInspectionUiState.() -> MainPanelInspectionUiState) {
        _uiState.update {
            it.transform().copy(saved = false, ratedAmpsError = null, sensitivityError = null, circuitCountError = null)
        }
    }

    fun save() {
        val state = _uiState.value
        val ratedAmps = state.differentialRatedAmps.toIntOrNull()
        val sensitivity = state.differentialSensitivityMa.toIntOrNull()
        val circuits = state.circuitCount.toIntOrNull()
        val ratedError = InspectionValidation.validatePositiveInt(ratedAmps, "La corriente nominal")
        val sensitivityError = InspectionValidation.validatePositiveInt(sensitivity, "La sensibilidad")
        val circuitsError = InspectionValidation.validatePositiveInt(circuits, "La cantidad de circuitos")
        if (ratedError != null || sensitivityError != null || circuitsError != null) {
            _uiState.update { it.copy(ratedAmpsError = ratedError, sensitivityError = sensitivityError, circuitCountError = circuitsError) }
            return
        }
        viewModelScope.launch(ioDispatcher) {
            val now = Instant.now()
            repository.saveMainPanel(
                MainPanelInspection(
                    inspectionId = inspectionId,
                    accessible = state.accessible,
                    generalCondition = state.generalCondition,
                    differentialPresent = state.differentialPresent,
                    differentialRatedAmps = ratedAmps,
                    differentialSensitivityMa = sensitivity,
                    differentialTestResult = state.differentialTestResult,
                    circuitCount = circuits,
                    circuitsIdentified = state.circuitsIdentified,
                    neutralBarPresent = state.neutralBarPresent,
                    groundBarPresent = state.groundBarPresent,
                    neutralAndGroundSeparated = state.neutralAndGroundSeparated,
                    improvisedConnections = state.improvisedConnections,
                    mixedOrIncorrectColors = state.mixedOrIncorrectColors,
                    overheatingSigns = state.overheatingSigns,
                    protectionCompatibility = state.protectionCompatibility,
                    notes = state.notes.trim().ifBlank { null },
                    createdAt = createdAt,
                    updatedAt = now,
                ),
            )
            _uiState.update { it.copy(saved = true) }
        }
    }
}

class MainPanelInspectionViewModelFactory(
    private val repository: InspectionRepository,
    private val inspectionId: String,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainPanelInspectionViewModel(repository, inspectionId) as T
    }
}
