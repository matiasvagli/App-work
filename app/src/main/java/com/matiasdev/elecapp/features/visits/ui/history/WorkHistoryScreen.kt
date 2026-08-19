package com.matiasdev.elecapp.features.visits.ui.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.WorkHistory
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.matiasdev.elecapp.core.ui.components.ElecBadge
import com.matiasdev.elecapp.core.ui.components.ElecEmptyState
import com.matiasdev.elecapp.core.ui.components.ElecLoadingState
import com.matiasdev.elecapp.features.finance.domain.VisitWorkType
import com.matiasdev.elecapp.features.finance.domain.label
import com.matiasdev.elecapp.features.visits.data.VisitRepository
import com.matiasdev.elecapp.features.visits.domain.WorkHistoryItem
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkHistoryScreen(
    visitRepository: VisitRepository,
    clientId: String?,
    onBackClick: () -> Unit,
    onVisitClick: (String) -> Unit,
    onReportsClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WorkHistoryViewModel = viewModel(
        factory = WorkHistoryViewModelFactory(visitRepository, clientId),
    ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val filteredByClient = !clientId.isNullOrBlank()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (filteredByClient) "Trabajos del cliente" else "Historial de trabajos",
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { padding ->
        WorkHistoryContent(
            uiState = uiState,
            onVisitClick = onVisitClick,
            onReportsClick = onReportsClick,
            modifier = Modifier.padding(padding),
        )
    }
}

@Composable
private fun WorkHistoryContent(
    uiState: WorkHistoryUiState,
    onVisitClick: (String) -> Unit,
    onReportsClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        uiState.isLoading -> ElecLoadingState(
            message = "Cargando historial de trabajos...",
            modifier = modifier,
        )
        uiState.errorMessage != null -> Box(
            modifier = modifier.fillMaxSize().padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = uiState.errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        uiState.items.isEmpty() -> ElecEmptyState(
            icon = Icons.Default.WorkHistory,
            title = "Sin trabajos finalizados",
            description = "Los trabajos completados y documentados aparecerán en este historial.",
            modifier = modifier.fillMaxSize(),
        )
        else -> LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            items(uiState.items, key = { it.visitId }) { item ->
                WorkHistoryCard(
                    item = item,
                    onClick = { onVisitClick(item.visitId) },
                    onReportsClick = { onReportsClick(item.visitId) },
                )
            }
        }
    }
}

@Composable
private fun WorkHistoryCard(
    item: WorkHistoryItem,
    onClick: () -> Unit,
    onReportsClick: () -> Unit,
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Encabezado con avatar tonal de trabajo, cliente y flecha de acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                    modifier = Modifier.size(44.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = item.workTypeIcon(),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.clientName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Event,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp),
                        )
                        Text(
                            text = item.completedAt.formatWorkHistoryDate(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Ver detalle",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp),
                )
            }

            // Badges de tipo de trabajo y duración
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                item.workTypeLabel()?.let { typeLabel ->
                    ElecBadge(
                        text = typeLabel,
                        icon = Icons.Default.Bolt,
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        contentColor = MaterialTheme.colorScheme.primary,
                    )
                }
                item.durationMinutes?.let { minutes ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(13.dp),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = durationText(minutes),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            // Bloque de trabajo realizado
            item.workDescription?.takeIf(String::isNotBlank)?.let { description ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp).padding(top = 2.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            // Motivo inicial de la atención
            item.reason.takeIf(String::isNotBlank)?.let { reason ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 2.dp),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Notes,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Motivo: $reason",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            // Informes generados
            WorkHistoryReports(item, onReportsClick)
        }
    }
}

/**
 * Muestra qué informes quedaron guardados en esta atención.
 */
@Composable
private fun WorkHistoryReports(item: WorkHistoryItem, onReportsClick: () -> Unit) {
    if (!item.hasTechnicalReport && !item.hasClientReport) return
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (item.hasTechnicalReport) {
            AssistChip(
                onClick = onReportsClick,
                label = { Text("Informe técnico", fontWeight = FontWeight.Medium) },
                leadingIcon = {
                    Icon(
                        Icons.Default.Description,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                },
                shape = RoundedCornerShape(10.dp),
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                    labelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    leadingIconContentColor = MaterialTheme.colorScheme.primary,
                ),
            )
        }
        if (item.hasClientReport) {
            AssistChip(
                onClick = onReportsClick,
                label = { Text("Informe al cliente", fontWeight = FontWeight.Medium) },
                leadingIcon = {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                },
                shape = RoundedCornerShape(10.dp),
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
                    labelColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    leadingIconContentColor = MaterialTheme.colorScheme.tertiary,
                ),
            )
        }
    }
}

private fun WorkHistoryItem.workTypeIcon(): ImageVector {
    val rawType = workType ?: return Icons.Default.Build
    val type = runCatching { VisitWorkType.valueOf(rawType) }.getOrNull()
    return when (type) {
        VisitWorkType.INSTALLATION -> Icons.Default.Bolt
        VisitWorkType.REPAIR -> Icons.Default.Build
        VisitWorkType.FAULT_FINDING -> Icons.Default.Build
        VisitWorkType.MAINTENANCE -> Icons.Default.CheckCircle
        VisitWorkType.TECHNICAL_INSPECTION -> Icons.Default.Description
        else -> Icons.Default.Build
    }
}

private fun WorkHistoryItem.workTypeLabel(): String? {
    val rawType = workType ?: return null
    return runCatching { VisitWorkType.valueOf(rawType).label() }.getOrDefault(rawType)
}

private fun Instant.formatWorkHistoryDate(): String {
    return DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(atZone(ZoneId.systemDefault()))
}

private fun durationText(minutes: Long): String {
    val hours = minutes / 60
    val remainingMinutes = minutes % 60
    return when {
        hours > 0 && remainingMinutes > 0 -> "${hours} h ${remainingMinutes} min"
        hours > 0 -> "${hours} h"
        else -> "${remainingMinutes} min"
    }
}

