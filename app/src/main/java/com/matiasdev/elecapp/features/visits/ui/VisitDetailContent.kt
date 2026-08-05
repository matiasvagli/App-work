package com.matiasdev.elecapp.features.visits.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.matiasdev.elecapp.core.external.browserMapsIntent
import com.matiasdev.elecapp.core.external.browserWhatsappIntent
import com.matiasdev.elecapp.core.external.dialIntent
import com.matiasdev.elecapp.core.external.mapsIntent
import com.matiasdev.elecapp.core.external.tryStartActivity
import com.matiasdev.elecapp.core.external.whatsappIntent
import com.matiasdev.elecapp.features.inspections.domain.InspectionStatus
import com.matiasdev.elecapp.features.materials.domain.MaterialListStatus
import com.matiasdev.elecapp.features.materials.summary.label
import com.matiasdev.elecapp.features.quotes.domain.QuoteStatus
import com.matiasdev.elecapp.features.quotes.summary.label
import com.matiasdev.elecapp.features.visits.domain.Visit
import com.matiasdev.elecapp.features.visits.domain.VisitStatus
import com.matiasdev.elecapp.features.visits.domain.VisitWorkActions
import com.matiasdev.elecapp.features.visits.domain.VisitWorkPrimaryAction
import com.matiasdev.elecapp.features.visits.domain.VisitWorkSession
import com.matiasdev.elecapp.features.visits.domain.VisitWorkSessionDurations
import com.matiasdev.elecapp.features.visits.domain.VisitWorkSessionStatus

@Composable
fun VisitDetailContent(
    uiState: VisitDetailUiState,
    onInspectionClick: () -> Unit,
    onQuoteClick: () -> Unit,
    onMaterialClick: () -> Unit,
    onElectricalToolsClick: () -> Unit,
    onStartVisitClick: () -> Unit,
    onPauseWorkClick: () -> Unit,
    onResumeWorkClick: () -> Unit,
    onCompleteVisitClick: () -> Unit,
    onEditSessionNotesClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val visit = uiState.visit
    when {
        uiState.isLoading -> Text("Cargando visita...", modifier = modifier.padding(24.dp))
        visit == null -> Text(uiState.errorMessage ?: "Visita no encontrada", modifier = modifier.padding(24.dp))
        else -> LazyColumn(
            modifier = modifier,
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp, 16.dp, 16.dp, 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item { VisitHeaderCard(uiState) }
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
            item { VisitQuickActions(uiState, onInspectionClick, onQuoteClick, onMaterialClick, onElectricalToolsClick) }
            item { VisitDocumentsCard(uiState, onInspectionClick, onQuoteClick, onMaterialClick) }
            item { WorkSessionsCard(uiState, onEditSessionNotesClick) }
            item { VisitTimelineCard(uiState) }
            item { VisitNotesCard(visit) }
        }
    }
}

@Composable
private fun VisitHeaderCard(uiState: VisitDetailUiState) {
    val visit = uiState.visit ?: return
    val client = uiState.client
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(visit.reason, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                AssistChip(onClick = {}, label = { Text(visit.status.label) })
            }
            DetailLine("Cliente", client?.fullName ?: "Cliente no encontrado")
            DetailLine("Fecha y hora", visit.scheduledAt.formatVisitDateTime())
            client?.address?.takeIf(String::isNotBlank)?.let { DetailLine("Dirección", listOf(it, client.locality).filterNotNull().joinToString(", ")) }
            visit.estimatedDurationMinutes?.let { DetailLine("Duración estimada", "$it minutos") }
            if (visit.status == VisitStatus.IN_PROGRESS) Text("Visita en curso", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
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
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        when (action) {
            VisitWorkPrimaryAction.START -> Button(onClick = onStartVisitClick, enabled = !uiState.isOperationInProgress, modifier = Modifier.fillMaxWidth()) { Text("Iniciar visita") }
            VisitWorkPrimaryAction.PAUSE -> Button(onClick = onPauseWorkClick, enabled = !uiState.isOperationInProgress, modifier = Modifier.fillMaxWidth()) { Text("Pausar trabajo") }
            VisitWorkPrimaryAction.RESUME -> Button(onClick = onResumeWorkClick, enabled = !uiState.isOperationInProgress, modifier = Modifier.fillMaxWidth()) { Text("Reanudar trabajo") }
            VisitWorkPrimaryAction.NONE -> Unit
        }
        if (visit.status == VisitStatus.IN_PROGRESS) {
            OutlinedButton(onClick = onCompleteVisitClick, enabled = !uiState.isOperationInProgress, modifier = Modifier.fillMaxWidth()) {
                Text("Finalizar visita")
            }
        }
    }
}

