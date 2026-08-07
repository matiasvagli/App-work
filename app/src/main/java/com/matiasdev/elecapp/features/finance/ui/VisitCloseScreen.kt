package com.matiasdev.elecapp.features.finance.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.matiasdev.elecapp.features.clients.data.ClientRepository
import com.matiasdev.elecapp.features.finance.data.FinanceRepository
import com.matiasdev.elecapp.features.finance.domain.MoneyFormatter
import com.matiasdev.elecapp.features.finance.domain.VisitTechnicalResult
import com.matiasdev.elecapp.features.finance.domain.VisitWorkType
import com.matiasdev.elecapp.features.finance.domain.label
import com.matiasdev.elecapp.features.visits.data.VisitRepository
import com.matiasdev.elecapp.features.visits.data.VisitWorkSessionRepository
import com.matiasdev.elecapp.features.visits.ui.formatCompactDuration
import com.matiasdev.elecapp.features.visits.ui.formatVisitDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisitCloseScreen(
    clientRepository: ClientRepository,
    visitRepository: VisitRepository,
    workSessionRepository: VisitWorkSessionRepository,
    financeRepository: FinanceRepository,
    visitId: String,
    onBackClick: () -> Unit,
    onSaved: (String, String?) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: VisitCloseViewModel = viewModel(
        factory = VisitCloseViewModelFactory(clientRepository, visitRepository, workSessionRepository, financeRepository, visitId),
    ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is VisitCloseEvent.Message -> snackbarHostState.showSnackbar(event.text)
                is VisitCloseEvent.Saved -> onSaved(event.result.visitId, event.result.receiptId)
            }
        }
    }
    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Cierre de visita") },
                navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver") } },
            )
        },
        bottomBar = {
            Button(
                onClick = viewModel::save,
                enabled = !uiState.isSaving,
                modifier = Modifier.fillMaxWidth().navigationBarsPadding().imePadding().padding(16.dp),
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Text(
                    if (uiState.scheduledFollowUpVisit == null) "Cobrar y finalizar" else "Cobrar, finalizar y confirmar agenda",
                    Modifier.padding(start = 8.dp),
                )
            }
        },
    ) { padding ->
        VisitCloseContent(uiState, viewModel, Modifier.padding(padding))
    }
    VisitCloseDialogs(uiState, viewModel)
}

@Composable
private fun VisitCloseContent(uiState: VisitCloseUiState, viewModel: VisitCloseViewModel, modifier: Modifier = Modifier) {
    Column(
        modifier.verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        VisitSummaryCard(uiState)
        WorkCard(uiState, viewModel)
        AmountCard(uiState, viewModel)
        PaymentMethodCard(uiState, viewModel)
        FollowUpCard(uiState, viewModel)
        FinalCard(uiState, viewModel)
    }
}

@Composable
private fun VisitSummaryCard(uiState: VisitCloseUiState) {
    val visit = uiState.visit
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            SectionTitle("Visita")
            Text(uiState.client?.fullName ?: "Cliente", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            uiState.client?.addressLine()?.takeIf(String::isNotBlank)?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            visit?.let { Text("Fecha: ${it.scheduledAt.formatVisitDateTime()}") }
            visit?.startedAt?.let { Text("Inicio: ${it.timeText()}") }
            Text("Finalización: ${uiState.now.timeText()}")
            Text("Tiempo trabajado: ${uiState.workSummary?.totalWorkedDuration?.formatCompactDuration() ?: "0 min"}")
            Text("Tiempo pausado: ${uiState.workSummary?.totalPausedDuration?.formatCompactDuration() ?: "0 min"}")
            Text("Sesiones: ${uiState.workSummary?.sessionCount ?: 0}")
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WorkCard(uiState: VisitCloseUiState, viewModel: VisitCloseViewModel) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SectionTitle("Trabajo realizado")
            Text("Tipo de trabajo", color = MaterialTheme.colorScheme.onSurfaceVariant)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                VisitWorkType.entries.forEach { type ->
                    FilterChip(
                        selected = uiState.workType == type,
                        onClick = { viewModel.selectWorkType(type) },
                        label = { Text(type.label()) },
                    )
                }
            }
            TextField("Diagnóstico", uiState.diagnosis) { viewModel.updateText(VisitCloseTextField.DIAGNOSIS, it) }
            TextField("Descripción del trabajo realizado", uiState.workPerformed, minLines = 3) { viewModel.updateText(VisitCloseTextField.WORK, it) }
            TextField("Sectores intervenidos", uiState.workSectors) { viewModel.updateText(VisitCloseTextField.WORK_SECTORS, it) }
            TextField("Elementos reemplazados o instalados", uiState.workItems) { viewModel.updateText(VisitCloseTextField.WORK_ITEMS, it) }
            TextField("Pruebas o verificaciones realizadas", uiState.workTests) { viewModel.updateText(VisitCloseTextField.WORK_TESTS, it) }
            TextField("Observaciones del trabajo", uiState.workObservations) { viewModel.updateText(VisitCloseTextField.WORK_OBSERVATIONS, it) }
            Text("Resultado de la visita", color = MaterialTheme.colorScheme.onSurfaceVariant)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                VisitTechnicalResult.entries.forEach { result ->
                    FilterChip(
                        selected = uiState.technicalResult == result,
                        onClick = { viewModel.selectTechnicalResult(result) },
                        label = { Text(result.label()) },
                    )
                }
            }
            TextField("Pendientes / próximos pasos", uiState.pendingWork) { viewModel.updateText(VisitCloseTextField.PENDING, it) }
            TextField("Notas para el cliente", uiState.customerNotes) { viewModel.updateText(VisitCloseTextField.CUSTOMER_NOTES, it) }
            TextField("Notas internas", uiState.internalNotes) { viewModel.updateText(VisitCloseTextField.INTERNAL_NOTES, it) }
        }
    }
}

