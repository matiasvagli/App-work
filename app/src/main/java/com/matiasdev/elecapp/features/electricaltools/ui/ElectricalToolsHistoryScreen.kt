package com.matiasdev.elecapp.features.electricaltools.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.matiasdev.elecapp.core.ui.theme.ElecAppTheme
import com.matiasdev.elecapp.features.electricaltools.data.TechnicalCalculationRepository
import com.matiasdev.elecapp.features.electricaltools.domain.CalculationSource
import com.matiasdev.elecapp.features.electricaltools.domain.TechnicalCalculation
import com.matiasdev.elecapp.features.electricaltools.domain.TechnicalCalculationType
import com.matiasdev.elecapp.features.electricaltools.domain.TechnicalClassification
import com.matiasdev.elecapp.features.electricaltools.domain.TechnicianConclusion
import com.matiasdev.elecapp.features.electricaltools.summary.TechnicalCalculationTextGenerator
import com.matiasdev.elecapp.features.electricaltools.summary.label
import java.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElectricalToolsHistoryScreen(
    repository: TechnicalCalculationRepository,
    onBackClick: () -> Unit,
    onDetailClick: (String) -> Unit,
    onDuplicatePower: (String) -> Unit,
    onDuplicateVoltageDrop: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ElectricalToolsHistoryViewModel = viewModel(factory = ElectricalToolsHistoryViewModelFactory(repository)),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text("Historial de cálculos") }, navigationIcon = { IconButton(onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") } }) },
    ) { padding ->
        ElectricalToolsHistoryContent(
            state = state,
            onQueryChange = viewModel::updateQuery,
            onFilterChange = viewModel::selectFilter,
            onDetailClick = onDetailClick,
            onDuplicatePower = onDuplicatePower,
            onDuplicateVoltageDrop = onDuplicateVoltageDrop,
            onDelete = viewModel::delete,
            modifier = Modifier.padding(padding),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ElectricalToolsHistoryContent(
    state: ElectricalToolsHistoryUiState,
    onQueryChange: (String) -> Unit,
    onFilterChange: (CalculationHistoryFilter) -> Unit,
    onDetailClick: (String) -> Unit,
    onDuplicatePower: (String) -> Unit,
    onDuplicateVoltageDrop: (String) -> Unit,
    onDelete: (TechnicalCalculation) -> Unit,
    modifier: Modifier = Modifier,
) {
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    LazyColumn(modifier = modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            OutlinedTextField(state.query, onQueryChange, label = { Text("Buscar por cliente, título, descripción, visita o fecha") }, modifier = Modifier.fillMaxWidth())
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                CalculationHistoryFilter.entries.forEach { filter ->
                    FilterChip(selected = filter == state.filter, onClick = { onFilterChange(filter) }, label = { Text(filter.label()) })
                }
            }
        }
        if (state.calculations.isEmpty()) item { Text(if (state.isLoading) "Cargando..." else "No hay cálculos para mostrar") }
        items(state.calculations, key = { it.id }) { calculation ->
            CalculationHistoryCard(
                calculation = calculation,
                onDetailClick = { onDetailClick(calculation.id) },
                onCopy = { clipboard.setText(AnnotatedString(TechnicalCalculationTextGenerator.generate(calculation))) },
                onShare = { shareCalculationText(context, TechnicalCalculationTextGenerator.generate(calculation)) },
                onDuplicate = {
                    if (calculation.type == TechnicalCalculationType.POWER_CURRENT_VOLTAGE) onDuplicatePower(calculation.id) else onDuplicateVoltageDrop(calculation.id)
                },
                onDelete = { onDelete(calculation) },
            )
        }
    }
}

@Composable
private fun CalculationHistoryCard(
    calculation: TechnicalCalculation,
    onDetailClick: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(calculation.type.label(), fontWeight = FontWeight.Bold)
            Text(calculation.primaryResultText(), style = MaterialTheme.typography.titleLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CalculationOriginChip(calculation.source)
                TechnicalClassificationChip(calculation.classification)
            }
            Text(calculation.title)
            val association = listOfNotNull(
                calculation.clientId?.let { "Cliente vinculado" },
                calculation.visitId?.let { "Visita vinculada" },
                calculation.inspectionId?.let { "Relevamiento vinculado" },
            ).joinToString(" · ").ifBlank { "Sin asociar" }
            Text(association, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = onDetailClick, modifier = Modifier.weight(1f)) { Text("Ver") }
                OutlinedButton(onClick = onCopy, modifier = Modifier.weight(1f)) { Text("Copiar") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = onShare, modifier = Modifier.weight(1f)) { Text("Compartir") }
                OutlinedButton(onClick = onDuplicate, modifier = Modifier.weight(1f)) { Text("Duplicar") }
                OutlinedButton(onClick = onDelete, modifier = Modifier.weight(1f)) { Text("Eliminar") }
            }
        }
    }
}

private fun CalculationHistoryFilter.label(): String = when (this) {
    CalculationHistoryFilter.ALL -> "Todos"
    CalculationHistoryFilter.POWER_CURRENT_VOLTAGE -> "Potencia"
    CalculationHistoryFilter.VOLTAGE_DROP -> "Caída"
    CalculationHistoryFilter.MEASURED -> "Medidos"
    CalculationHistoryFilter.CALCULATED -> "Calculados"
    CalculationHistoryFilter.ESTIMATED -> "Estimados"
    CalculationHistoryFilter.REQUIRES_REVIEW -> "Requieren revisión"
    CalculationHistoryFilter.ASSOCIATED_TO_INSPECTION -> "Con relevamiento"
    CalculationHistoryFilter.UNASSOCIATED -> "Sin asociar"
}

@Preview(showBackground = true)
@Composable
private fun HistoryPreview() {
    ElecAppTheme {
        ElectricalToolsHistoryContent(
            state = ElectricalToolsHistoryUiState(
                isLoading = false,
                calculations = listOf(
                    TechnicalCalculation(
                        id = "1",
                        type = TechnicalCalculationType.VOLTAGE_DROP,
                        source = CalculationSource.CALCULATED,
                        clientId = "c1",
                        visitId = "v1",
                        inspectionId = "i1",
                        title = "Caída de tensión",
                        description = null,
                        inputDataJson = "{}",
                        resultDataJson = "{}",
                        primaryResultValue = 5.36,
                        primaryResultUnit = "%",
                        classification = TechnicalClassification.REQUIRES_REVIEW,
                        technicianConclusion = TechnicianConclusion.NOT_REVIEWED,
                        technicianNotes = null,
                        formulaVersion = "v1",
                        createdAt = Instant.now(),
                        updatedAt = Instant.now(),
                        isDeleted = false,
                    ),
                ),
            ),
            onQueryChange = {},
            onFilterChange = {},
            onDetailClick = {},
            onDuplicatePower = {},
            onDuplicateVoltageDrop = {},
            onDelete = {},
        )
    }
}