@Composable
private fun WorkTimerCard(uiState: VisitDetailUiState) {
    val visit = uiState.visit ?: return
    val summary = uiState.workSummary ?: return
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(if (visit.status == VisitStatus.IN_PROGRESS) "VISITA EN CURSO" else "Resumen de tiempos", fontWeight = FontWeight.Bold)
            Text("Tiempo trabajado", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(summary.totalWorkedDuration.formatTimerText(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            visit.startedAt?.let { Text("Inicio: ${it.formatVisitDateTime()}") }
            visit.completedAt?.let { Text("Fin: ${it.formatVisitDateTime()}") }
            Text("Sesiones: ${summary.sessionCount} · Pausas: ${summary.pauseCount}")
            Text("Tiempo pausado: ${summary.totalPausedDuration.formatCompactDuration()}")
            Text("Tiempo transcurrido: ${summary.totalElapsedDuration.formatCompactDuration()}")
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun VisitQuickActions(
    uiState: VisitDetailUiState,
    onInspectionClick: () -> Unit,
    onQuoteClick: () -> Unit,
    onMaterialClick: () -> Unit,
    onElectricalToolsClick: () -> Unit,
) {
    val context = LocalContext.current
    val client = uiState.client
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Acciones de trabajo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onInspectionClick, modifier = Modifier.widthIn(min = 136.dp)) { Text("Relevamiento") }
                OutlinedButton(onClick = onQuoteClick, modifier = Modifier.widthIn(min = 136.dp)) { Text("Presupuesto") }
                OutlinedButton(onClick = onMaterialClick, modifier = Modifier.widthIn(min = 136.dp)) { Text("Materiales") }
                OutlinedButton(onClick = onElectricalToolsClick, modifier = Modifier.widthIn(min = 136.dp)) { Text("Herramientas") }
            }
            Text("Comunicación", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { openWhatsapp(context, client?.phone) }, modifier = Modifier.widthIn(min = 136.dp)) { Text("WhatsApp") }
                OutlinedButton(onClick = { openDial(context, client?.phone) }, modifier = Modifier.widthIn(min = 136.dp)) { Text("Llamar") }
                OutlinedButton(onClick = { openMaps(context, client?.address, client?.locality) }, modifier = Modifier.widthIn(min = 136.dp)) { Text("Maps") }
            }
        }
    }
}

@Composable
private fun VisitDocumentsCard(uiState: VisitDetailUiState, onInspectionClick: () -> Unit, onQuoteClick: () -> Unit, onMaterialClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Documentos de la visita", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            DocumentRow("Relevamiento", inspectionVisitLabel(uiState.inspection?.status), if (uiState.inspection == null) "Iniciar" else "Abrir", onInspectionClick)
            DocumentRow("Presupuesto", quoteVisitLabel(uiState.quote?.status), if (uiState.quote == null) "Crear" else "Abrir", onQuoteClick)
            DocumentRow("Materiales", materialVisitLabel(uiState.materialList?.status), if (uiState.materialList == null) "Crear" else "Abrir", onMaterialClick)
        }
    }
}

