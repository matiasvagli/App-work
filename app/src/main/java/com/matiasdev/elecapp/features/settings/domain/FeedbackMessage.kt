package com.matiasdev.elecapp.features.settings.domain

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Datos del equipo que acompañan a un comentario.
 *
 * Sin ellos un reporte de la beta es casi inútil: “se cierra al guardar” no se puede reproducir
 * si no se sabe versión de app, de Android ni modelo.
 */
data class FeedbackContext(
    val appVersion: String,
    val androidRelease: String,
    val androidSdk: Int,
    val deviceModel: String,
)

private val TIMESTAMP_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

object FeedbackMessage {
    /**
     * Arma el texto que se comparte. No se manda solo: el usuario elige la app en el Sharesheet,
     * igual que el resto de lo que comparte ElecApp.
     */
    fun build(
        comment: String,
        context: FeedbackContext,
        now: Instant,
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): String {
        val body = comment.trim().ifBlank { "(sin comentario)" }
        val timestamp = TIMESTAMP_FORMAT.format(now.atZone(zoneId))
        return buildString {
            appendLine("Comentario sobre ElecApp")
            appendLine()
            appendLine(body)
            appendLine()
            appendLine("---")
            appendLine("App: ${context.appVersion}")
            appendLine("Android: ${context.androidRelease} (API ${context.androidSdk})")
            appendLine("Equipo: ${context.deviceModel}")
            append("Fecha: $timestamp")
        }
    }

    fun isSendable(comment: String): Boolean = comment.isNotBlank()
}
