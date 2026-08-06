package com.matiasdev.elecapp.features.electricalrules.domain

import java.math.BigDecimal
import java.math.RoundingMode

object ElectricalRuleValueFormatter {
    fun decimal(value: Double): String {
        return BigDecimal.valueOf(value)
            .setScale(2, RoundingMode.HALF_UP)
            .stripTrailingZeros()
            .toPlainString()
            .replace(".", ",")
    }
}
