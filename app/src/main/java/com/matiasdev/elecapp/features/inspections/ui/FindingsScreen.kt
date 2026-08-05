package com.matiasdev.elecapp.features.inspections.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.matiasdev.elecapp.features.inspections.data.InspectionRepository
import com.matiasdev.elecapp.features.inspections.domain.FindingCategory
import com.matiasdev.elecapp.features.inspections.domain.FindingSeverity
import com.matiasdev.elecapp.features.inspections.domain.InspectionFinding
import com.matiasdev.elecapp.features.inspections.domain.InspectionStatus
import com.matiasdev.elecapp.features.inspections.summary.label

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FindingsScreen(
    repository: InspectionRepository,
    inspectionId: String,
    onBackClick: () -> Unit,
    onAddToQuoteClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: FindingsViewModel = viewModel(factory = FindingsViewModelFactory(repository, inspectionId)),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.saved) {
        if (uiState.saved) snackbarHostState.showSnackbar("Hallazgo guardado")
    }
    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Hallazgos") },
                navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver") } },
            )
        },
    ) { padding ->
        if (uiState.isLoading) CircularProgressIndicator(Modifier.padding(padding).padding(24.dp)) else {
            FindingsContent(uiState, viewModel, onAddToQuoteClick, Modifier.padding(padding))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FindingsContent(
    uiState: FindingsUiState,
    viewModel: FindingsViewModel,
    onAddToQuoteClick: () -> Unit,
    modifier: Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        InspectionFormBlock("Plantillas rápidas") {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                findingTemplates.forEach { template ->
                    AssistChip(onClick = { viewModel.applyTemplate(template) }, label = { Text(template.title) })
                }
            }
        }
        FindingForm(uiState, viewModel)
        Text("Hallazgos cargados", style = MaterialTheme.typography.titleMedium)
        uiState.findings.forEach { finding ->
            FindingCard(
                finding = finding,
                onEdit = { viewModel.edit(it) },
                onDelete = { viewModel.delete(it) },
                onAddToQuoteClick = onAddToQuoteClick,
                enabled = uiState.status == InspectionStatus.DRAFT,
            )
        }
    }
}

@Composable
private fun FindingForm(uiState: FindingsUiState, viewModel: FindingsViewModel) {
    InspectionFormBlock(if (uiState.editingId == null) "Nuevo hallazgo" else "Editar hallazgo") {
        InspectionDropdownField("Categoría", uiState.category, FindingCategory.entries.toList(), FindingCategory::label) {
            viewModel.update { copy(category = it) }
        }
        InspectionDropdownField("Severidad", uiState.severity, FindingSeverity.entries.toList(), FindingSeverity::label) {
            viewModel.update { copy(severity = it) }
        }
        InspectionTextField("Título", uiState.title, { viewModel.update { copy(title = it) } }, error = uiState.titleError)
        InspectionTextField("Descripción", uiState.description, { viewModel.update { copy(description = it) } }, minLines = 3, error = uiState.descriptionError)
        InspectionTextField("Recomendación", uiState.recommendation, { viewModel.update { copy(recommendation = it) } }, minLines = 2)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = viewModel::save, enabled = uiState.status == InspectionStatus.DRAFT) { Text("Guardar hallazgo") }
            OutlinedButton(onClick = viewModel::clearForm) { Text("Limpiar") }
        }
    }
}

@Composable
private fun FindingCard(
    finding: InspectionFinding,
    onEdit: (InspectionFinding) -> Unit,
    onDelete: (InspectionFinding) -> Unit,
    onAddToQuoteClick: () -> Unit,
    enabled: Boolean,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${finding.severity.symbol()} ${finding.severity.label()}", fontWeight = FontWeight.Bold)
                Row {
                    IconButton(onClick = { onEdit(finding) }, enabled = enabled) { Icon(Icons.Default.Edit, contentDescription = "Editar") }
                    IconButton(onClick = { onDelete(finding) }, enabled = enabled) { Icon(Icons.Default.Delete, contentDescription = "Eliminar") }
                }
            }
            Text(finding.title, style = MaterialTheme.typography.titleMedium)
            Text(finding.description)
            finding.recommendation?.let { Text("Recomendación: $it") }
            OutlinedButton(onClick = onAddToQuoteClick) {
                Text("Agregar al presupuesto")
            }
        }
    }
}
