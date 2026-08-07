package com.matiasdev.elecapp.features.inspections.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onHomeClick: () -> Unit,
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
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = viewModel::openManualEditor,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Agregar hallazgo manual") },
            )
        },
    ) { padding ->
        if (uiState.isLoading) {
            CircularProgressIndicator(Modifier.padding(padding).padding(24.dp))
        } else {
            FindingsContent(uiState, viewModel, onPreviousClick, onNextClick, onHomeClick, Modifier.padding(padding))
        }
    }
    if (uiState.isEditorOpen) {
        FindingEditorDialog(uiState, viewModel, enabled = uiState.status == InspectionStatus.DRAFT)
    }
    uiState.excludeDialogFindingId?.let { id ->
        val finding = uiState.allFindings.firstOrNull { it.id == id }
        if (finding != null) {
            ExcludeFindingDialog(uiState, viewModel, finding)
        }
    }
}

@Composable
private fun FindingsContent(
    uiState: FindingsUiState,
    viewModel: FindingsViewModel,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onHomeClick: () -> Unit,
    modifier: Modifier,
) {
    val sorted = uiState.allFindings
        .filter(uiState.filter::accepts)
        .sortedWith(compareBy<InspectionFinding> { it.isExcluded }.thenBy { it.severity.sortRank() }.thenBy { it.title })
    val includedCount = uiState.allFindings.count { !it.isExcluded }
    val excludedCount = uiState.allFindings.count { it.isExcluded }
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text("$includedCount incluidos · $excludedCount excluidos", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            FindingsFilter.entries.forEach { filter ->
                FilterChip(
                    selected = uiState.filter == filter,
                    onClick = { viewModel.updateFilter(filter) },
                    label = { Text(filter.label()) },
                )
            }
        }
        if (sorted.isEmpty()) {
            InspectionFormBlock("Sin hallazgos") {
                Text("No hay elementos para el filtro seleccionado.")
                Button(onClick = viewModel::openManualEditor) {
                    Text("Agregar hallazgo manual")
                }
            }
        } else {
            sorted.forEach { finding ->
                FindingCard(
                    finding = finding,
                    enabled = uiState.status == InspectionStatus.DRAFT,
                    onOpen = { viewModel.edit(finding) },
                    onExclude = { viewModel.requestExclude(finding) },
                    onRestore = { viewModel.restoreInReport(finding) },
                    onDelete = { viewModel.delete(finding) },
                )
            }
        }
        InspectionSectionNavigation(
            onPreviousClick = onPreviousClick,
            onNextClick = onNextClick,
            onHomeClick = onHomeClick,
        )
    }
}

