package com.matiasdev.elecapp.features.quotes.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "quotes",
    indices = [
        Index("client_id"),
        Index("visit_id"),
        Index("inspection_id"),
        Index("status"),
        Index(value = ["quote_number"], unique = true),
    ],
)
data class QuoteEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "client_id") val clientId: String,
    @ColumnInfo(name = "visit_id") val visitId: String?,
    @ColumnInfo(name = "inspection_id") val inspectionId: String?,
    @ColumnInfo(name = "quote_number") val quoteNumber: String,
    val title: String,
    val description: String?,
    val status: String,
    val currency: String,
    @ColumnInfo(name = "subtotal_amount") val subtotalAmount: Long,
    @ColumnInfo(name = "discount_type") val discountType: String,
    @ColumnInfo(name = "discount_value") val discountValue: Long,
    @ColumnInfo(name = "total_amount") val totalAmount: Long,
    @ColumnInfo(name = "valid_until") val validUntil: Long?,
    @ColumnInfo(name = "payment_terms") val paymentTerms: String?,
    @ColumnInfo(name = "general_notes") val generalNotes: String?,
    @ColumnInfo(name = "client_message") val clientMessage: String?,
    @ColumnInfo(name = "sent_at") val sentAt: Long?,
    @ColumnInfo(name = "approved_at") val approvedAt: Long?,
    @ColumnInfo(name = "rejected_at") val rejectedAt: Long?,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean,
)

@Entity(
    tableName = "quote_items",
    indices = [
        Index("quote_id"),
        Index(value = ["quote_id", "sort_order"]),
    ],
)
data class QuoteItemEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "quote_id") val quoteId: String,
    val type: String,
    val description: String,
    val quantity: Double,
    val unit: String,
    @ColumnInfo(name = "custom_unit_label") val customUnitLabel: String?,
    @ColumnInfo(name = "unit_price_amount") val unitPriceAmount: Long,
    @ColumnInfo(name = "line_total_amount") val lineTotalAmount: Long,
    @ColumnInfo(name = "sort_order") val sortOrder: Int,
    val notes: String?,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean,
)

data class QuoteListItemEntity(
    val id: String,
    @ColumnInfo(name = "client_id") val clientId: String,
    @ColumnInfo(name = "visit_id") val visitId: String?,
    @ColumnInfo(name = "inspection_id") val inspectionId: String?,
    @ColumnInfo(name = "quote_number") val quoteNumber: String,
    val title: String,
    val description: String?,
    val status: String,
    val currency: String,
    @ColumnInfo(name = "subtotal_amount") val subtotalAmount: Long,
    @ColumnInfo(name = "discount_type") val discountType: String,
    @ColumnInfo(name = "discount_value") val discountValue: Long,
    @ColumnInfo(name = "total_amount") val totalAmount: Long,
    @ColumnInfo(name = "valid_until") val validUntil: Long?,
    @ColumnInfo(name = "payment_terms") val paymentTerms: String?,
    @ColumnInfo(name = "general_notes") val generalNotes: String?,
    @ColumnInfo(name = "client_message") val clientMessage: String?,
    @ColumnInfo(name = "sent_at") val sentAt: Long?,
    @ColumnInfo(name = "approved_at") val approvedAt: Long?,
    @ColumnInfo(name = "rejected_at") val rejectedAt: Long?,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean,
    @ColumnInfo(name = "client_name") val clientName: String,
    val address: String?,
    val locality: String?,
    @ColumnInfo(name = "item_count") val itemCount: Int,
    @ColumnInfo(name = "has_material_list") val hasMaterialList: Boolean,
)
