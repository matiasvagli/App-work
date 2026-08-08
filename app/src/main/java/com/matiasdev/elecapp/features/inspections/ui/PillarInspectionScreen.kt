package com.matiasdev.elecapp.features.inspections.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.matiasdev.elecapp.features.inspections.data.InspectionRepository
import com.matiasdev.elecapp.features.inspections.domain.AccessStatus
import com.matiasdev.elecapp.features.inspections.domain.ConductorCondition
import com.matiasdev.elecapp.features.inspections.domain.ConductorMaterial
import com.matiasdev.elecapp.features.inspections.domain.DifferentialTestResult
import com.matiasdev.elecapp.features.inspections.domain.GeneralCondition
import com.matiasdev.elecapp.features.inspections.domain.InspectionSectionReviewStatus
import com.matiasdev.elecapp.features.inspections.domain.InspectionScope
import com.matiasdev.elecapp.features.inspections.domain.InspectionStatus
import com.matiasdev.elecapp.features.inspections.domain.MeasurementOrigin
import com.matiasdev.elecapp.features.inspections.domain.PillarMeasurement
import com.matiasdev.elecapp.features.inspections.domain.PillarMeasurementType
import com.matiasdev.elecapp.features.inspections.domain.PropertyType
import com.matiasdev.elecapp.features.inspections.domain.ProtectionCompatibility
import com.matiasdev.elecapp.features.inspections.domain.SupplyType
import com.matiasdev.elecapp.features.inspections.domain.YesNoUnknown
import com.matiasdev.elecapp.features.inspections.summary.label

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PillarInspectionScreen(
    repository: InspectionRepository,
    inspectionId: String,
    onBackClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onHomeClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PillarInspectionViewModel = viewModel(factory = PillarInspectionViewModelFactory(repository, inspectionId)),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Pilar y acometida") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver") }
                },
            )
        },
    ) { padding ->
        if (uiState.isLoading) {
            CircularProgressIndicator(Modifier.padding(padding).padding(24.dp))
        } else {
            PillarForm(uiState, viewModel, onPreviousClick, onNextClick, onHomeClick, Modifier.padding(padding))
        }
    }
}

