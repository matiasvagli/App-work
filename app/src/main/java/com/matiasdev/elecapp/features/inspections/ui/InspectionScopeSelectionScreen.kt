package com.matiasdev.elecapp.features.inspections.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.matiasdev.elecapp.R
import com.matiasdev.elecapp.features.clients.data.ClientRepository
import com.matiasdev.elecapp.features.inspections.data.InspectionRepository
import com.matiasdev.elecapp.features.inspections.domain.InspectionScope
import com.matiasdev.elecapp.features.visits.data.VisitRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InspectionScopeSelectionScreen(
    clientRepository: ClientRepository,
    visitRepository: VisitRepository,
    inspectionRepository: InspectionRepository,
    visitId: String,
    onBackClick: () -> Unit,
    onInspectionReady: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: InspectionScopeSelectionViewModel = viewModel(
        factory = InspectionScopeSelectionViewModelFactory(
            clientRepository = clientRepository,
            visitRepository = visitRepository,
            inspectionRepository = inspectionRepository,
            visitId = visitId,
        ),
    ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is InspectionScopeSelectionEvent.InspectionReady -> onInspectionReady(event.inspectionId)
            }
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.inspection_scope_new_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = stringResource(R.string.inspection_scope_question),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            InspectionScope.entries.forEach { scope ->
                InspectionScopeCard(
                    scope = scope,
                    selected = uiState.selectedScope == scope,
                    onClick = { viewModel.onScopeSelected(scope) },
                )
            }
            if (uiState.isCreating) CircularProgressIndicator()
            uiState.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        }
    }
}

@Composable
private fun InspectionScopeCard(
    scope: InspectionScope,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(stringResource(scope.titleRes()), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(stringResource(scope.shortDescriptionRes()), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@StringRes
private fun InspectionScope.titleRes(): Int = when (this) {
    InspectionScope.VISUAL_INSPECTION -> R.string.inspection_scope_visual_title
    InspectionScope.SECTOR_ASSESSMENT -> R.string.inspection_scope_sector_title
    InspectionScope.GENERAL_ASSESSMENT -> R.string.inspection_scope_general_title
}

@StringRes
private fun InspectionScope.shortDescriptionRes(): Int = when (this) {
    InspectionScope.VISUAL_INSPECTION -> R.string.inspection_scope_visual_short_description
    InspectionScope.SECTOR_ASSESSMENT -> R.string.inspection_scope_sector_short_description
    InspectionScope.GENERAL_ASSESSMENT -> R.string.inspection_scope_general_short_description
}
