package com.matiasdev.elecapp.features.referencedocs.data

import com.matiasdev.elecapp.features.referencedocs.domain.ReferenceDocument
import java.time.Instant

fun ReferenceDocumentEntity.toDomain(): ReferenceDocument = ReferenceDocument(
    id = id,
    title = title,
    fileName = fileName,
    sourceUrl = sourceUrl,
    sizeBytes = sizeBytes,
    importedAt = Instant.ofEpochMilli(importedAt),
    isDeleted = isDeleted,
)

fun ReferenceDocument.toEntity(): ReferenceDocumentEntity = ReferenceDocumentEntity(
    id = id,
    title = title,
    fileName = fileName,
    sourceUrl = sourceUrl,
    sizeBytes = sizeBytes,
    importedAt = importedAt.toEpochMilli(),
    isDeleted = isDeleted,
)
