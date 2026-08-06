package com.matiasdev.elecapp.features.visits.ui

import android.widget.Toast
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.matiasdev.elecapp.core.external.calendarInsertIntent
import com.matiasdev.elecapp.core.external.tryStartActivity
import com.matiasdev.elecapp.features.clients.data.ClientRepository
import com.matiasdev.elecapp.features.finance.data.FinanceRepository
import com.matiasdev.elecapp.features.inspections.data.InspectionRepository
import com.matiasdev.elecapp.features.materials.data.MaterialRepository
import com.matiasdev.elecapp.features.quotes.data.QuoteRepository
import com.matiasdev.elecapp.features.reminders.scheduling.ReminderCoordinator
import com.matiasdev.elecapp.features.visits.data.VisitRepository
import com.matiasdev.elecapp.features.visits.data.VisitWorkSessionRepository
import com.matiasdev.elecapp.features.visits.domain.VisitStatus
import com.matiasdev.elecapp.features.visits.domain.VisitWorkSessionStatus
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisitDetailScreen(
    clientRepository: ClientRepository,
    visitRepository: VisitRepository,
    workSessionRepository: VisitWorkSessionRepository,
    financeRepository: FinanceRepository,
    inspectionRepository: InspectionRepository,
    quoteRepository: QuoteRepository,
    materialRepository: MaterialRepository,
    reminderCoordinator: ReminderCoordinator,
    visitId: String,
    onBackClick: () -> Unit,
    onEditClick: (String) -> Unit,
    onInspectionClick: (String) -> Unit,
    onCreateInspectionClick: (String) -> Unit,
    onCreateQuoteClick: (String, String) -> Unit,
    onQuoteClick: (String) -> Unit,
    onCreateMaterialClick: (String, String) -> Unit,
    onMaterialClick: (String) -> Unit,
    onElectricalToolsClick: (String, String) -> Unit,
    onCloseVisitClick: (String) -> Unit,
    onReceiptClick: (String) -> Unit,
    onRegisterPaymentClick: (String, String, String?) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: VisitDetailViewModel = viewModel(
        factory = VisitDetailViewModelFactory(
            clientRepository,
            visitRepository,
            workSessionRepository,
            financeRepository,
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
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is VisitDetailEvent.Message -> snackbarHostState.showSnackbar(event.text)
            }
        }
    }
    LaunchedEffect(uiState.workSummary?.activeSession?.id) {
        while (uiState.workSummary?.activeSession?.status == VisitWorkSessionStatus.RUNNING) {
            viewModel.refreshNow()
            delay(1_000)
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            VisitDetailTopBar(
                onBackClick = onBackClick,
                onEditClick = { uiState.visit?.id?.let(onEditClick) },
                onCalendarClick = {
                    val visit = uiState.visit
                    val client = uiState.client
                    if (visit != null && client != null && !context.tryStartActivity(calendarInsertIntent(client, visit))) {
                        Toast.makeText(context, "No hay una app de calendario", Toast.LENGTH_SHORT).show()
                    }
                },
                onManualSessionClick = viewModel::requestManualSession,
                onCancelClick = { uiState.visit?.let { viewModel.askStatusChange(VisitStatus.CANCELLED) } },
                onDeleteClick = { uiState.visit?.let(viewModel::askDelete) },
            )
        },
    ) { padding ->
        VisitDetailContent(
            uiState = uiState,
            onInspectionClick = { viewModel.requestOpenInspection(onInspectionClick, onCreateInspectionClick) },
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
            onPauseWorkClick = viewModel::pauseWork,
            onResumeWorkClick = viewModel::resumeWork,
            onCompleteVisitClick = { uiState.visit?.id?.let(onCloseVisitClick) },
            onReceiptClick = onReceiptClick,
            onRegisterPaymentClick = onRegisterPaymentClick,
            onEditSessionNotesClick = viewModel::requestEditSessionNotes,
            modifier = Modifier.padding(padding),
        )
    }
    VisitDetailDialogs(uiState, viewModel, onBackClick, onCreateInspectionClick)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VisitDetailTopBar(
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onCalendarClick: () -> Unit,
    onManualSessionClick: () -> Unit,
    onCancelClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    TopAppBar(
        title = { Text("Detalle de visita") },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
            }
        },
        actions = {
            IconButton(onClick = onEditClick) { Icon(Icons.Default.Edit, contentDescription = "Editar visita") }
            IconButton(onClick = { expanded = true }) { Icon(Icons.Default.MoreVert, contentDescription = "Más opciones") }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                DropdownMenuItem(
                    text = { Text("Agregar al calendario") },
                    leadingIcon = { Icon(Icons.Default.Event, contentDescription = null) },
                    onClick = { expanded = false; onCalendarClick() },
                )
                DropdownMenuItem(
                    text = { Text("Registrar tiempo manual") },
                    leadingIcon = { Icon(Icons.Default.Timer, contentDescription = null) },
                    onClick = { expanded = false; onManualSessionClick() },
                )
                DropdownMenuItem(text = { Text("Cancelar visita") }, onClick = { expanded = false; onCancelClick() })
                DropdownMenuItem(
                    text = { Text("Eliminar visita") },
                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                    onClick = { expanded = false; onDeleteClick() },
                )
            }
        },
    )
}

@Composable
private fun VisitDetailDialogs(
    uiState: VisitDetailUiState,
    viewModel: VisitDetailViewModel,
    onDeleted: () -> Unit,
    onCreateInspectionClick: (String) -> Unit,
) {
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
            confirmButton = { TextButton(onClick = { viewModel.confirmDelete(onDeleted) }) { Text("Eliminar") } },
            dismissButton = { TextButton(onClick = viewModel::dismissDelete) { Text("Cancelar") } },
        )
    }
    if (uiState.showCancelledInspectionWarning) {
        AlertDialog(
            onDismissRequest = viewModel::dismissCancelledInspectionWarning,
            title = { Text("Visita cancelada") },
            text = { Text("La visita está cancelada. ¿Querés iniciar el relevamiento igualmente?") },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmStartCancelledInspection(onCreateInspectionClick) }) { Text("Iniciar") }
            },
            dismissButton = { TextButton(onClick = viewModel::dismissCancelledInspectionWarning) { Text("Cancelar") } },
        )
    }
    if (uiState.showStartVisitConfirmation || uiState.showDistantStartWarning) StartVisitDialog(uiState, viewModel)
    if (uiState.showCompletionDialog) CompleteVisitDialog(uiState, viewModel)
    if (uiState.showManualSessionDialog) ManualSessionDialog(uiState, viewModel)
    if (uiState.sessionNoteTarget != null) SessionNoteDialog(uiState, viewModel)
}

@Composable
private fun StartVisitDialog(uiState: VisitDetailUiState, viewModel: VisitDetailViewModel) {
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
