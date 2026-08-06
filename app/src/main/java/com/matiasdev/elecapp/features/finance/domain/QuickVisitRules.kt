package com.matiasdev.elecapp.features.finance.domain

import com.matiasdev.elecapp.features.clients.domain.Client
import com.matiasdev.elecapp.features.visits.domain.Visit
import java.time.Instant

enum class QuickVisitClientMode {
    EXISTING,
    QUICK_CREATE,
}

enum class VisitAttentionType(val label: String) {
    EMERGENCY("Urgencia"),
    DIAGNOSTIC("Diagnóstico"),
    REPAIR("Reparación"),
    INSTALLATION("Instalación"),
    MAINTENANCE("Mantenimiento"),
    INSPECTION("Relevamiento"),
    OTHER("Otro"),
}

data class QuickVisitDraft(
    val clientMode: QuickVisitClientMode = QuickVisitClientMode.EXISTING,
    val selectedClientId: String? = null,
    val quickClientName: String = "",
    val phone: String = "",
    val address: String = "",
    val locality: String = "",
    val attentionType: VisitAttentionType = VisitAttentionType.EMERGENCY,
    val briefDetail: String = "",
    val estimatedDurationMinutes: String = "",
)

data class QuickVisitValidation(
    val isValid: Boolean,
    val clientError: String? = null,
    val detailError: String? = null,
    val durationError: String? = null,
)

object QuickVisitValidator {
    fun validate(draft: QuickVisitDraft): QuickVisitValidation {
        val clientError = when {
            draft.clientMode == QuickVisitClientMode.EXISTING && draft.selectedClientId.isNullOrBlank() -> "Seleccioná un cliente"
            draft.clientMode == QuickVisitClientMode.QUICK_CREATE && draft.quickClientName.isBlank() -> "Ingresá el nombre"
            else -> null
        }
        val detailError = if (draft.attentionType == VisitAttentionType.OTHER && draft.briefDetail.isBlank()) {
            "Ingresá un detalle breve"
        } else {
            null
        }
        val duration = draft.estimatedDurationMinutes.trim()
        val durationError = if (duration.isNotBlank() && duration.toIntOrNull()?.let { it > 0 } != true) {
            "La duración debe ser mayor a cero"
        } else {
            null
        }
        return QuickVisitValidation(clientError == null && detailError == null && durationError == null, clientError, detailError, durationError)
    }

    fun overlapWarning(newVisit: Visit, existingVisits: List<Pair<Visit, Client?>>, now: Instant): String? {
        val duration = newVisit.estimatedDurationMinutes ?: return null
        val start = newVisit.scheduledAt
        val end = start.plusSeconds(duration * 60L)
        val overlapping = existingVisits.firstOrNull { (visit, _) ->
            visit.id != newVisit.id &&
                !visit.isDeleted &&
                visit.scheduledAt >= now.minusSeconds(86_400) &&
                visit.estimatedDurationMinutes != null &&
                visit.scheduledAt < end &&
                visit.scheduledAt.plusSeconds(visit.estimatedDurationMinutes * 60L) > start
        }?.first ?: return null
        val overlapStart = overlapping.scheduledAt
        val overlapEnd = overlapStart.plusSeconds((overlapping.estimatedDurationMinutes ?: 0) * 60L)
        return "Esta visita podría superponerse con otra de ${overlapStart.toLocalTimeText()} a ${overlapEnd.toLocalTimeText()}."
    }

    private fun Instant.toLocalTimeText(): String {
        val time = atZone(java.time.ZoneId.systemDefault()).toLocalTime()
        return "${time.hour.toString().padStart(2, '0')}:${time.minute.toString().padStart(2, '0')}"
    }
}
