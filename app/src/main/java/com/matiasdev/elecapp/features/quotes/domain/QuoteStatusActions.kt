package com.matiasdev.elecapp.features.quotes.domain

object QuoteStatusActions {
    fun primaryTransitions(status: QuoteStatus): List<QuoteStatus> = when (status) {
        QuoteStatus.DRAFT -> listOf(QuoteStatus.READY)
        QuoteStatus.READY -> listOf(QuoteStatus.SENT)
        QuoteStatus.SENT -> listOf(QuoteStatus.APPROVED, QuoteStatus.REJECTED)
        QuoteStatus.APPROVED,
        QuoteStatus.REJECTED,
        QuoteStatus.EXPIRED,
        QuoteStatus.CANCELLED,
        -> emptyList()
    }

    fun canCancel(status: QuoteStatus): Boolean = status in setOf(
        QuoteStatus.DRAFT,
        QuoteStatus.READY,
        QuoteStatus.SENT,
    )
}
