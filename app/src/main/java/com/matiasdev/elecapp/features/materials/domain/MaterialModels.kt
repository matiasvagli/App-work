package com.matiasdev.elecapp.features.materials.domain

import java.time.Instant

data class MaterialList(
    val id: String,
    val clientId: String,
    val visitId: String?,
    val inspectionId: String?,
    val quoteId: String?,
    val title: String,
    val status: MaterialListStatus,
    val purchaseResponsibility: PurchaseResponsibility,
    val introduction: String?,
    val notes: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
    val deliveredAt: Instant?,
    val isDeleted: Boolean,
)

data class MaterialItem(
    val id: String,
    val materialListId: String,
    val description: String,
    val quantity: Double,
    val unit: MaterialUnit,
    val customUnitLabel: String?,
    val specifications: String?,
    val preferredBrand: String?,
    val alternativeAllowed: Boolean,
    val estimatedUnitPriceAmount: Long?,
    val actualUnitPriceAmount: Long?,
    val sortOrder: Int,
    val notes: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
    val isDeleted: Boolean,
)

data class MaterialListItem(
    val materialList: MaterialList,
    val clientName: String,
    val address: String?,
    val locality: String?,
    val itemCount: Int,
    val quoteNumber: String?,
)

enum class MaterialListStatus {
    DRAFT,
    READY,
    DELIVERED,
    PURCHASED,
    CANCELLED,
}

enum class PurchaseResponsibility {
    CLIENT,
    TECHNICIAN,
    TO_BE_DEFINED,
}

enum class MaterialUnit {
    UNIT,
    METER,
    ROLL,
    BOX,
    PACK,
    OTHER,
}
