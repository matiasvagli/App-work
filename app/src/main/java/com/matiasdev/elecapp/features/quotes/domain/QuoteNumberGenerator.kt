package com.matiasdev.elecapp.features.quotes.domain

import java.time.Instant
import java.time.ZoneId

object QuoteNumberGenerator {
    fun next(now: Instant, existingForYear: Int, zoneId: ZoneId = ZoneId.systemDefault()): String {
        val year = now.atZone(zoneId).year
        return "PRES-$year-${(existingForYear + 1).toString().padStart(4, '0')}"
    }
}
