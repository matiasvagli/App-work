package com.matiasdev.elecapp.features.clients.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ClientDao {
    @Query(
        """
        SELECT * FROM clients
        WHERE is_deleted = 0
        ORDER BY full_name COLLATE NOCASE ASC
        """,
    )
    fun observeActiveClients(): Flow<List<ClientEntity>>

    @Query(
        """
        SELECT * FROM clients
        WHERE is_deleted = 0
            AND (:query = ''
                OR full_name LIKE '%' || :query || '%' COLLATE NOCASE
                OR phone LIKE '%' || :query || '%')
        ORDER BY full_name COLLATE NOCASE ASC
        LIMIT :limit
        """,
    )
    fun observeActiveClientsMatching(query: String, limit: Int = 20): Flow<List<ClientEntity>>

    @Query("SELECT * FROM clients WHERE id = :id AND is_deleted = 0 LIMIT 1")
    fun observeActiveClientById(id: String): Flow<ClientEntity?>

    @Query("SELECT * FROM clients WHERE id = :id LIMIT 1")
    suspend fun findById(id: String): ClientEntity?

    @Upsert
    suspend fun upsert(client: ClientEntity)

    @Query("UPDATE clients SET is_deleted = 1, updated_at = :updatedAt WHERE id = :id")
    suspend fun softDelete(id: String, updatedAt: Long)
}
