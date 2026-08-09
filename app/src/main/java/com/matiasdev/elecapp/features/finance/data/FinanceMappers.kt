package com.matiasdev.elecapp.features.finance.data

import com.matiasdev.elecapp.features.finance.domain.MaterialSuppliedBy
import com.matiasdev.elecapp.features.finance.domain.Payment
import com.matiasdev.elecapp.features.finance.domain.PaymentMethod
import com.matiasdev.elecapp.features.finance.domain.PaymentStatus
import com.matiasdev.elecapp.features.finance.domain.ReceiptItemSourceType
import com.matiasdev.elecapp.features.finance.domain.ServiceReceipt
import com.matiasdev.elecapp.features.finance.domain.ServiceReceiptItem
import com.matiasdev.elecapp.features.finance.domain.ServiceReceiptItemType
import com.matiasdev.elecapp.features.finance.domain.ServiceReceiptStatus
import com.matiasdev.elecapp.features.finance.domain.VisitCompletion
import com.matiasdev.elecapp.features.finance.domain.VisitTechnicalResult
import com.matiasdev.elecapp.features.finance.domain.VisitWorkType
import java.time.Instant

fun VisitCompletionEntity.toDomain(): VisitCompletion = VisitCompletion(
    id = id,
    visitId = visitId,
    diagnosis = diagnosis,
    workType = workType?.let { runCatching { VisitWorkType.valueOf(it) }.getOrNull() },
    workPerformed = workPerformed,
    workSectors = workSectors,
    workItems = workItems,
    workTests = workTests,
    workObservations = workObservations,
    technicalResult = technicalResult?.let { runCatching { VisitTechnicalResult.valueOf(it) }.getOrNull() },
    pendingWork = pendingWork,
    requiresFollowUp = requiresFollowUp,
    followUpSuggestedAt = followUpSuggestedAt?.let(Instant::ofEpochMilli),
    internalNotes = internalNotes,
    customerNotes = customerNotes,
    completedAt = Instant.ofEpochMilli(completedAt),
    technicalReportSnapshot = technicalReportSnapshot,
    clientReport = clientReport,
    reportsGeneratedAt = reportsGeneratedAt?.let(Instant::ofEpochMilli),
    createdAt = Instant.ofEpochMilli(createdAt),
    updatedAt = Instant.ofEpochMilli(updatedAt),
    isDeleted = isDeleted,
)

fun VisitCompletion.toEntity(): VisitCompletionEntity = VisitCompletionEntity(
    id = id,
    visitId = visitId,
    diagnosis = diagnosis,
    workType = workType?.name,
    workPerformed = workPerformed,
    workSectors = workSectors,
    workItems = workItems,
    workTests = workTests,
    workObservations = workObservations,
    technicalResult = technicalResult?.name,
    pendingWork = pendingWork,
    requiresFollowUp = requiresFollowUp,
    followUpSuggestedAt = followUpSuggestedAt?.toEpochMilli(),
    internalNotes = internalNotes,
    customerNotes = customerNotes,
    completedAt = completedAt.toEpochMilli(),
    technicalReportSnapshot = technicalReportSnapshot,
    clientReport = clientReport,
    reportsGeneratedAt = reportsGeneratedAt?.toEpochMilli(),
    createdAt = createdAt.toEpochMilli(),
    updatedAt = updatedAt.toEpochMilli(),
    isDeleted = isDeleted,
)

fun ServiceReceiptEntity.toDomain(): ServiceReceipt = ServiceReceipt(
    id = id,
    receiptNumber = receiptNumber,
    clientId = clientId,
    visitId = visitId,
    quoteId = quoteId,
    issuedAt = issuedAt?.let(Instant::ofEpochMilli),
    title = title,
    description = description,
    status = ServiceReceiptStatus.valueOf(status),
    subtotalLaborCents = subtotalLaborCents,
    subtotalMaterialsCents = subtotalMaterialsCents,
    subtotalOtherCents = subtotalOtherCents,
    discountCents = discountCents,
    totalCents = totalCents,
    notes = notes,
    internalNotes = internalNotes,
    createdAt = Instant.ofEpochMilli(createdAt),
    updatedAt = Instant.ofEpochMilli(updatedAt),
    isDeleted = isDeleted,
)

fun ServiceReceipt.toEntity(): ServiceReceiptEntity = ServiceReceiptEntity(
    id = id,
    receiptNumber = receiptNumber,
    clientId = clientId,
    visitId = visitId,
    quoteId = quoteId,
    issuedAt = issuedAt?.toEpochMilli(),
    title = title,
    description = description,
    status = status.name,
    subtotalLaborCents = subtotalLaborCents,
    subtotalMaterialsCents = subtotalMaterialsCents,
    subtotalOtherCents = subtotalOtherCents,
    discountCents = discountCents,
    totalCents = totalCents,
    notes = notes,
    internalNotes = internalNotes,
    createdAt = createdAt.toEpochMilli(),
    updatedAt = updatedAt.toEpochMilli(),
    isDeleted = isDeleted,
)

fun ServiceReceiptItemEntity.toDomain(): ServiceReceiptItem = ServiceReceiptItem(
    id = id,
    receiptId = receiptId,
    type = ServiceReceiptItemType.valueOf(type),
    description = description,
    quantityMillis = quantityMillis,
    unitPriceCents = unitPriceCents,
    totalCents = totalCents,
    sourceType = ReceiptItemSourceType.valueOf(sourceType),
    sourceId = sourceId,
    suppliedBy = MaterialSuppliedBy.valueOf(suppliedBy),
    isChargeable = isChargeable,
    sortOrder = sortOrder,
    notes = notes,
    createdAt = Instant.ofEpochMilli(createdAt),
    updatedAt = Instant.ofEpochMilli(updatedAt),
    isDeleted = isDeleted,
)

fun ServiceReceiptItem.toEntity(): ServiceReceiptItemEntity = ServiceReceiptItemEntity(
    id = id,
    receiptId = receiptId,
    type = type.name,
    description = description,
    quantityMillis = quantityMillis,
    unitPriceCents = unitPriceCents,
    totalCents = totalCents,
    sourceType = sourceType.name,
    sourceId = sourceId,
    suppliedBy = suppliedBy.name,
    isChargeable = isChargeable,
    sortOrder = sortOrder,
    notes = notes,
    createdAt = createdAt.toEpochMilli(),
    updatedAt = updatedAt.toEpochMilli(),
    isDeleted = isDeleted,
)

fun PaymentEntity.toDomain(): Payment = Payment(
    id = id,
    clientId = clientId,
    visitId = visitId,
    serviceReceiptId = serviceReceiptId,
    amountCents = amountCents,
    method = PaymentMethod.valueOf(method),
    paidAt = Instant.ofEpochMilli(paidAt),
    reference = reference,
    notes = notes,
    status = PaymentStatus.valueOf(status),
    createdAt = Instant.ofEpochMilli(createdAt),
    updatedAt = Instant.ofEpochMilli(updatedAt),
    isDeleted = isDeleted,
)

fun Payment.toEntity(): PaymentEntity = PaymentEntity(
    id = id,
    clientId = clientId,
    visitId = visitId,
    serviceReceiptId = serviceReceiptId,
    amountCents = amountCents,
    method = method.name,
    paidAt = paidAt.toEpochMilli(),
    reference = reference,
    notes = notes,
    status = status.name,
    createdAt = createdAt.toEpochMilli(),
    updatedAt = updatedAt.toEpochMilli(),
    isDeleted = isDeleted,
)
