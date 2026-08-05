package com.matiasdev.elecapp.features.agenda.domain

import com.matiasdev.elecapp.features.visits.domain.Visit
import com.matiasdev.elecapp.features.visits.domain.VisitStatus
import java.time.Instant

enum class VisitVisualState(val label: String) {
    UPCOMING("Próxima"),
    SOON("En menos de una hora"),
    IN_PROGRESS("En curso"),
    PAST_PENDING("Pendiente vencida"),
    COMPLETED("Realizada"),
    CANCELLED("Cancelada"),
}

fun visitVisualState(visit: Visit, now: Instant = Instant.now()): VisitVisualState {
    return when {
        visit.status == VisitStatus.COMPLETED -> VisitVisualState.COMPLETED
        visit.status == VisitStatus.CANCELLED -> VisitVisualState.CANCELLED
        visit.status == VisitStatus.IN_PROGRESS -> VisitVisualState.IN_PROGRESS
        visit.scheduledAt.isBefore(now) -> VisitVisualState.PAST_PENDING
        visit.scheduledAt <= now.plusSeconds(3600) -> VisitVisualState.SOON
        else -> VisitVisualState.UPCOMING
    }
}
