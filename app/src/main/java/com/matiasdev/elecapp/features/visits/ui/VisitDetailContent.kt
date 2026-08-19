package com.matiasdev.elecapp.features.visits.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.matiasdev.elecapp.core.external.browserMapsIntent
import com.matiasdev.elecapp.core.external.browserWhatsappIntent
import com.matiasdev.elecapp.core.external.dialIntent
import com.matiasdev.elecapp.core.external.mapsIntent
import com.matiasdev.elecapp.core.external.tryStartActivity
import com.matiasdev.elecapp.core.external.whatsappBusinessIntent
import com.matiasdev.elecapp.core.external.whatsappIntent
import com.matiasdev.elecapp.core.ui.components.ElecBadge
import com.matiasdev.elecapp.core.ui.components.ElecLoadingState
import com.matiasdev.elecapp.features.agenda.ui.VisitStatusChip
import com.matiasdev.elecapp.features.inspections.domain.InspectionStatus
import com.matiasdev.elecapp.features.materials.domain.MaterialListStatus
import com.matiasdev.elecapp.features.materials.summary.label
import com.matiasdev.elecapp.features.quotes.domain.QuoteStatus
import com.matiasdev.elecapp.features.quotes.summary.label
import com.matiasdev.elecapp.features.visits.domain.Visit
import com.matiasdev.elecapp.features.visits.domain.VisitStatus
import com.matiasdev.elecapp.features.visits.domain.VisitWorkActions
import com.matiasdev.elecapp.features.visits.domain.VisitWorkPrimaryAction

