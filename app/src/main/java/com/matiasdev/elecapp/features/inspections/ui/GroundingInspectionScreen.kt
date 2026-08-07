package com.matiasdev.elecapp.features.inspections.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.matiasdev.elecapp.features.inspections.data.InspectionRepository
import com.matiasdev.elecapp.features.inspections.domain.MeasurementOrigin
import com.matiasdev.elecapp.features.inspections.domain.YesNoUnknown
import com.matiasdev.elecapp.features.inspections.summary.label

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroundingInspectionScreen(
    repository: InspectionRepository,
    inspectionId: String,
    onBackClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onHomeClick: () -> Unit,
) {
    val viewModel: GroundingInspectionViewModel = viewModel(factory = GroundingInspectionViewModelFactory(repository, inspectionId))
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Puesta a tierra") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
            )
        },
    ) { padding ->
        when {
            state.loading -> CircularProgressIndicator(modifier = Modifier.padding(padding).padding(24.dp))
            state.errorMessage != null -> Text(state.errorMessage.orEmpty(), modifier = Modifier.padding(padding).padding(16.dp), color = MaterialTheme.colorScheme.error)
            else -> Column(
                modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                InspectionFormBlock("Inspección visual") {
                    GroundingYesNoField("Electrodo o jabalina visible", state.electrodePresent) { viewModel.update { copy(electrodePresent = it) } }
                    GroundingYesNoField("Cámara de inspección accesible", state.inspectionChamberAccessible) { viewModel.update { copy(inspectionChamberAccessible = it) } }
                    GroundingYesNoField("Conductor principal de tierra visible", state.mainGroundConductorPresent) { viewModel.update { copy(mainGroundConductorPresent = it) } }
                    GroundingYesNoField("Continuidad del conductor de protección verificada", state.protectiveConductorContinuity) { viewModel.update { copy(protectiveConductorContinuity = it) } }
                }
                InspectionFormBlock("Resistencia de puesta a tierra") {
                    Text(
                        "Para clasificarla como verificada debe existir una medición con telurómetro. Un control de tensión neutro-tierra no reemplaza esta medición.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    InspectionDropdownField(
                        label = "Origen del valor",
                        value = state.resistanceOrigin,
                        options = resistanceOrigins,
                        optionLabel = MeasurementOrigin::label,
                        onValueChange = viewModel::updateResistanceOrigin,
                    )
                    if (state.resistanceOrigin != MeasurementOrigin.NOT_VERIFIED) {
                        OutlinedTextField(
                            value = state.resistanceOhms,
                            onValueChange = viewModel::updateResistance,
                            label = { Text("Resistencia") },
                            suffix = { Text("Ω") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            isError = state.resistanceError != null,
                            supportingText = state.resistanceError?.let { error -> { Text(error) } },
                        )
                    }
                }
                InspectionFormBlock("Observaciones") {
                    InspectionTextField("Notas", state.notes, { value -> viewModel.update { copy(notes = value) } }, minLines = 3)
                }
                Text(if (state.saved) "Guardado" else "Guardando…", color = MaterialTheme.colorScheme.onSurfaceVariant)
                InspectionSectionNavigation(onPreviousClick, onNextClick, onHomeClick)
            }
        }
    }
}

@Composable
private fun GroundingYesNoField(label: String, value: YesNoUnknown, onValueChange: (YesNoUnknown) -> Unit) {
    InspectionDropdownField(label, value, YesNoUnknown.entries, YesNoUnknown::label, onValueChange = onValueChange)
}

private val resistanceOrigins = listOf(
    MeasurementOrigin.MEASURED,
    MeasurementOrigin.DECLARED_BY_CLIENT,
    MeasurementOrigin.NOT_VERIFIED,
)