@Composable
private fun FindingCard(
    finding: InspectionFinding,
    enabled: Boolean,
    onOpen: () -> Unit,
    onExclude: () -> Unit,
    onRestore: () -> Unit,
    onDelete: () -> Unit,
) {
    val priorityColor = finding.severity.semanticColor()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (finding.isExcluded) 0.72f else 1f)
            .clickable(onClick = onOpen),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (finding.isExcluded) MaterialTheme.colorScheme.outlineVariant else priorityColor.copy(alpha = 0.55f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    PriorityChip(finding.severity)
                    if (finding.isExcluded) StatusChip("EXCLUIDO")
                }
                FindingOverflowMenu(finding, enabled, onOpen, onExclude, onRestore, onDelete)
            }
            Text(finding.locationText(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(finding.cardTitle(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(
                finding.description,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
            finding.technicalValueText()?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(finding.originText(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                TextButton(onClick = if (finding.isExcluded) onRestore else onExclude, enabled = enabled) {
                    Text(if (finding.isExcluded) "Restaurar" else "Excluir")
                }
            }
        }
    }
}

@Composable
private fun PriorityChip(severity: FindingSeverity) {
    AssistChip(
        onClick = {},
        label = { Text(severity.label().uppercase()) },
        border = BorderStroke(1.dp, severity.semanticColor()),
    )
}

@Composable
private fun StatusChip(text: String) {
    AssistChip(onClick = {}, label = { Text(text) })
}

@Composable
private fun FindingOverflowMenu(
    finding: InspectionFinding,
    enabled: Boolean,
    onOpen: () -> Unit,
    onExclude: () -> Unit,
    onRestore: () -> Unit,
    onDelete: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    IconButton(onClick = { expanded = true }) {
        Icon(Icons.Default.MoreVert, contentDescription = "Más opciones")
    }
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        DropdownMenuItem(text = { Text("Editar detalle") }, onClick = { expanded = false; onOpen() })
        DropdownMenuItem(
            text = { Text(if (finding.isExcluded) "Restaurar" else "Excluir") },
            enabled = enabled,
            onClick = {
                expanded = false
                if (finding.isExcluded) onRestore() else onExclude()
            },
        )
        if (finding.sourceType == FindingSourceType.MANUAL) {
            DropdownMenuItem(
                text = { Text("Eliminar") },
                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                enabled = enabled,
                onClick = { expanded = false; onDelete() },
            )
        }
    }
}

@Composable
private fun FindingEditorDialog(uiState: FindingsUiState, viewModel: FindingsViewModel, enabled: Boolean) {
    val editing = uiState.allFindings.firstOrNull { it.id == uiState.editingId }
    AlertDialog(
        onDismissRequest = viewModel::clearForm,
        title = { Text(if (editing == null) "Agregar hallazgo manual" else "Detalle del hallazgo") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.widthIn(max = 560.dp)) {
                InspectionDropdownField("Categoría o sección", uiState.category, manualCategories, FindingCategory::label) {
                    viewModel.update { copy(category = it) }
                }
                InspectionDropdownField("Prioridad", uiState.severity, FindingSeverity.entries.toList(), FindingSeverity::label) {
                    viewModel.update { copy(severity = it) }
                }
                InspectionTextField("Título", uiState.title, { viewModel.update { copy(title = it) } })
                InspectionTextField("Descripción", uiState.description, { viewModel.update { copy(description = it) } }, minLines = 3, error = uiState.descriptionError)
                InspectionTextField("Observación", uiState.technicianNotes, { viewModel.update { copy(technicianNotes = it) } }, minLines = 2)
                if (editing != null) {
                    Text(editing.originText(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        confirmButton = {
            Button(onClick = viewModel::saveManual, enabled = enabled) {
                Text("Guardar")
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                editing?.let {
                    TextButton(
                        onClick = { if (it.isExcluded) viewModel.restoreInReport(it) else viewModel.requestExclude(it) },
                        enabled = enabled,
                    ) {
                        Text(if (it.isExcluded) "Restaurar" else "Excluir")
                    }
                }
                TextButton(onClick = viewModel::clearForm) {
                    Text("Cerrar")
                }
            }
        },
    )
}

@Composable
private fun ExcludeFindingDialog(uiState: FindingsUiState, viewModel: FindingsViewModel, finding: InspectionFinding) {
    AlertDialog(
        onDismissRequest = viewModel::dismissExcludeDialog,
        title = { Text("Excluir hallazgo") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(finding.cardTitle(), fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = uiState.exclusionReason,
                    onValueChange = viewModel::updateExclusionReason,
                    label = { Text("Motivo opcional") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Button(onClick = viewModel::confirmExclude) {
                Text("Excluir")
            }
        },
        dismissButton = {
            TextButton(onClick = viewModel::dismissExcludeDialog) {
                Text("Cancelar")
            }
        },
    )
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

private val InspectionFinding.isExcluded: Boolean
    get() = !includeInReport || reviewStatus == FindingReviewStatus.EXCLUDED

private fun FindingsFilter.accepts(finding: InspectionFinding): Boolean = when (this) {
    FindingsFilter.ALL -> true
    FindingsFilter.IMPORTANT -> !finding.isExcluded && finding.severity in listOf(FindingSeverity.URGENT, FindingSeverity.PRIORITY)
    FindingsFilter.EXCLUDED -> finding.isExcluded
}

private fun FindingsFilter.label(): String = when (this) {
    FindingsFilter.ALL -> "Todos"
    FindingsFilter.IMPORTANT -> "Urgentes/prioritarios"
    FindingsFilter.EXCLUDED -> "Excluidos"
}

private fun FindingSeverity.sortRank(): Int = when (this) {
    FindingSeverity.URGENT -> 0
    FindingSeverity.PRIORITY -> 1
    FindingSeverity.RECOMMENDED -> 2
    FindingSeverity.OK -> 3
}

@Composable
private fun FindingSeverity.semanticColor(): Color = when (this) {
    FindingSeverity.OK -> MaterialTheme.colorScheme.primary
    FindingSeverity.RECOMMENDED -> Color(0xFFB7791F)
    FindingSeverity.PRIORITY -> Color(0xFFD97706)
    FindingSeverity.URGENT -> MaterialTheme.colorScheme.error
}

private fun InspectionFinding.originText(): String = when (sourceType) {
    FindingSourceType.MANUAL -> "Agregado manualmente"
    else -> "Detectado automáticamente"
}

private fun InspectionFinding.locationText(): String {
    return sourceSection?.label() ?: title
}

private fun InspectionFinding.cardTitle(): String {
    val location = locationText()
    return title.takeIf { it.isNotBlank() && it != location } ?: description.substringBefore(".").take(80)
}

private fun InspectionFinding.technicalValueText(): String? {
    val value = sourceValue ?: return null
    val unit = sourceUnit.orEmpty()
    return "Valor técnico: ${value.formatNumber()} $unit".trim()
}

private fun Double.formatNumber(): String {
    val whole = toLong()
    return if (this == whole.toDouble()) whole.toString() else toString().replace(".", ",")
}
