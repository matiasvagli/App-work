package com.matiasdev.elecapp.features.reminders.data

import com.matiasdev.elecapp.features.reminders.domain.VisitReminder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomVisitReminderRepository(
    private val dao: VisitReminderDao,
) : VisitReminderRepository {
    override fun observeForVisit(visitId: String): Flow<List<VisitReminder>> {
        return dao.observeForVisit(visitId).map { reminders -> reminders.map(VisitReminderEntity::toDomain) }
    }

    override suspend fun findEnabledForVisit(visitId: String): List<VisitReminder> {
        return dao.findEnabledForVisit(visitId).map(VisitReminderEntity::toDomain)
    }

    override suspend fun findAllEnabled(): List<VisitReminder> {
        return dao.findAllEnabled().map(VisitReminderEntity::toDomain)
    }

    override suspend fun replaceForVisit(visitId: String, reminders: List<VisitReminder>) {
        dao.deleteForVisit(visitId)
        dao.upsertAll(reminders.map(VisitReminder::toEntity))
    }

    override suspend fun deleteForVisit(visitId: String) {
        dao.deleteForVisit(visitId)
    }
}
