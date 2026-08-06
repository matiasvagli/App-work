package com.matiasdev.elecapp.features.finance.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "visit_completions",
    indices = [
        Index(value = ["visit_id"], unique = true),
        Index(value = ["completed_at"]),
        Index(value = ["is_deleted"]),
    ],
)
data class VisitCompletionEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "visit_id") val visitId: String,
    val diagnosis: String?,
    @ColumnInfo(name = "work_performed") val workPerformed: String,
    @ColumnInfo(name = "pending_work") val pendingWork: String?,
    @ColumnInfo(name = "requires_follow_up") val requiresFollowUp: Boolean,
    @ColumnInfo(name = "follow_up_suggested_at") val followUpSuggestedAt: Long?,
    @ColumnInfo(name = "internal_notes") val internalNotes: String?,
    @ColumnInfo(name = "customer_notes") val customerNotes: String?,
    @ColumnInfo(name = "completed_at") val completedAt: Long,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean,
)

@Entity(
    tableName = "service_receipts",
    indices = [
        Index(value = ["receipt_number"], unique = true),
        Index(value = ["client_id"]),
        Index(value = ["visit_id"]),
        Index(value = ["quote_id"]),
        Index(value = ["issued_at"]),
        Index(value = ["status"]),
        Index(value = ["is_deleted"]),
    ],
)
data class ServiceReceiptEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "receipt_number") val receiptNumber: Long?,
    @ColumnInfo(name = "client_id") val clientId: String,
    @ColumnInfo(name = "visit_id") val visitId: String?,
    @ColumnInfo(name = "quote_id") val quoteId: String?,
    @ColumnInfo(name = "issued_at") val issuedAt: Long?,
    val title: String,
    val description: String?,
    val status: String,
    @ColumnInfo(name = "subtotal_labor_cents") val subtotalLaborCents: Long,
    @ColumnInfo(name = "subtotal_materials_cents") val subtotalMaterialsCents: Long,
    @ColumnInfo(name = "subtotal_other_cents") val subtotalOtherCents: Long,
    @ColumnInfo(name = "discount_cents") val discountCents: Long,
    @ColumnInfo(name = "total_cents") val totalCents: Long,
    val notes: String?,
    @ColumnInfo(name = "internal_notes") val internalNotes: String?,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean,
)

@Entity(
    tableName = "service_receipt_items",
    indices = [
        Index(value = ["receipt_id"]),
        Index(value = ["type"]),
        Index(value = ["source_id"]),
        Index(value = ["sort_order"]),
        Index(value = ["is_deleted"]),
    ],
)
data class ServiceReceiptItemEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "receipt_id") val receiptId: String,
    val type: String,
    val description: String,
    @ColumnInfo(name = "quantity_millis") val quantityMillis: Long,
    @ColumnInfo(name = "unit_price_cents") val unitPriceCents: Long,
    @ColumnInfo(name = "total_cents") val totalCents: Long,
    @ColumnInfo(name = "source_type") val sourceType: String,
    @ColumnInfo(name = "source_id") val sourceId: String?,
    @ColumnInfo(name = "supplied_by") val suppliedBy: String,
    @ColumnInfo(name = "is_chargeable") val isChargeable: Boolean,
    @ColumnInfo(name = "sort_order") val sortOrder: Int,
    val notes: String?,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean,
)

@Entity(
    tableName = "payments",
    indices = [
        Index(value = ["client_id"]),
        Index(value = ["visit_id"]),
        Index(value = ["service_receipt_id"]),
        Index(value = ["paid_at"]),
        Index(value = ["method"]),
        Index(value = ["status"]),
        Index(value = ["is_deleted"]),
    ],
)
data class PaymentEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "client_id") val clientId: String,
    @ColumnInfo(name = "visit_id") val visitId: String?,
    @ColumnInfo(name = "service_receipt_id") val serviceReceiptId: String?,
    @ColumnInfo(name = "amount_cents") val amountCents: Long,
    val method: String,
    @ColumnInfo(name = "paid_at") val paidAt: Long,
    val reference: String?,
    val notes: String?,
    val status: String,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean,
)

@Entity(tableName = "receipt_sequence")
data class ReceiptSequenceEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "next_number") val nextNumber: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
)
