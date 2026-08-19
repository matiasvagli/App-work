package com.matiasdev.elecapp.features.referencedocs.data

import com.matiasdev.elecapp.features.referencedocs.domain.ReferenceDocument
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface ReferenceDocumentRepository {
    fun observeAll(): Flow<List<ReferenceDocument>>

    suspend fun findById(id: String): ReferenceDocument?

    suspend fun save(document: ReferenceDocument)

    suspend fun softDelete(id: String)
}

class RoomReferenceDocumentRepository(
    private val dao: ReferenceDocumentDao,
) : ReferenceDocumentRepository {
    override fun observeAll(): Flow<List<ReferenceDocument>> = dao.observeAll().map { rows ->
        rows.map(ReferenceDocumentEntity::toDomain)
    }

    override suspend fun findById(id: String): ReferenceDocument? = dao.findById(id)?.toDomain()

    override suspend fun save(document: ReferenceDocument) = dao.upsert(document.toEntity())

    override suspend fun softDelete(id: String) = dao.softDelete(id)
}
