package com.matiasdev.elecapp.app

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.matiasdev.elecapp.features.clients.data.AppDatabase
import com.matiasdev.elecapp.features.clients.data.ClientRepository
import com.matiasdev.elecapp.features.clients.data.MIGRATION_1_2
import com.matiasdev.elecapp.features.clients.data.MIGRATION_2_3
import com.matiasdev.elecapp.features.clients.data.MIGRATION_3_4
import com.matiasdev.elecapp.features.clients.data.MIGRATION_4_5
import com.matiasdev.elecapp.features.clients.data.MIGRATION_5_6
import com.matiasdev.elecapp.features.clients.data.MIGRATION_6_7
import com.matiasdev.elecapp.features.clients.data.MIGRATION_7_8
import com.matiasdev.elecapp.features.clients.data.MIGRATION_8_9
import com.matiasdev.elecapp.features.clients.data.MIGRATION_9_10
import com.matiasdev.elecapp.features.clients.data.MIGRATION_10_11
import com.matiasdev.elecapp.features.clients.data.MIGRATION_11_12
import com.matiasdev.elecapp.features.clients.data.MIGRATION_12_13
import com.matiasdev.elecapp.features.clients.data.MIGRATION_13_14
import com.matiasdev.elecapp.features.clients.data.MIGRATION_14_15
import com.matiasdev.elecapp.features.clients.data.MIGRATION_15_16
import com.matiasdev.elecapp.features.clients.data.MIGRATION_16_17
import com.matiasdev.elecapp.features.clients.data.MIGRATION_17_18
import com.matiasdev.elecapp.features.clients.data.MIGRATION_18_19
import com.matiasdev.elecapp.features.clients.data.RoomClientRepository
import com.matiasdev.elecapp.features.electricalrules.data.RoomElectricalRuleConfigRepository
import com.matiasdev.elecapp.features.electricalrules.data.insertDefaultElectricalRuleConfigsIgnoringExisting
import com.matiasdev.elecapp.features.electricalrules.domain.ElectricalRuleConfigRepository
import com.matiasdev.elecapp.features.electricaltools.data.RoomTechnicalCalculationRepository
import com.matiasdev.elecapp.features.electricaltools.data.TechnicalCalculationRepository
import com.matiasdev.elecapp.features.finance.data.FinanceRepository
import com.matiasdev.elecapp.features.finance.data.RoomFinanceRepository
import com.matiasdev.elecapp.features.inspections.data.InspectionRepository
import com.matiasdev.elecapp.features.inspections.data.RoomInspectionRepository
import com.matiasdev.elecapp.features.materials.data.MaterialRepository
import com.matiasdev.elecapp.features.materials.data.RoomMaterialRepository
import com.matiasdev.elecapp.features.quotes.data.QuoteRepository
import com.matiasdev.elecapp.features.quotes.data.RoomQuoteRepository
import com.matiasdev.elecapp.features.reminders.data.RoomVisitReminderRepository
import com.matiasdev.elecapp.features.reminders.data.VisitReminderRepository
import com.matiasdev.elecapp.features.reminders.scheduling.VisitReminderScheduler
import com.matiasdev.elecapp.features.reminders.scheduling.ReminderCoordinator
import com.matiasdev.elecapp.features.settings.data.DataStoreReminderSettingsRepository
import com.matiasdev.elecapp.features.settings.data.ReminderSettingsRepository
import com.matiasdev.elecapp.features.visits.data.RoomVisitRepository
import com.matiasdev.elecapp.features.visits.data.RoomVisitWorkSessionRepository
import com.matiasdev.elecapp.features.visits.data.VisitRepository
import com.matiasdev.elecapp.features.visits.data.VisitWorkSessionRepository

class AppContainer(context: Context) {
    private val database: AppDatabase = Room.databaseBuilder(
        context = context.applicationContext,
        klass = AppDatabase::class.java,
        name = "elec_app.db",
    ).addMigrations(
        MIGRATION_1_2,
        MIGRATION_2_3,
        MIGRATION_3_4,
        MIGRATION_4_5,
        MIGRATION_5_6,
        MIGRATION_6_7,
        MIGRATION_7_8,
        MIGRATION_8_9,
        MIGRATION_9_10,
        MIGRATION_10_11,
        MIGRATION_11_12,
        MIGRATION_12_13,
        MIGRATION_13_14,
        MIGRATION_14_15,
        MIGRATION_15_16,
        MIGRATION_16_17,
        MIGRATION_17_18,
        MIGRATION_18_19,
    ).addCallback(
        object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                db.insertDefaultElectricalRuleConfigsIgnoringExisting()
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                db.insertDefaultElectricalRuleConfigsIgnoringExisting()
            }
        },
    ).build()

    val clientRepository: ClientRepository = RoomClientRepository(
        clientDao = database.clientDao(),
    )

    val visitRepository: VisitRepository = RoomVisitRepository(
        visitDao = database.visitDao(),
    )

    val visitWorkSessionRepository: VisitWorkSessionRepository = RoomVisitWorkSessionRepository(
        database = database,
        visitDao = database.visitDao(),
        sessionDao = database.visitWorkSessionDao(),
    )

    val financeRepository: FinanceRepository = RoomFinanceRepository(
        database = database,
        clientDao = database.clientDao(),
        visitDao = database.visitDao(),
        sessionDao = database.visitWorkSessionDao(),
        completionDao = database.visitCompletionDao(),
        receiptDao = database.serviceReceiptDao(),
        itemDao = database.serviceReceiptItemDao(),
        paymentDao = database.paymentDao(),
        sequenceDao = database.receiptSequenceDao(),
    )

    val reminderRepository: VisitReminderRepository = RoomVisitReminderRepository(
        dao = database.visitReminderDao(),
    )

    val inspectionRepository: InspectionRepository = RoomInspectionRepository(
        dao = database.inspectionDao(),
    )

    val quoteRepository: QuoteRepository = RoomQuoteRepository(
        dao = database.quoteDao(),
    )

    val materialRepository: MaterialRepository = RoomMaterialRepository(
        dao = database.materialDao(),
    )

    val technicalCalculationRepository: TechnicalCalculationRepository = RoomTechnicalCalculationRepository(
        dao = database.technicalCalculationDao(),
    )

    val electricalRuleConfigRepository: ElectricalRuleConfigRepository = RoomElectricalRuleConfigRepository(
        dao = database.electricalRuleConfigDao(),
    )

    val reminderSettingsRepository: ReminderSettingsRepository = DataStoreReminderSettingsRepository(
        context = context.applicationContext,
    )

    val reminderScheduler = VisitReminderScheduler(
        context = context.applicationContext,
    )

    val reminderCoordinator = ReminderCoordinator(
        clientRepository = clientRepository,
        reminderRepository = reminderRepository,
        scheduler = reminderScheduler,
    )
}
