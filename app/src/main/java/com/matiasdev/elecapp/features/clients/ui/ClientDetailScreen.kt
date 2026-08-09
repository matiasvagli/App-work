package com.matiasdev.elecapp.features.clients.ui

import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.WorkHistory
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.matiasdev.elecapp.core.external.browserMapsIntent
import com.matiasdev.elecapp.core.external.browserWhatsappIntent
import com.matiasdev.elecapp.core.external.dialIntent
import com.matiasdev.elecapp.core.external.emailIntent
import com.matiasdev.elecapp.core.external.mapsIntent
import com.matiasdev.elecapp.core.external.tryStartActivity
import com.matiasdev.elecapp.core.external.whatsappBusinessIntent
import com.matiasdev.elecapp.core.external.whatsappIntent
import com.matiasdev.elecapp.core.ui.components.ElecBadge
import com.matiasdev.elecapp.core.ui.components.ElecLoadingState
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
    onWorkHistoryClick: (String) -> Unit,
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
                title = { Text("Detalle del cliente", fontWeight = FontWeight.Bold) },
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
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Eliminar cliente",
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
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
            onWorkHistoryClick = onWorkHistoryClick,
            onVisitClick = onVisitClick,
            modifier = Modifier.padding(padding),
        )
    }

    uiState.clientPendingDelete?.let { client ->
        AlertDialog(
            onDismissRequest = viewModel::dismissDelete,
            title = { Text("Eliminar cliente", fontWeight = FontWeight.Bold) },
            text = { Text("¿Seguro que querés eliminar a ${client.fullName}? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmDelete(onBackClick) }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
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
    onWorkHistoryClick: (String) -> Unit,
    onVisitClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val client = uiState.client
    when {
        uiState.isLoading -> ElecLoadingState("Cargando información del cliente...")
        client == null -> Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(uiState.errorMessage ?: "Cliente no encontrado", style = MaterialTheme.typography.bodyLarge)
        }
        else -> Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ClientHeroHeader(client = client)
            ClientQuickContactRow(client = client)
            ClientInfoCard(client = client)
            ClientWorkflowCard(
                client = client,
                onScheduleVisitClick = onScheduleVisitClick,
                onCreateQuoteClick = onCreateQuoteClick,
                onCreateMaterialClick = onCreateMaterialClick,
            )
            ClientFinanceCard(
                client = client,
                onViewReceiptsClick = onViewReceiptsClick,
                onRegisterPaymentClick = onRegisterPaymentClick,
            )
            ClientWorkHistoryCard(client = client, onWorkHistoryClick = onWorkHistoryClick)
            UpcomingVisitsCard(
                visits = uiState.upcomingVisits,
                onScheduleVisitClick = { onScheduleVisitClick(client.id) },
                onViewVisitsClick = { onViewVisitsClick(client.id) },
                onVisitClick = onVisitClick,
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ClientHeroHeader(client: Client) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp),
                shadowElevation = 4.dp,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = client.fullName.take(1).uppercase(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = client.fullName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(6.dp))
                val formattedDate = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                    .format(client.createdAt.atZone(ZoneId.systemDefault()))
                ElecBadge(
                    text = "Creado $formattedDate",
                    icon = Icons.Default.Event,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ClientQuickContactRow(client: Client) {
    val context = LocalContext.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Button(
            onClick = {
                val opened = whatsappIntent(client.phone)?.let(context::tryStartActivity) == true ||
                    whatsappBusinessIntent(client.phone)?.let(context::tryStartActivity) == true ||
                    browserWhatsappIntent(client.phone)?.let(context::tryStartActivity) == true
                if (!opened) Toast.makeText(context, "No hay una app compatible para WhatsApp", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366), contentColor = Color.White),
            shape = RoundedCornerShape(14.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
        ) {
            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("WhatsApp", fontWeight = FontWeight.Bold, maxLines = 1)
        }

        FilledTonalButton(
            onClick = {
                if (!context.tryStartActivity(dialIntent(client.phone))) {
                    Toast.makeText(context, "No hay una app para llamar", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(14.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
        ) {
            Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Llamar", fontWeight = FontWeight.Bold, maxLines = 1)
        }

        if (client.address != null || client.locality != null) {
            FilledTonalButton(
                onClick = {
                    val opened = mapsIntent(client.address, client.locality)?.let(context::tryStartActivity) == true ||
                        browserMapsIntent(client.address, client.locality)?.let(context::tryStartActivity) == true
                    if (!opened) Toast.makeText(context, "No hay una app de mapas", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
            ) {
                Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Mapa", fontWeight = FontWeight.Bold, maxLines = 1)
            }
        }
    }
}

@Composable
private fun ClientInfoCard(client: Client) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = "Información de contacto",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            InfoRow(icon = Icons.Default.Phone, label = "Teléfono", value = client.phone.ifBlank { "Sin registrar" })

            val fullAddress = listOfNotNull(client.address, client.locality).filter { it.isNotBlank() }.joinToString(", ")
            if (fullAddress.isNotBlank()) {
                InfoRow(icon = Icons.Default.LocationOn, label = "Dirección", value = fullAddress)
            }

            client.email?.takeIf { it.isNotBlank() }?.let { email ->
                InfoRow(
                    icon = Icons.Default.Email,
                    label = "Correo electrónico",
                    value = email,
                    onActionClick = {
                        if (!context.tryStartActivity(emailIntent(email))) {
                            Toast.makeText(context, "No hay app de correo instalada", Toast.LENGTH_SHORT).show()
                        }
                    },
                    actionIcon = Icons.AutoMirrored.Filled.Send,
                )
            }

            client.notes?.takeIf { it.isNotBlank() }?.let { notes ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Notes,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Notas", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            Text(notes, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    onActionClick: (() -> Unit)? = null,
    actionIcon: ImageVector? = null,
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
            modifier = Modifier.size(38.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        }
        if (onActionClick != null && actionIcon != null) {
            IconButton(onClick = onActionClick) {
                Icon(actionIcon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun ClientWorkflowCard(
    client: Client,
    onScheduleVisitClick: (String) -> Unit,
    onCreateQuoteClick: (String) -> Unit,
    onCreateMaterialClick: (String) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Acciones rápidas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Button(
                onClick = { onScheduleVisitClick(client.id) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
            ) {
                Icon(Icons.Default.CalendarMonth, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Agendar visita técnica", fontWeight = FontWeight.Bold)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = { onCreateQuoteClick(client.id) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Presupuesto", maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                OutlinedButton(
                    onClick = { onCreateMaterialClick(client.id) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Icon(Icons.AutoMirrored.Filled.FormatListBulleted, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Lista materiales", maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

@Composable
private fun ClientFinanceCard(
    client: Client,
    onViewReceiptsClick: (String) -> Unit,
    onRegisterPaymentClick: (String) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Economía del cliente", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Text("Comprobantes, cobros y saldos del cliente.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FilledTonalButton(
                    onClick = { onViewReceiptsClick(client.id) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Comprobantes", maxLines = 1)
                }
                FilledTonalButton(
                    onClick = { onRegisterPaymentClick(client.id) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Registrar cobro", maxLines = 1)
                }
            }
        }
    }
}

@Composable
private fun ClientWorkHistoryCard(
    client: Client,
    onWorkHistoryClick: (String) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.size(44.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.WorkHistory, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(22.dp))
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Historial de trabajos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Trabajos finalizados para este cliente.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            TextButton(onClick = { onWorkHistoryClick(client.id) }, shape = RoundedCornerShape(10.dp)) {
                Text("Ver", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun UpcomingVisitsCard(
    visits: List<Visit>,
    onScheduleVisitClick: () -> Unit,
    onViewVisitsClick: () -> Unit,
    onVisitClick: (String) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Próximas visitas (${visits.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                TextButton(onClick = onViewVisitsClick) { Text("Ver todas", fontWeight = FontWeight.SemiBold) }
            }
            if (visits.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("No hay visitas agendadas", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        TextButton(onClick = onScheduleVisitClick) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Agendar")
                        }
                    }
                }
            } else {
                visits.take(3).forEach { visit ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onVisitClick(visit.id) },
                    ) {
                        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Event, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(visit.reason, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text("${visit.formattedDateTime()} · ${visit.status.label}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

private fun Visit.formattedDateTime(): String =
    DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(scheduledAt.atZone(ZoneId.systemDefault()))

