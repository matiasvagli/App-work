package com.matiasdev.elecapp.features.clients.data

import com.matiasdev.elecapp.features.clients.domain.Client
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomClientRepository(
    private val clientDao: ClientDao,
) : ClientRepository {
    override fun observeActiveClients(): Flow<List<Client>> {
        return clientDao.observeActiveClients()
            .map { clients -> clients.map(ClientEntity::toDomain) }
    }

    override fun observeActiveClientsMatching(query: String, limit: Int): Flow<List<Client>> {
        return clientDao.observeActiveClientsMatching(query.trim(), limit)
            .map { clients -> clients.map(ClientEntity::toDomain) }
    }

    override fun observeActiveClientById(id: String): Flow<Client?> {
        return clientDao.observeActiveClientById(id)
            .map { client -> client?.toDomain() }
    }

    override suspend fun findById(id: String): Client? {
        return clientDao.findById(id)?.toDomain()
    }

    override suspend fun save(client: Client) {
        clientDao.upsert(client.toEntity())
    }

    override suspend fun softDelete(id: String) {
        clientDao.softDelete(id = id, updatedAt = Instant.now().toEpochMilli())
    }
}
