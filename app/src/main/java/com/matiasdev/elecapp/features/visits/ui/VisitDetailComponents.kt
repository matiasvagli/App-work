package com.matiasdev.elecapp.features.visits.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.NoteAlt
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.matiasdev.elecapp.core.ui.components.ElecBadge
import com.matiasdev.elecapp.features.finance.domain.MoneyFormatter
import com.matiasdev.elecapp.features.finance.domain.PaymentBalanceCalculator
import com.matiasdev.elecapp.features.finance.domain.VisitTechnicalResult
import com.matiasdev.elecapp.features.finance.domain.displayNumber
import com.matiasdev.elecapp.features.finance.domain.label
import com.matiasdev.elecapp.features.visits.domain.VisitWorkSession
import com.matiasdev.elecapp.features.visits.domain.VisitWorkSessionDurations
import com.matiasdev.elecapp.features.visits.domain.VisitWorkSessionStatus
import java.time.Instant

/**
 * Tarjeta de resumen de cierre del trabajo técnico y módulo de cobro/comprobante.
 */
@Composable
fun WorkClosureCard(
    uiState: VisitDetailUiState,
    onReceiptClick: (String) -> Unit,
    onRegisterPaymentClick: (String, String, String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val visit = uiState.visit ?: return
    val receipt = uiState.receipt
    val completion = uiState.completion

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Título y badge de resultado técnico
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                        modifier = Modifier.size(36.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.FactCheck,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Cierre del trabajo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }

                completion?.technicalResult?.let { result ->
                    val (containerColor, contentColor) = when (result) {
                        VisitTechnicalResult.RESOLVED, VisitTechnicalResult.TECHNICAL_INSPECTION_COMPLETED ->
                            Color(0xFFE8F5E9) to Color(0xFF1B5E20)
                        VisitTechnicalResult.PARTIALLY_RESOLVED, VisitTechnicalResult.DIAGNOSED, VisitTechnicalResult.PENDING_CONTINUATION ->
                            Color(0xFFFFF3E0) to Color(0xFFE65100)
                        VisitTechnicalResult.NOT_RESOLVED ->
                            MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
                    }
                    ElecBadge(
                        text = result.label(),
                        icon = Icons.Default.CheckCircle,
                        containerColor = containerColor,
                        contentColor = contentColor,
                    )
                }
            }

            // Tipo de trabajo
            completion?.workType?.let { workType ->
                TechnicalInfoBlock(
                    icon = Icons.Default.Handyman,
                    label = "Tipo de intervención",
                    value = workType.label(),
                )
            }

            // Trabajo realizado
            val workPerformedText = completion?.workPerformed
                ?: visit.completionNotes.orEmpty().ifBlank { "Sin detalle registrado" }
            TechnicalInfoBlock(
                icon = Icons.Default.Description,
                label = "Trabajo realizado",
                value = workPerformedText,
                isEmphasized = true,
            )

            // Sectores intervenidos
            completion?.workSectors?.takeIf(String::isNotBlank)?.let {
                TechnicalInfoBlock(icon = Icons.AutoMirrored.Filled.Assignment, label = "Sectores intervenidos", value = it)
            }

            // Elementos
            completion?.workItems?.takeIf(String::isNotBlank)?.let {
                TechnicalInfoBlock(icon = Icons.Default.Build, label = "Elementos / Materiales", value = it)
            }

            // Pruebas
            completion?.workTests?.takeIf(String::isNotBlank)?.let {
                TechnicalInfoBlock(icon = Icons.Default.Speed, label = "Pruebas y verificaciones", value = it)
            }

            // Observaciones
            completion?.workObservations?.takeIf(String::isNotBlank)?.let {
                TechnicalInfoBlock(icon = Icons.Default.NoteAlt, label = "Observaciones", value = it)
            }

            // Pendientes
            completion?.pendingWork?.takeIf(String::isNotBlank)?.let {
                TechnicalInfoBlock(icon = Icons.Default.Schedule, label = "Pendientes / Próximos pasos", value = it)
            }

            // Módulo de Comprobante y Cobro
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Estado de cobro",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        receipt?.let {
                            Text(
                                text = it.displayNumber(),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    if (receipt == null) {
                        Text(
                            text = "Sin comprobante emitido para este trabajo.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        FilledTonalButton(
                            onClick = { onRegisterPaymentClick("", visit.clientId, visit.id) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Registrar cobro", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        val balance = PaymentBalanceCalculator.balance(receipt.totalCents, uiState.payments)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            FinanceMiniPill(
                                label = "Total",
                                amount = MoneyFormatter.format(receipt.totalCents),
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            FinanceMiniPill(
                                label = "Cobrado",
                                amount = MoneyFormatter.format(balance.paidCents),
                                color = Color(0xFF2E7D32),
                            )
                            FinanceMiniPill(
                                label = "Pendiente",
                                amount = MoneyFormatter.format(balance.pendingCents),
                                color = if (balance.pendingCents > 0L) MaterialTheme.colorScheme.error else Color(0xFF2E7D32),
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            OutlinedButton(
                                onClick = { onReceiptClick(receipt.id) },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f),
                            ) {
                                Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Comprobante", maxLines = 1)
                            }
                            if (balance.pendingCents > 0L) {
                                Button(
                                    onClick = { onRegisterPaymentClick(receipt.id, receipt.clientId, visit.id) },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f),
                                ) {
                                    Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Cobrar", maxLines = 1)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FinanceMiniPill(label: String, amount: String, color: Color) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(amount, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
private fun TechnicalInfoBlock(
    icon: ImageVector,
    label: String,
    value: String,
    isEmphasized: Boolean = false,
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isEmphasized) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isEmphasized) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp).padding(top = 2.dp),
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

/**
 * Tarjeta del historial de sesiones de trabajo registradas durante la visita.
 */
@Composable
fun WorkSessionsCard(
    uiState: VisitDetailUiState,
    onEditSessionNotesClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                        modifier = Modifier.size(36.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Sesiones de trabajo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }

                uiState.workSummary?.let { summary ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    ) {
                        Text(
                            text = summary.totalWorkedDuration.formatCompactDuration(),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        )
                    }
                }
            }

            if (uiState.sessions.isEmpty()) {
                Text(
                    text = "No se registraron sesiones de trabajo activas.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                uiState.sessions.forEachIndexed { index, session ->
                    WorkSessionItem(
                        index = index + 1,
                        session = session,
                        now = uiState.now,
                        onEditSessionNotesClick = onEditSessionNotesClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun WorkSessionItem(
    index: Int,
    session: VisitWorkSession,
    now: Instant,
    onEditSessionNotesClick: (String) -> Unit,
) {
    val duration = VisitWorkSessionDurations.sessionDuration(session, now)
    val isRunning = session.status == VisitWorkSessionStatus.RUNNING

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isRunning) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = if (isRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                        modifier = Modifier.size(24.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "$index",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isRunning) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isRunning) "Sesión en curso" else "Sesión $index",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                ) {
                    Text(
                        text = duration.formatCompactDuration(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isRunning) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Inicio: ${session.startedAt.formatVisitDateTime()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = session.endedAt?.let { "Fin: ${it.formatVisitDateTime()}" } ?: "En progreso",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (isRunning) FontWeight.Bold else FontWeight.Normal,
                )
            }

            session.notes?.takeIf(String::isNotBlank)?.let { notes ->
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Default.NoteAlt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = notes,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }

            OutlinedButton(
                onClick = { onEditSessionNotesClick(session.id) },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (session.notes.isNullOrBlank()) "Agregar nota a la sesión" else "Editar nota de la sesión",
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
    }
}

/**
 * Línea de tiempo visual conectada con nodos e hitos del trabajo.
 */
@Composable
fun VisitTimelineCard(
    uiState: VisitDetailUiState,
    modifier: Modifier = Modifier,
) {
    val visit = uiState.visit ?: return

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f),
                    modifier = Modifier.size(36.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Línea de tiempo de la actividad",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                TimelineNode(
                    title = "Visita agendada",
                    time = visit.scheduledAt.formatVisitDateTime(),
                    icon = Icons.Default.Event,
                    isFirst = true,
                    isLast = visit.startedAt == null && uiState.sessions.isEmpty() && visit.completedAt == null,
                    isCompleted = true,
                )

                visit.startedAt?.let { startedAt ->
                    TimelineNode(
                        title = "Visita iniciada",
                        time = startedAt.formatVisitDateTime(),
                        icon = Icons.Default.PlayArrow,
                        isCompleted = true,
                        isLast = uiState.sessions.isEmpty() && visit.completedAt == null,
                    )
                }

                uiState.sessions.forEach { session ->
                    val isRunning = session.status == VisitWorkSessionStatus.RUNNING
                    TimelineNode(
                        title = if (isRunning) "Sesión en curso" else "Sesión de trabajo",
                        time = "Inicio: ${session.startedAt.formatVisitDateTime()}" +
                            (session.endedAt?.let { " · Fin: ${it.formatVisitDateTime()}" } ?: ""),
                        icon = if (isRunning) Icons.Default.PlayArrow else Icons.Default.Pause,
                        isCompleted = true,
                    )
                }

                visit.completedAt?.let { completedAt ->
                    TimelineNode(
                        title = "Visita finalizada",
                        time = completedAt.formatVisitDateTime(),
                        icon = Icons.Default.CheckCircle,
                        isCompleted = true,
                        isLast = true,
                        isFinalSuccess = true,
                    )
                }
            }
        }
    }
}

@Composable
private fun TimelineNode(
    title: String,
    time: String,
    icon: ImageVector,
    isCompleted: Boolean = true,
    isFirst: Boolean = false,
    isLast: Boolean = false,
    isFinalSuccess: Boolean = false,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(32.dp),
        ) {
            Surface(
                shape = CircleShape,
                color = when {
                    isFinalSuccess -> Color(0xFF2E7D32)
                    isCompleted -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.surfaceVariant
                },
                modifier = Modifier.size(24.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(28.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                )
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f).padding(bottom = if (isLast) 0.dp else 12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = time,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
