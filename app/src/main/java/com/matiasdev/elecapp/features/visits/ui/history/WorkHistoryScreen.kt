package com.matiasdev.elecapp.features.visits.ui.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.matiasdev.elecapp.features.finance.domain.VisitWorkType
import com.matiasdev.elecapp.features.finance.domain.label
import com.matiasdev.elecapp.features.visits.data.VisitRepository
import com.matiasdev.elecapp.features.visits.domain.WorkHistoryItem
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkHistoryScreen(
    visitRepository: VisitRepository,
    clientId: String?,
    onBackClick: () -> Unit,
    onVisitClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WorkHistoryViewModel = viewModel(
        factory = WorkHistoryViewModelFactory(visitRepository, clientId),
    ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val filteredByClient = !clientId.isNullOrBlank()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(if (filteredByClient) "Trabajos del cliente" else "Historial de trabajos") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
            )
        },
    ) { padding ->
        WorkHistoryContent(
            uiState = uiState,
            onVisitClick = onVisitClick,
            modifier = Modifier.padding(padding),
        )
    }
}

@Composable
private fun WorkHistoryContent(
    uiState: WorkHistoryUiState,
    onVisitClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        uiState.isLoading -> CircularProgressIndicator(modifier = modifier.padding(24.dp))
        uiState.errorMessage != null -> Text(
            text = uiState.errorMessage,
            color = MaterialTheme.colorScheme.error,
            modifier = modifier.padding(24.dp),
        )
        uiState.items.isEmpty() -> Text(
            text = "Aún no hay trabajos finalizados.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier.padding(24.dp),
        )
        else -> LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(uiState.items, key = { it.visitId }) { item ->
                WorkHistoryCard(
                    item = item,
                    onClick = { onVisitClick(item.visitId) },
                )
            }
        }
    }
}

@Composable
private fun WorkHistoryCard(item: WorkHistoryItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(item.clientName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(item.completedAt.formatWorkHistoryDate(), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                item.durationMinutes?.let { minutes ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(durationText(minutes), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            item.workTypeLabel()?.let {
                Text(it, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            }
            item.workDescription?.takeIf(String::isNotBlank)?.let {
                Text(it, maxLines = 3, overflow = TextOverflow.Ellipsis)
            }
            item.reason.takeIf(String::isNotBlank)?.let {
                Text("Motivo: $it", color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

private fun WorkHistoryItem.workTypeLabel(): String? {
    val rawType = workType ?: return null
    return runCatching { VisitWorkType.valueOf(rawType).label() }.getOrDefault(rawType)
}

private fun Instant.formatWorkHistoryDate(): String {
    return DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(atZone(ZoneId.systemDefault()))
}

private fun durationText(minutes: Long): String {
    val hours = minutes / 60
    val remainingMinutes = minutes % 60
    return when {
        hours > 0 && remainingMinutes > 0 -> "${hours} h ${remainingMinutes} min"
        hours > 0 -> "${hours} h"
        else -> "${remainingMinutes} min"
    }
}
