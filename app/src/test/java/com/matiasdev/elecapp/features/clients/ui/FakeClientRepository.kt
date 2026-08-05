package com.matiasdev.elecapp.features.clients.ui

import com.matiasdev.elecapp.features.clients.data.ClientRepository
import com.matiasdev.elecapp.features.clients.domain.Client
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeClientRepository(
    initialClients: List<Client> = emptyList(),
) : ClientRepository {
    private val clients = MutableStateFlow(initialClients)

    override fun observeActiveClients(): Flow<List<Client>> {
        return clients.map { values ->
            values
                .filterNot(Client::isDeleted)
                .sortedBy { it.fullName.lowercase() }
        }
    }

    override fun observeActiveClientsMatching(query: String, limit: Int): Flow<List<Client>> {
        val normalizedQuery = query.trim().lowercase()
        return observeActiveClients().map { values ->
            values
                .filter { client ->
                    normalizedQuery.isBlank() ||
                        client.fullName.lowercase().contains(normalizedQuery) ||
                        client.phone.contains(normalizedQuery)
                }
                .take(limit)
        }
    }

    override fun observeActiveClientById(id: String): Flow<Client?> {
        return clients.map { values ->
            values.firstOrNull { it.id == id && !it.isDeleted }
        }
    }

    override suspend fun findById(id: String): Client? {
        return clients.value.firstOrNull { it.id == id }
    }

    override suspend fun save(client: Client) {
        clients.value = clients.value
            .filterNot { it.id == client.id }
            .plus(client)
    }

    fun currentClients(): List<Client> = clients.value

    override suspend fun softDelete(id: String) {
        clients.value = clients.value.map { client ->
            if (client.id == id) {
                client.copy(isDeleted = true, updatedAt = Instant.now())
            } else {
                client
            }
        }
    }
}
