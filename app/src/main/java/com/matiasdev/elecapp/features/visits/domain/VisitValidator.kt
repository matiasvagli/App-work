package com.matiasdev.elecapp.features.visits.domain

import java.time.Instant

data class VisitValidationResult(
    val reasonError: String? = null,
    val dateTimeError: String? = null,
    val durationError: String? = null,
) {
    val isValid: Boolean = reasonError == null && dateTimeError == null && durationError == null
}

object VisitValidator {
    fun validate(
        reason: String,
        scheduledAt: Instant?,
        estimatedDurationMinutes: Int?,
        now: Instant = Instant.now(),
    ): VisitValidationResult {
        return VisitValidationResult(
            reasonError = if (reason.trim().isBlank()) "El motivo es obligatorio" else null,
            dateTimeError = when {
                scheduledAt == null -> "Ingresá fecha y hora válidas"
                scheduledAt.isBefore(now) -> "La visita no puede quedar en el pasado"
                else -> null
            },
            durationError = if (estimatedDurationMinutes != null && estimatedDurationMinutes <= 0) {
                "La duración debe ser mayor a cero"
            } else {
                null
            },
        )
    }
}
