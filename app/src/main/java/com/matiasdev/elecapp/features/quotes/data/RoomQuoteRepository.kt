package com.matiasdev.elecapp.features.quotes.data

import com.matiasdev.elecapp.features.quotes.domain.Quote
import com.matiasdev.elecapp.features.quotes.domain.QuoteCalculator
import com.matiasdev.elecapp.features.quotes.domain.QuoteItem
import com.matiasdev.elecapp.features.quotes.domain.QuoteListItem
import com.matiasdev.elecapp.features.quotes.domain.QuoteNumberGenerator
import com.matiasdev.elecapp.features.quotes.domain.QuoteStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.ZoneId

class RoomQuoteRepository(
    private val dao: QuoteDao,
) : QuoteRepository {
    override fun observeList(
        status: QuoteStatus?,
        query: String,
        orderByDue: Boolean,
    ): Flow<List<QuoteListItem>> {
        return dao.observeList(status?.name, query.trim(), orderByDue).map { rows ->
            rows.map { it.toDomain() }
        }
    }

    override fun observeById(id: String): Flow<Quote?> = dao.observeById(id).map { it?.toDomain() }

    override fun observeItems(quoteId: String): Flow<List<QuoteItem>> {
        return dao.observeItems(quoteId).map { rows -> rows.map { it.toDomain() } }
    }

    override fun observeDraftCount(): Flow<Int> = dao.observeDraftCount()

    override fun observeLatestForVisit(visitId: String): Flow<Quote?> {
        return dao.observeLatestForVisit(visitId).map { it?.toDomain() }
    }

    override suspend fun findById(id: String): Quote? = dao.findById(id)?.toDomain()

    override suspend fun findItems(quoteId: String): List<QuoteItem> {
        return dao.findItems(quoteId).map { it.toDomain() }
    }

    override suspend fun nextQuoteNumber(now: Instant): String {
        val year = now.atZone(ZoneId.systemDefault()).year
        val count = dao.countQuoteNumbersForYear("PRES-$year-")
        return QuoteNumberGenerator.next(now, count)
    }

    override suspend fun findDrafts(clientId: String, visitId: String?, inspectionId: String?): List<Quote> {
        return dao.findDrafts(clientId, visitId, inspectionId).map { it.toDomain() }
    }

    override suspend fun saveQuoteWithItems(quote: Quote, items: List<QuoteItem>) {
        val totals = QuoteCalculator.totals(items, quote.discountType, quote.discountValue)
        dao.saveQuoteWithItems(
            quote.copy(
                subtotalAmount = totals.subtotalAmount,
                totalAmount = totals.totalAmount,
            ).toEntity(),
            items.map { item ->
                item.copy(lineTotalAmount = QuoteCalculator.lineTotal(item.quantity, item.unitPriceAmount)).toEntity()
            },
        )
    }

    override suspend fun updateStatus(id: String, status: QuoteStatus, now: Instant) {
        dao.updateStatus(
            id = id,
            status = status.name,
            sentAt = if (status == QuoteStatus.SENT) now.toEpochMilli() else null,
            approvedAt = if (status == QuoteStatus.APPROVED) now.toEpochMilli() else null,
            rejectedAt = if (status == QuoteStatus.REJECTED) now.toEpochMilli() else null,
            updatedAt = now.toEpochMilli(),
        )
    }
}