@Composable
private fun PillarForm(
    uiState: PillarInspectionUiState,
    viewModel: PillarInspectionViewModel,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onHomeClick: () -> Unit,
    modifier: Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        InspectionFormBlock("Pilar y acometida") {
            InspectionDropdownField("Estado de la sección", uiState.reviewStatus, InspectionSectionReviewStatus.entries.toList(), InspectionSectionReviewStatus::label) {
                viewModel.update { copy(reviewStatus = it) }
            }
            if (uiState.reviewStatus == InspectionSectionReviewStatus.NOT_VERIFIED && uiState.scope == InspectionScope.VISUAL_INSPECTION) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = uiState.addToUnverified,
                        onCheckedChange = { checked -> viewModel.update { copy(addToUnverified = checked) } },
                    )
                    Text("Agregar a elementos no verificados")
                }
            }
        }
        if (uiState.reviewStatus != InspectionSectionReviewStatus.REVIEWED) {
            SavedIndicator(uiState)
            InspectionSectionNavigation(onPreviousClick = onPreviousClick, onNextClick = onNextClick, onHomeClick = onHomeClick)
            return@Column
        }
        InspectionFormBlock("Características del suministro") {
            InspectionDropdownField("Tipo de inmueble", uiState.propertyType, PropertyType.entries.toList(), PropertyType::label) {
                viewModel.update { copy(propertyType = it) }
            }
            if (uiState.propertyType == PropertyType.OTHER) {
                InspectionTextField("Otro tipo de inmueble", uiState.propertyTypeOther, { viewModel.update { copy(propertyTypeOther = it) } })
            }
            InspectionDropdownField("Tipo de suministro", uiState.supplyType, SupplyType.entries.toList(), SupplyType::label) {
                viewModel.update { copy(supplyType = it) }
            }
        }
        InspectionFormBlock("Acceso y estado") {
            InspectionDropdownField("Accesible", uiState.accessible, AccessStatus.entries.toList(), AccessStatus::label) {
                viewModel.update { copy(accessible = it) }
            }
            InspectionDropdownField("Estado general", uiState.generalCondition, listOf(GeneralCondition.GOOD, GeneralCondition.FAIR, GeneralCondition.POOR, GeneralCondition.NOT_ASSESSED), GeneralCondition::label) {
                viewModel.update { copy(generalCondition = it) }
            }
        }
        InspectionFormBlock("Mediciones opcionales") {
            MeasurementForm(uiState, viewModel)
            MeasurementSummary(uiState.measurements, viewModel)
        }
        InspectionFormBlock("Protecciones") {
            InspectionDropdownField("Térmica principal", uiState.mainBreakerPresent, YesNoUnknown.entries.toList(), YesNoUnknown::label) {
                viewModel.update { copy(mainBreakerPresent = it) }
            }
            if (uiState.mainBreakerPresent == YesNoUnknown.YES) {
                InspectionDropdownField("Corriente nominal", uiState.mainBreakerAmps, breakerOptions, ::ampOptionLabel) {
                    viewModel.update { copy(mainBreakerAmps = it, mainBreakerOtherAmps = "") }
                }
                if (uiState.mainBreakerAmps == OTHER_VALUE) {
                    NumberField("Otra corriente nominal", uiState.mainBreakerOtherAmps, { viewModel.update { copy(mainBreakerOtherAmps = it.filter(Char::isDigit)) } }, uiState.ampError)
                }
            }
            InspectionDropdownField("Interruptor diferencial en el pilar", uiState.differentialPresent, YesNoUnknown.entries.toList(), YesNoUnknown::label) {
                viewModel.update { copy(differentialPresent = it) }
            }
            if (uiState.differentialPresent == YesNoUnknown.YES) {
                InspectionDropdownField("Corriente nominal", uiState.differentialRatedAmps, differentialAmpOptions, ::ampOptionLabel) {
                    viewModel.update { copy(differentialRatedAmps = it, differentialOtherRatedAmps = "") }
                }
                if (uiState.differentialRatedAmps == OTHER_VALUE) {
                    NumberField("Otra corriente nominal", uiState.differentialOtherRatedAmps, { viewModel.update { copy(differentialOtherRatedAmps = it.filter(Char::isDigit)) } }, null)
                }
                InspectionDropdownField("Sensibilidad diferencial", uiState.differentialSensitivityMa, differentialSensitivityOptions, ::sensitivityOptionLabel) {
                    viewModel.update { copy(differentialSensitivityMa = it, differentialOtherSensitivityMa = "") }
                }
                if (uiState.differentialSensitivityMa == OTHER_VALUE) {
                    NumberField("Otra sensibilidad", uiState.differentialOtherSensitivityMa, { viewModel.update { copy(differentialOtherSensitivityMa = it.filter(Char::isDigit)) } }, null)
                }
                InspectionDropdownField("Prueba manual", uiState.differentialTestResult, DifferentialTestResult.entries.toList(), DifferentialTestResult::label) {
                    viewModel.update { copy(differentialTestResult = it) }
                }
            }
        }
        InspectionFormBlock("Conductores observados") {
            InspectionDropdownField("Sección", uiState.conductorSectionMm2, conductorSectionOptions, ::sectionOptionLabel) {
                viewModel.update { copy(conductorSectionMm2 = it, conductorOtherSectionMm2 = "") }
            }
            if (uiState.conductorSectionMm2 == OTHER_VALUE) {
                DecimalField("Otra sección mm²", uiState.conductorOtherSectionMm2, { viewModel.update { copy(conductorOtherSectionMm2 = it) } }, uiState.sectionError)
            }
            InspectionDropdownField("Material", uiState.conductorMaterial, ConductorMaterial.entries.toList(), ConductorMaterial::label) {
                viewModel.update { copy(conductorMaterial = it) }
            }
            if (uiState.conductorMaterial == ConductorMaterial.OTHER) {
                InspectionTextField("Otro material", uiState.conductorMaterialOther, { viewModel.update { copy(conductorMaterialOther = it) } })
            }
            InspectionDropdownField("Estado", uiState.conductorCondition, conductorConditionOptions, ConductorCondition::label) {
                viewModel.update { copy(conductorCondition = it) }
            }
        }
        InspectionFormBlock("Verificaciones visibles") {
            InspectionDropdownField("Compatibilidad protección/conductor", uiState.protectionCompatibility, ProtectionCompatibility.entries.toList(), ProtectionCompatibility::label) {
                viewModel.update { copy(protectionCompatibility = it) }
            }
            InspectionTextField("Observación de compatibilidad", uiState.protectionCompatibilityNotes, { viewModel.update { copy(protectionCompatibilityNotes = it) } }, minLines = 2)
            InspectionTextField("Observaciones", uiState.notes, { viewModel.update { copy(notes = it) } }, minLines = 4)
        }
        SavedIndicator(uiState)
        InspectionSectionNavigation(onPreviousClick = onPreviousClick, onNextClick = onNextClick, onHomeClick = onHomeClick)
    }
}

@Composable
private fun SavedIndicator(uiState: PillarInspectionUiState) {
    if (uiState.saved && uiState.status == InspectionStatus.DRAFT) {
        Text("Cambios guardados automáticamente")
    }
}

