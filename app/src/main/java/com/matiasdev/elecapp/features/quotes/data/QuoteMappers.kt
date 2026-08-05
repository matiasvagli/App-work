package com.matiasdev.elecapp.features.quotes.data

import com.matiasdev.elecapp.features.quotes.domain.DiscountType
import com.matiasdev.elecapp.features.quotes.domain.Quote
import com.matiasdev.elecapp.features.quotes.domain.QuoteCurrency
import com.matiasdev.elecapp.features.quotes.domain.QuoteItem
import com.matiasdev.elecapp.features.quotes.domain.QuoteItemType
import com.matiasdev.elecapp.features.quotes.domain.QuoteListItem
import com.matiasdev.elecapp.features.quotes.domain.QuoteStatus
import com.matiasdev.elecapp.features.quotes.domain.QuoteUnit
import java.time.Instant

fun QuoteEntity.toDomain(): Quote = Quote(
    id = id,
    clientId = clientId,
    visitId = visitId,
    inspectionId = inspectionId,
    quoteNumber = quoteNumber,
    title = title,
    description = description,
    status = QuoteStatus.valueOf(status),
    currency = QuoteCurrency.valueOf(currency),
    subtotalAmount = subtotalAmount,
    discountType = DiscountType.valueOf(discountType),
    discountValue = discountValue,
    totalAmount = totalAmount,
    validUntil = validUntil?.let(Instant::ofEpochMilli),
    paymentTerms = paymentTerms,
    generalNotes = generalNotes,
    clientMessage = clientMessage,
    sentAt = sentAt?.let(Instant::ofEpochMilli),
    approvedAt = approvedAt?.let(Instant::ofEpochMilli),
    rejectedAt = rejectedAt?.let(Instant::ofEpochMilli),
    createdAt = Instant.ofEpochMilli(createdAt),
    updatedAt = Instant.ofEpochMilli(updatedAt),
    isDeleted = isDeleted,
)

fun Quote.toEntity(): QuoteEntity = QuoteEntity(
    id = id,
    clientId = clientId,
    visitId = visitId,
    inspectionId = inspectionId,
    quoteNumber = quoteNumber,
    title = title,
    description = description,
    status = status.name,
    currency = currency.name,
    subtotalAmount = subtotalAmount,
    discountType = discountType.name,
    discountValue = discountValue,
    totalAmount = totalAmount,
    validUntil = validUntil?.toEpochMilli(),
    paymentTerms = paymentTerms,
    generalNotes = generalNotes,
    clientMessage = clientMessage,
    sentAt = sentAt?.toEpochMilli(),
    approvedAt = approvedAt?.toEpochMilli(),
    rejectedAt = rejectedAt?.toEpochMilli(),
    createdAt = createdAt.toEpochMilli(),
    updatedAt = updatedAt.toEpochMilli(),
    isDeleted = isDeleted,
)

fun QuoteItemEntity.toDomain(): QuoteItem = QuoteItem(
    id = id,
    quoteId = quoteId,
    type = QuoteItemType.valueOf(type),
    description = description,
    quantity = quantity,
    unit = QuoteUnit.valueOf(unit),
    customUnitLabel = customUnitLabel,
    unitPriceAmount = unitPriceAmount,
    lineTotalAmount = lineTotalAmount,
    sortOrder = sortOrder,
    notes = notes,
    createdAt = Instant.ofEpochMilli(createdAt),
    updatedAt = Instant.ofEpochMilli(updatedAt),
    isDeleted = isDeleted,
)

fun QuoteItem.toEntity(): QuoteItemEntity = QuoteItemEntity(
    id = id,
    quoteId = quoteId,
    type = type.name,
    description = description,
    quantity = quantity,
    unit = unit.name,
    customUnitLabel = customUnitLabel,
    unitPriceAmount = unitPriceAmount,
    lineTotalAmount = lineTotalAmount,
    sortOrder = sortOrder,
    notes = notes,
    createdAt = createdAt.toEpochMilli(),
    updatedAt = updatedAt.toEpochMilli(),
    isDeleted = isDeleted,
)

fun QuoteListItemEntity.toDomain(): QuoteListItem {
    val quote = QuoteEntity(
        id = id,
        clientId = clientId,
        visitId = visitId,
        inspectionId = inspectionId,
        quoteNumber = quoteNumber,
        title = title,
        description = description,
        status = status,
        currency = currency,
        subtotalAmount = subtotalAmount,
        discountType = discountType,
        discountValue = discountValue,
        totalAmount = totalAmount,
        validUntil = validUntil,
        paymentTerms = paymentTerms,
        generalNotes = generalNotes,
        clientMessage = clientMessage,
        sentAt = sentAt,
        approvedAt = approvedAt,
        rejectedAt = rejectedAt,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isDeleted = isDeleted,
    ).toDomain()
    return QuoteListItem(quote, clientName, address, locality, itemCount, hasMaterialList)
}
