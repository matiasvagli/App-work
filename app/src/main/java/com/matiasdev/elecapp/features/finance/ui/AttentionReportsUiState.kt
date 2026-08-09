package com.matiasdev.elecapp.features.finance.ui

import com.matiasdev.elecapp.features.finance.domain.AttentionReportState

data class AttentionReportsUiState(
    val isLoading: Boolean = true,
    val clientName: String = "",
    val technicalReport: String? = null,
    val clientReport: String = "",
    val reportState: AttentionReportState = AttentionReportState.NOT_GENERATED,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
) {
    val hasTechnicalReport: Boolean = !technicalReport.isNullOrBlank()

    /** El informe guardado sigue siendo el vigente; solo avisamos que hay cambios posteriores. */
    val showsStaleWarning: Boolean = reportState == AttentionReportState.STALE
}

sealed interface AttentionReportsEvent {
    data class Message(val text: String) : AttentionReportsEvent
    data class CopyToClipboard(val label: String, val text: String) : AttentionReportsEvent
    data class Share(val text: String) : AttentionReportsEvent
}
