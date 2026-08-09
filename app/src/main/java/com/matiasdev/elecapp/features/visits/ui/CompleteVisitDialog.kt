package com.matiasdev.elecapp.features.visits.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
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

    val warnings = mutableListOf<String>()
    if (uiState.inspection == null) warnings.add("No existe relevamiento asociado.")
    if (uiState.inspection?.status == InspectionStatus.DRAFT) warnings.add("El relevamiento está en borrador.")
    if (uiState.quote?.status == QuoteStatus.DRAFT) warnings.add("El presupuesto está en borrador.")
    if (uiState.materialList?.status == MaterialListStatus.DRAFT) warnings.add("La lista de materiales está en borrador.")

    AlertDialog(
        onDismissRequest = viewModel::dismissCompleteVisit,
        icon = {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp),
            )
        },
        title = { Text("Finalizar visita técnica", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(clientName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text(visit.reason, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        uiState.workSummary?.let { summary ->
                            Text(
                                text = "Tiempo trabajado: ${summary.totalWorkedDuration.formatCompactDuration()}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                        Text(
                            text = "Relevamiento: $inspectionStatus",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                if (warnings.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)),
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "Advertencias:",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error,
                                )
                            }
                            warnings.forEach { warning ->
                                Text(
                                    text = "• $warning",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = uiState.completionNotes,
                    onValueChange = viewModel::onCompletionNotesChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Trabajo realizado") },
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp),
                )

                OutlinedTextField(
                    value = uiState.pendingWorkNotes,
                    onValueChange = viewModel::onPendingWorkNotesChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Trabajos o verificaciones pendientes") },
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = viewModel::confirmCompleteVisit,
                shape = RoundedCornerShape(10.dp),
            ) {
                Text("Finalizar", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = viewModel::dismissCompleteVisit,
                shape = RoundedCornerShape(10.dp),
            ) {
                Text("Cancelar")
            }
        },
    )
}

