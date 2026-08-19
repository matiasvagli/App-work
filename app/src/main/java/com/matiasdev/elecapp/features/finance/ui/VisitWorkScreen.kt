package com.matiasdev.elecapp.features.finance.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.matiasdev.elecapp.features.clients.data.ClientRepository
import com.matiasdev.elecapp.features.finance.data.FinanceRepository
import com.matiasdev.elecapp.features.finance.domain.VisitWorkType
import com.matiasdev.elecapp.features.finance.domain.label
import com.matiasdev.elecapp.features.visits.data.VisitRepository
import com.matiasdev.elecapp.features.visits.data.VisitWorkSessionRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisitWorkScreen(
    clientRepository: ClientRepository,
    visitRepository: VisitRepository,
    workSessionRepository: VisitWorkSessionRepository,
    financeRepository: FinanceRepository,
    visitId: String,
    onBackClick: () -> Unit,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: VisitCloseViewModel = viewModel(
        factory = VisitCloseViewModelFactory(clientRepository, visitRepository, workSessionRepository, financeRepository, visitId),
    ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is VisitCloseEvent.Message -> snackbarHostState.showSnackbar(event.text)
                VisitCloseEvent.WorkSaved -> onSaved()
                is VisitCloseEvent.Saved -> Unit
            }
        }
    }
    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Registro de Trabajo", fontWeight = FontWeight.Bold) },
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
        bottomBar = {
            Button(
                onClick = viewModel::saveWorkDraft,
                enabled = !uiState.isSaving,
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(vertical = 14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(16.dp),
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Guardar trabajo", fontWeight = FontWeight.Bold)
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            VisitSummaryCard(uiState)
            VisitWorkDraftCard(uiState, viewModel)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun VisitWorkDraftCard(uiState: VisitCloseUiState, viewModel: VisitCloseViewModel) {
    val descriptionFocusRequester = remember { FocusRequester() }
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                    modifier = Modifier.size(36.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Handyman,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("Trabajo realizado", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Tipo y detalle de la intervención", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Text("Tipo de trabajo", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                VisitWorkType.entries.forEach { type ->
                    FilterChip(
                        selected = uiState.workType == type,
                        onClick = {
                            viewModel.selectWorkType(type)
                            if (uiState.workPerformed.isBlank()) {
                                val prefix = type.workDescriptionPrefix()
                                if (prefix.isNotEmpty()) viewModel.updateText(VisitCloseTextField.WORK, prefix)
                            }
                            descriptionFocusRequester.requestFocus()
                        },
                        label = { Text(type.label(), fontWeight = if (uiState.workType == type) FontWeight.Bold else FontWeight.Normal) },
                        shape = RoundedCornerShape(10.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        ),
                    )
                }
            }

            OutlinedTextField(
                value = uiState.workPerformed,
                onValueChange = { viewModel.updateText(VisitCloseTextField.WORK, it) },
                label = { Text("Descripción del trabajo realizado") },
                placeholder = { Text("Ej: Se realizó cambio de térmicas y verificación de circuitos...") },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(descriptionFocusRequester),
                minLines = 5,
            )
        }
    }
}

private fun VisitWorkType.workDescriptionPrefix(): String = when (this) {
    VisitWorkType.REPAIR -> "Se reparó "
    VisitWorkType.INSTALLATION -> "Se instaló "
    VisitWorkType.REPLACEMENT -> "Se reemplazó "
    VisitWorkType.DIAGNOSIS -> "Se diagnosticó "
    VisitWorkType.FAULT_FINDING -> "Se realizó búsqueda de falla "
    VisitWorkType.MAINTENANCE -> "Se realizó mantenimiento de "
    VisitWorkType.TECHNICAL_INSPECTION -> "Se realizó relevamiento técnico de "
    VisitWorkType.MODIFICATION -> "Se modificó "
    VisitWorkType.OTHER -> ""
}

