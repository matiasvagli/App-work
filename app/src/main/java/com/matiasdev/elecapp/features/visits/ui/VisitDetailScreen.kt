package com.matiasdev.elecapp.features.visits.ui

import android.widget.Toast
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.matiasdev.elecapp.core.external.browserMapsIntent
import com.matiasdev.elecapp.core.external.browserWhatsappIntent
import com.matiasdev.elecapp.core.external.calendarInsertIntent
import com.matiasdev.elecapp.core.external.dialIntent
import com.matiasdev.elecapp.core.external.mapsIntent
import com.matiasdev.elecapp.core.external.tryStartActivity
import com.matiasdev.elecapp.core.external.whatsappIntent
import com.matiasdev.elecapp.features.clients.data.ClientRepository
import com.matiasdev.elecapp.features.inspections.data.InspectionRepository
import com.matiasdev.elecapp.features.inspections.domain.InspectionStatus
import com.matiasdev.elecapp.features.materials.data.MaterialRepository
import com.matiasdev.elecapp.features.quotes.data.QuoteRepository
import com.matiasdev.elecapp.features.reminders.scheduling.ReminderCoordinator
import com.matiasdev.elecapp.features.visits.data.VisitRepository
import com.matiasdev.elecapp.features.visits.domain.Visit
import com.matiasdev.elecapp.features.visits.domain.VisitStatus
import java.time.Duration
import java.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisitDetailScreen(
    clientRepository: ClientRepository,
    visitRepository: VisitRepository,
    inspectionRepository: InspectionRepository,
    quoteRepository: QuoteRepository,
    materialRepository: MaterialRepository,
    reminderCoordinator: ReminderCoordinator,
    visitId: String,
    onBackClick: () -> Unit,
    onEditClick: (String) -> Unit,
    onInspectionClick: (String) -> Unit,
    onCreateQuoteClick: (String, String) -> Unit,
    onQuoteClick: (String) -> Unit,
    onCreateMaterialClick: (String, String) -> Unit,
    onMaterialClick: (String) -> Unit,
    onElectricalToolsClick: (String, String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: VisitDetailViewModel = viewModel(
        factory = VisitDetailViewModelFactory(
            clientRepository,
            visitRepository,
            inspectionRepository,
            quoteRepository,
            materialRepository,
            reminderCoordinator,
            visitId,
        ),
    ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbar()
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Detalle de visita") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    uiState.visit?.let { visit ->
                        IconButton(onClick = { onEditClick(visit.id) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar visita")
                        }
                        IconButton(onClick = { viewModel.askDelete(visit) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar visita")
                        }
                    }
                },
            )
        },
    ) { padding ->
        VisitDetailContent(
            uiState = uiState,
            allowedStatuses = viewModel.allowedNextStatuses(),
            onEditClick = onEditClick,
            onInspectionClick = { viewModel.requestOpenInspection(onInspectionClick) },
            onQuoteClick = {
                val quote = uiState.quote
                val visit = uiState.visit
                val client = uiState.client
                if (quote != null) onQuoteClick(quote.id) else if (visit != null && client != null) onCreateQuoteClick(visit.id, client.id)
            },
            onMaterialClick = {
                val list = uiState.materialList
                val visit = uiState.visit
                val client = uiState.client
                if (list != null) onMaterialClick(list.id) else if (visit != null && client != null) onCreateMaterialClick(visit.id, client.id)
            },
            onElectricalToolsClick = {
                val visit = uiState.visit
                val client = uiState.client
                if (visit != null && client != null) onElectricalToolsClick(visit.id, client.id)
            },
            onStartVisitClick = viewModel::requestStartVisit,
            onCompleteVisitClick = viewModel::requestCompleteVisit,
            onStatusClick = viewModel::askStatusChange,
            onDeleteClick = viewModel::askDelete, modifier = Modifier.padding(padding),
        )
    }
    uiState.statusPendingChange?.let { status ->
        AlertDialog(
            onDismissRequest = viewModel::dismissStatusChange,
            title = { Text("Cambiar estado") },
            text = { Text("¿Cambiar la visita a ${status.label.lowercase()}?") },
            confirmButton = { TextButton(onClick = viewModel::confirmStatusChange) { Text("Confirmar") } },
            dismissButton = { TextButton(onClick = viewModel::dismissStatusChange) { Text("Cancelar") } },
        )
    }

    uiState.visitPendingDelete?.let {
        AlertDialog(
            onDismissRequest = viewModel::dismissDelete,
            title = { Text("Eliminar visita") },
            text = { Text("¿Querés eliminar esta visita?") },
            confirmButton = { TextButton(onClick = { viewModel.confirmDelete(onBackClick) }) { Text("Eliminar") } },
            dismissButton = { TextButton(onClick = viewModel::dismissDelete) { Text("Cancelar") } },
        )
    }

    if (uiState.showCancelledInspectionWarning) {
        AlertDialog(
            onDismissRequest = viewModel::dismissCancelledInspectionWarning,
            title = { Text("Visita cancelada") },
            text = { Text("La visita está cancelada. ¿Querés iniciar el relevamiento igualmente?") },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmStartCancelledInspection(onInspectionClick) }) { Text("Iniciar") }
            },
            dismissButton = { TextButton(onClick = viewModel::dismissCancelledInspectionWarning) { Text("Cancelar") } },
        )
    }
    if (uiState.showStartVisitConfirmation || uiState.showDistantStartWarning) {
        AlertDialog(
            onDismissRequest = viewModel::dismissStartVisit,
            title = { Text("Iniciar visita") },
            text = {
                Text(
                    if (uiState.showDistantStartWarning) {
                        "La visita está programada para otro momento. ¿Querés iniciarla igualmente?"
                    } else {
                        "¿Querés marcar esta visita como en curso?"
                    },
                )
            },
            confirmButton = { TextButton(onClick = viewModel::confirmStartVisit) { Text("Iniciar") } },
            dismissButton = { TextButton(onClick = viewModel::dismissStartVisit) { Text("Cancelar") } },
        )
    }
    if (uiState.showCompletionDialog) {
        CompleteVisitDialog(uiState, viewModel)
    }
}

