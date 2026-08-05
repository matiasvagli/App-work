package com.matiasdev.elecapp.features.visits.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.matiasdev.elecapp.features.inspections.domain.InspectionStatus
import com.matiasdev.elecapp.features.materials.domain.MaterialListStatus
import com.matiasdev.elecapp.features.quotes.domain.QuoteStatus

@Composable
fun CompleteVisitDialog(uiState: VisitDetailUiState, viewModel: VisitDetailViewModel) {
    val clientName = uiState.client?.fullName ?: "Cliente no encontrado"
    val visit = uiState.visit ?: return
    val inspectionStatus = when (uiState.inspection?.status) {
        null -> "No iniciado"
        InspectionStatus.DRAFT -> "En borrador"
        InspectionStatus.COMPLETED -> "Finalizado"
    }
    AlertDialog(
        onDismissRequest = viewModel::dismissCompleteVisit,
        title = { Text("Finalizar visita") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("$clientName · ${visit.reason}")
                visit.startedAt?.let { Text("Inicio: ${it.formatVisitDateTime()}") }
                Text("Fin: ahora")
                uiState.workSummary?.let { summary ->
                    Text("Tiempo trabajado: ${summary.totalWorkedDuration.formatCompactDuration()}")
                    Text("Pausas: ${summary.totalPausedDuration.formatCompactDuration()}")
                    Text("Sesiones: ${summary.sessionCount}")
                }
                Text("Relevamiento: $inspectionStatus")
                if (uiState.inspection == null) Text("Advertencia: no existe relevamiento asociado.")
                if (uiState.inspection?.status == InspectionStatus.DRAFT) Text("Advertencia: el relevamiento está en borrador.")
                if (uiState.quote?.status == QuoteStatus.DRAFT) Text("Advertencia: el presupuesto está en borrador.")
                if (uiState.materialList?.status == MaterialListStatus.DRAFT) {
                    Text("Advertencia: la lista de materiales está en borrador.")
                }
                OutlinedTextField(
                    uiState.completionNotes,
                    viewModel::onCompletionNotesChange,
                    label = { Text("Trabajo realizado") },
                    minLines = 3,
                )
                OutlinedTextField(
                    uiState.pendingWorkNotes,
                    viewModel::onPendingWorkNotesChange,
                    label = { Text("Trabajos o verificaciones pendientes") },
                    minLines = 3,
                )
            }
        },
        confirmButton = { TextButton(onClick = viewModel::confirmCompleteVisit) { Text("Finalizar") } },
        dismissButton = { TextButton(onClick = viewModel::dismissCompleteVisit) { Text("Cancelar") } },
    )
}
