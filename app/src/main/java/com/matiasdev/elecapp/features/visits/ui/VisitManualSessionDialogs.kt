package com.matiasdev.elecapp.features.visits.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ManualSessionDialog(uiState: VisitDetailUiState, viewModel: VisitDetailViewModel) {
    AlertDialog(
        onDismissRequest = viewModel::dismissManualSession,
        title = { Text("Registrar tiempo manual") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Formato: dd/MM/yyyy HH:mm", color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedTextField(
                    value = uiState.manualSessionStart,
                    onValueChange = viewModel::onManualStartChange,
                    label = { Text("Inicio") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = uiState.manualSessionEnd,
                    onValueChange = viewModel::onManualEndChange,
                    label = { Text("Fin") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = uiState.manualSessionNotes,
                    onValueChange = viewModel::onManualNotesChange,
                    label = { Text("Nota opcional") },
                    minLines = 2,
                )
                uiState.manualSessionError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = { TextButton(onClick = viewModel::confirmManualSession) { Text("Guardar") } },
        dismissButton = { TextButton(onClick = viewModel::dismissManualSession) { Text("Cancelar") } },
    )
}

@Composable
fun SessionNoteDialog(uiState: VisitDetailUiState, viewModel: VisitDetailViewModel) {
    val session = uiState.sessionNoteTarget ?: return
    AlertDialog(
        onDismissRequest = viewModel::dismissSessionNote,
        title = { Text("Nota de sesión") },
        text = {
            OutlinedTextField(
                value = uiState.sessionNoteText,
                onValueChange = viewModel::onSessionNoteChange,
                label = { Text("Nota") },
                minLines = 3,
                modifier = Modifier,
                supportingText = { Text("Sesión iniciada ${session.startedAt.formatVisitDateTime()}") },
            )
        },
        confirmButton = { TextButton(onClick = viewModel::saveSessionNote) { Text("Guardar") } },
        dismissButton = { TextButton(onClick = viewModel::dismissSessionNote) { Text("Cancelar") } },
    )
}
