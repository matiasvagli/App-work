package com.matiasdev.elecapp.features.inspections.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.matiasdev.elecapp.features.inspections.data.InspectionRepository
import com.matiasdev.elecapp.features.inspections.domain.InspectionListItem
import com.matiasdev.elecapp.features.inspections.domain.InspectionStatus
import com.matiasdev.elecapp.features.inspections.summary.label
import com.matiasdev.elecapp.features.visits.ui.formatVisitDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InspectionsListScreen(
    repository: InspectionRepository,
    onBackClick: () -> Unit,
    onInspectionClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: InspectionsListViewModel = viewModel(factory = InspectionsListViewModelFactory(repository)),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Relevamientos") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            FilterRow(uiState.filter, viewModel::onFilterChange)
            OutlinedTextField(
                value = uiState.query,
                onValueChange = viewModel::onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Buscar por cliente, domicilio, localidad o motivo") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
            )
            when {
                uiState.isLoading -> CircularProgressIndicator()
                uiState.errorMessage != null -> Text(uiState.errorMessage.orEmpty(), color = MaterialTheme.colorScheme.error)
                uiState.inspections.isEmpty() -> EmptyState(uiState.filter)
                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(uiState.inspections, key = { it.inspection.id }) { item ->
                        InspectionListCard(item, onInspectionClick)
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterRow(selected: InspectionListFilter, onChange: (InspectionListFilter) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        InspectionListFilter.entries.forEach { filter ->
            FilterChip(
                selected = selected == filter,
                onClick = { onChange(filter) },
                label = { Text(filter.label) },
            )
        }
    }
}

@Composable
private fun EmptyState(filter: InspectionListFilter) {
    Text(
        text = when (filter) {
            InspectionListFilter.DRAFT -> "No hay relevamientos en borrador"
            InspectionListFilter.COMPLETED -> "No hay relevamientos finalizados"
            InspectionListFilter.ALL -> "No hay relevamientos cargados"
        },
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun InspectionListCard(item: InspectionListItem, onInspectionClick: (String) -> Unit) {
    val inspection = item.inspection
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(inspection.clientNameSnapshot, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(inspection.status.label())
            }
            val location = listOf(inspection.addressSnapshot, inspection.localitySnapshot).filter(String::isNotBlank).joinToString(", ")
            if (location.isNotBlank()) Text(location)
            Text((item.visitScheduledAt ?: inspection.startedAt).formatVisitDateTime())
            LinearProgressIndicator(progress = { item.progress.percent / 100f }, modifier = Modifier.fillMaxWidth())
            Text("${item.progress.completedCount}/${item.progress.totalCount} secciones completas")
            Button(onClick = { onInspectionClick(inspection.id) }, modifier = Modifier.fillMaxWidth()) {
                Text(if (inspection.status == InspectionStatus.DRAFT) "Continuar" else "Ver")
            }
        }
    }
}