@Composable
private fun VisitDetailContent(
    uiState: VisitDetailUiState,
    allowedStatuses: List<VisitStatus>,
    onEditClick: (String) -> Unit,
    onInspectionClick: () -> Unit,
    onQuoteClick: () -> Unit,
    onMaterialClick: () -> Unit,
    onElectricalToolsClick: () -> Unit,
    onStartVisitClick: () -> Unit,
    onCompleteVisitClick: () -> Unit,
    onStatusClick: (VisitStatus) -> Unit,
    onDeleteClick: (Visit) -> Unit,
    modifier: Modifier = Modifier,
) {
    val visit = uiState.visit
    val client = uiState.client
    when {
        uiState.isLoading -> CircularProgressIndicator(modifier = modifier.padding(24.dp))
        visit == null -> Text(uiState.errorMessage ?: "Visita no encontrada", modifier = modifier.padding(24.dp))
        else -> Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            if (visit.status == VisitStatus.IN_PROGRESS) {
                InProgressHeader(uiState, onInspectionClick, onQuoteClick, onMaterialClick, onElectricalToolsClick, onCompleteVisitClick)
            }
            Text(visit.reason, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            DetailLine("Cliente", client?.fullName ?: "Cliente no encontrado")
            DetailLine("Fecha y hora", visit.scheduledAt.formatVisitDateTime())
            visit.estimatedDurationMinutes?.let { DetailLine("Duración", "$it minutos") }
            DetailLine("Estado", visit.status.label)
            visit.startedAt?.let { DetailLine("Inicio", it.formatVisitDateTime()) }
            visit.completedAt?.let { DetailLine("Finalización", it.formatVisitDateTime()) }
            visit.completionNotes?.let { DetailLine("Trabajo realizado", it) }
            visit.pendingWorkNotes?.let { DetailLine("Pendientes", it) }
            visit.notes?.let { DetailLine("Notas", it) }
            CalendarButton(uiState)
            if (visit.status in listOf(VisitStatus.PENDING, VisitStatus.CONFIRMED)) {
                Button(onClick = onStartVisitClick, modifier = Modifier.fillMaxWidth()) {
                    Text("Iniciar visita")
                }
            }
            if (visit.status != VisitStatus.IN_PROGRESS) {
                InspectionButton(uiState, onInspectionClick)
                VisitDocumentsSection(uiState, onQuoteClick, onMaterialClick)
                OutlinedButton(onClick = onElectricalToolsClick, modifier = Modifier.fillMaxWidth()) {
                    Text("Herramientas")
                }
            }
            OutlinedButton(onClick = { onEditClick(visit.id) }, modifier = Modifier.fillMaxWidth()) {
                Text("Editar")
            }
            StatusButtons(allowedStatuses, onStatusClick)
            OutlinedButton(onClick = { onDeleteClick(visit) }, modifier = Modifier.fillMaxWidth()) {
                Text("Eliminar")
            }
        }
    }
}

