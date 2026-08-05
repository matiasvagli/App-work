package com.matiasdev.elecapp.features.agenda.domain

import com.matiasdev.elecapp.features.visits.domain.Visit
import com.matiasdev.elecapp.features.visits.domain.VisitStatus
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

class AgendaFiltersTest {
    @Test
    fun `selects visits for local date and sorts them`() {
        val zone = ZoneId.of("America/Argentina/Buenos_Aires")
        val visits = listOf(
            visit("2", "2026-08-04T15:00:00Z"),
            visit("1", "2026-08-04T12:00:00Z"),
            visit("3", "2026-08-05T04:00:00Z"),
        )

        val result = visitsForDate(visits, LocalDate.of(2026, 8, 4), zone)

        assertEquals(listOf("1", "2"), result.map { it.id })
    }

    @Test
    fun `orders upcoming visits`() {
        val now = Instant.parse("2026-08-04T10:00:00Z")

        val result = upcomingVisits(listOf(visit("2", "2026-08-04T12:00:00Z"), visit("1", "2026-08-04T11:00:00Z")), now)

        assertEquals(listOf("1", "2"), result.map { it.id })
    }

    @Test
    fun `today sections keep in progress visit as operative even when scheduled time passed`() {
        val now = Instant.parse("2026-08-04T15:00:00Z")
        val inProgress = visit("current", "2026-08-04T12:00:00Z").copy(status = VisitStatus.IN_PROGRESS)
        val completed = visit("done", "2026-08-04T13:00:00Z").copy(status = VisitStatus.COMPLETED)

        val result = todaySections(listOf(inProgress, completed), now)

        assertEquals(listOf("current"), result.upcoming.map { it.id })
        assertEquals(listOf("done"), result.doneOrPast.map { it.id })
    }

    private fun visit(id: String, scheduledAt: String): Visit {
        val now = Instant.parse("2026-01-01T00:00:00Z")
        return Visit(id, "client", Instant.parse(scheduledAt), null, "Motivo", null, VisitStatus.PENDING, now, now, false)
    }
}
