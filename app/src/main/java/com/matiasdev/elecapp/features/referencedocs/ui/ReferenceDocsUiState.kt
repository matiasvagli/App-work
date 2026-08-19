package com.matiasdev.elecapp.features.referencedocs.ui

import android.net.Uri

data class ReferenceDocsUiState(
    val isLoading: Boolean = true,
    val isImporting: Boolean = false,
    val documents: List<ReferenceDocumentRow> = emptyList(),
)

data class ReferenceDocumentRow(
    val id: String,
    val title: String,
    val sizeLabel: String,
    val ageLabel: String,
    val isStale: Boolean,
    val sourceUrl: String?,
)

sealed interface ReferenceDocsEvent {
    data class OpenPdf(val uri: Uri) : ReferenceDocsEvent

    data class OpenUrl(val url: String) : ReferenceDocsEvent

    data class Message(val text: String) : ReferenceDocsEvent
}
