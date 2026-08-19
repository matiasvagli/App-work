package com.matiasdev.elecapp.features.referencedocs.domain

import java.time.Duration
import java.time.Instant

/**
 * Un documento se considera desactualizado a los 60 días.
 *
 * Las listas de precios de referencia se publican por mes, así que a los dos meses ya arrastra
 * al menos una actualización. No bloquea nada: solo avisa, porque el criterio de cuándo sirve
 * un valor es del técnico.
 */
private val STALE_AFTER: Duration = Duration.ofDays(60)

object ReferenceDocumentLabels {
    fun size(bytes: Long): String = when {
        bytes < 0 -> "—"
        bytes < 1_000 -> "$bytes B"
        bytes < 1_000_000 -> "${bytes / 1_000} kB"
        else -> String.format("%.1f MB", bytes / 1_000_000.0)
    }

    fun age(importedAt: Instant, now: Instant): String {
        val days = daysBetween(importedAt, now)
        return when {
            days <= 0L -> "Importado hoy"
            days == 1L -> "Importado ayer"
            days < 30L -> "Importado hace $days días"
            days < 60L -> "Importado hace 1 mes"
            days < 365L -> "Importado hace ${days / 30} meses"
            else -> "Importado hace más de un año"
        }
    }

    fun isStale(importedAt: Instant, now: Instant): Boolean {
        return daysBetween(importedAt, now) >= STALE_AFTER.toDays()
    }

    private fun daysBetween(from: Instant, to: Instant): Long {
        return Duration.between(from, to).toDays().coerceAtLeast(0L)
    }
}
