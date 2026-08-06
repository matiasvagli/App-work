package com.matiasdev.elecapp.features.clients.ui

import android.widget.Toast
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.matiasdev.elecapp.core.external.browserMapsIntent
import com.matiasdev.elecapp.core.external.browserWhatsappIntent
import com.matiasdev.elecapp.core.external.dialIntent
import com.matiasdev.elecapp.core.external.emailIntent
import com.matiasdev.elecapp.core.external.mapsIntent
import com.matiasdev.elecapp.core.external.tryStartActivity
import com.matiasdev.elecapp.core.external.whatsappIntent
import com.matiasdev.elecapp.features.clients.data.ClientRepository
import com.matiasdev.elecapp.features.clients.domain.Client
import com.matiasdev.elecapp.features.finance.data.FinanceRepository
import com.matiasdev.elecapp.features.visits.data.VisitRepository
import com.matiasdev.elecapp.features.visits.domain.Visit
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientDetailScreen(
    clientRepository: ClientRepository,
    visitRepository: VisitRepository,
    financeRepository: FinanceRepository,
    clientId: String,
    onBackClick: () -> Unit,
    onEditClick: (String) -> Unit,
    onScheduleVisitClick: (String) -> Unit,
    onViewVisitsClick: (String) -> Unit,
    onViewReceiptsClick: (String) -> Unit,
    onRegisterPaymentClick: (String) -> Unit,
    onCreateQuoteClick: (String) -> Unit,
    onCreateMaterialClick: (String) -> Unit,
    onVisitClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ClientDetailViewModel = viewModel(
        factory = ClientDetailViewModelFactory(clientRepository, visitRepository, clientId),
    ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Detalle del cliente") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    uiState.client?.let { client ->
                        IconButton(onClick = { onEditClick(client.id) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar cliente")
                        }
                        IconButton(onClick = { viewModel.askDelete(client) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar cliente")
                        }
                    }
                },
            )
        },
    ) { padding ->
        ClientDetailContent(
            uiState = uiState,
            onEditClick = onEditClick,
            onDeleteClick = viewModel::askDelete,
            onScheduleVisitClick = onScheduleVisitClick,
            onViewVisitsClick = onViewVisitsClick,
            onViewReceiptsClick = onViewReceiptsClick,
            onRegisterPaymentClick = onRegisterPaymentClick,
            onCreateQuoteClick = onCreateQuoteClick,
            onCreateMaterialClick = onCreateMaterialClick,
            onVisitClick = onVisitClick,
            modifier = Modifier.padding(padding),
        )
    }

    uiState.clientPendingDelete?.let { client ->
        AlertDialog(
            onDismissRequest = viewModel::dismissDelete,
            title = { Text("Eliminar cliente") },
            text = { Text("¿Querés eliminar a ${client.fullName}?") },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmDelete(onBackClick) }) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissDelete) {
                    Text("Cancelar")
                }
            },
        )
    }
}

@Composable
private fun ClientDetailContent(
    uiState: ClientDetailUiState,
    onEditClick: (String) -> Unit,
    onDeleteClick: (Client) -> Unit,
    onScheduleVisitClick: (String) -> Unit,
    onViewVisitsClick: (String) -> Unit,
    onViewReceiptsClick: (String) -> Unit,
    onRegisterPaymentClick: (String) -> Unit,
    onCreateQuoteClick: (String) -> Unit,
    onCreateMaterialClick: (String) -> Unit,
    onVisitClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val client = uiState.client
    when {
        uiState.isLoading -> CircularProgressIndicator(modifier = modifier.padding(24.dp))
        client == null -> Text(
            text = uiState.errorMessage ?: "Cliente no encontrado",
            modifier = modifier.padding(24.dp),
        )
        else -> Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ClientInfo(client)
            ClientQuickActions(client, onScheduleVisitClick, onCreateQuoteClick, onCreateMaterialClick)
            ClientFinanceActions(client, onViewReceiptsClick, onRegisterPaymentClick)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onEditClick(client.id) }) {
                    Text("Editar")
                }
                OutlinedButton(onClick = { onDeleteClick(client) }) {
                    Text("Eliminar")
                }
            }
            UpcomingVisits(
                visits = uiState.upcomingVisits,
                onScheduleVisitClick = { onScheduleVisitClick(client.id) },
                onViewVisitsClick = { onViewVisitsClick(client.id) },
                onVisitClick = onVisitClick,
            )
        }
    }
}