@Composable
private fun AmountCard(uiState: VisitCloseUiState, viewModel: VisitCloseViewModel) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionTitle("Importe")
            MoneyTextField("Mano de obra", uiState.laborInput) { viewModel.updateMoney(VisitCloseMoneyField.LABOR, it) }
            MoneyTextField("Materiales", uiState.materialsInput) { viewModel.updateMoney(VisitCloseMoneyField.MATERIALS, it) }
            Text("Total del trabajo", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(MoneyFormatter.format(uiState.totalCents), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            TextButton(onClick = viewModel::requestNoCharge) { Text("Trabajo sin cargo") }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PaymentMethodCard(uiState: VisitCloseUiState, viewModel: VisitCloseViewModel) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SectionTitle("Método de pago")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                ClosePaymentMethod.entries.forEach { method ->
                    FilterChip(
                        selected = uiState.selectedPaymentMethod == method,
                        onClick = { viewModel.selectPaymentMethod(method) },
                        label = { Text(method.label()) },
                    )
                }
            }
            if (uiState.selectedPaymentMethod == ClosePaymentMethod.BANK_TRANSFER) {
                TextField("Referencia opcional", uiState.transferReference) { viewModel.updateText(VisitCloseTextField.TRANSFER_REFERENCE, it) }
            }
            if (uiState.selectedPaymentMethod == ClosePaymentMethod.MERCADO_PAGO) {
                TextField("Referencia opcional", uiState.mercadoPagoReference) { viewModel.updateText(VisitCloseTextField.MERCADO_PAGO_REFERENCE, it) }
            }
            if (uiState.selectedPaymentMethod == ClosePaymentMethod.MIXED) MixedPaymentFields(uiState, viewModel)
        }
    }
}

@Composable
private fun MixedPaymentFields(uiState: VisitCloseUiState, viewModel: VisitCloseViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        MoneyTextField("Efectivo", uiState.mixedCashInput) { viewModel.updateMoney(VisitCloseMoneyField.MIXED_CASH, it) }
        MoneyTextField("Transferencia", uiState.mixedTransferInput) { viewModel.updateMoney(VisitCloseMoneyField.MIXED_TRANSFER, it) }
        TextField("Referencia transferencia opcional", uiState.transferReference) { viewModel.updateText(VisitCloseTextField.TRANSFER_REFERENCE, it) }
        MoneyTextField("Mercado Pago", uiState.mixedMercadoPagoInput) { viewModel.updateMoney(VisitCloseMoneyField.MIXED_MERCADO_PAGO, it) }
        TextField("Referencia Mercado Pago opcional", uiState.mercadoPagoReference) { viewModel.updateText(VisitCloseTextField.MERCADO_PAGO_REFERENCE, it) }
        SummaryLine("Total del trabajo", MoneyFormatter.format(uiState.totalCents))
        SummaryLine("Distribuido", MoneyFormatter.format(uiState.mixedDistributedCents))
        SummaryLine("Falta asignar", MoneyFormatter.format((uiState.totalCents - uiState.mixedDistributedCents).coerceAtLeast(0L)))
    }
}

