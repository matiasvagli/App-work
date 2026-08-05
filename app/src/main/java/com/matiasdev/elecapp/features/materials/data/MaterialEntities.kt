package com.matiasdev.elecapp.features.materials.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "material_lists",
    indices = [
        Index("client_id"),
        Index("visit_id"),
        Index("inspection_id"),
        Index("quote_id"),
        Index("status"),
    ],
)
data class MaterialListEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "client_id") val clientId: String,
    @ColumnInfo(name = "visit_id") val visitId: String?,
    @ColumnInfo(name = "inspection_id") val inspectionId: String?,
    @ColumnInfo(name = "quote_id") val quoteId: String?,
    val title: String,
    val status: String,
    @ColumnInfo(name = "purchase_responsibility") val purchaseResponsibility: String,
    val introduction: String?,
    val notes: String?,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "delivered_at") val deliveredAt: Long?,
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean,
)

@Entity(
    tableName = "material_items",
    indices = [
        Index("material_list_id"),
        Index(value = ["material_list_id", "sort_order"]),
    ],
)
data class MaterialItemEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "material_list_id") val materialListId: String,
    val description: String,
    val quantity: Double,
    val unit: String,
    @ColumnInfo(name = "custom_unit_label") val customUnitLabel: String?,
    val specifications: String?,
    @ColumnInfo(name = "preferred_brand") val preferredBrand: String?,
    @ColumnInfo(name = "alternative_allowed") val alternativeAllowed: Boolean,
    @ColumnInfo(name = "estimated_unit_price_amount") val estimatedUnitPriceAmount: Long?,
    @ColumnInfo(name = "actual_unit_price_amount") val actualUnitPriceAmount: Long?,
    @ColumnInfo(name = "sort_order") val sortOrder: Int,
    val notes: String?,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean,
)

data class MaterialListItemEntity(
    val id: String,
    @ColumnInfo(name = "client_id") val clientId: String,
    @ColumnInfo(name = "visit_id") val visitId: String?,
    @ColumnInfo(name = "inspection_id") val inspectionId: String?,
    @ColumnInfo(name = "quote_id") val quoteId: String?,
    val title: String,
    val status: String,
    @ColumnInfo(name = "purchase_responsibility") val purchaseResponsibility: String,
    val introduction: String?,
    val notes: String?,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "delivered_at") val deliveredAt: Long?,
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean,
    @ColumnInfo(name = "client_name") val clientName: String,
    val address: String?,
    val locality: String?,
    @ColumnInfo(name = "item_count") val itemCount: Int,
    @ColumnInfo(name = "quote_number") val quoteNumber: String?,
)
