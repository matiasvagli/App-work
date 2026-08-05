package com.matiasdev.elecapp.features.quotes.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.time.Instant

class QuoteCalculatorTest {
    @Test
    fun `line total supports decimal quantity without Float money`() {
        assertEquals(37_500L, QuoteCalculator.lineTotal(1.5, 25_000L))
    }

    @Test
    fun `subtotal and fixed discount are calculated from active items`() {
        val totals = QuoteCalculator.totals(
            listOf(item(100_000L), item(50_000L), item(10_000L, deleted = true)),
            DiscountType.FIXED,
            25_000L,
        )

        assertEquals(150_000L, totals.subtotalAmount)
        assertEquals(25_000L, totals.discountAmount)
        assertEquals(125_000L, totals.totalAmount)
    }

    @Test
    fun `percentage discount uses basis points and total never goes below zero`() {
        assertEquals(20_000L, QuoteCalculator.discountAmount(200_000L, DiscountType.PERCENTAGE, 1_000L))
        assertEquals(0L, QuoteCalculator.totals(listOf(item(100L)), DiscountType.FIXED, 500L).totalAmount)
    }

    @Test
    fun `invalid quantity and discounts are rejected`() {
        assertThrows(IllegalArgumentException::class.java) { QuoteCalculator.lineTotal(0.0, 1L) }
        assertThrows(IllegalArgumentException::class.java) {
            QuoteCalculator.discountAmount(100L, DiscountType.PERCENTAGE, 10_001L)
        }
    }

    private fun item(total: Long, deleted: Boolean = false): QuoteItem {
        val now = Instant.parse("2026-08-04T12:00:00Z")
        return QuoteItem(
            id = total.toString(),
            quoteId = "quote",
            type = QuoteItemType.SERVICE,
            description = "Trabajo",
            quantity = 1.0,
            unit = QuoteUnit.FIXED,
            customUnitLabel = null,
            unitPriceAmount = total,
            lineTotalAmount = total,
            sortOrder = 0,
            notes = null,
            createdAt = now,
            updatedAt = now,
            isDeleted = deleted,
        )
    }
}