@Composable
private fun FollowUpCard(uiState: VisitCloseUiState, viewModel: VisitCloseViewModel) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SectionTitle("Próxima visita")
            val followUp = uiState.scheduledFollowUpVisit
            if (followUp == null) {
                Text("Sin próxima visita agendada", color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedButton(onClick = viewModel::showFollowUpForm, modifier = Modifier.fillMaxWidth()) {
                    Text("Agendar nueva visita")
                }
            } else {
                Text("Próxima visita agendada", fontWeight = FontWeight.SemiBold)
                Text("${followUp.scheduledAt.formatVisitDate()} — ${followUp.scheduledAt.timeText()}")
                Text(followUp.reason)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = viewModel::showFollowUpForm) { Text("Editar") }
                    OutlinedButton(onClick = viewModel::requestRemoveFollowUp) { Text("Quitar del cierre") }
                }
            }
        }
    }
}

@Composable
private fun FinalCard(uiState: VisitCloseUiState, viewModel: VisitCloseViewModel) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionTitle("Confirmación final")
            SummaryLine("Total", MoneyFormatter.format(uiState.totalCents))
            Text("Se emitirá un comprobante interno de servicio. No válido como factura.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            uiState.validationErrors.forEach { Text(it, color = MaterialTheme.colorScheme.error) }
        }
    }
}

@Composable
private fun VisitCloseDialogs(uiState: VisitCloseUiState, viewModel: VisitCloseViewModel) {
    if (uiState.showFollowUpForm) FollowUpDialog(uiState, viewModel)
    if (uiState.showRemoveFollowUpDialog) {
        AlertDialog(
            onDismissRequest = viewModel::detachFollowUp,
            title = { Text("Quitar próxima visita") },
            text = { Text("¿También querés cancelar/eliminar la visita agendada?") },
            confirmButton = { TextButton(onClick = viewModel::deleteFollowUp) { Text("Eliminar visita") } },
            dismissButton = { TextButton(onClick = viewModel::detachFollowUp) { Text("Solo quitar") } },
        )
    }
    if (uiState.showNoChargeDialog) {
        AlertDialog(
            onDismissRequest = viewModel::dismissNoCharge,
            title = { Text("Trabajo sin cargo") },
            text = { Text("Este trabajo se cerrará sin registrar un cobro.") },
            confirmButton = { TextButton(onClick = viewModel::confirmNoCharge) { Text("Cerrar sin cargo") } },
            dismissButton = { TextButton(onClick = viewModel::dismissNoCharge) { Text("Cancelar") } },
        )
    }
}

@Composable
private fun FollowUpDialog(uiState: VisitCloseUiState, viewModel: VisitCloseViewModel) {
    val draft = uiState.followUpDraft
    AlertDialog(
        onDismissRequest = viewModel::dismissFollowUpForm,
        title = { Text("Agendar nueva visita") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField("Fecha dd/MM/yyyy", draft.date) { value -> viewModel.updateFollowUp { it.copy(date = value) } }
                TextField("Hora HH:mm", draft.time) { value -> viewModel.updateFollowUp { it.copy(time = value) } }
                TextField("Duración estimada", draft.durationMinutes) { value -> viewModel.updateFollowUp { it.copy(durationMinutes = value.filter(Char::isDigit)) } }
                TextField("Motivo", draft.reason) { value -> viewModel.updateFollowUp { it.copy(reason = value) } }
                TextField("Notas", draft.notes) { value -> viewModel.updateFollowUp { it.copy(notes = value) } }
            }
        },
        confirmButton = { TextButton(onClick = viewModel::saveFollowUp) { Text("Guardar") } },
        dismissButton = { TextButton(onClick = viewModel::dismissFollowUpForm) { Text("Cancelar") } },
    )
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
}

@Composable
private fun TextField(label: String, value: String, minLines: Int = 1, onValueChange: (String) -> Unit) {
    OutlinedTextField(value, onValueChange, label = { Text(label) }, modifier = Modifier.fillMaxWidth(), minLines = minLines)
}

@Composable
private fun MoneyTextField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(value, onValueChange, label = { Text(label) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
}

@Composable
private fun SummaryLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label)
        Text(value, fontWeight = FontWeight.SemiBold)
    }
}

private fun ClosePaymentMethod.label(): String = when (this) {
    ClosePaymentMethod.CASH -> "Efectivo"
    ClosePaymentMethod.BANK_TRANSFER -> "Transferencia"
    ClosePaymentMethod.MERCADO_PAGO -> "Mercado Pago"
    ClosePaymentMethod.MIXED -> "Mixto"
}

private fun java.time.Instant.timeText(): String = DateTimeFormatter.ofPattern("HH:mm").format(atZone(ZoneId.systemDefault()))

private fun java.time.Instant.formatVisitDate(): String = DateTimeFormatter.ofPattern("dd/MM/yyyy").format(atZone(ZoneId.systemDefault()))
