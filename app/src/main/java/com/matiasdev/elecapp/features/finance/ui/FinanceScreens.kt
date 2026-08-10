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
    receiptId: String,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onVisitClick: (String) -> Unit,
    onWorkReportsClick: (String) -> Unit,
    onRegisterPaymentClick: (String, String, String?) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReceiptDetailViewModel = viewModel(factory = ReceiptDetailViewModelFactory(financeRepository, clientRepository, receiptId)),
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
                item { ReceiptContextActions(uiState, onVisitClick, onWorkReportsClick) }
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
/**
 * Los informes de la atención cuelgan de la visita, no del relevamiento, así que el
 * acceso directo desde el cobro va a la visita: ahí están el informe técnico y el que se
 * manda al cliente.
 */
private fun ReceiptContextActions(uiState: ReceiptDetailUiState, onVisitClick: (String) -> Unit, onWorkReportsClick: (String) -> Unit) {
    val visitId = uiState.receipt?.visitId ?: return
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Trabajo e informe", fontWeight = FontWeight.SemiBold)
            OutlinedButton(onClick = { onVisitClick(visitId) }, modifier = Modifier.fillMaxWidth()) { Text("Ver visita") }
            Button(onClick = { onWorkReportsClick(visitId) }, modifier = Modifier.fillMaxWidth()) { Text("Ver trabajo") }
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
