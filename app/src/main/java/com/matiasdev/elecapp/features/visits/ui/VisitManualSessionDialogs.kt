package com.matiasdev.elecapp.features.visits.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreTime
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ManualSessionDialog(uiState: VisitDetailUiState, viewModel: VisitDetailViewModel) {
    AlertDialog(
        onDismissRequest = viewModel::dismissManualSession,
        icon = {
            Icon(
                Icons.Default.MoreTime,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp),
            )
        },
        title = { Text("Registrar tiempo manual", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Formato fecha/hora: dd/MM/yyyy HH:mm",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedTextField(
                    value = uiState.manualSessionStart,
                    onValueChange = viewModel::onManualStartChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Inicio (ej: 09/08/2026 14:00)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                )
                OutlinedTextField(
                    value = uiState.manualSessionEnd,
                    onValueChange = viewModel::onManualEndChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Fin (ej: 09/08/2026 16:30)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                )
                OutlinedTextField(
                    value = uiState.manualSessionNotes,
                    onValueChange = viewModel::onManualNotesChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Nota u observación (opcional)") },
                    minLines = 2,
                    shape = RoundedCornerShape(12.dp),
                )
                uiState.manualSessionError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = viewModel::confirmManualSession,
                shape = RoundedCornerShape(10.dp),
            ) {
                Text("Guardar", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = viewModel::dismissManualSession,
                shape = RoundedCornerShape(10.dp),
            ) {
                Text("Cancelar")
            }
        },
    )
}

@Composable
fun SessionNoteDialog(uiState: VisitDetailUiState, viewModel: VisitDetailViewModel) {
    val session = uiState.sessionNoteTarget ?: return
    AlertDialog(
        onDismissRequest = viewModel::dismissSessionNote,
        icon = {
            Icon(
                Icons.Default.NoteAdd,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp),
            )
        },
        title = { Text("Nota de la sesión", fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = uiState.sessionNoteText,
                onValueChange = viewModel::onSessionNoteChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Detalle o nota de la jornada") },
                minLines = 3,
                shape = RoundedCornerShape(12.dp),
                supportingText = { Text("Sesión iniciada: ${session.startedAt.formatVisitDateTime()}") },
            )
        },
        confirmButton = {
            Button(
                onClick = viewModel::saveSessionNote,
                shape = RoundedCornerShape(10.dp),
            ) {
                Text("Guardar", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = viewModel::dismissSessionNote,
                shape = RoundedCornerShape(10.dp),
            ) {
                Text("Cancelar")
            }
        },
    )
}

