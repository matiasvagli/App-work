package com.matiasdev.elecapp.features.electricaltools.ui

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.matiasdev.elecapp.core.ui.components.ElecEmptyState
import com.matiasdev.elecapp.core.ui.components.ElecLoadingState
import com.matiasdev.elecapp.core.ui.theme.ElecAppTheme
import com.matiasdev.elecapp.features.clients.data.ClientRepository
import com.matiasdev.elecapp.features.electricaltools.data.TechnicalCalculationRepository
import com.matiasdev.elecapp.features.electricaltools.domain.CalculationSource
import com.matiasdev.elecapp.features.electricaltools.domain.TechnicalCalculation
import com.matiasdev.elecapp.features.electricaltools.domain.TechnicalCalculationType
import com.matiasdev.elecapp.features.electricaltools.domain.TechnicalClassification
import com.matiasdev.elecapp.features.electricaltools.domain.TechnicianConclusion
import com.matiasdev.elecapp.features.electricaltools.summary.TechnicalCalculationTextGenerator
import com.matiasdev.elecapp.features.electricaltools.summary.*
import com.matiasdev.elecapp.features.inspections.data.InspectionRepository
import java.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TechnicalCalculationDetailScreen(
    repository: TechnicalCalculationRepository,
    clientRepository: ClientRepository,
    inspectionRepository: InspectionRepository,
    calculationId: String,
    onBackClick: () -> Unit,
    onDeleted: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TechnicalCalculationDetailViewModel = viewModel(
        factory = TechnicalCalculationDetailViewModelFactory(repository, clientRepository, inspectionRepository, calculationId),
    ),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.snackbarMessage) {
        state.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbar()
        }
    }
    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Detalle del cálculo", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
            )
        },
    ) { padding ->
        TechnicalCalculationDetailContent(
            state = state,
            onUpdate = viewModel::update,
            onSaveEditable = viewModel::saveEditableFields,
            onSaveAssociation = viewModel::saveAssociation,
            onUnlinkInspection = viewModel::unlinkInspection,
            onCreateFinding = viewModel::createSuggestedFinding,
            onDelete = { viewModel.delete(onDeleted) },
            modifier = Modifier.padding(padding),
        )
    }
}

@Composable
fun TechnicalCalculationDetailContent(
    state: TechnicalCalculationDetailUiState,
    onUpdate: (TechnicalCalculationDetailUiState.() -> TechnicalCalculationDetailUiState) -> Unit,
    onSaveEditable: () -> Unit,
    onSaveAssociation: () -> Unit,
    onUnlinkInspection: () -> Unit,
    onCreateFinding: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val calculation = state.calculation
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current

    when {
        state.isLoading -> ElecLoadingState(message = "Cargando detalle...", modifier = modifier.padding(24.dp))
        calculation == null -> ElecEmptyState(icon = Icons.Default.Warning, title = "Cálculo no encontrado", description = state.errorMessage ?: "El registro solicitado no existe o fue eliminado.", modifier = modifier.padding(24.dp))
        else -> Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Hero Card
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = calculation.type.label(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = calculation.primaryResultText(),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CalculationOriginChip(calculation.source)
                        TechnicalClassificationChip(calculation.classification)
                    }
                }
            }

            // Technical Report Summary Box
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("Resumen técnico", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Text(
                        text = TechnicalCalculationTextGenerator.generate(calculation, includeFormulaVersion = true),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Editable Fields Form
            EditableFields(state, onUpdate, onSaveEditable)

            // Association Fields Form
            AssociationFields(state, onUpdate, onSaveAssociation, onUnlinkInspection)

            // Suggested Finding Action Button
            if (calculation.classification in listOf(TechnicalClassification.REQUIRES_REVIEW, TechnicalClassification.CRITICAL_REVIEW)) {
                Button(
                    onClick = onCreateFinding,
                    enabled = calculation.inspectionId != null,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer),
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Crear hallazgo sugerido en relevamiento", fontWeight = FontWeight.Bold)
                }
            }

            // Secondary Actions
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = { clipboard.setText(AnnotatedString(TechnicalCalculationTextGenerator.generate(calculation))) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copiar")
                }
                OutlinedButton(
                    onClick = { shareCalculationText(context, TechnicalCalculationTextGenerator.generate(calculation)) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Compartir")
                }
            }

            OutlinedButton(
                onClick = onDelete,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Eliminar cálculo", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun EditableFields(
    state: TechnicalCalculationDetailUiState,
    onUpdate: (TechnicalCalculationDetailUiState.() -> TechnicalCalculationDetailUiState) -> Unit,
    onSave: () -> Unit,
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Datos editables", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            OutlinedTextField(
                value = state.title,
                onValueChange = { onUpdate { copy(title = it) } },
                label = { Text("Título") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = state.description,
                onValueChange = { onUpdate { copy(description = it) } },
                label = { Text("Descripción") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
            )
            EnumSegmentedField("Conclusión del técnico", state.technicianConclusion, TechnicianConclusion.entries.toList(), TechnicianConclusion::label) {
                onUpdate { copy(technicianConclusion = it) }
            }
            OutlinedTextField(
                value = state.technicianNotes,
                onValueChange = { onUpdate { copy(technicianNotes = it) } },
                label = { Text("Notas del técnico") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
            )
            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Guardar cambios", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun AssociationFields(
    state: TechnicalCalculationDetailUiState,
    onUpdate: (TechnicalCalculationDetailUiState.() -> TechnicalCalculationDetailUiState) -> Unit,
    onSave: () -> Unit,
    onUnlinkInspection: () -> Unit,
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Link, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Asociación con el sistema", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            state.clientName?.let {
                Text("Cliente: $it", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            }
            OutlinedTextField(
                value = state.associationClientId,
                onValueChange = { onUpdate { copy(associationClientId = it) } },
                label = { Text("ID cliente") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = state.associationVisitId,
                onValueChange = { onUpdate { copy(associationVisitId = it) } },
                label = { Text("ID visita") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = state.associationInspectionId,
                onValueChange = { onUpdate { copy(associationInspectionId = it) } },
                label = { Text("ID relevamiento") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
            )
            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Guardar asociación", fontWeight = FontWeight.Bold)
            }
            OutlinedButton(
                onClick = onUnlinkInspection,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text("Desvincular del relevamiento")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DetailPreview() {
    ElecAppTheme {
        TechnicalCalculationDetailContent(
            state = TechnicalCalculationDetailUiState(
                isLoading = false,
                calculation = TechnicalCalculation(
                    id = "1",
                    type = TechnicalCalculationType.POWER_CURRENT_VOLTAGE,
                    source = CalculationSource.MEASURED,
                    clientId = null,
                    visitId = null,
                    inspectionId = null,
                    title = "Medición de tensión",
                    description = null,
                    inputDataJson = "{}",
                    resultDataJson = "{}",
                    primaryResultValue = 207.0,
                    primaryResultUnit = "V",
                    classification = TechnicalClassification.INFORMATIONAL,
                    technicianConclusion = TechnicianConclusion.NOT_REVIEWED,
                    technicianNotes = null,
                    formulaVersion = "v1",
                    createdAt = Instant.now(),
                    updatedAt = Instant.now(),
                    isDeleted = false,
                ),
            ),
            onUpdate = {},
            onSaveEditable = {},
            onSaveAssociation = {},
            onUnlinkInspection = {},
            onCreateFinding = {},
            onDelete = {},
        )
    }
}

