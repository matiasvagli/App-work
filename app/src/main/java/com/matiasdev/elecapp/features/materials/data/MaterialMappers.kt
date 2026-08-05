package com.matiasdev.elecapp.features.materials.data

import com.matiasdev.elecapp.features.materials.domain.MaterialItem
import com.matiasdev.elecapp.features.materials.domain.MaterialList
import com.matiasdev.elecapp.features.materials.domain.MaterialListItem
import com.matiasdev.elecapp.features.materials.domain.MaterialListStatus
import com.matiasdev.elecapp.features.materials.domain.MaterialUnit
import com.matiasdev.elecapp.features.materials.domain.PurchaseResponsibility
import java.time.Instant

fun MaterialListEntity.toDomain(): MaterialList = MaterialList(
    id = id,
    clientId = clientId,
    visitId = visitId,
    inspectionId = inspectionId,
    quoteId = quoteId,
    title = title,
    status = MaterialListStatus.valueOf(status),
    purchaseResponsibility = PurchaseResponsibility.valueOf(purchaseResponsibility),
    introduction = introduction,
    notes = notes,
    createdAt = Instant.ofEpochMilli(createdAt),
    updatedAt = Instant.ofEpochMilli(updatedAt),
    deliveredAt = deliveredAt?.let(Instant::ofEpochMilli),
    isDeleted = isDeleted,
)

fun MaterialList.toEntity(): MaterialListEntity = MaterialListEntity(
    id = id,
    clientId = clientId,
    visitId = visitId,
    inspectionId = inspectionId,
    quoteId = quoteId,
    title = title,
    status = status.name,
    purchaseResponsibility = purchaseResponsibility.name,
    introduction = introduction,
    notes = notes,
    createdAt = createdAt.toEpochMilli(),
    updatedAt = updatedAt.toEpochMilli(),
    deliveredAt = deliveredAt?.toEpochMilli(),
    isDeleted = isDeleted,
)

fun MaterialItemEntity.toDomain(): MaterialItem = MaterialItem(
    id = id,
    materialListId = materialListId,
    description = description,
    quantity = quantity,
    unit = MaterialUnit.valueOf(unit),
    customUnitLabel = customUnitLabel,
    specifications = specifications,
    preferredBrand = preferredBrand,
    alternativeAllowed = alternativeAllowed,
    estimatedUnitPriceAmount = estimatedUnitPriceAmount,
    actualUnitPriceAmount = actualUnitPriceAmount,
    sortOrder = sortOrder,
    notes = notes,
    createdAt = Instant.ofEpochMilli(createdAt),
    updatedAt = Instant.ofEpochMilli(updatedAt),
    isDeleted = isDeleted,
)

fun MaterialItem.toEntity(): MaterialItemEntity = MaterialItemEntity(
    id = id,
    materialListId = materialListId,
    description = description,
    quantity = quantity,
    unit = unit.name,
    customUnitLabel = customUnitLabel,
    specifications = specifications,
    preferredBrand = preferredBrand,
    alternativeAllowed = alternativeAllowed,
    estimatedUnitPriceAmount = estimatedUnitPriceAmount,
    actualUnitPriceAmount = actualUnitPriceAmount,
    sortOrder = sortOrder,
    notes = notes,
    createdAt = createdAt.toEpochMilli(),
    updatedAt = updatedAt.toEpochMilli(),
    isDeleted = isDeleted,
)

fun MaterialListItemEntity.toDomain(): MaterialListItem {
    val list = MaterialListEntity(
        id = id,
        clientId = clientId,
        visitId = visitId,
        inspectionId = inspectionId,
        quoteId = quoteId,
        title = title,
        status = status,
        purchaseResponsibility = purchaseResponsibility,
        introduction = introduction,
        notes = notes,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deliveredAt = deliveredAt,
        isDeleted = isDeleted,
    ).toDomain()
    return MaterialListItem(list, clientName, address, locality, itemCount, quoteNumber)
}
