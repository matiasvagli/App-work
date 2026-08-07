package com.matiasdev.elecapp.features.clients.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.matiasdev.elecapp.features.electricalrules.data.ElectricalRuleConfigDao
import com.matiasdev.elecapp.features.electricalrules.data.ElectricalRuleConfigEntity
import com.matiasdev.elecapp.features.electricaltools.data.TechnicalCalculationDao
import com.matiasdev.elecapp.features.electricaltools.data.TechnicalCalculationEntity
import com.matiasdev.elecapp.features.finance.data.PaymentDao
import com.matiasdev.elecapp.features.finance.data.PaymentEntity
import com.matiasdev.elecapp.features.finance.data.ReceiptSequenceDao
import com.matiasdev.elecapp.features.finance.data.ReceiptSequenceEntity
import com.matiasdev.elecapp.features.finance.data.ServiceReceiptDao
import com.matiasdev.elecapp.features.finance.data.ServiceReceiptEntity
import com.matiasdev.elecapp.features.finance.data.ServiceReceiptItemDao
import com.matiasdev.elecapp.features.finance.data.ServiceReceiptItemEntity
import com.matiasdev.elecapp.features.finance.data.VisitCompletionDao
import com.matiasdev.elecapp.features.finance.data.VisitCompletionEntity
import com.matiasdev.elecapp.features.inspections.data.ElectricalInspectionEntity
import com.matiasdev.elecapp.features.inspections.data.InspectionDao
import com.matiasdev.elecapp.features.inspections.data.InspectionFindingEntity
import com.matiasdev.elecapp.features.inspections.data.InspectionUnverifiedItemEntity
import com.matiasdev.elecapp.features.inspections.data.MainPanelCircuitEntity
import com.matiasdev.elecapp.features.inspections.data.MainPanelInspectionEntity
import com.matiasdev.elecapp.features.inspections.data.MainPanelMeasurementEntity
import com.matiasdev.elecapp.features.inspections.data.PillarMeasurementEntity
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
import com.matiasdev.elecapp.features.visits.data.VisitWorkSessionDao
import com.matiasdev.elecapp.features.visits.data.VisitWorkSessionEntity

@Database(
    entities = [
        ClientEntity::class,
        VisitEntity::class,
        VisitReminderEntity::class,
        ElectricalInspectionEntity::class,
        PillarInspectionEntity::class,
        PillarMeasurementEntity::class,
        MainPanelInspectionEntity::class,
        MainPanelMeasurementEntity::class,
        MainPanelCircuitEntity::class,
        InspectionFindingEntity::class,
        InspectionUnverifiedItemEntity::class,
        QuoteEntity::class,
        QuoteItemEntity::class,
        MaterialListEntity::class,
        MaterialItemEntity::class,
        TechnicalCalculationEntity::class,
        VisitWorkSessionEntity::class,
        VisitCompletionEntity::class,
        ServiceReceiptEntity::class,
        ServiceReceiptItemEntity::class,
        PaymentEntity::class,
        ReceiptSequenceEntity::class,
        ElectricalRuleConfigEntity::class,
    ],
    version = 14,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun clientDao(): ClientDao

    abstract fun visitDao(): VisitDao

    abstract fun visitReminderDao(): VisitReminderDao

    abstract fun inspectionDao(): InspectionDao

    abstract fun quoteDao(): QuoteDao

    abstract fun materialDao(): MaterialDao

    abstract fun technicalCalculationDao(): TechnicalCalculationDao

    abstract fun visitWorkSessionDao(): VisitWorkSessionDao

    abstract fun visitCompletionDao(): VisitCompletionDao

    abstract fun serviceReceiptDao(): ServiceReceiptDao

    abstract fun serviceReceiptItemDao(): ServiceReceiptItemDao

    abstract fun paymentDao(): PaymentDao

    abstract fun receiptSequenceDao(): ReceiptSequenceDao

    abstract fun electricalRuleConfigDao(): ElectricalRuleConfigDao
}