@Composable
private fun InProgressHeader(
    uiState: VisitDetailUiState,
    onInspectionClick: () -> Unit,
    onQuoteClick: () -> Unit,
    onMaterialClick: () -> Unit,
    onElectricalToolsClick: () -> Unit,
    onCompleteVisitClick: () -> Unit,
) {
    val visit = uiState.visit ?: return
    val client = uiState.client
    Column(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Visita en curso", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        visit.startedAt?.let {
            Text("Inicio: ${it.formatVisitDateTime()} · ${elapsedText(it)}")
        }
        Text(client?.fullName ?: "Cliente no encontrado")
        Text(listOf(client?.address, client?.locality).filterNotNull().filter(String::isNotBlank).joinToString(", "))
        Text("Motivo: ${visit.reason}")
        InspectionButton(uiState, onInspectionClick)
        VisitDocumentsSection(uiState, onQuoteClick, onMaterialClick)
        OutlinedButton(onClick = onElectricalToolsClick, modifier = Modifier.fillMaxWidth()) {
            Text("Herramientas")
        }
        QuickContactActions(uiState)
        Button(onClick = onCompleteVisitClick, modifier = Modifier.fillMaxWidth()) {
            Text("Finalizar visita")
        }
    }
}

@Composable
private fun QuickContactActions(uiState: VisitDetailUiState) {
    val context = LocalContext.current
    val client = uiState.client ?: return
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = {
                val opened = whatsappIntent(client.phone)?.let(context::tryStartActivity) == true ||
                    browserWhatsappIntent(client.phone)?.let(context::tryStartActivity) == true
                if (!opened) Toast.makeText(context, "No se pudo abrir WhatsApp", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.weight(1f),
        ) { Text("WhatsApp") }
        OutlinedButton(
            onClick = { if (!context.tryStartActivity(dialIntent(client.phone))) Toast.makeText(context, "No hay app de llamadas", Toast.LENGTH_SHORT).show() },
            modifier = Modifier.weight(1f),
        ) { Text("Llamar") }
        OutlinedButton(
            onClick = {
                val opened = mapsIntent(client.address, client.locality)?.let(context::tryStartActivity) == true ||
                    browserMapsIntent(client.address, client.locality)?.let(context::tryStartActivity) == true
                if (!opened) Toast.makeText(context, "No hay una app de mapas", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.weight(1f),
        ) { Text("Maps") }
    }
}

@Composable
private fun InspectionButton(uiState: VisitDetailUiState, onInspectionClick: () -> Unit) {
    val label = when (uiState.inspection?.status) {
        null -> "Iniciar relevamiento"
        InspectionStatus.DRAFT -> "Continuar relevamiento"
        InspectionStatus.COMPLETED -> "Ver relevamiento"
    }
    Button(onClick = onInspectionClick, modifier = Modifier.fillMaxWidth()) {
        Text(label)
    }
    val statusText = when (uiState.inspection?.status) {
        null -> "Sin relevamiento"
        InspectionStatus.DRAFT -> "Relevamiento en borrador"
        InspectionStatus.COMPLETED -> "Relevamiento finalizado"
    }
    Text(statusText, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun CalendarButton(uiState: VisitDetailUiState) {
    val context = LocalContext.current
    val client = uiState.client ?: return
    val visit = uiState.visit ?: return
    Button(
        onClick = {
            if (!context.tryStartActivity(calendarInsertIntent(client, visit))) {
                Toast.makeText(context, "No hay una app de calendario", Toast.LENGTH_SHORT).show()
            }
        },
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text("Agregar al calendario")
    }
}

@Composable
private fun StatusButtons(allowedStatuses: List<VisitStatus>, onStatusClick: (VisitStatus) -> Unit) {
    if (allowedStatuses.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Cambiar estado", style = MaterialTheme.typography.titleMedium)
        if (VisitStatus.CONFIRMED in allowedStatuses) {
            OutlinedButton(onClick = { onStatusClick(VisitStatus.CONFIRMED) }, modifier = Modifier.fillMaxWidth()) {
                Text("Confirmar")
            }
        }
        if (VisitStatus.COMPLETED in allowedStatuses) {
            OutlinedButton(onClick = { onStatusClick(VisitStatus.COMPLETED) }, modifier = Modifier.fillMaxWidth()) {
                Text("Marcar realizada")
            }
        }
        if (VisitStatus.CANCELLED in allowedStatuses) {
            OutlinedButton(onClick = { onStatusClick(VisitStatus.CANCELLED) }, modifier = Modifier.fillMaxWidth()) {
                Text("Cancelar visita")
            }
        }
    }
}

@Composable
private fun DetailLine(label: String, value: String) {
    Column { Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(value, style = MaterialTheme.typography.bodyLarge) }
}

private fun elapsedText(startedAt: Instant): String {
    val minutes = Duration.between(startedAt, Instant.now()).toMinutes().coerceAtLeast(0)
    val hours = minutes / 60
    val remaining = minutes % 60
    return if (hours > 0) "hace ${hours}h ${remaining}m" else "hace ${remaining}m"
}