@Composable
private fun ClientInfo(client: Client) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(client.fullName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        DetailLine("Teléfono", client.phone)
        client.email?.let { DetailLine("Email", it) }
        client.address?.let { DetailLine("Dirección", it) }
        client.locality?.let { DetailLine("Localidad", it) }
        client.notes?.let { DetailLine("Notas", it) }
        DetailLine("Creado", DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(client.createdAt.atZone(ZoneId.systemDefault())))
    }
}

@Composable
private fun DetailLine(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun ClientQuickActions(
    client: Client,
    onScheduleVisitClick: (String) -> Unit,
    onCreateQuoteClick: (String) -> Unit,
    onCreateMaterialClick: (String) -> Unit,
) {
    val context = LocalContext.current
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Acciones rápidas", style = MaterialTheme.typography.titleMedium)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = {
                val opened = whatsappIntent(client.phone)?.let(context::tryStartActivity) == true ||
                    browserWhatsappIntent(client.phone)?.let(context::tryStartActivity) == true
                if (!opened) Toast.makeText(context, "No hay una app compatible para WhatsApp", Toast.LENGTH_SHORT).show()
            }) { Text("WhatsApp") }
            OutlinedButton(onClick = {
                if (!context.tryStartActivity(dialIntent(client.phone))) {
                    Toast.makeText(context, "No hay una app para llamar", Toast.LENGTH_SHORT).show()
                }
            }) { Text("Llamar") }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            client.email?.let {
                OutlinedButton(onClick = {
                    if (!context.tryStartActivity(emailIntent(it))) {
                        Toast.makeText(context, "No hay una app de correo", Toast.LENGTH_SHORT).show()
                    }
                }) { Text("Email") }
            }
            if (client.address != null || client.locality != null) {
                OutlinedButton(onClick = {
                    val opened = mapsIntent(client.address, client.locality)?.let(context::tryStartActivity) == true ||
                        browserMapsIntent(client.address, client.locality)?.let(context::tryStartActivity) == true
                    if (!opened) Toast.makeText(context, "No hay una app de mapas", Toast.LENGTH_SHORT).show()
                }) { Text("Maps") }
            }
        }
        Button(onClick = { onScheduleVisitClick(client.id) }) {
            Text("Agendar visita")
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { onCreateQuoteClick(client.id) }) {
                Text("Nuevo presupuesto")
            }
            OutlinedButton(onClick = { onCreateMaterialClick(client.id) }) {
                Text("Nueva lista")
            }
        }
    }
}

@Composable
private fun ClientFinanceActions(
    client: Client,
    onViewReceiptsClick: (String) -> Unit,
    onRegisterPaymentClick: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Economía", style = MaterialTheme.typography.titleMedium)
        Text("Comprobantes, cobros y saldos del cliente.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { onViewReceiptsClick(client.id) }) {
                Text("Ver comprobantes")
            }
            OutlinedButton(onClick = { onRegisterPaymentClick(client.id) }) {
                Text("Registrar cobro")
            }
        }
    }
}

@Composable
private fun UpcomingVisits(
    visits: List<Visit>,
    onScheduleVisitClick: () -> Unit,
    onViewVisitsClick: () -> Unit,
    onVisitClick: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Próximas visitas", style = MaterialTheme.typography.titleMedium)
        if (visits.isEmpty()) {
            Text("No hay visitas próximas", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            visits.forEach { visit ->
                ListItem(
                    headlineContent = { Text(visit.reason) },
                    supportingContent = { Text("${visit.formattedDateTime()} · ${visit.status.label}") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingContent = null,
                    trailingContent = {
                        TextButton(onClick = { onVisitClick(visit.id) }) {
                            Text("Ver")
                        }
                    },
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = onViewVisitsClick) {
                Text("Ver todas")
            }
            TextButton(onClick = onScheduleVisitClick) {
                Text("Agregar")
            }
        }
    }
}

private fun Visit.formattedDateTime(): String {
    return DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(scheduledAt.atZone(ZoneId.systemDefault()))
}
