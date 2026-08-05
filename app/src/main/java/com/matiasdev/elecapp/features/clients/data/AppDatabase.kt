package com.matiasdev.elecapp.features.clients.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.matiasdev.elecapp.features.inspections.data.ElectricalInspectionEntity
import com.matiasdev.elecapp.features.inspections.data.InspectionDao
import com.matiasdev.elecapp.features.inspections.data.InspectionFindingEntity
import com.matiasdev.elecapp.features.inspections.data.InspectionUnverifiedItemEntity
import com.matiasdev.elecapp.features.inspections.data.MainPanelInspectionEntity
import com.matiasdev.elecapp.features.inspections.data.PillarInspectionEntity
import com.matiasdev.elecapp.features.materials.data.MaterialDao
import com.matiasdev.elecapp.features.materials.data.MaterialItemEntity
import com.matiasdev.elecapp.features.materials.data.MaterialListEntity
import com.matiasdev.elecapp.features.quotes.data.QuoteDao
import com.matiasdev.elecapp.features.quotes.data.QuoteEntity
import com.matiasdev.elecapp.features.quotes.data.QuoteItemEntity
import com.matiasdev.elecapp.features.reminders.data.VisitReminderDao
import com.matiasdev.elecapp.features.reminders.data.VisitReminderEntity
import com.matiasdev.elecapp.features.visits.data.VisitDao
import com.matiasdev.elecapp.features.visits.data.VisitEntity

@Database(
    entities = [
        ClientEntity::class,
        VisitEntity::class,
        VisitReminderEntity::class,
        ElectricalInspectionEntity::class,
        PillarInspectionEntity::class,
        MainPanelInspectionEntity::class,
        InspectionFindingEntity::class,
        InspectionUnverifiedItemEntity::class,
        QuoteEntity::class,
        QuoteItemEntity::class,
        MaterialListEntity::class,
        MaterialItemEntity::class,
    ],
    version = 6,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun clientDao(): ClientDao

    abstract fun visitDao(): VisitDao

    abstract fun visitReminderDao(): VisitReminderDao

    abstract fun inspectionDao(): InspectionDao

    abstract fun quoteDao(): QuoteDao

    abstract fun materialDao(): MaterialDao
}
