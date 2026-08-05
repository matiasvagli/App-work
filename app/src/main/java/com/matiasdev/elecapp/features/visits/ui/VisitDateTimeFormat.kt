package com.matiasdev.elecapp.features.visits.ui

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val DateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
private val TimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
private val VisitDateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

fun LocalDate.formatVisitDate(): String = DateFormatter.format(this)

fun LocalTime.formatVisitTime(): String = TimeFormatter.format(this)

fun Instant.formatVisitDateTime(): String = VisitDateTimeFormatter.format(atZone(ZoneId.systemDefault()))
