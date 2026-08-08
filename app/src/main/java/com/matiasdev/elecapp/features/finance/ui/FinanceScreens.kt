package com.matiasdev.elecapp.features.finance.ui

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.matiasdev.elecapp.features.clients.data.ClientRepository
import com.matiasdev.elecapp.features.finance.data.FinanceRepository
import com.matiasdev.elecapp.features.finance.domain.FinancePeriodPreset
import com.matiasdev.elecapp.features.finance.domain.MoneyFormatter
import com.matiasdev.elecapp.features.finance.domain.MoneyParser
import com.matiasdev.elecapp.features.finance.domain.PaymentBalanceCalculator
import com.matiasdev.elecapp.features.finance.domain.PaymentMethod
import com.matiasdev.elecapp.features.finance.domain.ServiceReceipt
import com.matiasdev.elecapp.features.finance.domain.displayNumber
import com.matiasdev.elecapp.features.inspections.data.InspectionRepository



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceReceiptListScreen(
    financeRepository: FinanceRepository,
    clientId: String?,
    onBackClick: () -> Unit,
    onReceiptClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReceiptListViewModel = viewModel(factory = ReceiptListViewModelFactory(financeRepository, clientId)),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text("Comprobantes") }, navigationIcon = { BackButton(onBackClick) }) },
    ) { padding ->
        LazyColumn(Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (uiState.receipts.isEmpty()) item { Text("Sin comprobantes") }
            items(uiState.receipts, key = { it.id }) { receipt -> ReceiptRow(receipt, onReceiptClick) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceReceiptDetailScreen(
    financeRepository: FinanceRepository,
    clientRepository: ClientRepository,
    inspectionRepository: InspectionRepository,
    receiptId: String,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onVisitClick: (String) -> Unit,
    onFullReportClick: (String) -> Unit,
    onRegisterPaymentClick: (String, String, String?) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReceiptDetailViewModel = viewModel(factory = ReceiptDetailViewModelFactory(financeRepository, clientRepository, inspectionRepository, receiptId)),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            if (event is ReceiptDetailEvent.Share) {
                context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, event.text), "Compartir comprobante"))
            }
        }
    }
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Comprobante") },
                navigationIcon = { BackButton(onBackClick) },
                actions = {
                    IconButton(onClick = onHomeClick) { Icon(Icons.Default.Home, contentDescription = "Ir al inicio") }
                    IconButton(onClick = viewModel::share) { Icon(Icons.Default.Share, contentDescription = "Compartir") }
                },
            )
        },
    ) { padding ->
        val receipt = uiState.receipt
        LazyColumn(Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (receipt == null) item { Text("Comprobante no encontrado") } else {
                item { ReceiptHeader(receipt, uiState.client?.fullName, uiState.payments) }
                item { ReceiptContextActions(uiState, onVisitClick, onFullReportClick) }
                item { ItemsCard(uiState.items) }
                item { PaymentsCard(uiState) { onRegisterPaymentClick(receipt.id, receipt.clientId, receipt.visitId) } }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterPaymentScreen(
    financeRepository: FinanceRepository,
    receiptId: String?,
    clientId: String,
    visitId: String?,
    onBackClick: () -> Unit,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RegisterPaymentViewModel = viewModel(factory = RegisterPaymentViewModelFactory(financeRepository, receiptId, clientId, visitId)),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.events.collect { onSaved() } }
    Scaffold(modifier = modifier, topBar = { TopAppBar(title = { Text("Registrar cobro") }, navigationIcon = { BackButton(onBackClick) }) }) { padding ->
        Column(Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(if (receiptId == null) "Cobro sin comprobante asociado" else "Cobro asociado a comprobante", fontWeight = FontWeight.SemiBold)
            uiState.receipt?.let { receipt ->
                val balance = PaymentBalanceCalculator.balance(receipt.totalCents, uiState.payments)
                Text("Saldo pendiente: ${MoneyFormatter.format(balance.pendingCents)}")
            }
            OutlinedTextField(uiState.amount, { value -> viewModel.update { it.copy(amount = value) } }, label = { Text("Importe") }, modifier = Modifier.fillMaxWidth())
            PaymentMethod.entries.forEach { method ->
                FilterChip(selected = uiState.method == method, onClick = { viewModel.update { it.copy(method = method) } }, label = { Text(method.label()) })
            }
            OutlinedTextField(uiState.reference, { value -> viewModel.update { it.copy(reference = value) } }, label = { Text("Referencia") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(uiState.notes, { value -> viewModel.update { it.copy(notes = value) } }, label = { Text("Nota") }, modifier = Modifier.fillMaxWidth())
            uiState.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Button(onClick = viewModel::save, enabled = !uiState.isSaving, modifier = Modifier.fillMaxWidth()) { Text("Guardar cobro") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceDashboardScreen(
    financeRepository: FinanceRepository,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FinanceDashboardViewModel = viewModel(factory = FinanceDashboardViewModelFactory(financeRepository)),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val metrics = uiState.metrics
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Economía y Rendimiento", fontWeight = FontWeight.Bold) },
                navigationIcon = { BackButton(onBackClick) },
            )
        },
    ) { padding ->
        LazyColumn(
            Modifier
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    FinancePeriodPreset.entries.forEach { preset ->
                        FilterChip(
                            selected = uiState.preset == preset,
                            onClick = { viewModel.load(preset) },
                            label = { Text(preset.label(), fontWeight = FontWeight.SemiBold) },
                            shape = RoundedCornerShape(10.dp),
                        )
                    }
                }
            }
            item {
                MetricCard(
                    title = "Importe total de trabajos",
                    value = MoneyFormatter.format(metrics.generatedCents),
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    isHeadline = true,
                )
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        MetricCard(
                            title = "Cobrado",
                            value = MoneyFormatter.format(metrics.collectedCents),
                            accentColor = MaterialTheme.colorScheme.primary,
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        MetricCard(
                            title = "Pendiente",
                            value = MoneyFormatter.format(metrics.pendingCents),
                            accentColor = MaterialTheme.colorScheme.secondary,
                        )
                    }
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        MetricCard(
                            title = "Trabajos completados",
                            value = metrics.completedJobs.toString(),
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        MetricCard(
                            title = "Ticket promedio",
                            value = MoneyFormatter.format(metrics.averageTicketCents),
                        )
                    }
                }
            }
            item {
                MetricCard(
                    title = "Rendimiento estimado por hora",
                    value = MoneyFormatter.format(metrics.generatedPerHourCents),
                    accentColor = MaterialTheme.colorScheme.tertiary,
                )
            }
        }
    }
}


@Composable
private fun ReceiptRow(receipt: ServiceReceipt, onReceiptClick: (String) -> Unit) {
    Card(onClick = { onReceiptClick(receipt.id) }, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(receipt.displayNumber(), fontWeight = FontWeight.SemiBold)
            Text(receipt.status.name)
            Text(MoneyFormatter.format(receipt.totalCents), style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun ReceiptContextActions(uiState: ReceiptDetailUiState, onVisitClick: (String) -> Unit, onFullReportClick: (String) -> Unit) {
    val visitId = uiState.receipt?.visitId
    if (visitId == null && uiState.inspectionId == null) return
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Trabajo e informe", fontWeight = FontWeight.SemiBold)
            visitId?.let {
                OutlinedButton(onClick = { onVisitClick(it) }, modifier = Modifier.fillMaxWidth()) { Text("Ver visita") }
            }
            uiState.inspectionId?.let {
                Button(onClick = { onFullReportClick(it) }, modifier = Modifier.fillMaxWidth()) { Text("Ver informe completo") }
            }
        }
    }
}

@Composable
private fun ReceiptHeader(receipt: ServiceReceipt, clientName: String?, payments: List<com.matiasdev.elecapp.features.finance.domain.Payment>) {
    val balance = PaymentBalanceCalculator.balance(receipt.totalCents, payments)
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(receipt.displayNumber(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(clientName ?: "Cliente")
            Text("Comprobante interno de servicio. No válido como factura.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            SummaryLine("Total del trabajo", MoneyFormatter.format(receipt.totalCents))
            SummaryLine("Cobrado", MoneyFormatter.format(balance.paidCents))
            SummaryLine("Pendiente", MoneyFormatter.format(balance.pendingCents))
        }
    }
}

@Composable
private fun ItemsCard(items: List<com.matiasdev.elecapp.features.finance.domain.ServiceReceiptItem>) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Detalle", fontWeight = FontWeight.SemiBold)
            items.forEach { item -> SummaryLine(item.description, MoneyFormatter.format(item.totalCents)) }
        }
    }
}

@Composable
private fun PaymentsCard(uiState: ReceiptDetailUiState, onRegisterPayment: () -> Unit) {
    val receipt = uiState.receipt ?: return
    val balance = PaymentBalanceCalculator.balance(receipt.totalCents, uiState.payments)
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Cobros", fontWeight = FontWeight.SemiBold)
            uiState.payments.forEach { payment -> SummaryLine(payment.method.label(), MoneyFormatter.format(payment.amountCents)) }
            if (balance.pendingCents > 0L) Button(onClick = onRegisterPayment, modifier = Modifier.fillMaxWidth()) { Text("Registrar pago") }
            else Text("Cobrado completamente")
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    accentColor: Color? = null,
    isHeadline: Boolean = false,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isHeadline) 3.dp else 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = title,
                style = if (isHeadline) MaterialTheme.typography.titleMedium else MaterialTheme.typography.labelLarge,
                color = accentColor ?: MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (accentColor != null) FontWeight.Bold else FontWeight.Medium,
            )
            Text(
                text = value,
                style = if (isHeadline) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}


@Composable
private fun SummaryLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label)
        Text(value, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun BackButton(onBackClick: () -> Unit) {
    IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver") }
}

private fun PaymentMethod.label(): String = when (this) {
    PaymentMethod.CASH -> "Efectivo"
    PaymentMethod.BANK_TRANSFER -> "Transferencia"
    PaymentMethod.MERCADO_PAGO -> "Mercado Pago"
    PaymentMethod.CARD -> "Tarjeta"
    PaymentMethod.CHECK -> "Cheque"
    PaymentMethod.OTHER -> "Otro"
}

private fun FinancePeriodPreset.label(): String = when (this) {
    FinancePeriodPreset.TODAY -> "Hoy"
    FinancePeriodPreset.LAST_7_DAYS -> "7 días"
    FinancePeriodPreset.THIS_MONTH -> "Mes"
    FinancePeriodPreset.PREVIOUS_MONTH -> "Mes ant."
    FinancePeriodPreset.THIS_YEAR -> "Año"
}
