package com.matiasdev.elecapp.features.electricaltools.ui

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
import androidx.compose.material3.Button
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.matiasdev.elecapp.core.ui.theme.ElecAppTheme
import com.matiasdev.elecapp.features.clients.data.ClientRepository
import com.matiasdev.elecapp.features.electricaltools.data.TechnicalCalculationRepository
import com.matiasdev.elecapp.features.electricaltools.domain.CalculationSource
import com.matiasdev.elecapp.features.electricaltools.domain.TechnicalCalculation
import com.matiasdev.elecapp.features.electricaltools.domain.TechnicalCalculationType
import com.matiasdev.elecapp.features.electricaltools.domain.TechnicalClassification
import com.matiasdev.elecapp.features.electricaltools.domain.TechnicianConclusion
import com.matiasdev.elecapp.features.electricaltools.summary.TechnicalCalculationTextGenerator
import com.matiasdev.elecapp.features.electricaltools.summary.label
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
        topBar = { TopAppBar(title = { Text("Detalle de cálculo") }, navigationIcon = { IconButton(onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") } }) },
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
        state.isLoading -> CircularProgressIndicator(modifier.padding(24.dp))
        calculation == null -> Text(state.errorMessage ?: "Cálculo no encontrado", modifier.padding(24.dp))
        else -> Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(calculation.type.label(), style = MaterialTheme.typography.headlineSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CalculationOriginChip(calculation.source)
                TechnicalClassificationChip(calculation.classification)
            }
            Text("Resultado principal: ${calculation.primaryResultText()}")
            Text(TechnicalCalculationTextGenerator.generate(calculation, includeFormulaVersion = true))
            EditableFields(state, onUpdate, onSaveEditable)
            AssociationFields(state, onUpdate, onSaveAssociation, onUnlinkInspection)
            if (calculation.classification in listOf(TechnicalClassification.REQUIRES_REVIEW, TechnicalClassification.CRITICAL_REVIEW)) {
                Button(onClick = onCreateFinding, enabled = calculation.inspectionId != null, modifier = Modifier.fillMaxWidth()) {
                    Text("Crear hallazgo sugerido")
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = { clipboard.setText(AnnotatedString(TechnicalCalculationTextGenerator.generate(calculation))) }, modifier = Modifier.weight(1f)) { Text("Copiar") }
                OutlinedButton(onClick = { shareCalculationText(context, TechnicalCalculationTextGenerator.generate(calculation)) }, modifier = Modifier.weight(1f)) { Text("Compartir") }
            }
            OutlinedButton(onClick = onDelete, modifier = Modifier.fillMaxWidth()) { Text("Eliminar") }
        }
    }
}

@Composable
private fun EditableFields(
    state: TechnicalCalculationDetailUiState,
    onUpdate: (TechnicalCalculationDetailUiState.() -> TechnicalCalculationDetailUiState) -> Unit,
    onSave: () -> Unit,
) {
    OutlinedTextField(state.title, { onUpdate { copy(title = it) } }, label = { Text("Título") }, modifier = Modifier.fillMaxWidth())
    OutlinedTextField(state.description, { onUpdate { copy(description = it) } }, label = { Text("Descripción") }, modifier = Modifier.fillMaxWidth())
    EnumSegmentedField("Conclusión del técnico", state.technicianConclusion, TechnicianConclusion.entries.toList(), TechnicianConclusion::label) {
        onUpdate { copy(technicianConclusion = it) }
    }
    OutlinedTextField(state.technicianNotes, { onUpdate { copy(technicianNotes = it) } }, label = { Text("Notas del técnico") }, modifier = Modifier.fillMaxWidth())
    Button(onClick = onSave, modifier = Modifier.fillMaxWidth()) { Text("Guardar cambios") }
}

@Composable
private fun AssociationFields(
    state: TechnicalCalculationDetailUiState,
    onUpdate: (TechnicalCalculationDetailUiState.() -> TechnicalCalculationDetailUiState) -> Unit,
    onSave: () -> Unit,
    onUnlinkInspection: () -> Unit,
) {
    Text("Asociación", style = MaterialTheme.typography.titleMedium)
    state.clientName?.let { Text("Cliente: $it") }
    OutlinedTextField(state.associationClientId, { onUpdate { copy(associationClientId = it) } }, label = { Text("ID cliente") }, modifier = Modifier.fillMaxWidth())
    OutlinedTextField(state.associationVisitId, { onUpdate { copy(associationVisitId = it) } }, label = { Text("ID visita") }, modifier = Modifier.fillMaxWidth())
    OutlinedTextField(state.associationInspectionId, { onUpdate { copy(associationInspectionId = it) } }, label = { Text("ID relevamiento") }, modifier = Modifier.fillMaxWidth())
    Button(onClick = onSave, modifier = Modifier.fillMaxWidth()) { Text("Guardar asociación") }
    OutlinedButton(onClick = onUnlinkInspection, modifier = Modifier.fillMaxWidth()) { Text("Desvincular del relevamiento") }
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
