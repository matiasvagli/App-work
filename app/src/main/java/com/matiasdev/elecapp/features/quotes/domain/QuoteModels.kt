package com.matiasdev.elecapp.features.quotes.domain

import java.time.Instant

data class Quote(
    val id: String,
    val clientId: String,
    val visitId: String?,
    val inspectionId: String?,
    val quoteNumber: String,
    val title: String,
    val description: String?,
    val status: QuoteStatus,
    val currency: QuoteCurrency,
    val subtotalAmount: Long,
    val discountType: DiscountType,
    val discountValue: Long,
    val totalAmount: Long,
    val validUntil: Instant?,
    val paymentTerms: String?,
    val generalNotes: String?,
    val clientMessage: String?,
    val sentAt: Instant?,
    val approvedAt: Instant?,
    val rejectedAt: Instant?,
    val createdAt: Instant,
    val updatedAt: Instant,
    val isDeleted: Boolean,
)

data class QuoteItem(
    val id: String,
    val quoteId: String,
    val type: QuoteItemType,
    val description: String,
    val quantity: Double,
    val unit: QuoteUnit,
    val customUnitLabel: String?,
    val unitPriceAmount: Long,
    val lineTotalAmount: Long,
    val sortOrder: Int,
    val notes: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
    val isDeleted: Boolean,
)

data class QuoteListItem(
    val quote: Quote,
    val clientName: String,
    val address: String?,
    val locality: String?,
    val itemCount: Int,
    val hasMaterialList: Boolean,
)

enum class QuoteStatus {
    DRAFT,
    READY,
    SENT,
    APPROVED,
    REJECTED,
    EXPIRED,
    CANCELLED,
}

enum class QuoteCurrency {
    ARS,
    USD,
}

enum class DiscountType {
    NONE,
    FIXED,
    PERCENTAGE,
}

enum class QuoteItemType {
    LABOR,
    SERVICE,
    MATERIAL,
    OTHER,
}

enum class QuoteUnit {
    UNIT,
    HOUR,
    METER,
    DAY,
    FIXED,
    OTHER,
}
