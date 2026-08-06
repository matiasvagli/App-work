package com.matiasdev.elecapp.features.finance.domain

import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Clock
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

object MoneyParser {
    fun parseCents(input: String): Long? {
        val normalized = input
            .replace("$", "")
            .replace("ARS", "", ignoreCase = true)
            .replace(" ", "")
            .replace(".", "")
            .replace(",", ".")
            .trim()
        if (normalized.isBlank()) return 0L
        val value = normalized.toBigDecimalOrNull() ?: return null
        if (value < BigDecimal.ZERO) return null
        return value.movePointRight(2).setScale(0, RoundingMode.HALF_UP).longValueExactOrNull()
    }

    private fun BigDecimal.longValueExactOrNull(): Long? = runCatching { longValueExact() }.getOrNull()
}

object MoneyFormatter {
    fun format(cents: Long): String {
        val sign = if (cents < 0) "-" else ""
        val absolute = kotlin.math.abs(cents)
        val major = absolute / 100
        val minor = absolute % 100
        val majorText = major.toString().reversed().chunked(3).joinToString(".").reversed()
        return if (minor == 0L) {
            "$sign$ $majorText"
        } else {
            "$sign$ $majorText,${minor.toString().padStart(2, '0')}"
        }
    }
}

object ReceiptCalculator {
    fun lineTotal(quantityMillis: Long, unitPriceCents: Long): Long {
        require(quantityMillis > 0L) { "La cantidad debe ser mayor a cero" }
        require(unitPriceCents >= 0L) { "El precio no puede ser negativo" }
        return BigDecimal(quantityMillis)
            .multiply(BigDecimal(unitPriceCents))
            .divide(BigDecimal(1_000), 0, RoundingMode.HALF_UP)
            .longValueExact()
    }

    fun totals(items: List<ServiceReceiptItem>, discountCents: Long): ReceiptTotals {
        require(discountCents >= 0L) { "El descuento no puede ser negativo" }
        val active = items.filter { !it.isDeleted && it.isChargeable && it.type != ServiceReceiptItemType.DISCOUNT }
        val labor = active.filter { it.type == ServiceReceiptItemType.LABOR }.sumOf { it.totalCents }
        val materials = active.filter { it.type == ServiceReceiptItemType.MATERIAL }.sumOf { it.totalCents }
        val other = active.filter { it.type == ServiceReceiptItemType.ADDITIONAL }.sumOf { it.totalCents }
        val subtotal = labor + materials + other
        val discount = discountCents.coerceAtMost(subtotal)
        return ReceiptTotals(labor, materials, other, subtotal, discount, subtotal - discount)
    }

    fun percentageDiscount(subtotalCents: Long, basisPoints: Long): Long {
        require(subtotalCents >= 0L) { "El subtotal no puede ser negativo" }
        require(basisPoints in 0L..10_000L) { "El porcentaje debe estar entre 0 y 100" }
        return BigDecimal(subtotalCents)
            .multiply(BigDecimal(basisPoints))
            .divide(BigDecimal(10_000), 0, RoundingMode.HALF_UP)
            .longValueExact()
            .coerceAtMost(subtotalCents)
    }
}

object PaymentBalanceCalculator {
    fun balance(totalCents: Long, payments: List<Payment>): PaymentBalance {
        require(totalCents >= 0L) { "El total no puede ser negativo" }
        val paid = payments.filter { !it.isDeleted && it.status == PaymentStatus.CONFIRMED }.sumOf { it.amountCents }
        val overpayment = (paid - totalCents).coerceAtLeast(0L)
        val pending = (totalCents - paid).coerceAtLeast(0L)
        return PaymentBalance(totalCents, paid, pending, overpayment)
    }
}

