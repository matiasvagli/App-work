package com.matiasdev.elecapp.features.finance.ui

import com.matiasdev.elecapp.features.clients.domain.Client
import com.matiasdev.elecapp.features.finance.domain.MoneyFormatter
import com.matiasdev.elecapp.features.finance.domain.Payment
import com.matiasdev.elecapp.features.finance.domain.PaymentBalanceCalculator
import com.matiasdev.elecapp.features.finance.domain.ServiceReceipt
import com.matiasdev.elecapp.features.finance.domain.ServiceReceiptItem
import com.matiasdev.elecapp.features.finance.domain.displayNumber
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object ReceiptShareText {
    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.forLanguageTag("es-AR"))

    fun build(
        receipt: ServiceReceipt,
        client: Client?,
        items: List<ServiceReceiptItem>,
        payments: List<Payment>,
    ): String {
        val balance = PaymentBalanceCalculator.balance(receipt.totalCents, payments)
        return buildString {
            appendLine("COMPROBANTE DE SERVICIO ${receipt.displayNumber()}")
            appendLine()
            appendLine("Cliente: ${client?.fullName ?: "Cliente"}")
            appendLine("Fecha: ${receipt.issuedAt?.atZone(ZoneId.systemDefault())?.format(dateFormatter).orEmpty()}")
            client?.address?.takeIf(String::isNotBlank)?.let { appendLine("Dirección: ${listOfNotNull(it, client.locality).joinToString(", ")}") }
            appendLine()
            receipt.description?.takeIf(String::isNotBlank)?.let {
                appendLine("Trabajo realizado:")
                appendLine(it)
                appendLine()
            }
            appendLine("Detalle:")
            items.filter { !it.isDeleted && it.isChargeable }.forEach { item ->
                appendLine("- ${item.description}: ${MoneyFormatter.format(item.totalCents)}")
            }
            appendLine()
            appendLine("Total: ${MoneyFormatter.format(receipt.totalCents)}")
            appendLine("Cobrado: ${MoneyFormatter.format(balance.paidCents)}")
            appendLine("Pendiente: ${MoneyFormatter.format(balance.pendingCents)}")
            if (payments.isNotEmpty()) {
                appendLine()
                appendLine("Forma de pago:")
                payments.filter { !it.isDeleted }.forEach { payment ->
                    appendLine("- ${payment.method.shareLabel()}: ${MoneyFormatter.format(payment.amountCents)}")
                }
            }
            receipt.notes?.takeIf(String::isNotBlank)?.let {
                appendLine()
                appendLine(it)
            }
            appendLine()
            appendLine("Comprobante interno de servicio.")
            appendLine("No válido como factura.")
        }
    }

    private fun com.matiasdev.elecapp.features.finance.domain.PaymentMethod.shareLabel(): String = when (this) {
        com.matiasdev.elecapp.features.finance.domain.PaymentMethod.CASH -> "Efectivo"
        com.matiasdev.elecapp.features.finance.domain.PaymentMethod.BANK_TRANSFER -> "Transferencia"
        com.matiasdev.elecapp.features.finance.domain.PaymentMethod.MERCADO_PAGO -> "Mercado Pago"
        com.matiasdev.elecapp.features.finance.domain.PaymentMethod.CARD -> "Tarjeta"
        com.matiasdev.elecapp.features.finance.domain.PaymentMethod.CHECK -> "Cheque"
        com.matiasdev.elecapp.features.finance.domain.PaymentMethod.OTHER -> "Otro"
    }
}
