package com.matiasdev.elecapp.features.finance.ui

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.matiasdev.elecapp.core.ui.components.ElecLoadingState
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
                title = { Text("Informes de la atención", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
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
        uiState.isLoading -> ElecLoadingState(message = "Cargando informes...", modifier = modifier)
        else -> Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            uiState.errorMessage?.let {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(12.dp),
                    )
                }
            }

            TechnicalReportCard(uiState, onCopyTechnical, onShareTechnical, onRegenerate)
            ClientReportCard(
                uiState = uiState,
                onCopyAiPrompt = onCopyAiPrompt,
                onShareAiPrompt = onShareAiPrompt,
                onClientReportChange = onClientReportChange,
                onSaveClientReport = onSaveClientReport,
                onShareClientReport = onShareClientReport,
            )
            Spacer(modifier = Modifier.height(16.dp))
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
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                    modifier = Modifier.size(36.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("Informe técnico", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Detalle normativo e ingeniería", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            if (!uiState.hasTechnicalReport) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "Todavía no se generó. Se congela al finalizar la atención; las atenciones sin relevamiento no generan informe técnico.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(12.dp),
                    )
                }
                FilledTonalButton(
                    onClick = onRegenerate,
                    enabled = !uiState.isSaving,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Generar ahora", fontWeight = FontWeight.Bold)
                }
                return@Column
            }

            if (uiState.showsStaleWarning) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "Se editaron datos después de generar este informe. El guardado sigue siendo el vigente hasta que lo regeneres.",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(10.dp),
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = uiState.technicalReport.orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(12.dp),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = onCopy,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copiar", maxLines = 1)
                }
                OutlinedButton(
                    onClick = onShare,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Enviar", maxLines = 1)
                }
            }

            if (uiState.showsStaleWarning) {
                FilledTonalButton(
                    onClick = onRegenerate,
                    enabled = !uiState.isSaving,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Regenerar informe", fontWeight = FontWeight.Bold)
                }
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
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f),
                    modifier = Modifier.size(36.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("Informe para el cliente", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Redacción clara y amigable", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.25f),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "Copiá la plantilla con tus datos estructurados para generar una explicación clara con IA o redactala directamente.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(10.dp),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = onCopyAiPrompt,
                    enabled = uiState.hasTechnicalReport,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copiar prompt", maxLines = 1)
                }
                OutlinedButton(
                    onClick = onShareAiPrompt,
                    enabled = uiState.hasTechnicalReport,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Abrir en IA", maxLines = 1)
                }
            }

            OutlinedTextField(
                value = uiState.clientReport,
                onValueChange = onClientReportChange,
                label = { Text("Informe final para el cliente") },
                placeholder = { Text("Pegá acá la respuesta de la IA, o redactalo a mano...") },
                shape = RoundedCornerShape(14.dp),
                minLines = 6,
                modifier = Modifier.fillMaxWidth(),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = onSaveClientReport,
                    enabled = !uiState.isSaving,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Guardar", maxLines = 1)
                }
                Button(
                    onClick = onShareClientReport,
                    enabled = uiState.clientReport.isNotBlank(),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Enviar", maxLines = 1)
                }
            }
        }
    }
}

