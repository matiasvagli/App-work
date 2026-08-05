package com.matiasdev.elecapp.features.clients.data

import com.matiasdev.elecapp.features.clients.domain.Client
import kotlinx.coroutines.flow.Flow

interface ClientRepository {
    fun observeActiveClients(): Flow<List<Client>>

    fun observeActiveClientsMatching(query: String, limit: Int = 20): Flow<List<Client>>

    fun observeActiveClientById(id: String): Flow<Client?>

    suspend fun findById(id: String): Client?

    suspend fun save(client: Client)

    suspend fun softDelete(id: String)
}
