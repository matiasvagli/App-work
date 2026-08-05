package com.matiasdev.elecapp.features.home.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.matiasdev.elecapp.features.agenda.ui.VisitStatusChip
import com.matiasdev.elecapp.features.clients.data.ClientRepository
import com.matiasdev.elecapp.features.inspections.data.InspectionRepository
import com.matiasdev.elecapp.features.inspections.domain.InspectionStatus
import com.matiasdev.elecapp.features.materials.data.MaterialRepository
import com.matiasdev.elecapp.features.quotes.data.QuoteRepository
import com.matiasdev.elecapp.features.visits.data.VisitRepository
import com.matiasdev.elecapp.features.visits.ui.formatVisitDateTime

@Composable
fun HomeScreen(
    clientRepository: ClientRepository,
    visitRepository: VisitRepository,
    inspectionRepository: InspectionRepository,
    quoteRepository: QuoteRepository,
    materialRepository: MaterialRepository,
    onClientsClick: () -> Unit,
    onAgendaClick: () -> Unit,
    onInspectionsClick: () -> Unit,
    onQuotesClick: () -> Unit,
    onMaterialsClick: () -> Unit,
    onElectricalToolsClick: () -> Unit,
    onNewVisitClick: () -> Unit,
    onVisitClick: (String) -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(
            clientRepository,
            visitRepository,
            inspectionRepository,
            quoteRepository,
            materialRepository,
        ),
    ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val cards = listOf(
        HomeCard("Clientes", "Activo", Icons.Default.Person, enabled = true, action = onClientsClick),
        HomeCard("Agenda", "Activo", Icons.Default.CalendarMonth, enabled = true, action = onAgendaClick),
        HomeCard(
            "Relevamientos",
            "${uiState.draftInspectionCount} borrador(es)",
            Icons.AutoMirrored.Filled.Assignment,
            enabled = true,
            action = onInspectionsClick,
        ),
        HomeCard(
            "Presupuestos",
            "${uiState.draftQuoteCount} borrador(es)",
            Icons.AutoMirrored.Filled.ReceiptLong,
            enabled = true,
            action = onQuotesClick,
        ),
        HomeCard(
            "Materiales",
            "${uiState.draftMaterialListCount} borrador(es)",
            Icons.Default.Inventory2,
            enabled = true,
            action = onMaterialsClick,
        ),
        HomeCard("Herramientas eléctricas", "Activo", Icons.Default.Bolt, enabled = true, action = onElectricalToolsClick),
    )

    Scaffold(
        modifier = modifier,
        topBar = {
            Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = onSettingsClick) {
                    Icon(Icons.Default.Settings, contentDescription = "Configuración")
                }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 16.dp),
        ) {
            Text("Buen trabajo", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Tu agenda eléctrica para hoy", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(16.dp))
            HomeSummary(uiState, onNewVisitClick, onVisitClick)
            Spacer(modifier = Modifier.height(16.dp))
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(cards) { card -> HomeActionCard(card) }
            }
        }
    }
}

@Composable
private fun HomeSummary(uiState: HomeUiState, onNewVisitClick: () -> Unit, onVisitClick: (String) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            when {
                uiState.isLoading -> CircularProgressIndicator()
                uiState.errorMessage != null -> Text(uiState.errorMessage, color = MaterialTheme.colorScheme.error)
                uiState.currentVisit != null -> {
                    Text("Visita en curso", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text("${uiState.currentVisit.clientName} · ${uiState.currentVisit.visit.reason}")
                    Text(uiState.currentVisit.location)
                    Text("Relevamiento: ${uiState.currentVisit.inspectionStatus.homeLabel()}")
                    Button(onClick = { onVisitClick(uiState.currentVisit.visit.id) }, modifier = Modifier.fillMaxWidth()) {
                        Text("Continuar visita")
                    }
                }
                uiState.nextVisit == null -> Text("No hay próximas visitas cargadas")
                else -> {
                    Text("Próxima visita", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text("${uiState.nextVisit.visit.scheduledAt.formatVisitDateTime()} · ${uiState.nextVisit.clientName}")
                    Text(listOf(uiState.nextVisit.visit.reason, uiState.nextVisit.location).filter(String::isNotBlank).joinToString(" · "))
                    Text("Relevamiento: ${uiState.nextVisit.inspectionStatus.homeLabel()}")
                    VisitStatusChip(uiState.nextVisit.visit)
                    OutlinedButton(onClick = { onVisitClick(uiState.nextVisit.visit.id) }) {
                        Text("Ver detalle")
                    }
                }
            }
            Text("Hoy: ${uiState.todayCount} visitas · Mañana: ${uiState.tomorrowCount}")
            Button(onClick = onNewVisitClick, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Event, contentDescription = null)
                Text("Nueva visita", modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}

@Composable
private fun HomeActionCard(card: HomeCard, modifier: Modifier = Modifier) {
    Card(
        onClick = card.action,
        modifier = modifier.fillMaxWidth().height(128.dp),
        enabled = card.enabled,
        colors = CardDefaults.cardColors(
            containerColor = if (card.enabled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = card.icon, contentDescription = null)
                Text(card.status, style = MaterialTheme.typography.labelMedium)
            }
            Text(card.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

private data class HomeCard(
    val title: String,
    val status: String,
    val icon: ImageVector,
    val enabled: Boolean,
    val action: () -> Unit,
)

private fun InspectionStatus?.homeLabel(): String = when (this) {
    null -> "Sin relevamiento"
    InspectionStatus.DRAFT -> "Borrador"
    InspectionStatus.COMPLETED -> "Finalizado"
}
