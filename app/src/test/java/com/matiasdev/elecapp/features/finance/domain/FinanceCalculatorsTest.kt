package com.matiasdev.elecapp.features.finance.domain

import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FinanceCalculatorsTest {
    @Test
    fun `money parser accepts argentine formats`() {
        assertEquals(10_000_000L, MoneyParser.parseCents("100000"))
        assertEquals(10_000_000L, MoneyParser.parseCents("100.000"))
        assertEquals(10_000_050L, MoneyParser.parseCents("100000,50"))
        assertEquals(10_000_000L, MoneyParser.parseCents("$100.000"))
        assertNull(MoneyParser.parseCents("abc"))
    }

    @Test
    fun `money formatter keeps cents`() {
        assertEquals("$ 100.000", MoneyFormatter.format(10_000_000L))
        assertEquals("$ 100.000,50", MoneyFormatter.format(10_000_050L))
    }

    @Test
    fun `receipt totals separate categories and ignore client supplied material`() {
        val items = listOf(
            item(ServiceReceiptItemType.LABOR, 80_000_00),
            item(ServiceReceiptItemType.MATERIAL, 25_000_00),
            item(ServiceReceiptItemType.ADDITIONAL, 5_000_00),
            item(ServiceReceiptItemType.MATERIAL, 12_000_00, chargeable = false, suppliedBy = MaterialSuppliedBy.CLIENT),
        )
        val totals = ReceiptCalculator.totals(items, discountCents = 10_000_00)
        assertEquals(80_000_00L, totals.subtotalLaborCents)
        assertEquals(25_000_00L, totals.subtotalMaterialsCents)
        assertEquals(5_000_00L, totals.subtotalOtherCents)
        assertEquals(100_000_00L, totals.totalCents)
    }

    @Test
    fun `discount cannot exceed subtotal and percentage rounds explicitly`() {
        assertEquals(25_00L, ReceiptCalculator.percentageDiscount(100_00L, 2_500L))
        assertEquals(100_00L, ReceiptCalculator.totals(listOf(item(ServiceReceiptItemType.LABOR, 100_00)), 200_00).discountCents)
        assertEquals(0L, ReceiptCalculator.totals(listOf(item(ServiceReceiptItemType.LABOR, 100_00)), 200_00).totalCents)
    }

    @Test
    fun `payment balance ignores cancelled and deleted payments`() {
        val payments = listOf(
            payment(40_000_00),
            payment(30_000_00, status = PaymentStatus.CANCELLED),
            payment(10_000_00, deleted = true),
        )
        val balance = PaymentBalanceCalculator.balance(100_000_00, payments)
        assertEquals(40_000_00L, balance.paidCents)
        assertEquals(60_000_00L, balance.pendingCents)
    }

    @Test
    fun `receipt status follows confirmed payments`() {
        val receipt = receipt(total = 100_000_00, status = ServiceReceiptStatus.ISSUED)
        assertEquals(ServiceReceiptStatus.ISSUED, ReceiptStatusResolver.resolve(receipt, emptyList()))
        assertEquals(ServiceReceiptStatus.PARTIALLY_PAID, ReceiptStatusResolver.resolve(receipt, listOf(payment(40_000_00))))
        assertEquals(ServiceReceiptStatus.PAID, ReceiptStatusResolver.resolve(receipt, listOf(payment(100_000_00))))
        assertEquals(ServiceReceiptStatus.PAID, ReceiptStatusResolver.resolve(receipt(total = 0L, status = ServiceReceiptStatus.ISSUED), emptyList()))
        assertEquals(ServiceReceiptStatus.CANCELLED, ReceiptStatusResolver.resolve(receipt.copy(status = ServiceReceiptStatus.CANCELLED), listOf(payment(100_000_00))))
    }

    @Test
    fun `quick visit validates required fields and optional duration`() {
        assertFalse(QuickVisitValidator.validate(QuickVisitDraft()).isValid)
        assertTrue(QuickVisitValidator.validate(QuickVisitDraft(selectedClientId = "client")).isValid)
        assertTrue(
            QuickVisitValidator.validate(
                QuickVisitDraft(clientMode = QuickVisitClientMode.QUICK_CREATE, quickClientName = "Ana"),
            ).isValid,
        )
        assertFalse(QuickVisitValidator.validate(QuickVisitDraft(selectedClientId = "client", attentionType = VisitAttentionType.OTHER)).isValid)
        assertTrue(QuickVisitValidator.validate(QuickVisitDraft(selectedClientId = "client", attentionType = VisitAttentionType.OTHER, briefDetail = "Corte intermitente")).isValid)
        assertFalse(QuickVisitValidator.validate(QuickVisitDraft(selectedClientId = "client", estimatedDurationMinutes = "0")).isValid)
    }

    @Test
    fun `date ranges use local zone`() {
        val zone = ZoneId.of("America/Argentina/Buenos_Aires")
        val clock = Clock.fixed(Instant.parse("2026-08-05T12:00:00Z"), zone)
        val day = DateRangeCalculator.range(FinancePeriodPreset.TODAY, clock, zone)
        assertEquals(LocalDate.of(2026, 8, 5).atStartOfDay(zone).toInstant(), day.start)
        assertEquals(LocalDate.of(2026, 8, 6).atStartOfDay(zone).toInstant(), day.endExclusive)
        val month = DateRangeCalculator.range(FinancePeriodPreset.THIS_MONTH, clock, zone)
        assertEquals(LocalDate.of(2026, 8, 1).atStartOfDay(zone).toInstant(), month.start)
    }

    private fun item(type: ServiceReceiptItemType, total: Long, chargeable: Boolean = true, suppliedBy: MaterialSuppliedBy = MaterialSuppliedBy.UNKNOWN): ServiceReceiptItem {
        return ServiceReceiptItem("item-$total", "receipt", type, "Item", 1_000, total, total, ReceiptItemSourceType.MANUAL, null, suppliedBy, chargeable, 0, null, now, now, false)
    }

    private fun payment(amount: Long, status: PaymentStatus = PaymentStatus.CONFIRMED, deleted: Boolean = false): Payment {
        return Payment("payment-$amount", "client", null, "receipt", amount, PaymentMethod.CASH, now, null, null, status, now, now, deleted)
    }

    private fun receipt(total: Long, status: ServiceReceiptStatus): ServiceReceipt {
        return ServiceReceipt("receipt", 1L, "client", null, null, now, "Servicio", null, status, total, 0, 0, 0, total, null, null, now, now, false)
    }

    private companion object {
        val now: Instant = Instant.parse("2026-08-05T12:00:00Z")
    }
}