object ReceiptStatusResolver {
    fun resolve(receipt: ServiceReceipt, payments: List<Payment>): ServiceReceiptStatus {
        if (receipt.status == ServiceReceiptStatus.CANCELLED) return ServiceReceiptStatus.CANCELLED
        if (receipt.status == ServiceReceiptStatus.DRAFT || receipt.receiptNumber == null) return ServiceReceiptStatus.DRAFT
        if (receipt.totalCents == 0L) return ServiceReceiptStatus.PAID
        val balance = PaymentBalanceCalculator.balance(receipt.totalCents, payments)
        return when {
            balance.paidCents <= 0L -> ServiceReceiptStatus.ISSUED
            balance.pendingCents == 0L -> ServiceReceiptStatus.PAID
            else -> ServiceReceiptStatus.PARTIALLY_PAID
        }
    }
}

data class DateRange(val start: Instant, val endExclusive: Instant)

object DateRangeCalculator {
    fun range(preset: FinancePeriodPreset, clock: Clock, zoneId: ZoneId = ZoneId.systemDefault()): DateRange {
        val today = LocalDate.now(clock.withZone(zoneId))
        return when (preset) {
            FinancePeriodPreset.TODAY -> day(today, zoneId)
            FinancePeriodPreset.LAST_7_DAYS -> DateRange(today.minusDays(6).start(zoneId), today.plusDays(1).start(zoneId))
            FinancePeriodPreset.THIS_MONTH -> month(YearMonth.from(today), zoneId)
            FinancePeriodPreset.PREVIOUS_MONTH -> month(YearMonth.from(today).minusMonths(1), zoneId)
            FinancePeriodPreset.THIS_YEAR -> DateRange(today.withDayOfYear(1).start(zoneId), today.plusDays(1).start(zoneId))
        }
    }

    fun day(date: LocalDate, zoneId: ZoneId): DateRange = DateRange(date.start(zoneId), date.plusDays(1).start(zoneId))

    fun week(date: LocalDate, zoneId: ZoneId): DateRange {
        val start = date.with(DayOfWeek.MONDAY)
        return DateRange(start.start(zoneId), start.plusDays(7).start(zoneId))
    }

    fun month(month: YearMonth, zoneId: ZoneId): DateRange {
        return DateRange(month.atDay(1).start(zoneId), month.plusMonths(1).atDay(1).start(zoneId))
    }

    private fun LocalDate.start(zoneId: ZoneId): Instant = atStartOfDay(zoneId).toInstant()
}

object FinanceMetricsCalculator {
    fun calculate(
        receipts: List<ServiceReceipt>,
        payments: List<Payment>,
        completedJobs: Int,
        workedMinutes: Long,
        servedClientCount: Int,
    ): FinanceMetrics {
        val validReceipts = receipts.filter { !it.isDeleted && it.status != ServiceReceiptStatus.CANCELLED }
        val generated = validReceipts.sumOf { it.totalCents }
        val collected = payments.filter { !it.isDeleted && it.status == PaymentStatus.CONFIRMED }.sumOf { it.amountCents }
        val pending = validReceipts.sumOf { receipt ->
            PaymentBalanceCalculator.balance(receipt.totalCents, payments.filter { it.serviceReceiptId == receipt.id }).pendingCents
        }
        val hours = workedMinutes.toBigDecimal().divide(BigDecimal(60), 4, RoundingMode.HALF_UP)
        return FinanceMetrics(
            completedJobs = completedJobs,
            workedMinutes = workedMinutes,
            generatedCents = generated,
            collectedCents = collected,
            pendingCents = pending,
            paymentCount = payments.count { !it.isDeleted && it.status == PaymentStatus.CONFIRMED },
            averageTicketCents = if (validReceipts.isEmpty()) 0L else generated / validReceipts.size,
            generatedPerHourCents = centsPerHour(generated, hours),
            collectedPerHourCents = centsPerHour(collected, hours),
            servedClientCount = servedClientCount,
        )
    }

    private fun centsPerHour(cents: Long, hours: BigDecimal): Long {
        if (hours <= BigDecimal.ZERO) return 0L
        return BigDecimal(cents).divide(hours, 0, RoundingMode.HALF_UP).longValueExact()
    }
}