@Composable
fun VisitDetailContent(
    uiState: VisitDetailUiState,
    onInspectionClick: () -> Unit,
    onQuoteClick: () -> Unit,
    onMaterialClick: () -> Unit,
    onWorkClick: () -> Unit,
    onReportsClick: () -> Unit,
    onStartVisitClick: () -> Unit,
    onPauseWorkClick: () -> Unit,
    onResumeWorkClick: () -> Unit,
    onCompleteVisitClick: () -> Unit,
    onReceiptClick: (String) -> Unit,
    onRegisterPaymentClick: (String, String, String?) -> Unit,
    onEditSessionNotesClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val visit = uiState.visit
    when {
        uiState.isLoading -> ElecLoadingState(message = "Cargando detalle de la visita...", modifier = modifier)
        visit == null -> Box(
            modifier = modifier.fillMaxSize().padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = uiState.errorMessage ?: "Visita no encontrada",
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        else -> LazyColumn(
            modifier = modifier,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item { VisitHeroCard(uiState) }

            item {
                VisitPrimaryActions(
                    uiState = uiState,
                    onStartVisitClick = onStartVisitClick,
                    onPauseWorkClick = onPauseWorkClick,
                    onResumeWorkClick = onResumeWorkClick,
                    onCompleteVisitClick = onCompleteVisitClick,
                )
            }

            if (visit.status == VisitStatus.IN_PROGRESS || visit.status == VisitStatus.COMPLETED) {
                item { WorkTimerCard(uiState) }
            }

            item {
                VisitDocumentsCard(
                    uiState = uiState,
                    onWorkClick = onWorkClick,
                    onReportsClick = onReportsClick,
                    onInspectionClick = onInspectionClick,
                    onQuoteClick = onQuoteClick,
                    onMaterialClick = onMaterialClick,
                )
            }

            if (visit.status == VisitStatus.COMPLETED) {
                item {
                    WorkClosureCard(
                        uiState = uiState,
                        onReceiptClick = onReceiptClick,
                        onRegisterPaymentClick = onRegisterPaymentClick,
                    )
                }
            }

            item {
                WorkSessionsCard(
                    uiState = uiState,
                    onEditSessionNotesClick = onEditSessionNotesClick,
                )
            }

            item { VisitQuickActions(uiState) }
            item { VisitTimelineCard(uiState) }
            item { VisitNotesCard(visit) }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun VisitHeroCard(uiState: VisitDetailUiState) {
    val visit = uiState.visit ?: return
    val client = uiState.client

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = client?.fullName?.take(1)?.uppercase() ?: "V",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                }
                VisitStatusChip(visit = visit)
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = visit.reason,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = client?.fullName ?: "Cliente sin especificar",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Event,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = visit.scheduledAt.formatVisitDateTime(),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                        )
                        visit.estimatedDurationMinutes?.let { minutes ->
                            Text(
                                text = " · $minutes min estimados",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    client?.address?.takeIf(String::isNotBlank)?.let { address ->
                        val fullAddress = listOf(address, client.locality).filterNotNull().filter(String::isNotBlank).joinToString(", ")
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = fullAddress,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VisitPrimaryActions(
    uiState: VisitDetailUiState,
    onStartVisitClick: () -> Unit,
    onPauseWorkClick: () -> Unit,
    onResumeWorkClick: () -> Unit,
    onCompleteVisitClick: () -> Unit,
) {
    val visit = uiState.visit ?: return
    val action = VisitWorkActions.primaryAction(visit.status, uiState.workSummary?.activeSession)

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        when (action) {
            VisitWorkPrimaryAction.START -> Button(
                onClick = onStartVisitClick,
                enabled = !uiState.isOperationInProgress,
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(vertical = 14.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Iniciar visita", fontWeight = FontWeight.Bold)
            }
            VisitWorkPrimaryAction.PAUSE -> Button(
                onClick = onPauseWorkClick,
                enabled = !uiState.isOperationInProgress,
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(vertical = 14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Default.Pause, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Pausar trabajo", fontWeight = FontWeight.Bold)
            }
            VisitWorkPrimaryAction.RESUME -> Button(
                onClick = onResumeWorkClick,
                enabled = !uiState.isOperationInProgress,
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(vertical = 14.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Reanudar trabajo", fontWeight = FontWeight.Bold)
            }
            VisitWorkPrimaryAction.NONE -> Unit
        }

        if (visit.status == VisitStatus.IN_PROGRESS) {
            FilledTonalButton(
                onClick = onCompleteVisitClick,
                enabled = !uiState.isOperationInProgress,
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(vertical = 14.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Finalizar y cerrar visita", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun WorkTimerCard(uiState: VisitDetailUiState) {
    val visit = uiState.visit ?: return
    val summary = uiState.workSummary ?: return
    val isRunning = visit.status == VisitStatus.IN_PROGRESS

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (isRunning) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = if (isRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isRunning) "Cronómetro en curso" else "Resumen de tiempo trabajado",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
                if (isRunning) {
                    ElecBadge(
                        text = "Activo",
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "TIEMPO TRABAJADO EFECTIVO",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = summary.totalWorkedDuration.formatTimerText(),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                StatPill("Sesiones", "${summary.sessionCount}")
                StatPill("Pausas", "${summary.pauseCount}")
                StatPill("Pausado", summary.totalPausedDuration.formatCompactDuration())
                StatPill("Total", summary.totalElapsedDuration.formatCompactDuration())
            }
        }
    }
}

@Composable
private fun StatPill(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun VisitDocumentsCard(
    uiState: VisitDetailUiState,
    onWorkClick: () -> Unit,
    onReportsClick: () -> Unit,
    onInspectionClick: () -> Unit,
    onQuoteClick: () -> Unit,
    onMaterialClick: () -> Unit,
) {
    val visit = uiState.visit ?: return

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Módulos y Documentos",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }

            DocumentItemTile(
                icon = Icons.Default.Handyman,
                title = "Trabajo",
                subtitle = workVisitLabel(visit),
                actionLabel = "Editar",
                onClick = onWorkClick,
            )

            if (visit.status == VisitStatus.COMPLETED) {
                DocumentItemTile(
                    icon = Icons.Default.Description,
                    title = "Informes de atención",
                    subtitle = "Informe técnico y para el cliente",
                    actionLabel = "Ver",
                    onClick = onReportsClick,
                )
            }

            DocumentItemTile(
                icon = Icons.AutoMirrored.Filled.FactCheck,
                title = "Relevamiento técnico",
                subtitle = inspectionVisitLabel(uiState.inspection?.status),
                actionLabel = if (uiState.inspection == null) "Iniciar" else "Abrir",
                onClick = onInspectionClick,
            )

            DocumentItemTile(
                icon = Icons.AutoMirrored.Filled.ReceiptLong,
                title = "Presupuesto",
                subtitle = quoteVisitLabel(uiState.quote?.status),
                actionLabel = if (uiState.quote == null) "Crear" else "Abrir",
                onClick = onQuoteClick,
            )

            DocumentItemTile(
                icon = Icons.AutoMirrored.Filled.FormatListBulleted,
                title = "Lista de materiales",
                subtitle = materialVisitLabel(uiState.materialList?.status),
                actionLabel = if (uiState.materialList == null) "Crear" else "Abrir",
                onClick = onMaterialClick,
            )
        }
    }
}

@Composable
private fun DocumentItemTile(
    icon: ImageVector,
    title: String,
    subtitle: String,
    actionLabel: String,
    onClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                modifier = Modifier.size(36.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            FilledTonalButton(
                onClick = onClick,
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Text(actionLabel, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun VisitQuickActions(uiState: VisitDetailUiState) {
    val context = LocalContext.current
    val client = uiState.client ?: return

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Contacto con el cliente",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Button(
                    onClick = { openWhatsapp(context, client.phone) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF25D366),
                        contentColor = Color.White,
                    ),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("WhatsApp", fontWeight = FontWeight.Bold, maxLines = 1)
                }

                FilledTonalButton(
                    onClick = {
                        if (!context.tryStartActivity(dialIntent(client.phone))) {
                            Toast.makeText(context, "No hay app para llamar", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Llamar", fontWeight = FontWeight.Bold, maxLines = 1)
                }

                if (client.address != null || client.locality != null) {
                    FilledTonalButton(
                        onClick = { openMaps(context, client.address, client.locality) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Mapa", fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                }
            }
        }
    }
}

@Composable
private fun VisitNotesCard(visit: Visit) {
    if (visit.notes.isNullOrBlank() && visit.completionNotes.isNullOrBlank() && visit.pendingWorkNotes.isNullOrBlank()) return

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Notes,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Notas de la visita",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }

            visit.notes?.takeIf(String::isNotBlank)?.let {
                NoteSectionItem("Notas iniciales", it)
            }
            visit.completionNotes?.takeIf(String::isNotBlank)?.let {
                NoteSectionItem("Trabajo realizado", it)
            }
            visit.pendingWorkNotes?.takeIf(String::isNotBlank)?.let {
                NoteSectionItem("Pendientes", it)
            }
        }
    }
}

@Composable
private fun NoteSectionItem(title: String, content: String) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

private fun openWhatsapp(context: android.content.Context, phone: String?) {
    val opened = whatsappIntent(phone.orEmpty())?.let(context::tryStartActivity) == true ||
        whatsappBusinessIntent(phone.orEmpty())?.let(context::tryStartActivity) == true ||
        browserWhatsappIntent(phone.orEmpty())?.let(context::tryStartActivity) == true
    if (!opened) Toast.makeText(context, "No se pudo abrir WhatsApp", Toast.LENGTH_SHORT).show()
}

private fun openMaps(context: android.content.Context, address: String?, locality: String?) {
    val opened = mapsIntent(address, locality)?.let(context::tryStartActivity) == true ||
        browserMapsIntent(address, locality)?.let(context::tryStartActivity) == true
    if (!opened) Toast.makeText(context, "No hay una app de mapas", Toast.LENGTH_SHORT).show()
}

private fun inspectionVisitLabel(status: InspectionStatus?): String = when (status) {
    null -> "No iniciado"
    InspectionStatus.DRAFT -> "Borrador"
    InspectionStatus.COMPLETED -> "Finalizado"
}

private fun workVisitLabel(visit: Visit): String {
    return when {
        !visit.notes.isNullOrBlank() -> visit.notes
        visit.reason.isNotBlank() -> visit.reason
        else -> "Sin detalle"
    }
}

private fun quoteVisitLabel(status: QuoteStatus?): String = status?.label() ?: "No creado"

private fun materialVisitLabel(status: MaterialListStatus?): String = status?.label() ?: "Sin lista"
