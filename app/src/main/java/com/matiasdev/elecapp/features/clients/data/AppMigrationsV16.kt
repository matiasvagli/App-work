package com.matiasdev.elecapp.features.clients.data

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_15_16 = object : Migration(15, 16) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE main_panel_inspections ADD COLUMN feeder_distance_meters REAL")
        db.execSQL("ALTER TABLE main_panel_inspections ADD COLUMN feeder_conductor_section_mm2 REAL")
        db.execSQL("ALTER TABLE main_panel_inspections ADD COLUMN feeder_conductor_material TEXT")
        db.execSQL("ALTER TABLE main_panel_inspections ADD COLUMN feeder_data_origin TEXT")
    }
}
