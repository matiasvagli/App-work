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
import com.matiasdev.elecapp.features.inspections.domain.DifferentialTestResult
import com.matiasdev.elecapp.features.inspections.domain.GeneralCondition
import com.matiasdev.elecapp.features.inspections.domain.InspectionSectionReviewStatus
import com.matiasdev.elecapp.features.inspections.domain.InspectionScope
import com.matiasdev.elecapp.features.inspections.domain.InspectionStatus
import com.matiasdev.elecapp.features.inspections.domain.ProtectionCompatibility
import com.matiasdev.elecapp.features.inspections.domain.YesNoPartialUnknown
import com.matiasdev.elecapp.features.inspections.domain.YesNoUnknown
import com.matiasdev.elecapp.features.inspections.summary.label

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainPanelInspectionScreen(
    repository: InspectionRepository,
    inspectionId: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MainPanelInspectionViewModel = viewModel(factory = MainPanelInspectionViewModelFactory(repository, inspectionId)),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(uiState.saved) {
        if (uiState.saved) onBackClick()
    }
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Tablero principal") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver") }
                },
            )
        },
    ) { padding ->
        if (uiState.isLoading) {
            CircularProgressIndicator(Modifier.padding(padding).padding(24.dp))
        } else {
            MainPanelForm(uiState, viewModel, Modifier.padding(padding))
        }
    }
}

@Composable
private fun MainPanelForm(uiState: MainPanelInspectionUiState, viewModel: MainPanelInspectionViewModel, modifier: Modifier) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        if (uiState.scope == InspectionScope.VISUAL_INSPECTION) {
            InspectionFormBlock("¿Se revisó el tablero principal?") {
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
            InspectionDropdownField("Accesible", uiState.accessible, AccessStatus.entries.toList(), AccessStatus::label) {
                viewModel.update { copy(accessible = it) }
            }
            InspectionDropdownField("Estado general", uiState.generalCondition, GeneralCondition.entries.toList(), GeneralCondition::label) {
                viewModel.update { copy(generalCondition = it) }
            }
        }
        InspectionFormBlock("Diferencial y circuitos") {
            InspectionDropdownField("Interruptor diferencial", uiState.differentialPresent, YesNoUnknown.entries.toList(), YesNoUnknown::label) {
                viewModel.update { copy(differentialPresent = it) }
            }
            NumberField("Corriente nominal A", uiState.differentialRatedAmps, { viewModel.update { copy(differentialRatedAmps = it.filter(Char::isDigit)) } }, uiState.ratedAmpsError)
            NumberField("Sensibilidad mA", uiState.differentialSensitivityMa, { viewModel.update { copy(differentialSensitivityMa = it.filter(Char::isDigit)) } }, uiState.sensitivityError)
            InspectionDropdownField("Prueba manual", uiState.differentialTestResult, DifferentialTestResult.entries.toList(), DifferentialTestResult::label) {
                viewModel.update { copy(differentialTestResult = it) }
            }
            NumberField("Cantidad de circuitos", uiState.circuitCount, { viewModel.update { copy(circuitCount = it.filter(Char::isDigit)) } }, uiState.circuitCountError)
            InspectionDropdownField("Circuitos identificados", uiState.circuitsIdentified, YesNoPartialUnknown.entries.toList(), YesNoPartialUnknown::label) {
                viewModel.update { copy(circuitsIdentified = it) }
            }
        }
        InspectionFormBlock("Barras y riesgos visibles") {
            yesNoField("Barra de neutro", uiState.neutralBarPresent) { viewModel.update { copy(neutralBarPresent = it) } }
            yesNoField("Barra de tierra", uiState.groundBarPresent) { viewModel.update { copy(groundBarPresent = it) } }
            yesNoField("Neutro y tierra separados", uiState.neutralAndGroundSeparated) { viewModel.update { copy(neutralAndGroundSeparated = it) } }
            yesNoField("Empalmes improvisados", uiState.improvisedConnections) { viewModel.update { copy(improvisedConnections = it) } }
            yesNoField("Colores incorrectos o mezclados", uiState.mixedOrIncorrectColors) { viewModel.update { copy(mixedOrIncorrectColors = it) } }
            yesNoField("Signos de recalentamiento", uiState.overheatingSigns) { viewModel.update { copy(overheatingSigns = it) } }
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
private fun yesNoField(label: String, value: YesNoUnknown, onChange: (YesNoUnknown) -> Unit) {
    InspectionDropdownField(label, value, YesNoUnknown.entries.toList(), YesNoUnknown::label, onValueChange = onChange)
}
