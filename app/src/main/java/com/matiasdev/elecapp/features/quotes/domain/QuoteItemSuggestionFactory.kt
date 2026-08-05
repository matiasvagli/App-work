package com.matiasdev.elecapp.features.quotes.domain

import com.matiasdev.elecapp.features.inspections.domain.InspectionFinding
import java.time.Instant
import java.util.UUID

object QuoteItemSuggestionFactory {
    fun fromFinding(finding: InspectionFinding, quoteId: String, now: Instant): QuoteItem {
        val description = finding.recommendation
            ?.takeIf { it.isNotBlank() }
            ?: "${finding.title}: ${finding.description}"
        return QuoteItem(
            id = UUID.randomUUID().toString(),
            quoteId = quoteId,
            type = QuoteItemType.SERVICE,
            description = description,
            quantity = 1.0,
            unit = QuoteUnit.FIXED,
            customUnitLabel = null,
            unitPriceAmount = 0L,
            lineTotalAmount = 0L,
            sortOrder = 0,
            notes = null,
            createdAt = now,
            updatedAt = now,
            isDeleted = false,
        )
    }
}
