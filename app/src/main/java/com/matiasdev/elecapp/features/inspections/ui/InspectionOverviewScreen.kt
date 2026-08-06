package com.matiasdev.elecapp.features.inspections.ui

import android.content.Context
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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.matiasdev.elecapp.R
import com.matiasdev.elecapp.features.inspections.data.InspectionRepository
import com.matiasdev.elecapp.features.electricaltools.data.TechnicalCalculationRepository
import com.matiasdev.elecapp.features.electricaltools.domain.TechnicalCalculation
import com.matiasdev.elecapp.features.electricaltools.ui.primaryResultText
import com.matiasdev.elecapp.features.electricaltools.summary.label
import com.matiasdev.elecapp.features.inspections.domain.InspectionSection
import com.matiasdev.elecapp.features.inspections.domain.InspectionScope
import com.matiasdev.elecapp.features.inspections.domain.InspectionStatus
import com.matiasdev.elecapp.features.inspections.summary.InspectionSummaryGenerator
import com.matiasdev.elecapp.features.inspections.summary.label
import com.matiasdev.elecapp.features.visits.data.VisitRepository
import com.matiasdev.elecapp.features.visits.ui.formatVisitDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InspectionOverviewScreen(
    inspectionRepository: InspectionRepository,
    visitRepository: VisitRepository,
    technicalCalculationRepository: TechnicalCalculationRepository,
    inspectionId: String,
    onBackClick: () -> Unit,
    onSectionClick: (InspectionSection) -> Unit,
    onCreateQuoteClick: (String, String) -> Unit,
    onCreateMaterialClick: (String, String) -> Unit,
    onAddCalculationClick: (String, String, String) -> Unit,
    onCalculationClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: InspectionOverviewViewModel = viewModel(
        factory = InspectionOverviewViewModelFactory(
            inspectionRepository = inspectionRepository,
            visitRepository = visitRepository,
            inspectionId = inspectionId,
            technicalCalculationRepository = technicalCalculationRepository,
        ),
    ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current

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
                title = { Text(uiState.aggregate?.inspection?.scope?.label() ?: "Relevamiento eléctrico") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
            )
        },
    ) { padding ->
        InspectionOverviewContent(
            uiState = uiState,
            onSectionClick = onSectionClick,
            onCompleteClick = viewModel::requestComplete,
            onReopenClick = viewModel::requestReopen,
            onCopySummary = {
                val summary = uiState.aggregate?.let {
                    InspectionSummaryGenerator.generate(it, uiState.visit, calculations = uiState.calculations)
                }.orEmpty()
                clipboard.setText(AnnotatedString(summary))
                viewModel.notifySummaryCopied()
            },
            onShareSummary = {
                val summary = uiState.aggregate?.let {
                    InspectionSummaryGenerator.generate(it, uiState.visit, calculations = uiState.calculations)
                }.orEmpty()
                context.sharePlainText(summary)
            },
            onCreateQuoteClick = {
                val visit = uiState.visit
                if (visit != null) onCreateQuoteClick(visit.clientId, visit.id)
            },
            onCreateMaterialClick = {
                val visit = uiState.visit
                if (visit != null) onCreateMaterialClick(visit.clientId, visit.id)
            },
            onAddCalculationClick = {
                val visit = uiState.visit
                if (visit != null) onAddCalculationClick(visit.clientId, visit.id, inspectionId)
            },
            onCalculationClick = onCalculationClick,
            modifier = Modifier.padding(padding),
        )
    }

    if (uiState.completionMissingItems.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = viewModel::dismissCompletionMessages,
            title = { Text("Hay datos sin completar") },
            text = {
                Text(
                    "Podés finalizar igual si estos puntos no aplican o no pudieron verificarse:\n\n" +
                        uiState.completionMissingItems.joinToString(separator = "\n") { "• $it" },
                )
            },
            confirmButton = { TextButton(onClick = viewModel::confirmComplete) { Text("Finalizar igual") } },
            dismissButton = { TextButton(onClick = viewModel::dismissCompletionMessages) { Text("Corregir") } },
        )
    }
    if (uiState.showCompleteConfirmation) {
        AlertDialog(
            onDismissRequest = viewModel::dismissCompletionMessages,
            title = { Text("Finalizar relevamiento") },
            text = { Text("¿Querés finalizar este relevamiento? Podrás reabrirlo con confirmación.") },
            confirmButton = { TextButton(onClick = viewModel::confirmComplete) { Text("Finalizar") } },
            dismissButton = { TextButton(onClick = viewModel::dismissCompletionMessages) { Text("Cancelar") } },
        )
    }
    if (uiState.showReopenConfirmation) {
        AlertDialog(
            onDismissRequest = viewModel::dismissReopen,
            title = { Text("Reabrir relevamiento") },
            text = { Text("Este relevamiento ya fue finalizado. ¿Deseás reabrirlo?") },
            confirmButton = { TextButton(onClick = viewModel::confirmReopen) { Text("Reabrir") } },
            dismissButton = { TextButton(onClick = viewModel::dismissReopen) { Text("Cancelar") } },
        )
    }
}

