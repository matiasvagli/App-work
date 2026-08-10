package com.matiasdev.elecapp.features.inspections.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.matiasdev.elecapp.features.inspections.data.InspectionRepository
import com.matiasdev.elecapp.features.inspections.domain.InspectionStatus
import com.matiasdev.elecapp.features.inspections.domain.UnverifiedItemType
import com.matiasdev.elecapp.features.inspections.summary.label

/**
 * Observaciones y no verificados: última sección del relevamiento visual.
 *
 * Guardar vuelve al overview, no a la pantalla anterior. Volver atrás dejaba al técnico
 * rebotando entre hallazgos y observaciones sin salida hacia adelante.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisualInspectionComplementaryScreen(
    repository: InspectionRepository,
    inspectionId: String,
    onBackClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onSaved: () -> Unit,
    onHomeClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: VisualInspectionComplementaryViewModel = viewModel(
        factory = VisualInspectionComplementaryViewModelFactory(repository, inspectionId),
    ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(uiState.saved) {
        if (uiState.saved) onSaved()
    }
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Observaciones y no verificados") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
            )
        },
        bottomBar = {
            InspectionSectionNavigation(
                onPreviousClick = onPreviousClick,
                onNextClick = { if (uiState.status == InspectionStatus.DRAFT) viewModel.save() else onSaved() },
                onHomeClick = onHomeClick,
                nextLabel = "Terminar",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        },
    ) { padding ->
        if (uiState.isLoading) {
            CircularProgressIndicator(Modifier.padding(padding).padding(24.dp))
        } else {
            VisualInspectionComplementaryContent(
                uiState = uiState,
                viewModel = viewModel,
                modifier = Modifier.padding(padding),
            )
        }
    }
}

@Composable
private fun VisualInspectionComplementaryContent(
    uiState: VisualInspectionComplementaryUiState,
    viewModel: VisualInspectionComplementaryViewModel,
    modifier: Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        InspectionFormBlock("Observación general") {
            InspectionTextField(
                label = "Observación general del electricista",
                value = uiState.observation,
                onValueChange = viewModel::onObservationChange,
                minLines = 5,
            )
        }
        InspectionFormBlock("Elementos o sectores no verificados") {
            UnverifiedItemType.entries.forEach { type ->
                VisualUnverifiedRow(type, uiState, viewModel)
            }
        }
        Button(
            onClick = viewModel::save,
            enabled = uiState.status == InspectionStatus.DRAFT,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Guardar sección")
        }
    }
}

@Composable
private fun VisualUnverifiedRow(
    type: UnverifiedItemType,
    uiState: VisualInspectionComplementaryUiState,
    viewModel: VisualInspectionComplementaryViewModel,
) {
    val checked = type in uiState.selectedTypes
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = checked, onCheckedChange = { viewModel.toggle(type) })
            Text(type.label())
        }
        if (checked) {
            InspectionTextField(
                label = "Descripción opcional",
                value = uiState.descriptions[type].orEmpty(),
                onValueChange = { viewModel.onDescriptionChange(type, it) },
                minLines = 2,
            )
        }
    }
}
