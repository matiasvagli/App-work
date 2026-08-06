package com.matiasdev.elecapp.features.clients.data

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_10_11 = object : Migration(10, 11) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Legacy inspections followed the previous general flow: general data, pillar, main panel, findings and report.
        db.execSQL("ALTER TABLE electrical_inspections ADD COLUMN scope TEXT NOT NULL DEFAULT 'GENERAL_ASSESSMENT'")
    }
}
