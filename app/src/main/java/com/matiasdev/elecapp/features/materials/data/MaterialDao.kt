package com.matiasdev.elecapp.features.materials.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface MaterialDao {
    @Query(
        """
        SELECT ml.*, c.full_name AS client_name, c.address, c.locality,
            (SELECT COUNT(*) FROM material_items mi WHERE mi.material_list_id = ml.id AND mi.is_deleted = 0) AS item_count,
            q.quote_number AS quote_number
        FROM material_lists ml
        INNER JOIN clients c ON c.id = ml.client_id
        LEFT JOIN quotes q ON q.id = ml.quote_id
        LEFT JOIN visits v ON v.id = ml.visit_id
        WHERE ml.is_deleted = 0
            AND (:status IS NULL OR ml.status = :status)
            AND (
                :query IS NULL OR :query = '' OR
                c.full_name LIKE '%' || :query || '%' OR
                ml.title LIKE '%' || :query || '%' OR
                c.address LIKE '%' || :query || '%' OR
                c.locality LIKE '%' || :query || '%' OR
                v.reason LIKE '%' || :query || '%' OR
                EXISTS(
                    SELECT 1 FROM material_items mi
                    WHERE mi.material_list_id = ml.id
                        AND mi.is_deleted = 0
                        AND mi.description LIKE '%' || :query || '%'
                )
            )
        ORDER BY ml.updated_at DESC
        """,
    )
    fun observeList(status: String?, query: String?): Flow<List<MaterialListItemEntity>>

    @Query("SELECT * FROM material_lists WHERE id = :id AND is_deleted = 0")
    fun observeById(id: String): Flow<MaterialListEntity?>

    @Query("SELECT * FROM material_lists WHERE id = :id AND is_deleted = 0")
    suspend fun findById(id: String): MaterialListEntity?

    @Query("SELECT * FROM material_items WHERE material_list_id = :listId AND is_deleted = 0 ORDER BY sort_order")
    fun observeItems(listId: String): Flow<List<MaterialItemEntity>>

    @Query("SELECT COUNT(*) FROM material_lists WHERE status = 'DRAFT' AND is_deleted = 0")
    fun observeDraftCount(): Flow<Int>

    @Query("SELECT * FROM material_items WHERE material_list_id = :listId AND is_deleted = 0 ORDER BY sort_order")
    suspend fun findItems(listId: String): List<MaterialItemEntity>

    @Query(
        """
        SELECT * FROM material_lists
        WHERE is_deleted = 0 AND visit_id = :visitId
        ORDER BY updated_at DESC
        LIMIT 1
        """,
    )
    fun observeLatestForVisit(visitId: String): Flow<MaterialListEntity?>

    @Query(
        """
        SELECT * FROM material_lists
        WHERE is_deleted = 0 AND quote_id = :quoteId
        ORDER BY updated_at DESC
        LIMIT 1
        """,
    )
    fun observeLatestForQuote(quoteId: String): Flow<MaterialListEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertList(entity: MaterialListEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertItems(items: List<MaterialItemEntity>)

    @Transaction
    suspend fun saveListWithItems(list: MaterialListEntity, items: List<MaterialItemEntity>) {
        upsertList(list)
        upsertItems(items)
    }

    @Query("UPDATE material_lists SET status = :status, delivered_at = :deliveredAt, updated_at = :updatedAt WHERE id = :id")
    suspend fun updateStatus(id: String, status: String, deliveredAt: Long?, updatedAt: Long)

    @Query("UPDATE material_items SET is_deleted = 1, updated_at = :updatedAt WHERE id = :id")
    suspend fun softDeleteItem(id: String, updatedAt: Long)
}
