package com.matiasdev.elecapp.features.electricaltools.summary

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object TechnicalValueFormatter {
    private val symbols = DecimalFormatSymbols(Locale("es", "AR")).apply {
        decimalSeparator = ','
        groupingSeparator = '.'
    }

    fun format(value: Double, maxDecimals: Int = 2): String {
        val rounded = BigDecimal.valueOf(value).setScale(maxDecimals, RoundingMode.HALF_UP).stripTrailingZeros()
        val pattern = if (maxDecimals == 0) "#,##0" else "#,##0.${"#".repeat(maxDecimals)}"
        return DecimalFormat(pattern, symbols).format(rounded)
    }

    fun withUnit(value: Double?, unit: String?, maxDecimals: Int = 2): String {
        if (value == null) return ""
        return listOf(format(value, maxDecimals), unit.orEmpty()).filter(String::isNotBlank).joinToString(" ")
    }
}
