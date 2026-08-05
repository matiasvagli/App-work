@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.matiasdev.elecapp.features.quotes.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.matiasdev.elecapp.features.quotes.domain.DiscountType
import com.matiasdev.elecapp.features.quotes.domain.QuoteCurrency
import com.matiasdev.elecapp.features.quotes.domain.QuoteItemType
import com.matiasdev.elecapp.features.quotes.domain.QuoteStatus
import com.matiasdev.elecapp.features.quotes.domain.QuoteStatusActions
import com.matiasdev.elecapp.features.quotes.domain.QuoteUnit
import com.matiasdev.elecapp.features.quotes.summary.label

@Composable
fun ClientSelector(uiState: QuoteFormUiState, viewModel: QuoteFormViewModel) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Cliente", fontWeight = FontWeight.SemiBold)
            OutlinedTextField(
                value = uiState.clientQuery,
                onValueChange = viewModel::updateClientQuery,
                label = { Text("Buscar cliente") },
                modifier = Modifier.fillMaxWidth(),
            )
            uiState.clients.forEach { client ->
                FilterChip(
                    selected = uiState.clientId == client.id,
                    onClick = { viewModel.selectClient(client.id) },
                    label = { Text(client.fullName) },
                )
            }
        }
    }
}

@Composable
fun QuoteFields(uiState: QuoteFormUiState, viewModel: QuoteFormViewModel) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Datos del presupuesto", fontWeight = FontWeight.SemiBold)
            OutlinedTextField(uiState.title, viewModel::updateTitle, label = { Text("Título") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(uiState.description, viewModel::updateDescription, label = { Text("Descripción") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                QuoteCurrency.entries.forEach { currency ->
                    FilterChip(uiState.currency == currency, { viewModel.updateCurrency(currency) }, label = { Text(currency.name) })
                }
            }
            OutlinedTextField(uiState.validUntilInput, viewModel::updateValidUntil, label = { Text("Válido hasta (yyyy-mm-dd)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(uiState.paymentTerms, viewModel::updatePaymentTerms, label = { Text("Condiciones de pago") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(uiState.generalNotes, viewModel::updateGeneralNotes, label = { Text("Notas") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
            OutlinedTextField(uiState.clientMessage, viewModel::updateClientMessage, label = { Text("Mensaje para el cliente") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
        }
    }
}

@Composable
fun QuoteItemButtons(viewModel: QuoteFormViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(onClick = { viewModel.addItem(QuoteItemType.LABOR) }, modifier = Modifier.fillMaxWidth()) { Text("Agregar mano de obra") }
        OutlinedButton(onClick = { viewModel.addItem(QuoteItemType.SERVICE) }, modifier = Modifier.fillMaxWidth()) { Text("Agregar servicio") }
    }
}

@Composable
fun QuoteItemEditor(item: QuoteItemFormState, viewModel: QuoteFormViewModel) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                QuoteItemType.entries.forEach { type ->
                    FilterChip(
                        selected = item.type == type,
                        onClick = { viewModel.updateItem(item.id) { it.copy(type = type) } },
                        label = { Text(type.label()) },
                    )
                }
            }
            OutlinedTextField(item.description, { value -> viewModel.updateItem(item.id) { it.copy(description = value) } }, label = { Text("Descripción") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(item.quantity, { value -> viewModel.updateItem(item.id) { it.copy(quantity = value) } }, label = { Text("Cantidad") }, modifier = Modifier.fillMaxWidth())
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(QuoteUnit.FIXED, QuoteUnit.HOUR, QuoteUnit.METER, QuoteUnit.UNIT).forEach { unit ->
                    FilterChip(item.unit == unit, { viewModel.updateItem(item.id) { it.copy(unit = unit) } }, label = { Text(unit.label()) })
                }
            }
            OutlinedTextField(item.unitPriceInput, { value -> viewModel.updateItem(item.id) { it.copy(unitPriceInput = value) } }, label = { Text("Precio unitario") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(item.notes, { value -> viewModel.updateItem(item.id) { it.copy(notes = value) } }, label = { Text("Notas") }, modifier = Modifier.fillMaxWidth())
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton({ viewModel.moveItem(item.id, -1) }) { Text("Subir") }
                TextButton({ viewModel.moveItem(item.id, 1) }) { Text("Bajar") }
                TextButton({ viewModel.duplicateItem(item.id) }) { Text("Duplicar") }
                TextButton({ viewModel.removeItem(item.id) }) { Text("Eliminar") }
            }
        }
    }
}

@Composable
fun QuoteDiscountFields(uiState: QuoteFormUiState, viewModel: QuoteFormViewModel) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Descuento", fontWeight = FontWeight.SemiBold)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DiscountType.entries.forEach { type ->
                    FilterChip(uiState.discountType == type, { viewModel.updateDiscountType(type) }, label = { Text(type.label()) })
                }
            }
            if (uiState.discountType != DiscountType.NONE) {
                OutlinedTextField(uiState.discountInput, viewModel::updateDiscountInput, label = { Text("Valor del descuento") }, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
fun CopyShareButtons(
    text: String,
    copyLabel: String,
    shareTitle: String,
    onCopy: () -> Unit,
    onShare: () -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(enabled = text.isNotBlank(), onClick = onCopy, modifier = Modifier.weight(1f)) { Text(copyLabel) }
        OutlinedButton(enabled = text.isNotBlank(), onClick = onShare, modifier = Modifier.weight(1f)) { Text(shareTitle) }
    }
}

@Composable
fun StatusButtons(status: QuoteStatus?, onStatusClick: (QuoteStatus) -> Unit) {
    if (status == null) return
    val showCancelDialog = remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        val transitions = QuoteStatusActions.primaryTransitions(status)
        if (transitions.isEmpty()) {
            Text(status.label(), color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            transitions.forEach { target ->
                AssistChip(onClick = { onStatusClick(target) }, label = { Text(target.actionLabel()) })
            }
        }
        if (QuoteStatusActions.canCancel(status)) {
            OutlinedButton(onClick = { showCancelDialog.value = true }, modifier = Modifier.fillMaxWidth()) {
                Text("Cancelar presupuesto")
            }
        }
    }
    if (showCancelDialog.value) {
        AlertDialog(
            onDismissRequest = { showCancelDialog.value = false },
            title = { Text("Cancelar presupuesto") },
            text = { Text("¿Querés cancelar este presupuesto?") },
            confirmButton = { TextButton(onClick = { showCancelDialog.value = false; onStatusClick(QuoteStatus.CANCELLED) }) { Text("Cancelar presupuesto") } },
            dismissButton = { TextButton(onClick = { showCancelDialog.value = false }) { Text("Seguir editando") } },
        )
    }
}

private fun QuoteStatus.actionLabel(): String = when (this) {
    QuoteStatus.READY -> "Marcar listo"
    QuoteStatus.SENT -> "Marcar enviado"
    QuoteStatus.APPROVED -> "Marcar aprobado"
    QuoteStatus.REJECTED -> "Marcar rechazado"
    else -> label()
}
