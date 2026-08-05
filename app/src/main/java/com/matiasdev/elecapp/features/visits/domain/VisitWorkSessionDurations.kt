package com.matiasdev.elecapp.features.visits.domain

import java.time.Duration
import java.time.Instant

data class VisitWorkSummary(
    val totalWorkedDuration: Duration,
    val totalElapsedDuration: Duration,
    val totalPausedDuration: Duration,
    val sessionCount: Int,
    val pauseCount: Int,
    val activeSession: VisitWorkSession?,
) {
    val totalWorkedMinutes: Long = totalWorkedDuration.toMinutes()
}

object VisitWorkSessionDurations {
    fun sessionDuration(session: VisitWorkSession, now: Instant): Duration {
        val end = session.endedAt ?: now
        return Duration.between(session.startedAt, end).coerceNotNegative()
    }

    fun summarize(visit: Visit, sessions: List<VisitWorkSession>, now: Instant): VisitWorkSummary {
        val ordered = sessions.filterNot { it.isDeleted }.sortedBy { it.startedAt }
        val worked = ordered.fold(Duration.ZERO) { total, session -> total + sessionDuration(session, now) }
        val elapsed = visit.startedAt?.let { start ->
            Duration.between(start, visit.completedAt ?: now).coerceNotNegative()
        } ?: Duration.ZERO
        return VisitWorkSummary(
            totalWorkedDuration = worked,
            totalElapsedDuration = elapsed,
            totalPausedDuration = (elapsed - worked).coerceNotNegative(),
            sessionCount = ordered.size,
            pauseCount = pauseCount(ordered),
            activeSession = ordered.firstOrNull { it.status == VisitWorkSessionStatus.RUNNING && it.endedAt == null },
        )
    }

    fun pauseCount(sessions: List<VisitWorkSession>): Int {
        return sessions.count { it.status == VisitWorkSessionStatus.PAUSED && !it.isDeleted }
    }

    fun overlaps(startedAt: Instant, endedAt: Instant, sessions: List<VisitWorkSession>): Boolean {
        return sessions.filterNot { it.isDeleted }.any { session ->
            val end = session.endedAt ?: Instant.MAX
            startedAt < end && endedAt > session.startedAt
        }
    }

    private fun Duration.coerceNotNegative(): Duration {
        return if (isNegative) Duration.ZERO else this
    }
}
