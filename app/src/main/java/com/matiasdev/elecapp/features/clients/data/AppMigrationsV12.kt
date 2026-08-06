package com.matiasdev.elecapp.features.clients.data

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_11_12 = object : Migration(11, 12) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE electrical_inspections ADD COLUMN review_reason TEXT")
        db.execSQL("ALTER TABLE electrical_inspections ADD COLUMN reviewed_element TEXT")
        db.execSQL("ALTER TABLE electrical_inspections ADD COLUMN task_description TEXT")
        db.execSQL("ALTER TABLE pillar_inspections ADD COLUMN review_status TEXT NOT NULL DEFAULT 'REVIEWED'")
        db.execSQL("ALTER TABLE main_panel_inspections ADD COLUMN review_status TEXT NOT NULL DEFAULT 'REVIEWED'")
    }
}
