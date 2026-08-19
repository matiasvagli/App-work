package com.matiasdev.elecapp.features.referencedocs.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ReferenceDocumentDao {
    @Query("SELECT * FROM reference_documents WHERE is_deleted = 0 ORDER BY imported_at DESC")
    fun observeAll(): Flow<List<ReferenceDocumentEntity>>

    @Query("SELECT * FROM reference_documents WHERE id = :id AND is_deleted = 0 LIMIT 1")
    suspend fun findById(id: String): ReferenceDocumentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ReferenceDocumentEntity)

    @Query("UPDATE reference_documents SET is_deleted = 1 WHERE id = :id")
    suspend fun softDelete(id: String)
}
