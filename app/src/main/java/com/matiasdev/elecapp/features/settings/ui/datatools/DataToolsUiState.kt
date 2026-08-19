package com.matiasdev.elecapp.features.settings.ui.datatools

import com.matiasdev.elecapp.features.settings.domain.FeedbackMessage

data class DataToolsUiState(
    val comment: String = "",
    val isSeeding: Boolean = false,
    val isWiping: Boolean = false,
) {
    val canSendFeedback: Boolean get() = FeedbackMessage.isSendable(comment) && !isBusy

    val isBusy: Boolean get() = isSeeding || isWiping
}

sealed interface DataToolsEvent {
    data class ShareText(val text: String) : DataToolsEvent

    data class Message(val text: String) : DataToolsEvent
}
