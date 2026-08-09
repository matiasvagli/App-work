package com.matiasdev.elecapp.features.finance.ui

import android.content.Intent
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
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.text.AnnotatedString
import com.matiasdev.elecapp.features.clients.data.ClientRepository
import com.matiasdev.elecapp.features.finance.data.FinanceRepository
import com.matiasdev.elecapp.features.finance.domain.AttentionReportCoordinator
import com.matiasdev.elecapp.features.inspections.data.InspectionRepository
import com.matiasdev.elecapp.features.visits.data.VisitRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttentionReportsScreen(
    visitId: String,
    financeRepository: FinanceRepository,
    visitRepository: VisitRepository,
    clientRepository: ClientRepository,
    inspectionRepository: InspectionRepository,
    attentionReportCoordinator: AttentionReportCoordinator,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AttentionReportsViewModel = viewModel(
        factory = AttentionReportsViewModelFactory(
            visitId = visitId,
            financeRepository = financeRepository,
            visitRepository = visitRepository,
            clientRepository = clientRepository,
            inspectionRepository = inspectionRepository,
            reportCoordinator = attentionReportCoordinator,
        ),
    ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is AttentionReportsEvent.Message -> snackbarHostState.showSnackbar(event.text)
                is AttentionReportsEvent.CopyToClipboard -> clipboard.setText(AnnotatedString(event.text))
                is AttentionReportsEvent.Share -> {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, event.text)
                    }
                    context.startActivity(Intent.createChooser(intent, "Compartir informe"))
                }
            }
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Informes de la atención") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
            )
        },
    ) { padding ->
        AttentionReportsContent(
            uiState = uiState,
            onCopyTechnical = viewModel::copyTechnicalReport,
            onShareTechnical = viewModel::shareTechnicalReport,
            onCopyAiPrompt = viewModel::copyAiPrompt,
            onShareAiPrompt = viewModel::shareAiPrompt,
            onRegenerate = viewModel::regenerateTechnicalReport,
            onClientReportChange = viewModel::updateClientReport,
            onSaveClientReport = viewModel::saveClientReport,
            onShareClientReport = viewModel::shareClientReport,
            modifier = Modifier.padding(padding),
        )
    }
}

@Composable
fun AttentionReportsContent(
    uiState: AttentionReportsUiState,
    onCopyTechnical: () -> Unit,
    onShareTechnical: () -> Unit,
    onCopyAiPrompt: () -> Unit,
    onShareAiPrompt: () -> Unit,
    onRegenerate: () -> Unit,
    onClientReportChange: (String) -> Unit,
    onSaveClientReport: () -> Unit,
    onShareClientReport: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        uiState.isLoading -> CircularProgressIndicator(modifier = modifier.padding(24.dp))
        else -> Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            uiState.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            TechnicalReportCard(uiState, onCopyTechnical, onShareTechnical, onRegenerate)
            ClientReportCard(
                uiState = uiState,
                onCopyAiPrompt = onCopyAiPrompt,
                onShareAiPrompt = onShareAiPrompt,
                onClientReportChange = onClientReportChange,
                onSaveClientReport = onSaveClientReport,
                onShareClientReport = onShareClientReport,
            )
        }
    }
}

@Composable
private fun TechnicalReportCard(
    uiState: AttentionReportsUiState,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onRegenerate: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Informe técnico", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            if (!uiState.hasTechnicalReport) {
                Text(
                    "Todavía no se generó. Se congela al finalizar la atención; " +
                        "las atenciones sin relevamiento no generan informe técnico.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedButton(onClick = onRegenerate, enabled = !uiState.isSaving) { Text("Generar ahora") }
                return@Column
            }
            if (uiState.showsStaleWarning) {
                Text(
                    "Se editaron datos después de generar este informe. El guardado sigue " +
                        "siendo el vigente hasta que lo regeneres.",
                    color = MaterialTheme.colorScheme.error,
                )
            }
            Text(uiState.technicalReport.orEmpty(), style = MaterialTheme.typography.bodySmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onCopy) { Text("Copiar") }
                OutlinedButton(onClick = onShare) { Text("Enviar simple") }
            }
            if (uiState.showsStaleWarning) {
                OutlinedButton(onClick = onRegenerate, enabled = !uiState.isSaving) { Text("Regenerar") }
            }
        }
    }
}

@Composable
private fun ClientReportCard(
    uiState: AttentionReportsUiState,
    onCopyAiPrompt: () -> Unit,
    onShareAiPrompt: () -> Unit,
    onClientReportChange: (String) -> Unit,
    onSaveClientReport: () -> Unit,
    onShareClientReport: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Informe para el cliente", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(
                "Copiá la plantilla, pegala en tu IA y traé la respuesta acá. " +
                    "Revisá los valores antes de enviársela al cliente.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onCopyAiPrompt, enabled = uiState.hasTechnicalReport) {
                    Text("Copiar plantilla")
                }
                OutlinedButton(onClick = onShareAiPrompt, enabled = uiState.hasTechnicalReport) {
                    Text("Abrir en IA")
                }
            }
            OutlinedTextField(
                value = uiState.clientReport,
                onValueChange = onClientReportChange,
                label = { Text("Informe del cliente") },
                placeholder = { Text("Pegá acá la respuesta de la IA, o redactalo a mano") },
                minLines = 6,
                modifier = Modifier.fillMaxWidth(),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onSaveClientReport, enabled = !uiState.isSaving) { Text("Guardar") }
                OutlinedButton(
                    onClick = onShareClientReport,
                    enabled = uiState.clientReport.isNotBlank(),
                ) { Text("Enviar al cliente") }
            }
        }
    }
}
