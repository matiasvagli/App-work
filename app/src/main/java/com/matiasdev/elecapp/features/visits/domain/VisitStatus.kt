package com.matiasdev.elecapp.features.visits.domain

enum class VisitStatus(val label: String) {
    PENDING("Pendiente"),
    CONFIRMED("Confirmada"),
    IN_PROGRESS("En curso"),
    COMPLETED("Realizada"),
    CANCELLED("Cancelada"),
}
