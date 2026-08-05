package com.matiasdev.elecapp.features.agenda.domain

import com.matiasdev.elecapp.features.visits.domain.Visit
import java.time.LocalDate
import java.time.ZoneId

data class AgendaVisitGroup(
    val title: String,
    val visits: List<Visit>,
)

fun groupUpcomingVisits(
    visits: List<Visit>,
    today: LocalDate = LocalDate.now(),
    zoneId: ZoneId = ZoneId.systemDefault(),
): List<AgendaVisitGroup> {
    val tomorrow = today.plusDays(1)
    val weekEnd = today.plusDays(7)
    val grouped = visits.groupBy { it.scheduledAt.atZone(zoneId).toLocalDate() }
    val tomorrowVisits = grouped.filterKeys { it == tomorrow }.values.flatten()
    val weekVisits = grouped.filterKeys { it > tomorrow && it <= weekEnd }.values.flatten()
    val laterVisits = grouped.filterKeys { it > weekEnd }.values.flatten()

    return listOf(
        AgendaVisitGroup("Mañana", tomorrowVisits),
        AgendaVisitGroup("Próximos 7 días", weekVisits),
        AgendaVisitGroup("Posteriores", laterVisits),
    ).filter { it.visits.isNotEmpty() }
}
