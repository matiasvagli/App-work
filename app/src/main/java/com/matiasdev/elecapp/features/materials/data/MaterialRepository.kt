package com.matiasdev.elecapp.features.materials.data

import com.matiasdev.elecapp.features.materials.domain.MaterialItem
import com.matiasdev.elecapp.features.materials.domain.MaterialList
import com.matiasdev.elecapp.features.materials.domain.MaterialListItem
import com.matiasdev.elecapp.features.materials.domain.MaterialListStatus
import kotlinx.coroutines.flow.Flow
import java.time.Instant

interface MaterialRepository {
    fun observeList(status: MaterialListStatus?, query: String): Flow<List<MaterialListItem>>

    fun observeById(id: String): Flow<MaterialList?>

    fun observeItems(listId: String): Flow<List<MaterialItem>>

    fun observeDraftCount(): Flow<Int>

    fun observeLatestForVisit(visitId: String): Flow<MaterialList?>

    fun observeLatestForQuote(quoteId: String): Flow<MaterialList?>

    suspend fun findById(id: String): MaterialList?

    suspend fun findItems(listId: String): List<MaterialItem>

    suspend fun saveListWithItems(list: MaterialList, items: List<MaterialItem>)

    suspend fun updateStatus(id: String, status: MaterialListStatus, now: Instant)
}
