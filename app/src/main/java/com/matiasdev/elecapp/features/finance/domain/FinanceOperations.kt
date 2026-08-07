package com.matiasdev.elecapp.features.finance.domain

import java.time.Instant

data class ReceiptItemDraft(
    val type: ServiceReceiptItemType,
    val description: String,
    val quantityMillis: Long,
    val unitPriceCents: Long,
    val sourceType: ReceiptItemSourceType = ReceiptItemSourceType.MANUAL,
    val sourceId: String? = null,
    val suppliedBy: MaterialSuppliedBy = MaterialSuppliedBy.UNKNOWN,
    val isChargeable: Boolean = true,
    val notes: String? = null,
)

data class PaymentDraft(
    val amountCents: Long,
    val method: PaymentMethod,
    val paidAt: Instant,
    val reference: String? = null,
    val notes: String? = null,
)

data class VisitCloseDraft(
    val diagnosis: String?,
    val workType: VisitWorkType?,
    val workPerformed: String,
    val workSectors: String?,
    val workItems: String?,
    val workTests: String?,
    val workObservations: String?,
    val technicalResult: VisitTechnicalResult?,
    val pendingWork: String?,
    val requiresFollowUp: Boolean,
    val followUpSuggestedAt: Instant?,
    val internalNotes: String?,
    val customerNotes: String?,
    val generateReceipt: Boolean,
    val quoteId: String?,
    val receiptTitle: String,
    val receiptDescription: String?,
    val items: List<ReceiptItemDraft>,
    val discountCents: Long,
    val initialPayments: List<PaymentDraft>,
)

data class VisitCloseResult(
    val visitId: String,
    val receiptId: String?,
)

data class RegisterPaymentResult(
    val paymentId: String,
    val receiptStatus: ServiceReceiptStatus?,
)
