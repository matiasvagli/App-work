package com.matiasdev.elecapp.features.visits.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.matiasdev.elecapp.features.clients.data.ClientRepository
import com.matiasdev.elecapp.features.visits.data.VisitRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientVisitsScreen(
    clientRepository: ClientRepository,
    visitRepository: VisitRepository,
    clientId: String,
    onBackClick: () -> Unit,
    onAddClick: (String) -> Unit,
    onVisitClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ClientVisitsViewModel = viewModel(
        factory = ClientVisitsViewModelFactory(clientRepository, visitRepository, clientId),
    ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Visitas") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onAddClick(clientId) }) {
                Icon(Icons.Default.Add, contentDescription = "Agendar visita")
            }
        },
    ) { padding ->
        when {
            uiState.isLoading -> CircularProgressIndicator(modifier = Modifier.padding(padding).padding(24.dp))
            uiState.client == null -> Text(
                text = uiState.errorMessage ?: "Cliente no encontrado",
                modifier = Modifier.padding(padding).padding(24.dp),
            )
            uiState.visits.isEmpty() -> Text(
                text = "No hay visitas cargadas",
                modifier = Modifier.padding(padding).padding(24.dp),
            )
            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(uiState.visits, key = { it.id }) { visit ->
                    ListItem(
                        headlineContent = { Text(visit.reason) },
                        supportingContent = {
                            Text("${visit.scheduledAt.formatVisitDateTime()} · ${visit.status.label}")
                        },
                        modifier = Modifier.clickable { onVisitClick(visit.id) },
                    )
                }
            }
        }
    }
}
