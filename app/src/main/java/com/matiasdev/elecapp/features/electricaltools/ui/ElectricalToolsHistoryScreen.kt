package com.matiasdev.elecapp.features.electricaltools.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CopyAll
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LineAxis
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material.icons.filled.History
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.matiasdev.elecapp.core.ui.components.ElecEmptyState
import com.matiasdev.elecapp.core.ui.components.ElecLoadingState
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
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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
        topBar = {
            TopAppBar(
                title = { Text("Historial de cálculos", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
            )
        },
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

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            OutlinedTextField(
                value = state.query,
                onValueChange = onQueryChange,
                label = { Text("Buscar por cliente, título, visita o fecha") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                ),
                modifier = Modifier.fillMaxWidth(),
            )
        }

        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp),
            ) {
                items(CalculationHistoryFilter.entries) { filter ->
                    val isSelected = filter == state.filter
                    FilterChip(
                        selected = isSelected,
                        onClick = { onFilterChange(filter) },
                        label = { Text(filter.label(), fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        shape = RoundedCornerShape(10.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        ),
                    )
                }
            }
        }

        if (state.isLoading) {
            item {
                ElecLoadingState(message = "Cargando historial...", modifier = Modifier.padding(top = 32.dp))
            }
        } else if (state.calculations.isEmpty()) {
            item {
                ElecEmptyState(
                    icon = Icons.Default.History,
                    title = "Sin cálculos guardados",
                    description = "No se encontraron registros que coincidan con la búsqueda o filtro seleccionado.",
                    modifier = Modifier.padding(top = 32.dp),
                )
            }
        } else {
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
    val icon = if (calculation.type == TechnicalCalculationType.POWER_CURRENT_VOLTAGE) Icons.Default.Bolt else Icons.Default.LineAxis
    val iconBg = if (calculation.type == TechnicalCalculationType.POWER_CURRENT_VOLTAGE) Color(0xFFFFF8E1) else Color(0xFFE3F2FD)
    val iconTint = if (calculation.type == TechnicalCalculationType.POWER_CURRENT_VOLTAGE) Color(0xFFF57F17) else Color(0xFF1976D2)

    val formattedDate = remember(calculation.createdAt) {
        try {
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.systemDefault())
            formatter.format(calculation.createdAt)
        } catch (_: Exception) {
            ""
        }
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = iconBg,
                        modifier = Modifier.size(36.dp),
                    ) {
                        Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.padding(8.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = calculation.type.label(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Text(
                text = calculation.primaryResultText(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CalculationOriginChip(calculation.source)
                TechnicalClassificationChip(calculation.classification)
            }

            if (calculation.title.isNotBlank()) {
                Text(calculation.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            }

            val association = listOfNotNull(
                calculation.clientId?.let { "Cliente" },
                calculation.visitId?.let { "Visita" },
                calculation.inspectionId?.let { "Relevamiento" },
            ).joinToString(" · ")

            if (association.isNotBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(association, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Action Buttons
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = onDetailClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ver")
                }
                OutlinedButton(
                    onClick = onCopy,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copiar")
                }
                OutlinedButton(
                    onClick = onShare,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Compartir")
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = onDuplicate,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Icon(Icons.Default.CopyAll, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Duplicar")
                }
                OutlinedButton(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Eliminar")
                }
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