@Composable
private fun DocumentRow(title: String, status: String, action: String, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(status, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        OutlinedButton(onClick = onClick) { Text(action) }
    }
}

@Composable
private fun WorkSessionsCard(uiState: VisitDetailUiState, onEditSessionNotesClick: (String) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Registro de trabajo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            val summary = uiState.workSummary
            if (summary != null) {
                Text("Total trabajado: ${summary.totalWorkedDuration.formatCompactDuration()}")
                Text("Total pausado: ${summary.totalPausedDuration.formatCompactDuration()}")
            }
            if (uiState.sessions.isEmpty()) {
                Text("Sin sesiones registradas", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                uiState.sessions.forEachIndexed { index, session -> WorkSessionRow(index + 1, session, uiState.now, onEditSessionNotesClick) }
            }
        }
    }
}

@Composable
private fun WorkSessionRow(index: Int, session: VisitWorkSession, now: java.time.Instant, onEditSessionNotesClick: (String) -> Unit) {
    val duration = VisitWorkSessionDurations.sessionDuration(session, now)
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Sesión $index", fontWeight = FontWeight.SemiBold)
        Text("Inicio: ${session.startedAt.formatVisitDateTime()}")
        Text(session.endedAt?.let { "Fin: ${it.formatVisitDateTime()}" } ?: "En curso")
        Text("Duración: ${duration.formatCompactDuration()}")
        session.notes?.let { Text("Nota: $it") }
        OutlinedButton(onClick = { onEditSessionNotesClick(session.id) }, modifier = Modifier.fillMaxWidth()) {
            Text(if (session.notes.isNullOrBlank()) "Agregar nota" else "Editar nota")
        }
    }
}

@Composable
private fun VisitTimelineCard(uiState: VisitDetailUiState) {
    val visit = uiState.visit ?: return
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Actividad de la visita", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("Visita agendada: ${visit.scheduledAt.formatVisitDateTime()}")
            visit.startedAt?.let { Text("Visita iniciada: ${it.formatVisitDateTime()}") }
            uiState.sessions.forEach { session ->
                val label = if (session.status == VisitWorkSessionStatus.RUNNING) "Trabajo reanudado" else "Trabajo registrado"
                Text("$label: ${session.startedAt.formatVisitDateTime()}")
                session.endedAt?.let { Text("Pausa o cierre: ${it.formatVisitDateTime()}") }
            }
            visit.completedAt?.let { Text("Visita finalizada: ${it.formatVisitDateTime()}") }
        }
    }
}

@Composable
private fun VisitNotesCard(visit: Visit) {
    if (visit.notes.isNullOrBlank() && visit.completionNotes.isNullOrBlank() && visit.pendingWorkNotes.isNullOrBlank()) return
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Notas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            visit.notes?.let { DetailLine("Notas iniciales", it) }
            visit.completionNotes?.let { DetailLine("Trabajo realizado", it) }
            visit.pendingWorkNotes?.let { DetailLine("Pendientes", it) }
        }
    }
}

@Composable
private fun DetailLine(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}

private fun openWhatsapp(context: android.content.Context, phone: String?) {
    val opened = whatsappIntent(phone.orEmpty())?.let(context::tryStartActivity) == true ||
        browserWhatsappIntent(phone.orEmpty())?.let(context::tryStartActivity) == true
    if (!opened) Toast.makeText(context, "No se pudo abrir WhatsApp", Toast.LENGTH_SHORT).show()
}

private fun openDial(context: android.content.Context, phone: String?) {
    if (!context.tryStartActivity(dialIntent(phone.orEmpty()))) Toast.makeText(context, "No hay app de llamadas", Toast.LENGTH_SHORT).show()
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

private fun quoteVisitLabel(status: QuoteStatus?): String = status?.label() ?: "No creado"

private fun materialVisitLabel(status: MaterialListStatus?): String = status?.label() ?: "Sin lista"
