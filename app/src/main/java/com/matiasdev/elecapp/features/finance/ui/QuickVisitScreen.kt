package com.matiasdev.elecapp.features.finance.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.matiasdev.elecapp.core.ui.theme.ElecAppTheme
import com.matiasdev.elecapp.features.clients.domain.Client
import com.matiasdev.elecapp.features.clients.data.ClientRepository
import com.matiasdev.elecapp.features.finance.data.FinanceRepository
import com.matiasdev.elecapp.features.finance.domain.QuickVisitClientMode
import com.matiasdev.elecapp.features.finance.domain.VisitAttentionType
import java.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickVisitScreen(
    clientRepository: ClientRepository,
    financeRepository: FinanceRepository,
    onBackClick: () -> Unit,
    onVisitStarted: (String) -> Unit,
    onContinueCurrentVisit: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: QuickVisitViewModel = viewModel(factory = QuickVisitViewModelFactory(clientRepository, financeRepository)),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                QuickVisitEvent.ContinueCurrentVisit -> onContinueCurrentVisit()
                is QuickVisitEvent.Message -> snackbarHostState.showSnackbar(event.text)
                is QuickVisitEvent.VisitStarted -> onVisitStarted(event.visitId)
            }
        }
    }
    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Atender ahora") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver") }
                },
            )
        },
    ) { padding ->
        QuickVisitContent(uiState, viewModel, Modifier.padding(padding))
    }
    if (uiState.showActiveVisitWarning) ActiveVisitDialog(viewModel)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuickVisitContent(uiState: QuickVisitUiState, viewModel: QuickVisitViewModel, modifier: Modifier = Modifier) {
    val draft = uiState.draft
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Client Selection Card
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
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 8.dp),
                    )
                    Text(
                        text = "Cliente",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilterChip(
                        selected = draft.clientMode == QuickVisitClientMode.EXISTING,
                        onClick = { viewModel.selectMode(QuickVisitClientMode.EXISTING) },
                        label = { Text("Cliente Existente", fontWeight = FontWeight.SemiBold) },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        ),
                    )
                    FilterChip(
                        selected = draft.clientMode == QuickVisitClientMode.QUICK_CREATE,
                        onClick = { viewModel.selectMode(QuickVisitClientMode.QUICK_CREATE) },
                        label = { Text("Crear Rápido", fontWeight = FontWeight.SemiBold) },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        ),
                    )
                }

                if (draft.clientMode == QuickVisitClientMode.EXISTING) {
                    if (uiState.clients.isEmpty()) {
                        Text(
                            text = "No hay clientes guardados",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            uiState.clients.take(8).forEach { client ->
                                FilterChip(
                                    selected = draft.selectedClientId == client.id,
                                    onClick = { viewModel.selectClient(client.id) },
                                    label = { Text(client.fullName) },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                    ),
                                )
                            }
                        }
                    }
                }
                uiState.validation.clientError?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium) }
            }
        }

        if (draft.clientMode == QuickVisitClientMode.QUICK_CREATE) QuickClientFields(uiState, viewModel)

        VisitReasonFields(uiState, viewModel)

        uiState.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium) }

        Button(
            onClick = viewModel::start,
            enabled = !uiState.isSaving && uiState.canStart,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            shape = RoundedCornerShape(12.dp),
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null)
            Text("Iniciar Atención Ahora", modifier = Modifier.padding(start = 8.dp), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun QuickClientFields(uiState: QuickVisitUiState, viewModel: QuickVisitViewModel) {
    val draft = uiState.draft
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Datos del Nuevo Cliente",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            OutlinedTextField(
                value = draft.quickClientName,
                onValueChange = { value -> viewModel.updateDraft { it.copy(quickClientName = value) } },
                label = { Text("Nombre y Apellido") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = draft.phone,
                onValueChange = { value -> viewModel.updateDraft { it.copy(phone = value) } },
                label = { Text("Teléfono (opcional)") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = draft.address,
                onValueChange = { value -> viewModel.updateDraft { it.copy(address = value) } },
                label = { Text("Dirección (opcional)") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = draft.locality,
                onValueChange = { value -> viewModel.updateDraft { it.copy(locality = value) } },
                label = { Text("Localidad (opcional)") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun VisitReasonFields(uiState: QuickVisitUiState, viewModel: QuickVisitViewModel) {
    val draft = uiState.draft
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Motivo del Trabajo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text("Tipo de atención", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                VisitAttentionType.entries.forEach { type ->
                    FilterChip(
                        selected = draft.attentionType == type,
                        onClick = { viewModel.selectType(type) },
                        label = { Text(type.label, fontWeight = if (draft.attentionType == type) FontWeight.Bold else FontWeight.Normal) },
                        shape = RoundedCornerShape(10.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        ),
                    )
                }
            }
            OutlinedTextField(
                value = draft.briefDetail,
                onValueChange = { value -> viewModel.updateDraft { it.copy(briefDetail = value) } },
                label = { Text(if (draft.attentionType == VisitAttentionType.OTHER) "Detalle breve" else "Detalle breve (opcional)") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
            )
            uiState.validation.detailError?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium) }
            OutlinedTextField(
                value = draft.estimatedDurationMinutes,
                onValueChange = { value -> viewModel.updateDraft { it.copy(estimatedDurationMinutes = value.filter(Char::isDigit)) } },
                label = { Text("Duración estimada opcional (minutos)") },
                supportingText = { Text("Ayuda a organizar la agenda. No modifica el tiempo real trabajado.") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
            )
            uiState.validation.durationError?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium) }
        }
    }
}

@Preview(name = "Atender ahora")
@Composable
private fun QuickVisitContentPreview() {
    ElecAppTheme {
        QuickVisitContentPreviewBody(
            QuickVisitUiState(
                clients = listOf(previewClient),
                draft = com.matiasdev.elecapp.features.finance.domain.QuickVisitDraft(selectedClientId = "client"),
            ),
        )
    }
}

@Preview(name = "Atender ahora otro")
@Composable
private fun QuickVisitOtherPreview() {
    ElecAppTheme {
        QuickVisitContentPreviewBody(
            QuickVisitUiState(
                draft = com.matiasdev.elecapp.features.finance.domain.QuickVisitDraft(
                    clientMode = QuickVisitClientMode.QUICK_CREATE,
                    quickClientName = "Carlos López",
                    attentionType = VisitAttentionType.OTHER,
                ),
            ),
        )
    }
}

@Composable
private fun QuickVisitContentPreviewBody(uiState: QuickVisitUiState) {
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Cliente", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(if (uiState.draft.clientMode == QuickVisitClientMode.QUICK_CREATE) uiState.draft.quickClientName else previewClient.fullName)
            }
        }
        VisitReasonPreview(uiState)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun VisitReasonPreview(uiState: QuickVisitUiState) {
    val draft = uiState.draft
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Trabajo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("Tipo de atención", style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                VisitAttentionType.entries.forEach { type ->
                    FilterChip(selected = draft.attentionType == type, onClick = {}, label = { Text(type.label) })
                }
            }
            OutlinedTextField(draft.briefDetail, {}, label = { Text("Detalle breve (opcional)") }, modifier = Modifier.fillMaxWidth())
        }
    }
}

private val previewNow = Instant.parse("2026-08-05T12:00:00Z")
private val previewClient = Client("client", "Carlos López", "111", null, "Av. Espora 1234", "Adrogué", null, previewNow, previewNow, false)

@Composable
private fun ModeRow(text: String, selected: Boolean, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().selectable(selected, onClick = onClick), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        RadioButton(selected = selected, onClick = onClick)
        Text(text, Modifier.padding(top = 12.dp))
    }
}

@Composable
private fun ActiveVisitDialog(viewModel: QuickVisitViewModel) {
    AlertDialog(
        onDismissRequest = viewModel::dismissActiveVisitWarning,
        title = { Text("Ya hay una visita en curso") },
        text = { Text("Podés continuar la visita actual o pausarla e iniciar esta atención.") },
        confirmButton = { TextButton(onClick = viewModel::pauseAndStart) { Text("Pausar e iniciar") } },
        dismissButton = {
            Row {
                TextButton(onClick = viewModel::continueCurrentVisit) { Text("Continuar actual") }
                OutlinedButton(onClick = viewModel::dismissActiveVisitWarning) { Text("Cancelar") }
            }
        },
    )
}
