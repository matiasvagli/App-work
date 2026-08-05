package com.matiasdev.elecapp.features.agenda.domain

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class InstantRange(
    val startInclusive: Instant,
    val endExclusive: Instant,
)

fun localDayBounds(date: LocalDate, zoneId: ZoneId = ZoneId.systemDefault()): InstantRange {
    val start = date.atStartOfDay(zoneId).toInstant()
    val end = date.plusDays(1).atStartOfDay(zoneId).toInstant()
    return InstantRange(start, end)
}
