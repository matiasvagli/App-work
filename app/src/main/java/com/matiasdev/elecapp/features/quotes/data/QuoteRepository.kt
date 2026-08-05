package com.matiasdev.elecapp.features.quotes.data

import com.matiasdev.elecapp.features.quotes.domain.Quote
import com.matiasdev.elecapp.features.quotes.domain.QuoteItem
import com.matiasdev.elecapp.features.quotes.domain.QuoteListItem
import com.matiasdev.elecapp.features.quotes.domain.QuoteStatus
import kotlinx.coroutines.flow.Flow
import java.time.Instant

interface QuoteRepository {
    fun observeList(status: QuoteStatus?, query: String, orderByDue: Boolean): Flow<List<QuoteListItem>>

    fun observeById(id: String): Flow<Quote?>

    fun observeItems(quoteId: String): Flow<List<QuoteItem>>

    fun observeDraftCount(): Flow<Int>

    fun observeLatestForVisit(visitId: String): Flow<Quote?>

    suspend fun findById(id: String): Quote?

    suspend fun findItems(quoteId: String): List<QuoteItem>

    suspend fun nextQuoteNumber(now: Instant): String

    suspend fun findDrafts(clientId: String, visitId: String?, inspectionId: String?): List<Quote>

    suspend fun saveQuoteWithItems(quote: Quote, items: List<QuoteItem>)

    suspend fun updateStatus(id: String, status: QuoteStatus, now: Instant)
}
