package com.matiasdev.elecapp.features.finance.domain

import java.time.Instant

/** Estado del informe guardado de una atención respecto de los datos que lo originaron. */
enum class AttentionReportState {
    /** Todavía no se generó ningún informe para esta atención. */
    NOT_GENERATED,

    /** El informe guardado refleja los datos actuales. */
    UP_TO_DATE,

    /**
     * Se editó algo después de generar el informe (una medición corregida, el cierre,
     * un hallazgo). El informe guardado sigue siendo el vigente hasta que el técnico
     * decida regenerarlo: nunca se reemplaza solo.
     */
    STALE,
}

/**
 * Compara el informe congelado contra las fuentes que lo alimentan.
 *
 * El informe no se regenera automáticamente a propósito. Si se regenerara solo, un
 * ajuste de umbrales cambiaría la conclusión de un informe ya entregado. Y si nunca
 * se pudiera regenerar, un dato mal cargado (215 V escrito como 2150 V) quedaría
 * congelado para siempre. Este estado habilita el punto medio: avisar y dejar que
 * el técnico decida.
 */
object AttentionReportStatus {

    /**
     * @param completion cierre de la atención, o null si todavía no cerró.
     * @param sourceUpdatedAt `updatedAt` de todo lo que alimenta el informe: relevamiento,
     *   sus secciones e hijos, cálculos y el propio cierre. Vacío = nada que comparar.
     */
    fun evaluate(
        completion: VisitCompletion?,
        sourceUpdatedAt: List<Instant>,
    ): AttentionReportState {
        val generatedAt = completion?.reportsGeneratedAt
        if (completion?.technicalReportSnapshot.isNullOrBlank() || generatedAt == null) {
            return AttentionReportState.NOT_GENERATED
        }
        val newestSource = sourceUpdatedAt.maxOrNull() ?: return AttentionReportState.UP_TO_DATE
        return if (newestSource.isAfter(generatedAt)) {
            AttentionReportState.STALE
        } else {
            AttentionReportState.UP_TO_DATE
        }
    }
}
