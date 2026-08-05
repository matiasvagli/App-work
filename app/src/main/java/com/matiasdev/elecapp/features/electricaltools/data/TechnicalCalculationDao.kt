package com.matiasdev.elecapp.features.electricaltools.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TechnicalCalculationDao {
    @Query("SELECT * FROM technical_calculations WHERE is_deleted = 0 ORDER BY created_at DESC")
    fun observeAll(): Flow<List<TechnicalCalculationEntity>>

    @Query("SELECT * FROM technical_calculations WHERE id = :id AND is_deleted = 0 LIMIT 1")
    fun observeById(id: String): Flow<TechnicalCalculationEntity?>

    @Query("SELECT * FROM technical_calculations WHERE id = :id AND is_deleted = 0 LIMIT 1")
    suspend fun findById(id: String): TechnicalCalculationEntity?

    @Query("SELECT * FROM technical_calculations WHERE client_id = :clientId AND is_deleted = 0 ORDER BY created_at DESC")
    fun observeByClient(clientId: String): Flow<List<TechnicalCalculationEntity>>

    @Query("SELECT * FROM technical_calculations WHERE visit_id = :visitId AND is_deleted = 0 ORDER BY created_at DESC")
    fun observeByVisit(visitId: String): Flow<List<TechnicalCalculationEntity>>

    @Query("SELECT * FROM technical_calculations WHERE inspection_id = :inspectionId AND is_deleted = 0 ORDER BY created_at DESC")
    fun observeByInspection(inspectionId: String): Flow<List<TechnicalCalculationEntity>>

    @Query("SELECT * FROM technical_calculations WHERE type = :type AND is_deleted = 0 ORDER BY created_at DESC")
    fun observeByType(type: String): Flow<List<TechnicalCalculationEntity>>

    @Query("SELECT * FROM technical_calculations WHERE source = :source AND is_deleted = 0 ORDER BY created_at DESC")
    fun observeBySource(source: String): Flow<List<TechnicalCalculationEntity>>

    @Query("SELECT * FROM technical_calculations WHERE classification = :classification AND is_deleted = 0 ORDER BY created_at DESC")
    fun observeByClassification(classification: String): Flow<List<TechnicalCalculationEntity>>

    @Query(
        """
        SELECT tc.* FROM technical_calculations tc
        LEFT JOIN clients c ON c.id = tc.client_id
        LEFT JOIN visits v ON v.id = tc.visit_id
        WHERE tc.is_deleted = 0
            AND (
                :query = ''
                OR tc.title LIKE '%' || :query || '%' COLLATE NOCASE
                OR tc.description LIKE '%' || :query || '%' COLLATE NOCASE
                OR c.full_name LIKE '%' || :query || '%' COLLATE NOCASE
                OR v.reason LIKE '%' || :query || '%' COLLATE NOCASE
                OR CAST(tc.created_at AS TEXT) LIKE '%' || :query || '%'
            )
            AND (:type IS NULL OR tc.type = :type)
            AND (:source IS NULL OR tc.source = :source)
            AND (:classification IS NULL OR tc.classification = :classification)
            AND (:associatedToInspection IS NULL OR (:associatedToInspection = 1 AND tc.inspection_id IS NOT NULL) OR (:associatedToInspection = 0 AND tc.inspection_id IS NULL))
            AND (:unassociated IS NULL OR (:unassociated = 1 AND tc.client_id IS NULL AND tc.visit_id IS NULL AND tc.inspection_id IS NULL))
        ORDER BY tc.created_at DESC
        """,
    )
    fun search(
        query: String,
        type: String?,
        source: String?,
        classification: String?,
        associatedToInspection: Boolean?,
        unassociated: Boolean?,
    ): Flow<List<TechnicalCalculationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: TechnicalCalculationEntity)

    @Query(
        """
        UPDATE technical_calculations
        SET title = :title,
            description = :description,
            technician_conclusion = :technicianConclusion,
            technician_notes = :technicianNotes,
            updated_at = :updatedAt
        WHERE id = :id AND is_deleted = 0
        """,
    )
    suspend fun updateEditableFields(
        id: String,
        title: String,
        description: String?,
        technicianConclusion: String,
        technicianNotes: String?,
        updatedAt: Long,
    )

    @Query(
        """
        UPDATE technical_calculations
        SET client_id = :clientId,
            visit_id = :visitId,
            inspection_id = :inspectionId,
            updated_at = :updatedAt
        WHERE id = :id AND is_deleted = 0
        """,
    )
    suspend fun associate(id: String, clientId: String?, visitId: String?, inspectionId: String?, updatedAt: Long)

    @Query("UPDATE technical_calculations SET inspection_id = NULL, updated_at = :updatedAt WHERE id = :id AND is_deleted = 0")
    suspend fun unlinkInspection(id: String, updatedAt: Long)

    @Query("UPDATE technical_calculations SET is_deleted = 1, updated_at = :updatedAt WHERE id = :id")
    suspend fun softDelete(id: String, updatedAt: Long)
}
