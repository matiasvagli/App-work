package com.matiasdev.elecapp.features.materials.summary

import com.matiasdev.elecapp.features.materials.domain.MaterialItem
import com.matiasdev.elecapp.features.materials.domain.MaterialList
import com.matiasdev.elecapp.features.quotes.domain.MoneyFormatter
import com.matiasdev.elecapp.features.quotes.domain.QuoteCurrency

data class MaterialSummaryContext(
    val clientName: String,
    val quoteNumber: String?,
)

object MaterialListSummaryGenerator {
    fun generate(
        materialList: MaterialList,
        items: List<MaterialItem>,
        context: MaterialSummaryContext,
        includePrices: Boolean = false,
        currency: QuoteCurrency = QuoteCurrency.ARS,
    ): String = buildString {
        appendLine("LISTA DE MATERIALES")
        appendLine()
        appendLine("Cliente: ${context.clientName}")
        appendLine("Trabajo: ${materialList.title}")
        context.quoteNumber?.let { appendLine("Presupuesto relacionado: $it") }
        appendLine()
        appendLine(materialList.purchaseResponsibility.label() + ".")
        materialList.introduction?.takeIf { it.isNotBlank() }?.let {
            appendLine()
            appendLine(it)
        }
        items.filterNot { it.isDeleted }.sortedBy { it.sortOrder }.forEach { item ->
            appendLine()
            append("- ${formatQuantity(item.quantity)} ${item.unit.label(item.customUnitLabel)} ${item.description}")
            item.specifications?.takeIf { it.isNotBlank() }?.let { append(" $it") }
            appendLine()
            item.preferredBrand?.takeIf { it.isNotBlank() }?.let { appendLine("  Marca sugerida: $it") }
            appendLine("  Se permiten alternativas equivalentes: ${if (item.alternativeAllowed) "sí" else "no"}")
            if (includePrices) {
                item.estimatedUnitPriceAmount?.let {
                    appendLine("  Precio estimado: ${MoneyFormatter.format(it, currency)}")
                }
                item.actualUnitPriceAmount?.let {
                    appendLine("  Precio real: ${MoneyFormatter.format(it, currency)}")
                }
            }
            item.notes?.takeIf { it.isNotBlank() }?.let { appendLine("  Nota: $it") }
        }
        materialList.notes?.takeIf { it.isNotBlank() }?.let {
            appendLine()
            appendLine("Observaciones:")
            appendLine(it)
        }
        appendLine()
        appendLine("Los materiales deberán cumplir con las características indicadas.")
        appendLine("Antes de comprar, confirmar disponibilidad y equivalencias.")
    }.trimEnd()

    private fun formatQuantity(quantity: Double): String {
        return if (quantity % 1.0 == 0.0) quantity.toInt().toString() else quantity.toString()
    }
}
