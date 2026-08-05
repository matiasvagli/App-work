package com.matiasdev.elecapp.features.quotes.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface QuoteDao {
    @Query(
        """
        SELECT q.*, c.full_name AS client_name, c.address, c.locality,
            (SELECT COUNT(*) FROM quote_items qi WHERE qi.quote_id = q.id AND qi.is_deleted = 0) AS item_count,
            EXISTS(SELECT 1 FROM material_lists ml WHERE ml.quote_id = q.id AND ml.is_deleted = 0) AS has_material_list
        FROM quotes q
        INNER JOIN clients c ON c.id = q.client_id
        LEFT JOIN visits v ON v.id = q.visit_id
        WHERE q.is_deleted = 0
            AND (:status IS NULL OR q.status = :status)
            AND (
                :query IS NULL OR :query = '' OR
                c.full_name LIKE '%' || :query || '%' OR
                q.quote_number LIKE '%' || :query || '%' OR
                q.title LIKE '%' || :query || '%' OR
                c.address LIKE '%' || :query || '%' OR
                c.locality LIKE '%' || :query || '%' OR
                v.reason LIKE '%' || :query || '%'
            )
        ORDER BY
            CASE WHEN :orderByDue = 1 AND q.valid_until IS NULL THEN 1 ELSE 0 END,
            CASE WHEN :orderByDue = 1 THEN q.valid_until END ASC,
            q.updated_at DESC
        """,
    )
    fun observeList(status: String?, query: String?, orderByDue: Boolean): Flow<List<QuoteListItemEntity>>

    @Query("SELECT * FROM quotes WHERE id = :id AND is_deleted = 0")
    fun observeById(id: String): Flow<QuoteEntity?>

    @Query("SELECT * FROM quotes WHERE id = :id AND is_deleted = 0")
    suspend fun findById(id: String): QuoteEntity?

    @Query("SELECT * FROM quote_items WHERE quote_id = :quoteId AND is_deleted = 0 ORDER BY sort_order")
    fun observeItems(quoteId: String): Flow<List<QuoteItemEntity>>

    @Query("SELECT COUNT(*) FROM quotes WHERE status = 'DRAFT' AND is_deleted = 0")
    fun observeDraftCount(): Flow<Int>

    @Query("SELECT * FROM quote_items WHERE quote_id = :quoteId AND is_deleted = 0 ORDER BY sort_order")
    suspend fun findItems(quoteId: String): List<QuoteItemEntity>

    @Query("SELECT COUNT(*) FROM quotes WHERE quote_number LIKE :yearPrefix || '%'")
    suspend fun countQuoteNumbersForYear(yearPrefix: String): Int

    @Query(
        """
        SELECT * FROM quotes
        WHERE is_deleted = 0 AND status = 'DRAFT' AND client_id = :clientId
            AND (:visitId IS NULL OR visit_id = :visitId)
            AND (:inspectionId IS NULL OR inspection_id = :inspectionId)
        ORDER BY updated_at DESC
        """,
    )
    suspend fun findDrafts(clientId: String, visitId: String?, inspectionId: String?): List<QuoteEntity>

    @Query(
        """
        SELECT * FROM quotes
        WHERE is_deleted = 0 AND visit_id = :visitId
        ORDER BY updated_at DESC
        LIMIT 1
        """,
    )
    fun observeLatestForVisit(visitId: String): Flow<QuoteEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertQuote(entity: QuoteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertItems(items: List<QuoteItemEntity>)

    @Transaction
    suspend fun saveQuoteWithItems(quote: QuoteEntity, items: List<QuoteItemEntity>) {
        upsertQuote(quote)
        upsertItems(items)
    }

    @Query("UPDATE quotes SET status = :status, sent_at = :sentAt, approved_at = :approvedAt, rejected_at = :rejectedAt, updated_at = :updatedAt WHERE id = :id")
    suspend fun updateStatus(id: String, status: String, sentAt: Long?, approvedAt: Long?, rejectedAt: Long?, updatedAt: Long)

    @Query("UPDATE quote_items SET is_deleted = 1, updated_at = :updatedAt WHERE id = :id")
    suspend fun softDeleteItem(id: String, updatedAt: Long)
}
