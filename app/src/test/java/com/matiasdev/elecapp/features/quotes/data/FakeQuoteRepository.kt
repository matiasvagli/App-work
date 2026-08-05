package com.matiasdev.elecapp.features.quotes.data

import com.matiasdev.elecapp.features.quotes.domain.Quote
import com.matiasdev.elecapp.features.quotes.domain.QuoteItem
import com.matiasdev.elecapp.features.quotes.domain.QuoteListItem
import com.matiasdev.elecapp.features.quotes.domain.QuoteNumberGenerator
import com.matiasdev.elecapp.features.quotes.domain.QuoteStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.time.Instant

class FakeQuoteRepository(initialQuotes: List<Quote> = emptyList()) : QuoteRepository {
    private val quotes = MutableStateFlow(initialQuotes)
    private val items = MutableStateFlow<List<QuoteItem>>(emptyList())

    override fun observeList(status: QuoteStatus?, query: String, orderByDue: Boolean): Flow<List<QuoteListItem>> {
        return quotes.map { values ->
            values.filter { !it.isDeleted && (status == null || it.status == status) }
                .filter { query.isBlank() || it.title.contains(query, ignoreCase = true) || it.quoteNumber.contains(query) }
                .sortedByDescending { it.updatedAt }
                .map { QuoteListItem(it, it.clientId, null, null, itemCount(it.id), false) }
        }
    }

    override fun observeById(id: String): Flow<Quote?> {
        return quotes.map { values -> values.firstOrNull { it.id == id && !it.isDeleted } }
    }

    override fun observeItems(quoteId: String): Flow<List<QuoteItem>> {
        return items.map { values -> values.filter { it.quoteId == quoteId && !it.isDeleted }.sortedBy { it.sortOrder } }
    }

    override fun observeDraftCount(): Flow<Int> {
        return quotes.map { values -> values.count { it.status == QuoteStatus.DRAFT && !it.isDeleted } }
    }

    override fun observeLatestForVisit(visitId: String): Flow<Quote?> {
        return quotes.map { values -> values.filter { it.visitId == visitId && !it.isDeleted }.maxByOrNull { it.updatedAt } }
    }

    override suspend fun findById(id: String): Quote? = quotes.value.firstOrNull { it.id == id && !it.isDeleted }

    override suspend fun findItems(quoteId: String): List<QuoteItem> {
        return items.value.filter { it.quoteId == quoteId && !it.isDeleted }.sortedBy { it.sortOrder }
    }

    override suspend fun nextQuoteNumber(now: Instant): String = QuoteNumberGenerator.next(now, quotes.value.size)

    override suspend fun findDrafts(clientId: String, visitId: String?, inspectionId: String?): List<Quote> {
        return quotes.value.filter {
            it.clientId == clientId && it.status == QuoteStatus.DRAFT &&
                (visitId == null || it.visitId == visitId) && (inspectionId == null || it.inspectionId == inspectionId)
        }
    }

    override suspend fun saveQuoteWithItems(quote: Quote, items: List<QuoteItem>) {
        quotes.value = quotes.value.filterNot { it.id == quote.id } + quote
        this.items.value = this.items.value.filterNot { it.quoteId == quote.id } + items
    }

    override suspend fun updateStatus(id: String, status: QuoteStatus, now: Instant) {
        quotes.value = quotes.value.map { if (it.id == id) it.copy(status = status, updatedAt = now) else it }
    }

    private fun itemCount(quoteId: String): Int = items.value.count { it.quoteId == quoteId && !it.isDeleted }
}