@Composable
private fun InspectionOverviewContent(
    uiState: InspectionOverviewUiState,
    onSectionClick: (InspectionSection) -> Unit,
    onCompleteClick: () -> Unit,
    onReopenClick: () -> Unit,
    onCopySummary: () -> Unit,
    onShareSummary: () -> Unit,
    onCreateQuoteClick: () -> Unit,
    onCreateMaterialClick: () -> Unit,
    onAddCalculationClick: () -> Unit,
    onCalculationClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val aggregate = uiState.aggregate
    when {
        uiState.isLoading -> CircularProgressIndicator(modifier = modifier.padding(24.dp))
        aggregate == null -> Text(uiState.errorMessage ?: "Relevamiento no encontrado", modifier = modifier.padding(24.dp))
        else -> Column(
            modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(aggregate.inspection.clientNameSnapshot, style = MaterialTheme.typography.headlineSmall)
            Text(listOf(aggregate.inspection.addressSnapshot, aggregate.inspection.localitySnapshot).filter(String::isNotBlank).joinToString(", "))
            uiState.visit?.let { Text("Visita: ${it.scheduledAt.formatVisitDateTime()}") }
            Text("${stringResource(R.string.inspection_scope_header_label)}: ${aggregate.inspection.scope.label()}")
            Text("Estado: ${aggregate.inspection.status.label()}", fontWeight = FontWeight.Bold)
            ProgressBlock(uiState)
            if (aggregate.inspection.scope == InspectionScope.VISUAL_INSPECTION) {
                uiState.progress?.sections.orEmpty().forEach { section ->
                    SectionProgressCard(section, onSectionClick)
                    if (section.section == InspectionSection.MAIN_PANEL) {
                        InspectionCalculationsSection(
                            calculations = uiState.calculations,
                            onAddCalculationClick = onAddCalculationClick,
                            onCalculationClick = onCalculationClick,
                            isVisualInspection = true,
                        )
                    }
                }
            } else {
                uiState.progress?.sections.orEmpty().forEach { section ->
                    SectionProgressCard(section, onSectionClick)
                }
                InspectionCalculationsSection(uiState.calculations, onAddCalculationClick, onCalculationClick)
            }
            SummaryActions(onCopySummary, onShareSummary)
            if (uiState.visit != null) {
                OutlinedButton(onClick = onCreateQuoteClick, modifier = Modifier.fillMaxWidth()) {
                    Text("Crear presupuesto")
                }
                OutlinedButton(onClick = onCreateMaterialClick, modifier = Modifier.fillMaxWidth()) {
                    Text("Crear lista de materiales")
                }
            }
            if (aggregate.inspection.status == InspectionStatus.COMPLETED) {
                OutlinedButton(onClick = onReopenClick, modifier = Modifier.fillMaxWidth()) { Text("Reabrir relevamiento") }
            } else {
                OutlinedButton(onClick = {}, modifier = Modifier.fillMaxWidth()) { Text("Guardar borrador") }
                Button(onClick = onCompleteClick, modifier = Modifier.fillMaxWidth()) { Text("Finalizar relevamiento") }
            }
        }
    }
}

@Composable
private fun SectionProgressCard(
    section: com.matiasdev.elecapp.features.inspections.domain.InspectionSectionProgress,
    onSectionClick: (InspectionSection) -> Unit,
) {
    Card(onClick = { onSectionClick(section.section) }, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(section.status.symbol())
                Text(section.section.label(), fontWeight = FontWeight.SemiBold)
            }
            Text(section.status.label(), color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (section.summary.isNotBlank()) Text(section.summary)
        }
    }
}

@Composable
private fun InspectionCalculationsSection(
    calculations: List<TechnicalCalculation>,
    onAddCalculationClick: () -> Unit,
    onCalculationClick: (String) -> Unit,
    isVisualInspection: Boolean = false,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Mediciones y cálculos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (isVisualInspection) {
                Text("Agregá únicamente las mediciones o cálculos realizados durante esta revisión.")
            } else {
                Text("${calculations.size} registro(s)")
            }
            calculations.take(3).forEach { calculation ->
                Card(onClick = { onCalculationClick(calculation.id) }, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("[${calculation.source.label().uppercase()}] ${calculation.type.label()}", fontWeight = FontWeight.SemiBold)
                        Text(calculation.primaryResultText())
                        Text(calculation.classification.label(), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Button(onClick = onAddCalculationClick, modifier = Modifier.fillMaxWidth()) {
                Text(if (isVisualInspection) "Agregar medición o cálculo" else "Agregar cálculo")
            }
        }
    }
}

@Composable
private fun ProgressBlock(uiState: InspectionOverviewUiState) {
    val progress = uiState.progress ?: return
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        LinearProgressIndicator(progress = { progress.percent / 100f }, modifier = Modifier.fillMaxWidth())
        Text("${progress.completedCount}/${progress.totalCount} secciones completas")
    }
}

@Composable
private fun SummaryActions(onCopySummary: () -> Unit, onShareSummary: () -> Unit) {
    Button(onClick = onCopySummary, modifier = Modifier.fillMaxWidth()) {
        Icon(Icons.Default.ContentCopy, contentDescription = null)
        Text("Copiar informe", modifier = Modifier.padding(start = 8.dp))
    }
    OutlinedButton(onClick = onShareSummary, modifier = Modifier.fillMaxWidth()) {
        Icon(Icons.Default.Share, contentDescription = null)
        Text("Enviar informe", modifier = Modifier.padding(start = 8.dp))
    }
}

private fun Context.sharePlainText(text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    startActivity(Intent.createChooser(intent, "Enviar informe"))
}
