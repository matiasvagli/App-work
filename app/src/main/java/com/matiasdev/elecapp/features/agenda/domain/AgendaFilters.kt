package com.matiasdev.elecapp.features.agenda.domain

import com.matiasdev.elecapp.features.visits.domain.Visit
import com.matiasdev.elecapp.features.visits.domain.VisitStatus
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

fun visitsForDate(visits: List<Visit>, date: LocalDate, zoneId: ZoneId = ZoneId.systemDefault()): List<Visit> {
    val bounds = localDayBounds(date, zoneId)
    return visits
        .filter { visit -> !visit.isDeleted && visit.scheduledAt >= bounds.startInclusive && visit.scheduledAt < bounds.endExclusive }
        .sortedBy { it.scheduledAt }
}

fun upcomingVisits(visits: List<Visit>, now: Instant = Instant.now()): List<Visit> {
    return visits
        .filter { visit ->
            !visit.isDeleted &&
                visit.scheduledAt >= now &&
                visit.status != VisitStatus.COMPLETED
        }
        .sortedBy { it.scheduledAt }
}

fun todaySections(visits: List<Visit>, now: Instant = Instant.now()): TodayVisitSections {
    return TodayVisitSections(
        upcoming = visits
            .filter { it.status == VisitStatus.IN_PROGRESS || (it.scheduledAt >= now && it.status != VisitStatus.COMPLETED) }
            .sortedBy { it.scheduledAt },
        doneOrPast = visits
            .filter { it.status != VisitStatus.IN_PROGRESS && (it.scheduledAt < now || it.status == VisitStatus.COMPLETED) }
            .sortedBy { it.scheduledAt },
    )
}

data class TodayVisitSections(
    val upcoming: List<Visit>,
    val doneOrPast: List<Visit>,
)
