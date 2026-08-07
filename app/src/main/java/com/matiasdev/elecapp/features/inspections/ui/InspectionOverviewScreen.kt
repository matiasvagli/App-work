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
import androidx.compose.material.icons.filled.WarningAmber
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.matiasdev.elecapp.R
import com.matiasdev.elecapp.features.electricalrules.domain.ElectricalMeasurementReviewItem
import com.matiasdev.elecapp.features.electricalrules.domain.ElectricalRuleConfigRepository
import com.matiasdev.elecapp.features.inspections.data.InspectionRepository
import com.matiasdev.elecapp.features.electricaltools.data.TechnicalCalculationRepository
import com.matiasdev.elecapp.features.electricaltools.domain.TechnicalCalculation
import com.matiasdev.elecapp.features.electricaltools.ui.primaryResultText
import com.matiasdev.elecapp.features.electricaltools.summary.label
import com.matiasdev.elecapp.features.inspections.domain.AutoInspectionCalculation
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
    electricalRuleConfigRepository: ElectricalRuleConfigRepository,
    inspectionId: String,
    onBackClick: () -> Unit,
    onSectionClick: (InspectionSection) -> Unit,
    onCreateQuoteClick: (String, String) -> Unit,
    onCreateMaterialClick: (String, String) -> Unit,
    onAddCalculationClick: (String, String, String, InspectionManualCalculationType) -> Unit,
    onCalculationClick: (String) -> Unit,
    onCompletionFinished: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: InspectionOverviewViewModel = viewModel(
        factory = InspectionOverviewViewModelFactory(
            inspectionRepository = inspectionRepository,
            visitRepository = visitRepository,
            inspectionId = inspectionId,
            technicalCalculationRepository = technicalCalculationRepository,
            electricalRuleConfigRepository = electricalRuleConfigRepository,
        ),
    ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    var showCalculationTypeDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbar()
        }
    }
    LaunchedEffect(uiState.navigateHomeAfterCompletion) {
        if (uiState.navigateHomeAfterCompletion) {
            viewModel.onCompletionNavigationHandled()
            onCompletionFinished()
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
                    InspectionSummaryGenerator.generate(it, uiState.visit, calculations = uiState.calculations, autoCalculations = uiState.autoCalculations)
                }.orEmpty()
                clipboard.setText(AnnotatedString(summary))
                viewModel.notifySummaryCopied()
            },
            onShareSummary = {
                val summary = uiState.aggregate?.let {
                    InspectionSummaryGenerator.generate(it, uiState.visit, calculations = uiState.calculations, autoCalculations = uiState.autoCalculations)
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
                if (visit != null) showCalculationTypeDialog = true
            },
            onCalculationClick = onCalculationClick,
            onToggleMeasurementReview = viewModel::toggleMeasurementReview,
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
    if (showCalculationTypeDialog) {
        CalculationTypeDialog(
            onDismiss = { showCalculationTypeDialog = false },
            onSelect = { type ->
                val visit = uiState.visit
                if (visit != null) {
                    showCalculationTypeDialog = false
                    onAddCalculationClick(visit.clientId, visit.id, inspectionId, type)
                }
            },
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
    onToggleMeasurementReview: () -> Unit,
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
            MeasurementReviewBlock(uiState, onToggleMeasurementReview)
            ProgressBlock(uiState)
            if (aggregate.inspection.scope == InspectionScope.VISUAL_INSPECTION) {
                uiState.progress?.sections.orEmpty().forEach { section ->
                    SectionProgressCard(section, onSectionClick)
                    if (section.section == InspectionSection.MAIN_PANEL) {
                        InspectionCalculationsSection(
                            calculations = uiState.calculations,
                            autoCalculations = uiState.autoCalculations,
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
                InspectionCalculationsSection(uiState.calculations, uiState.autoCalculations, onAddCalculationClick, onCalculationClick)
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
private fun MeasurementReviewBlock(
    uiState: InspectionOverviewUiState,
    onToggleMeasurementReview: () -> Unit,
) {
    val summary = uiState.measurementReviewSummary
    if (!summary.hasAnomalies) return
    Card(onClick = onToggleMeasurementReview, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.WarningAmber, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                Text(summary.indicatorText, fontWeight = FontWeight.SemiBold)
            }
            if (uiState.measurementReviewExpanded) {
                summary.items.forEach { item -> MeasurementReviewItemRow(item) }
            }
        }
    }
}

@Composable
private fun MeasurementReviewItemRow(item: ElectricalMeasurementReviewItem) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(item.title, fontWeight = FontWeight.SemiBold)
        Text("Medido: ${item.measuredValue.formatRuleNumber()} ${item.unit}")
        Text("Rango configurado: ${item.minimumAllowed.formatOptionalRuleNumber()} - ${item.maximumAllowed.formatOptionalRuleNumber()} ${item.unit}")
        Text(item.reason, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
    autoCalculations: List<AutoInspectionCalculation> = emptyList(),
    onAddCalculationClick: () -> Unit,
    onCalculationClick: (String) -> Unit,
    isVisualInspection: Boolean = false,
) {
    var showDetails by remember { mutableStateOf(false) }
    val activeCalculations = calculations.filterNot { it.isDeleted }
    Card(onClick = { showDetails = true }, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Mediciones y cálculos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (isVisualInspection) {
                Text("Agregá únicamente las mediciones o cálculos realizados durante esta revisión.")
            }
            Text("${activeCalculations.size} registro(s) manual(es) · ${autoCalculations.size} automático(s)")
            Text("Tocá para ver el detalle", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Button(onClick = onAddCalculationClick, modifier = Modifier.fillMaxWidth()) {
                Text(if (isVisualInspection) "Agregar medición o cálculo" else "Agregar cálculo")
            }
        }
    }
    if (showDetails) {
        CalculationsDetailDialog(
            calculations = activeCalculations,
            autoCalculations = autoCalculations,
            onDismiss = { showDetails = false },
            onCalculationClick = { id ->
                showDetails = false
                onCalculationClick(id)
            },
        )
    }
}

@Composable
private fun CalculationsDetailDialog(
    calculations: List<TechnicalCalculation>,
    autoCalculations: List<AutoInspectionCalculation>,
    onDismiss: () -> Unit,
    onCalculationClick: (String) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Mediciones y cálculos") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (calculations.isEmpty() && autoCalculations.isEmpty()) {
                    Text("No hay mediciones ni cálculos asociados.")
                }
                autoCalculations.forEach { calculation ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("[AUTO] ${calculation.title}", fontWeight = FontWeight.SemiBold)
                            Text(calculation.primaryResult)
                            Text(calculation.detail, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                calculations.forEach { calculation ->
                    Card(onClick = { onCalculationClick(calculation.id) }, modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("[${calculation.source.label().uppercase()}] ${calculation.type.label()}", fontWeight = FontWeight.SemiBold)
                            Text(calculation.primaryResultText())
                            Text(calculation.classification.label(), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cerrar") } },
    )
}

@Composable
private fun CalculationTypeDialog(
    onDismiss: () -> Unit,
    onSelect: (InspectionManualCalculationType) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Elegir cálculo") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Seleccioná qué cálculo querés cargar para este relevamiento.")
                CalculationTypeOption("Potencia, corriente y tensión", "Calcula la variable faltante con datos eléctricos básicos.") {
                    onSelect(InspectionManualCalculationType.POWER_CURRENT_VOLTAGE)
                }
                CalculationTypeOption("Caída de tensión", "Calcula caída estimada por longitud, corriente y sección.") {
                    onSelect(InspectionManualCalculationType.VOLTAGE_DROP)
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}

@Composable
private fun CalculationTypeOption(
    title: String,
    description: String,
    onClick: () -> Unit,
) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(description, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

enum class InspectionManualCalculationType {
    POWER_CURRENT_VOLTAGE,
    VOLTAGE_DROP,
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

private fun Double.formatRuleNumber(): String {
    return if (this % 1.0 == 0.0) toInt().toString() else String.format(java.util.Locale.US, "%.2f", this).trimEnd('0').trimEnd('.')
}

private fun Double?.formatOptionalRuleNumber(): String {
    return this?.formatRuleNumber() ?: "sin límite"
}

private fun Context.sharePlainText(text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    startActivity(Intent.createChooser(intent, "Enviar informe"))
}
