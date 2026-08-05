package com.matiasdev.elecapp.features.materials.data

import com.matiasdev.elecapp.features.materials.domain.MaterialItem
import com.matiasdev.elecapp.features.materials.domain.MaterialList
import com.matiasdev.elecapp.features.materials.domain.MaterialListItem
import com.matiasdev.elecapp.features.materials.domain.MaterialListStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.time.Instant

class FakeMaterialRepository(initialLists: List<MaterialList> = emptyList()) : MaterialRepository {
    private val lists = MutableStateFlow(initialLists)
    private val items = MutableStateFlow<List<MaterialItem>>(emptyList())

    override fun observeList(status: MaterialListStatus?, query: String): Flow<List<MaterialListItem>> {
        return lists.map { values ->
            values.filter { !it.isDeleted && (status == null || it.status == status) }
                .filter { query.isBlank() || it.title.contains(query, ignoreCase = true) }
                .sortedByDescending { it.updatedAt }
                .map { MaterialListItem(it, it.clientId, null, null, itemCount(it.id), null) }
        }
    }

    override fun observeById(id: String): Flow<MaterialList?> {
        return lists.map { values -> values.firstOrNull { it.id == id && !it.isDeleted } }
    }

    override fun observeItems(listId: String): Flow<List<MaterialItem>> {
        return items.map { values -> values.filter { it.materialListId == listId && !it.isDeleted }.sortedBy { it.sortOrder } }
    }

    override fun observeDraftCount(): Flow<Int> {
        return lists.map { values -> values.count { it.status == MaterialListStatus.DRAFT && !it.isDeleted } }
    }

    override fun observeLatestForVisit(visitId: String): Flow<MaterialList?> {
        return lists.map { values -> values.filter { it.visitId == visitId && !it.isDeleted }.maxByOrNull { it.updatedAt } }
    }

    override fun observeLatestForQuote(quoteId: String): Flow<MaterialList?> {
        return lists.map { values -> values.filter { it.quoteId == quoteId && !it.isDeleted }.maxByOrNull { it.updatedAt } }
    }

    override suspend fun findById(id: String): MaterialList? = lists.value.firstOrNull { it.id == id && !it.isDeleted }

    override suspend fun findItems(listId: String): List<MaterialItem> {
        return items.value.filter { it.materialListId == listId && !it.isDeleted }.sortedBy { it.sortOrder }
    }

    override suspend fun saveListWithItems(list: MaterialList, items: List<MaterialItem>) {
        lists.value = lists.value.filterNot { it.id == list.id } + list
        this.items.value = this.items.value.filterNot { it.materialListId == list.id } + items
    }

    override suspend fun updateStatus(id: String, status: MaterialListStatus, now: Instant) {
        lists.value = lists.value.map { if (it.id == id) it.copy(status = status, updatedAt = now) else it }
    }

    private fun itemCount(listId: String): Int = items.value.count { it.materialListId == listId && !it.isDeleted }
}
