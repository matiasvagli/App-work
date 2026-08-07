package com.matiasdev.elecapp.features.inspections.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.matiasdev.elecapp.features.inspections.data.InspectionRepository
import com.matiasdev.elecapp.features.inspections.domain.GroundingInspection
import com.matiasdev.elecapp.features.inspections.domain.InspectionStatus
import com.matiasdev.elecapp.features.inspections.domain.MeasurementOrigin
import com.matiasdev.elecapp.features.inspections.domain.YesNoUnknown
import java.time.Instant
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GroundingInspectionUiState(
    val loading: Boolean = true,
    val inspectionStatus: InspectionStatus = InspectionStatus.DRAFT,
    val electrodePresent: YesNoUnknown = YesNoUnknown.UNKNOWN,
    val inspectionChamberAccessible: YesNoUnknown = YesNoUnknown.UNKNOWN,
    val mainGroundConductorPresent: YesNoUnknown = YesNoUnknown.UNKNOWN,
    val protectiveConductorContinuity: YesNoUnknown = YesNoUnknown.UNKNOWN,
    val resistanceOhms: String = "",
    val resistanceOrigin: MeasurementOrigin = MeasurementOrigin.NOT_VERIFIED,
    val notes: String = "",
    val resistanceError: String? = null,
    val saved: Boolean = false,
    val errorMessage: String? = null,
)

class GroundingInspectionViewModel(
    private val repository: InspectionRepository,
    private val inspectionId: String,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {
    private val _uiState = MutableStateFlow(GroundingInspectionUiState())
    val uiState: StateFlow<GroundingInspectionUiState> = _uiState.asStateFlow()
    private var createdAt = Instant.now()

    init {
        viewModelScope.launch(ioDispatcher) {
            repository.observeAggregate(inspectionId)
                .catch { error -> _uiState.update { it.copy(loading = false, errorMessage = error.message) } }
                .collect { aggregate ->
                    if (aggregate == null) {
                        _uiState.update { it.copy(loading = false, errorMessage = "Relevamiento no encontrado") }
                        return@collect
                    }
                    val grounding = aggregate.grounding
                    createdAt = grounding?.createdAt ?: createdAt
                    _uiState.update {
                        it.copy(
                            loading = false,
                            inspectionStatus = aggregate.inspection.status,
                            electrodePresent = grounding?.electrodePresent ?: YesNoUnknown.UNKNOWN,
                            inspectionChamberAccessible = grounding?.inspectionChamberAccessible ?: YesNoUnknown.UNKNOWN,
                            mainGroundConductorPresent = grounding?.mainGroundConductorPresent ?: YesNoUnknown.UNKNOWN,
                            protectiveConductorContinuity = grounding?.protectiveConductorContinuity ?: YesNoUnknown.UNKNOWN,
                            resistanceOhms = grounding?.resistanceOhms?.toInputText().orEmpty(),
                            resistanceOrigin = grounding?.resistanceOrigin ?: MeasurementOrigin.NOT_VERIFIED,
                            notes = grounding?.notes.orEmpty(),
                            saved = grounding != null,
                            errorMessage = null,
                        )
                    }
                }
        }
    }

    fun update(transform: GroundingInspectionUiState.() -> GroundingInspectionUiState) {
        _uiState.update { it.transform().copy(saved = false, resistanceError = null) }
        if (!_uiState.value.loading && _uiState.value.inspectionStatus == InspectionStatus.DRAFT) save()
    }

    fun updateResistance(value: String) {
        update { copy(resistanceOhms = value.filter { it.isDigit() || it == ',' || it == '.' }) }
    }

    fun updateResistanceOrigin(origin: MeasurementOrigin) {
        update {
            copy(
                resistanceOrigin = origin,
                resistanceOhms = if (origin == MeasurementOrigin.NOT_VERIFIED) "" else resistanceOhms,
            )
        }
    }

    fun save() {
        val state = _uiState.value
        val resistance = state.resistanceOhms.parseDecimalInput()
        val error = when {
            state.resistanceOrigin == MeasurementOrigin.NOT_VERIFIED -> null
            resistance == null -> "Ingresá la resistencia medida o declarada."
            resistance <= 0.0 -> "La resistencia debe ser mayor que cero."
            else -> null
        }
        if (error != null) {
            _uiState.update { it.copy(resistanceError = error) }
            return
        }
        viewModelScope.launch(ioDispatcher) {
            repository.saveGrounding(
                GroundingInspection(
                    inspectionId = inspectionId,
                    electrodePresent = state.electrodePresent,
                    inspectionChamberAccessible = state.inspectionChamberAccessible,
                    mainGroundConductorPresent = state.mainGroundConductorPresent,
                    protectiveConductorContinuity = state.protectiveConductorContinuity,
                    resistanceOhms = if (state.resistanceOrigin == MeasurementOrigin.NOT_VERIFIED) null else resistance,
                    resistanceOrigin = state.resistanceOrigin,
                    notes = state.notes.trim().ifBlank { null },
                    createdAt = createdAt,
                    updatedAt = Instant.now(),
                ),
            )
            _uiState.update { it.copy(saved = true) }
        }
    }
}

private fun String.parseDecimalInput(): Double? = trim().replace(',', '.').takeIf(String::isNotBlank)?.toDoubleOrNull()

private fun Double.toInputText(): String {
    val whole = toLong()
    return if (this == whole.toDouble()) whole.toString() else toString()
}

class GroundingInspectionViewModelFactory(
    private val repository: InspectionRepository,
    private val inspectionId: String,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        GroundingInspectionViewModel(repository, inspectionId) as T
}
