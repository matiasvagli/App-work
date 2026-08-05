package com.matiasdev.elecapp.features.materials.data

import com.matiasdev.elecapp.features.materials.domain.MaterialItem
import com.matiasdev.elecapp.features.materials.domain.MaterialList
import com.matiasdev.elecapp.features.materials.domain.MaterialListItem
import com.matiasdev.elecapp.features.materials.domain.MaterialListStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant

class RoomMaterialRepository(
    private val dao: MaterialDao,
) : MaterialRepository {
    override fun observeList(status: MaterialListStatus?, query: String): Flow<List<MaterialListItem>> {
        return dao.observeList(status?.name, query.trim()).map { rows -> rows.map { it.toDomain() } }
    }

    override fun observeById(id: String): Flow<MaterialList?> = dao.observeById(id).map { it?.toDomain() }

    override fun observeItems(listId: String): Flow<List<MaterialItem>> {
        return dao.observeItems(listId).map { rows -> rows.map { it.toDomain() } }
    }

    override fun observeDraftCount(): Flow<Int> = dao.observeDraftCount()

    override fun observeLatestForVisit(visitId: String): Flow<MaterialList?> {
        return dao.observeLatestForVisit(visitId).map { it?.toDomain() }
    }

    override fun observeLatestForQuote(quoteId: String): Flow<MaterialList?> {
        return dao.observeLatestForQuote(quoteId).map { it?.toDomain() }
    }

    override suspend fun findById(id: String): MaterialList? = dao.findById(id)?.toDomain()

    override suspend fun findItems(listId: String): List<MaterialItem> {
        return dao.findItems(listId).map { it.toDomain() }
    }

    override suspend fun saveListWithItems(list: MaterialList, items: List<MaterialItem>) {
        require(items.filterNot { it.isDeleted }.all { it.quantity > 0.0 }) {
            "Material quantity must be greater than zero"
        }
        dao.saveListWithItems(list.toEntity(), items.map { it.toEntity() })
    }

    override suspend fun updateStatus(id: String, status: MaterialListStatus, now: Instant) {
        dao.updateStatus(
            id = id,
            status = status.name,
            deliveredAt = if (status == MaterialListStatus.DELIVERED) now.toEpochMilli() else null,
            updatedAt = now.toEpochMilli(),
        )
    }
}
