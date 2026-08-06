package com.matiasdev.elecapp.features.finance.domain

import java.time.Instant

data class VisitCompletion(
    val id: String,
    val visitId: String,
    val diagnosis: String?,
    val workPerformed: String,
    val pendingWork: String?,
    val requiresFollowUp: Boolean,
    val followUpSuggestedAt: Instant?,
    val internalNotes: String?,
    val customerNotes: String?,
    val completedAt: Instant,
    val createdAt: Instant,
    val updatedAt: Instant,
    val isDeleted: Boolean,
)

data class ServiceReceipt(
    val id: String,
    val receiptNumber: Long?,
    val clientId: String,
    val visitId: String?,
    val quoteId: String?,
    val issuedAt: Instant?,
    val title: String,
    val description: String?,
    val status: ServiceReceiptStatus,
    val subtotalLaborCents: Long,
    val subtotalMaterialsCents: Long,
    val subtotalOtherCents: Long,
    val discountCents: Long,
    val totalCents: Long,
    val notes: String?,
    val internalNotes: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
    val isDeleted: Boolean,
)

data class ServiceReceiptItem(
    val id: String,
    val receiptId: String,
    val type: ServiceReceiptItemType,
    val description: String,
    val quantityMillis: Long,
    val unitPriceCents: Long,
    val totalCents: Long,
    val sourceType: ReceiptItemSourceType,
    val sourceId: String?,
    val suppliedBy: MaterialSuppliedBy,
    val isChargeable: Boolean,
    val sortOrder: Int,
    val notes: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
    val isDeleted: Boolean,
)

data class Payment(
    val id: String,
    val clientId: String,
    val visitId: String?,
    val serviceReceiptId: String?,
    val amountCents: Long,
    val method: PaymentMethod,
    val paidAt: Instant,
    val reference: String?,
    val notes: String?,
    val status: PaymentStatus,
    val createdAt: Instant,
    val updatedAt: Instant,
    val isDeleted: Boolean,
)

enum class ServiceReceiptStatus {
    DRAFT,
    ISSUED,
    PARTIALLY_PAID,
    PAID,
    CANCELLED,
}

enum class ServiceReceiptItemType {
    LABOR,
    MATERIAL,
    ADDITIONAL,
    DISCOUNT,
}

enum class ReceiptItemSourceType {
    MANUAL,
    QUOTE,
    MATERIAL_LIST,
    VISIT,
}

enum class MaterialSuppliedBy {
    TECHNICIAN,
    CLIENT,
    UNKNOWN,
}

enum class PaymentMethod {
    CASH,
    BANK_TRANSFER,
    MERCADO_PAGO,
    CARD,
    CHECK,
    OTHER,
}

enum class PaymentStatus {
    CONFIRMED,
    CANCELLED,
}

enum class FinancePeriodPreset {
    TODAY,
    LAST_7_DAYS,
    THIS_MONTH,
    PREVIOUS_MONTH,
    THIS_YEAR,
}

data class ReceiptTotals(
    val subtotalLaborCents: Long,
    val subtotalMaterialsCents: Long,
    val subtotalOtherCents: Long,
    val subtotalCents: Long,
    val discountCents: Long,
    val totalCents: Long,
)

data class PaymentBalance(
    val totalCents: Long,
    val paidCents: Long,
    val pendingCents: Long,
    val overpaymentCents: Long,
)

data class FinanceMetrics(
    val completedJobs: Int,
    val workedMinutes: Long,
    val generatedCents: Long,
    val collectedCents: Long,
    val pendingCents: Long,
    val paymentCount: Int,
    val averageTicketCents: Long,
    val generatedPerHourCents: Long,
    val collectedPerHourCents: Long,
    val servedClientCount: Int,
)

fun ServiceReceipt.displayNumber(): String {
    return receiptNumber?.let { "CS-${it.toString().padStart(6, '0')}" } ?: "Borrador"
}
