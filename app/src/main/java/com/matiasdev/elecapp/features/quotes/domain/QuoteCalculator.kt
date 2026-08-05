package com.matiasdev.elecapp.features.quotes.domain

import kotlin.math.roundToLong

data class QuoteTotals(
    val subtotalAmount: Long,
    val discountAmount: Long,
    val totalAmount: Long,
)

object QuoteCalculator {
    fun lineTotal(quantity: Double, unitPriceAmount: Long): Long {
        require(quantity > 0.0) { "Quantity must be greater than zero" }
        require(unitPriceAmount >= 0L) { "Unit price cannot be negative" }
        return (quantity * unitPriceAmount).roundToLong()
    }

    fun totals(
        items: List<QuoteItem>,
        discountType: DiscountType,
        discountValue: Long,
    ): QuoteTotals {
        val subtotal = items.filterNot { it.isDeleted }.sumOf { it.lineTotalAmount }
        val discount = discountAmount(subtotal, discountType, discountValue)
        return QuoteTotals(
            subtotalAmount = subtotal,
            discountAmount = discount,
            totalAmount = (subtotal - discount).coerceAtLeast(0L),
        )
    }

    fun discountAmount(
        subtotalAmount: Long,
        discountType: DiscountType,
        discountValue: Long,
    ): Long {
        require(subtotalAmount >= 0L) { "Subtotal cannot be negative" }
        require(discountValue >= 0L) { "Discount cannot be negative" }
        return when (discountType) {
            DiscountType.NONE -> 0L
            DiscountType.FIXED -> discountValue.coerceAtMost(subtotalAmount)
            DiscountType.PERCENTAGE -> {
                require(discountValue <= 10_000L) { "Percentage discount must be between 0 and 100" }
                (subtotalAmount * discountValue / 10_000L).coerceAtMost(subtotalAmount)
            }
        }
    }
}
