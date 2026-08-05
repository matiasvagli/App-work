package com.matiasdev.elecapp.features.visits.domain

object VisitStatusTransitions {
    fun allowedNextStatuses(current: VisitStatus): List<VisitStatus> {
        return when (current) {
            VisitStatus.PENDING -> listOf(VisitStatus.CONFIRMED, VisitStatus.IN_PROGRESS, VisitStatus.CANCELLED)
            VisitStatus.CONFIRMED -> listOf(VisitStatus.IN_PROGRESS, VisitStatus.CANCELLED)
            VisitStatus.IN_PROGRESS -> listOf(VisitStatus.COMPLETED)
            VisitStatus.COMPLETED -> emptyList()
            VisitStatus.CANCELLED -> emptyList()
        }
    }
}
