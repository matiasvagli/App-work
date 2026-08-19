package com.matiasdev.elecapp.features.quotes.ui

import com.matiasdev.elecapp.features.clients.domain.Client
import com.matiasdev.elecapp.features.quotes.domain.DiscountType
import com.matiasdev.elecapp.features.quotes.domain.QuoteCurrency
import com.matiasdev.elecapp.features.quotes.domain.QuoteItemType
import com.matiasdev.elecapp.features.quotes.domain.QuoteStatus
import com.matiasdev.elecapp.features.quotes.domain.QuoteUnit

data class QuoteFormUiState(
    val quoteId: String? = null,
    val clientId: String? = null,
    val visitId: String? = null,
    val inspectionId: String? = null,
    val quoteNumber: String = "",
    val clientQuery: String = "",
    val clients: List<Client> = emptyList(),
    val title: String = "",
    val description: String = "",
    val currency: QuoteCurrency = QuoteCurrency.ARS,
    val items: List<QuoteItemFormState> = emptyList(),
    val expandedItemIds: Set<String> = emptySet(),
    val discountType: DiscountType = DiscountType.NONE,
    val discountInput: String = "",
    val validUntilInput: String = "",
    val paymentTerms: String = "",
    val generalNotes: String = "",
    val clientMessage: String = "",
    val isSaving: Boolean = false,
    val savedQuoteId: String? = null,
    val errorMessage: String? = null,
)

data class QuoteItemFormState(
    val id: String,
    val type: QuoteItemType,
    val description: String,
    val quantity: String,
    val unit: QuoteUnit,
    val customUnitLabel: String,
    val unitPriceInput: String,
    val notes: String,
)

enum class QuoteSaveMode {
    DRAFT,
    READY,
}
