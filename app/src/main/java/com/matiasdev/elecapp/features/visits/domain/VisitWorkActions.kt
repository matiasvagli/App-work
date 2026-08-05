package com.matiasdev.elecapp.features.visits.domain

enum class VisitWorkPrimaryAction {
    START,
    PAUSE,
    RESUME,
    NONE,
}

object VisitWorkActions {
    fun primaryAction(status: VisitStatus, activeSession: VisitWorkSession?): VisitWorkPrimaryAction {
        return when {
            status in listOf(VisitStatus.PENDING, VisitStatus.CONFIRMED) -> VisitWorkPrimaryAction.START
            status == VisitStatus.IN_PROGRESS && activeSession != null -> VisitWorkPrimaryAction.PAUSE
            status == VisitStatus.IN_PROGRESS -> VisitWorkPrimaryAction.RESUME
            else -> VisitWorkPrimaryAction.NONE
        }
    }
}
