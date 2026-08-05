package com.matiasdev.elecapp.features.quotes.summary

import com.matiasdev.elecapp.features.materials.domain.PurchaseResponsibility
import com.matiasdev.elecapp.features.materials.summary.label
import com.matiasdev.elecapp.features.quotes.domain.DiscountType
import com.matiasdev.elecapp.features.quotes.domain.MoneyFormatter
import com.matiasdev.elecapp.features.quotes.domain.Quote
import com.matiasdev.elecapp.features.quotes.domain.QuoteCalculator
import com.matiasdev.elecapp.features.quotes.domain.QuoteItem
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class QuoteSummaryContext(
    val clientName: String,
    val address: String?,
    val locality: String?,
    val linkedMaterialList: Boolean,
    val purchaseResponsibility: PurchaseResponsibility?,
)

object QuoteSummaryGenerator {
    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    fun generate(
        quote: Quote,
        items: List<QuoteItem>,
        context: QuoteSummaryContext,
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): String = buildString {
        appendLine("PRESUPUESTO ${quote.quoteNumber}")
        appendLine()
        appendLine("Cliente: ${context.clientName}")
        addressLine(context)?.let { appendLine("Domicilio: $it") }
        appendLine()
        appendLine("Trabajo:")
        appendLine(quote.title)
        quote.description?.takeIf { it.isNotBlank() }?.let {
            appendLine()
            appendLine(it)
        }
        appendLine()
        appendLine("MANO DE OBRA Y SERVICIOS")
        items.filterNot { it.isDeleted }.sortedBy { it.sortOrder }.forEach { item ->
            appendLine()
            appendLine("- ${item.description}")
            appendLine("  Cantidad: ${formatQuantity(item.quantity)} ${item.unit.label(item.customUnitLabel)}")
            appendLine("  Importe: ${MoneyFormatter.format(item.lineTotalAmount, quote.currency)}")
            item.notes?.takeIf { it.isNotBlank() }?.let { appendLine("  Nota: $it") }
        }
        val totals = QuoteCalculator.totals(items, quote.discountType, quote.discountValue)
        appendLine()
        appendLine("Subtotal: ${MoneyFormatter.format(totals.subtotalAmount, quote.currency)}")
        if (quote.discountType != DiscountType.NONE && totals.discountAmount > 0L) {
            appendLine("Descuento: ${MoneyFormatter.format(totals.discountAmount, quote.currency)}")
        }
        appendLine("TOTAL: ${MoneyFormatter.format(totals.totalAmount, quote.currency)}")
        quote.validUntil?.let { appendLine("Validez: ${dateFormatter.withZone(zoneId).format(it)}") }
        quote.paymentTerms?.takeIf { it.isNotBlank() }?.let { appendLine("Condiciones: $it") }
        appendLine()
        appendLine("MATERIALES")
        if (context.linkedMaterialList) {
            appendLine("Se entrega una lista de materiales separada.")
            context.purchaseResponsibility?.let { appendLine(it.label() + ".") }
        } else {
            appendLine("Los materiales no están incluidos en este presupuesto.")
            appendLine("Se puede entregar una lista separada.")
        }
        quote.generalNotes?.takeIf { it.isNotBlank() }?.let {
            appendLine()
            appendLine("Observaciones:")
            appendLine(it)
        }
        quote.clientMessage?.takeIf { it.isNotBlank() }?.let {
            appendLine()
            appendLine("Mensaje para el cliente:")
            appendLine(it)
        }
    }.trimEnd()

    private fun addressLine(context: QuoteSummaryContext): String? {
        val parts = listOfNotNull(
            context.address?.takeIf { it.isNotBlank() },
            context.locality?.takeIf { it.isNotBlank() },
        )
        return parts.joinToString(", ").takeIf { it.isNotBlank() }
    }

    private fun formatQuantity(quantity: Double): String {
        return if (quantity % 1.0 == 0.0) quantity.toInt().toString() else quantity.toString()
    }
}
