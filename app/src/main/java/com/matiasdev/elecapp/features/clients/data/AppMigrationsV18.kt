package com.matiasdev.elecapp.features.clients.data

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_17_18 = object : Migration(17, 18) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE visit_completions ADD COLUMN work_type TEXT")
        db.execSQL("ALTER TABLE visit_completions ADD COLUMN work_sectors TEXT")
        db.execSQL("ALTER TABLE visit_completions ADD COLUMN work_items TEXT")
        db.execSQL("ALTER TABLE visit_completions ADD COLUMN work_tests TEXT")
        db.execSQL("ALTER TABLE visit_completions ADD COLUMN work_observations TEXT")
        db.execSQL("ALTER TABLE visit_completions ADD COLUMN technical_result TEXT")
    }
}
