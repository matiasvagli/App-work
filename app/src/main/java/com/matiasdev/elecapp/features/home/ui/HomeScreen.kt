package com.matiasdev.elecapp.features.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.matiasdev.elecapp.R
import com.matiasdev.elecapp.core.ui.components.AboutAppDialog
import com.matiasdev.elecapp.core.ui.components.ElecBadge
import com.matiasdev.elecapp.features.agenda.ui.VisitStatusChip
import com.matiasdev.elecapp.features.clients.data.ClientRepository
import com.matiasdev.elecapp.features.inspections.data.InspectionRepository
import com.matiasdev.elecapp.features.inspections.domain.InspectionStatus
import com.matiasdev.elecapp.features.materials.data.MaterialRepository
import com.matiasdev.elecapp.features.quotes.data.QuoteRepository
import com.matiasdev.elecapp.features.visits.data.VisitRepository
import com.matiasdev.elecapp.features.visits.data.VisitWorkSessionRepository
import com.matiasdev.elecapp.features.visits.ui.formatVisitDateTime
import com.matiasdev.elecapp.features.visits.ui.formatCompactDuration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    clientRepository: ClientRepository,
    visitRepository: VisitRepository,
    workSessionRepository: VisitWorkSessionRepository,
    inspectionRepository: InspectionRepository,
    quoteRepository: QuoteRepository,
    materialRepository: MaterialRepository,
    onClientsClick: () -> Unit,
    onAgendaClick: () -> Unit,
    onInspectionsClick: () -> Unit,
    onQuotesClick: () -> Unit,
    onMaterialsClick: () -> Unit,
    onElectricalToolsClick: () -> Unit,
    onQuickVisitClick: () -> Unit,
    onFinanceClick: () -> Unit,
    onNewVisitClick: () -> Unit,
    onVisitClick: (String) -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(
            clientRepository,
            visitRepository,
            workSessionRepository,
            inspectionRepository,
            quoteRepository,
            materialRepository,
        ),
    ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAboutDialog by remember { mutableStateOf(false) }

    if (showAboutDialog) {
        AboutAppDialog(onDismiss = { showAboutDialog = false })
    }

    val cards = listOf(
        HomeCard("Clientes", "Directorio", Icons.Default.Person, enabled = true, action = onClientsClick),
        HomeCard("Agenda", "Visitas y turnos", Icons.Default.CalendarMonth, enabled = true, action = onAgendaClick),
        HomeCard(
            "Relevamientos",
            if (uiState.draftInspectionCount > 0) "${uiState.draftInspectionCount} borrador(es)" else "Inspecciones",
            Icons.AutoMirrored.Filled.Assignment,
            enabled = true,
            action = onInspectionsClick,
        ),
        HomeCard(
            "Presupuestos",
            if (uiState.draftQuoteCount > 0) "${uiState.draftQuoteCount} borrador(es)" else "Cotizaciones",
            Icons.AutoMirrored.Filled.ReceiptLong,
            enabled = true,
            action = onQuotesClick,
        ),
        HomeCard(
            "Materiales",
            if (uiState.draftMaterialListCount > 0) "${uiState.draftMaterialListCount} borrador(es)" else "Cómputos",
            Icons.Default.Inventory2,
            enabled = true,
            action = onMaterialsClick,
        ),
        HomeCard("Herramientas", "Cálculos eléctricos", Icons.Default.Bolt, enabled = true, action = onElectricalToolsClick),
        HomeCard("Economía", "Cobros y saldos", Icons.Default.PointOfSale, enabled = true, action = onFinanceClick),
    )

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { showAboutDialog = true },
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_elecapp_logo),
                            contentDescription = "Logo ElecApp",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(36.dp),
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "ElecApp Pro",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "(Versión Beta)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.tertiary,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                            Text(
                                "Desarrollado por V. Matías",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Configuración")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            HomeSummaryCard(uiState, onVisitClick)
            Spacer(modifier = Modifier.height(12.dp))
            QuickActionsBar(onQuickVisitClick, onNewVisitClick)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Módulos Principales",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
            )
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 150.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f),
            ) {
                items(cards) { card -> HomeActionCard(card) }
            }
        }
    }
}

@Composable
private fun QuickActionsBar(
    onQuickVisitClick: () -> Unit,
    onNewVisitClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Button(
            onClick = onQuickVisitClick,
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.tertiary,
                contentColor = MaterialTheme.colorScheme.onTertiary,
            ),
        ) {
            Icon(Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Atender ahora", fontWeight = FontWeight.Bold)
        }
        FilledTonalButton(
            onClick = onNewVisitClick,
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Nueva visita", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun HomeSummaryCard(
    uiState: HomeUiState,
    onVisitClick: (String) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            when {
                uiState.isLoading -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Cargando agenda...", style = MaterialTheme.typography.bodyMedium)
                    }
                }
                uiState.errorMessage != null -> {
                    Text(uiState.errorMessage, color = MaterialTheme.colorScheme.error)
                }
                uiState.currentVisit != null -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        ElecBadge(
                            text = "VISITA EN CURSO",
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            icon = Icons.Default.FlashOn,
                        )
                        uiState.currentVisit.workedDuration?.let {
                            Text(
                                "Tiempo: ${it.formatCompactDuration()}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                    Text(
                        uiState.currentVisit.clientName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        uiState.currentVisit.visit.reason,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    if (uiState.currentVisit.location.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                uiState.currentVisit.location,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    Button(
                        onClick = { onVisitClick(uiState.currentVisit.visit.id) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                    ) {
                        Text("Continuar Trabajo en Curso", fontWeight = FontWeight.Bold)
                    }
                }
                uiState.nextVisit == null -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            "Sin próximas visitas",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        ElecBadge(
                            text = "Hoy: ${uiState.todayCount} · Mañana: ${uiState.tomorrowCount}",
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text(
                        "No tenés turnos programados en este momento.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                else -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            "Próxima visita",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                        )
                        VisitStatusChip(uiState.nextVisit.visit)
                    }
                    Text(
                        uiState.nextVisit.clientName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        "${uiState.nextVisit.visit.scheduledAt.formatVisitDateTime()} · ${uiState.nextVisit.visit.reason}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            "Hoy: ${uiState.todayCount} · Mañana: ${uiState.tomorrowCount}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        OutlinedButton(
                            onClick = { onVisitClick(uiState.nextVisit.visit.id) },
                            shape = RoundedCornerShape(8.dp),
                        ) {
                            Text("Ver detalle")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeActionCard(card: HomeCard, modifier: Modifier = Modifier) {
    Card(
        onClick = card.action,
        modifier = modifier
            .fillMaxWidth()
            .height(115.dp),
        enabled = card.enabled,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                    modifier = Modifier.size(38.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = card.icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
                Text(
                    card.status,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (card.status.contains("borrador")) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (card.status.contains("borrador")) FontWeight.Bold else FontWeight.Normal,
                )
            }
            Text(
                card.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
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