@Composable
private fun MeasurementForm(uiState: PillarInspectionUiState, viewModel: PillarInspectionViewModel) {
    val types = when (uiState.supplyType) {
        SupplyType.THREE_PHASE -> threePhaseMeasurementTypes
        else -> singlePhaseMeasurementTypes
    }
    InspectionDropdownField("Medición", uiState.measurementType.takeIf { it in types } ?: types.first(), types, PillarMeasurementType::label) {
        viewModel.updateMeasurementDraft(type = it)
    }
    InspectionDropdownField("Origen", uiState.measurementOrigin, MeasurementOrigin.entries.toList(), MeasurementOrigin::label) {
        viewModel.updateMeasurementDraft(origin = it)
    }
    if (uiState.measurementOrigin != MeasurementOrigin.NOT_VERIFIED) {
        DecimalField("Valor", uiState.measurementValue, { viewModel.updateMeasurementDraft(value = it) }, uiState.measurementError)
    }
    if (uiState.status != InspectionStatus.DRAFT) {
        Text("Relevamiento finalizado. Reabrilo para agregar o editar mediciones.")
        Button(onClick = viewModel::reopenInspection) {
            Text("Reabrir relevamiento")
        }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onClick = viewModel::saveMeasurement, enabled = uiState.status == InspectionStatus.DRAFT) {
            Text(if (uiState.editingMeasurementId == null) "Agregar medición" else "Actualizar medición")
        }
        if (uiState.editingMeasurementId != null) {
            TextButton(onClick = viewModel::cancelMeasurementEdit) {
                Text("Cancelar")
            }
        }
    }
}

@Composable
private fun MeasurementSummary(measurements: List<PillarMeasurement>, viewModel: PillarInspectionViewModel) {
    if (measurements.isEmpty()) {
        Text("Sin mediciones registradas")
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        measurements.forEach { measurement ->
            HorizontalDivider()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("${measurement.type.label()}: ${measurement.formatValue()} (${measurement.origin.label()})")
                }
                TextButton(onClick = { viewModel.editMeasurement(measurement) }) { Text("Editar") }
                TextButton(onClick = { viewModel.deleteMeasurement(measurement.id) }) { Text("Eliminar") }
            }
        }
    }
}

@Composable
private fun NumberField(label: String, value: String, onChange: (String) -> Unit, error: String?) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth(),
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
    )
}

@Composable
private fun DecimalField(label: String, value: String, onChange: (String) -> Unit, error: String?) {
    OutlinedTextField(
        value = value,
        onValueChange = { onChange(it.filter { char -> char.isDigit() || char == '.' || char == ',' }) },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth(),
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
    )
}

private val breakerOptions = listOf("", "10", "16", "20", "25", "32", "40", "50", "63", OTHER_VALUE)
private val differentialAmpOptions = listOf("", "25", "40", "63", OTHER_VALUE)
private val differentialSensitivityOptions = listOf("", "30", "100", "300", OTHER_VALUE)
private val conductorSectionOptions = listOf("", "1.5", "2.5", "4", "6", "10", "16", "25", OTHER_VALUE)
private val conductorConditionOptions = listOf(
    ConductorCondition.GOOD,
    ConductorCondition.FAIR,
    ConductorCondition.DETERIORATED,
    ConductorCondition.VISIBLE_RISK,
    ConductorCondition.NOT_ASSESSED,
)
private val singlePhaseMeasurementTypes = listOf(
    PillarMeasurementType.SINGLE_PHASE_VOLTAGE_LN,
    PillarMeasurementType.SINGLE_PHASE_CURRENT,
)
private val threePhaseMeasurementTypes = listOf(
    PillarMeasurementType.VOLTAGE_L1_N,
    PillarMeasurementType.VOLTAGE_L2_N,
    PillarMeasurementType.VOLTAGE_L3_N,
    PillarMeasurementType.VOLTAGE_L1_L2,
    PillarMeasurementType.VOLTAGE_L2_L3,
    PillarMeasurementType.VOLTAGE_L3_L1,
    PillarMeasurementType.CURRENT_L1,
    PillarMeasurementType.CURRENT_L2,
    PillarMeasurementType.CURRENT_L3,
    PillarMeasurementType.CURRENT_NEUTRAL,
)

private fun ampOptionLabel(value: String): String = when (value) {
    "" -> "No verificada"
    OTHER_VALUE -> "Otro"
    else -> "$value A"
}

private fun sensitivityOptionLabel(value: String): String = when (value) {
    "" -> "No verificada"
    OTHER_VALUE -> "Otro"
    else -> "$value mA"
}

private fun sectionOptionLabel(value: String): String = when (value) {
    "" -> "No verificada"
    OTHER_VALUE -> "Otro"
    else -> "${value.replace(".", ",")} mm²"
}

private fun PillarMeasurement.formatValue(): String {
    return value?.let {
        val whole = it.toLong()
        val number = if (it == whole.toDouble()) whole.toString() else it.toString().replace(".", ",")
        "$number $unit"
    } ?: "No verificado"
}
