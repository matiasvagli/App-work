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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.matiasdev.elecapp.features.inspections.domain.FindingReviewStatus
import com.matiasdev.elecapp.features.inspections.domain.FindingSeverity
import com.matiasdev.elecapp.features.inspections.domain.FindingSourceType
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
        FindingGroup("Confirmados", uiState.confirmed, viewModel, onAddToQuoteClick, uiState.status)
        FindingGroup("Sugeridos por la app", uiState.suggested, viewModel, onAddToQuoteClick, uiState.status)
        FindingGroup("Datos para revisar", uiState.dataReview, viewModel, onAddToQuoteClick, uiState.status)
        FindingGroup("No verificado", uiState.notVerified, viewModel, onAddToQuoteClick, uiState.status)
        FindingForm(uiState, viewModel)
        FindingGroup("Manuales", uiState.manual, viewModel, onAddToQuoteClick, uiState.status)
    }
}

@Composable
private fun FindingGroup(
    title: String,
    findings: List<InspectionFinding>,
    viewModel: FindingsViewModel,
    onAddToQuoteClick: () -> Unit,
    status: InspectionStatus,
) {
    InspectionFormBlock(title) {
        if (findings.isEmpty()) {
            Text("Sin elementos")
        } else {
            findings.forEach { finding ->
                FindingCard(
                    finding = finding,
                    viewModel = viewModel,
                    onAddToQuoteClick = onAddToQuoteClick,
                    enabled = status == InspectionStatus.DRAFT,
                )
            }
        }
    }
}

@Composable
private fun FindingForm(uiState: FindingsUiState, viewModel: FindingsViewModel) {
    InspectionFormBlock(if (uiState.editingId == null) "+ Agregar hallazgo manual" else "Editar texto") {
        InspectionDropdownField("Categoría o sección", uiState.category, manualCategories, FindingCategory::label) {
            viewModel.update { copy(category = it) }
        }
        InspectionDropdownField("Prioridad", uiState.severity, FindingSeverity.entries.toList(), FindingSeverity::label) {
            viewModel.update { copy(severity = it) }
        }
        InspectionTextField("Descripción", uiState.description, { viewModel.update { copy(description = it) } }, minLines = 3, error = uiState.descriptionError)
        InspectionTextField("Observación", uiState.technicianNotes, { viewModel.update { copy(technicianNotes = it) } }, minLines = 2)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = viewModel::saveManual, enabled = uiState.status == InspectionStatus.DRAFT) { Text("Guardar") }
            OutlinedButton(onClick = viewModel::clearForm) { Text("Limpiar") }
        }
    }
}

@Composable
private fun FindingCard(
    finding: InspectionFinding,
    viewModel: FindingsViewModel,
    onAddToQuoteClick: () -> Unit,
    enabled: Boolean,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(finding.severity.label(), fontWeight = FontWeight.Bold)
                Text(finding.statusLabel())
            }
            Text(finding.title, fontWeight = FontWeight.SemiBold)
            Text(finding.description)
            finding.technicalValueText()?.let { Text(it) }
            finding.technicianNotes?.let { Text("Observación: $it") }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = { viewModel.includeInReport(finding) }, enabled = enabled) {
                    Text(if (finding.includeInReport) "Incluido" else "Incluir")
                }
                TextButton(onClick = { viewModel.excludeFromReport(finding) }, enabled = enabled) { Text("Excluir") }
                TextButton(onClick = { viewModel.confirmSuggestion(finding) }, enabled = enabled && finding.reviewStatus != FindingReviewStatus.CONFIRMED) { Text("Confirmar") }
                TextButton(onClick = { viewModel.changePriority(finding, finding.severity.next()) }, enabled = enabled) { Text("Prioridad") }
                IconButton(onClick = { viewModel.edit(finding) }, enabled = enabled) { Icon(Icons.Default.Edit, contentDescription = "Editar") }
                if (finding.sourceType == FindingSourceType.MANUAL) {
                    IconButton(onClick = { viewModel.delete(finding) }, enabled = enabled) { Icon(Icons.Default.Delete, contentDescription = "Eliminar") }
                }
            }
            if (finding.sourceType == FindingSourceType.MANUAL) {
                OutlinedButton(onClick = onAddToQuoteClick) { Text("Agregar al presupuesto") }
            }
        }
    }
}

private val manualCategories = listOf(
    FindingCategory.PILLAR,
    FindingCategory.MAIN_PANEL,
    FindingCategory.CIRCUITS,
    FindingCategory.CONDUCTORS,
    FindingCategory.PROTECTIONS,
    FindingCategory.GROUNDING,
    FindingCategory.EQUIPMENT,
    FindingCategory.VISIBLE_RISK,
    FindingCategory.OTHER,
)

private fun InspectionFinding.statusLabel(): String = when (sourceType) {
    FindingSourceType.MANUAL -> "Manual"
    FindingSourceType.OBSERVATION_CONFIRMED -> "Confirmado por el técnico"
    FindingSourceType.RULE_SUGGESTION -> "Sugerencia de la app"
    FindingSourceType.DATA_REVIEW -> "Dato para revisar"
    FindingSourceType.NOT_VERIFIED -> "No verificado"
}

private fun InspectionFinding.technicalValueText(): String? {
    val value = sourceValue ?: return null
    val unit = sourceUnit.orEmpty()
    return "Valor técnico: ${value.formatNumber()} $unit".trim()
}

private fun FindingSeverity.next(): FindingSeverity = when (this) {
    FindingSeverity.OK -> FindingSeverity.RECOMMENDED
    FindingSeverity.RECOMMENDED -> FindingSeverity.PRIORITY
    FindingSeverity.PRIORITY -> FindingSeverity.URGENT
    FindingSeverity.URGENT -> FindingSeverity.OK
}

private fun Double.formatNumber(): String {
    val whole = toLong()
    return if (this == whole.toDouble()) whole.toString() else toString().replace(".", ",")
}
