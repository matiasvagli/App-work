package com.matiasdev.elecapp.features.quotes.domain

import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

object MoneyFormatter {
    private val argentina = Locale("es", "AR")

    fun format(amountInMinorUnits: Long, currency: QuoteCurrency): String {
        val major = amountInMinorUnits / 100
        return when (currency) {
            QuoteCurrency.ARS -> "$ ${integerFormat().format(major)}"
            QuoteCurrency.USD -> "USD ${integerFormat().format(major)}"
        }
    }

    fun parseMajorAmount(input: String): Long {
        val normalized = input
            .replace("$", "")
            .replace("USD", "")
            .replace(".", "")
            .replace(",", ".")
            .trim()
        if (normalized.isBlank()) return 0L
        val value = normalized.toBigDecimalOrNull() ?: return 0L
        return value.multiply(100.toBigDecimal()).toLong()
    }

    fun formatPercentageBasisPoints(value: Long): String {
        val integer = abs(value) / 100
        val decimals = abs(value) % 100
        return if (decimals == 0L) "$integer%" else "$integer,${decimals.toString().padStart(2, '0')}%"
    }

    private fun integerFormat(): NumberFormat = NumberFormat.getIntegerInstance(argentina)
}
