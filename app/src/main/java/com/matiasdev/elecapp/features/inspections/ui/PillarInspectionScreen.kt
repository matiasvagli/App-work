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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.matiasdev.elecapp.features.inspections.domain.GeneralCondition
import com.matiasdev.elecapp.features.inspections.domain.InspectionSectionReviewStatus
import com.matiasdev.elecapp.features.inspections.domain.InspectionScope
import com.matiasdev.elecapp.features.inspections.domain.InspectionStatus
import com.matiasdev.elecapp.features.inspections.domain.ProtectionCompatibility
import com.matiasdev.elecapp.features.inspections.domain.YesNoUnknown
import com.matiasdev.elecapp.features.inspections.summary.label

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PillarInspectionScreen(
    repository: InspectionRepository,
    inspectionId: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PillarInspectionViewModel = viewModel(factory = PillarInspectionViewModelFactory(repository, inspectionId)),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(uiState.saved) {
        if (uiState.saved) onBackClick()
    }
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
            PillarForm(uiState, viewModel, Modifier.padding(padding))
        }
    }
}

@Composable
private fun PillarForm(uiState: PillarInspectionUiState, viewModel: PillarInspectionViewModel, modifier: Modifier) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        if (uiState.scope == InspectionScope.VISUAL_INSPECTION) {
            InspectionFormBlock("¿Se revisó el pilar o la acometida?") {
                InspectionDropdownField("Respuesta", uiState.reviewStatus, InspectionSectionReviewStatus.entries.toList(), InspectionSectionReviewStatus::label) {
                    viewModel.update { copy(reviewStatus = it) }
                }
                if (uiState.reviewStatus == InspectionSectionReviewStatus.NOT_VERIFIED) {
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
                Button(onClick = viewModel::save, enabled = uiState.status == InspectionStatus.DRAFT, modifier = Modifier.fillMaxWidth()) {
                    Text("Guardar sección")
                }
                return@Column
            }
        }
        InspectionFormBlock("Acceso y estado") {
            InspectionDropdownField("Existe", uiState.exists, listOf(null, true, false), ::existsLabel) {
                viewModel.update { copy(exists = it) }
            }
            InspectionDropdownField("Accesible", uiState.accessible, AccessStatus.entries.toList(), AccessStatus::label) {
                viewModel.update { copy(accessible = it) }
            }
            InspectionDropdownField("Estado general", uiState.generalCondition, GeneralCondition.entries.toList(), GeneralCondition::label) {
                viewModel.update { copy(generalCondition = it) }
            }
        }
        InspectionFormBlock("Protección y conductores") {
            InspectionDropdownField("Térmica principal", uiState.mainBreakerPresent, YesNoUnknown.entries.toList(), YesNoUnknown::label) {
                viewModel.update { copy(mainBreakerPresent = it) }
            }
            NumberField("Amperaje de térmica", uiState.mainBreakerAmps, { viewModel.update { copy(mainBreakerAmps = it.filter(Char::isDigit)) } }, uiState.ampError)
            DecimalField("Sección de conductor mm²", uiState.conductorSectionMm2, { viewModel.update { copy(conductorSectionMm2 = it) } }, uiState.sectionError)
            InspectionDropdownField("Material", uiState.conductorMaterial, ConductorMaterial.entries.toList(), ConductorMaterial::label) {
                viewModel.update { copy(conductorMaterial = it) }
            }
            InspectionDropdownField("Estado de conductores", uiState.conductorCondition, ConductorCondition.entries.toList(), ConductorCondition::label) {
                viewModel.update { copy(conductorCondition = it) }
            }
        }
        InspectionFormBlock("Verificaciones visibles") {
            InspectionDropdownField("Neutro identificado", uiState.neutralIdentified, YesNoUnknown.entries.toList(), YesNoUnknown::label) {
                viewModel.update { copy(neutralIdentified = it) }
            }
            InspectionDropdownField("Puesta a tierra visible", uiState.groundingVisible, YesNoUnknown.entries.toList(), YesNoUnknown::label) {
                viewModel.update { copy(groundingVisible = it) }
            }
            InspectionDropdownField("Compatibilidad protección/conductor", uiState.protectionCompatibility, ProtectionCompatibility.entries.toList(), ProtectionCompatibility::label) {
                viewModel.update { copy(protectionCompatibility = it) }
            }
            InspectionTextField("Notas", uiState.notes, { viewModel.update { copy(notes = it) } }, minLines = 4)
        }
        Button(onClick = viewModel::save, enabled = uiState.status == InspectionStatus.DRAFT, modifier = Modifier.fillMaxWidth()) {
            Text("Guardar sección")
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

private fun existsLabel(value: Boolean?): String = when (value) {
    true -> "Sí"
    false -> "No"
    null -> "No verificado"
}
