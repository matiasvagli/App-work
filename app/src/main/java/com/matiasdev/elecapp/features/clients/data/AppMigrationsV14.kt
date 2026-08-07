package com.matiasdev.elecapp.features.clients.data

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_13_14 = object : Migration(13, 14) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE main_panel_inspections ADD COLUMN differential_other_rated_amps INTEGER")
        db.execSQL("ALTER TABLE main_panel_inspections ADD COLUMN differential_other_sensitivity_ma INTEGER")
        db.execSQL("ALTER TABLE main_panel_inspections ADD COLUMN protection_conductors_present TEXT NOT NULL DEFAULT 'UNKNOWN'")
        db.execSQL("ALTER TABLE main_panel_inspections ADD COLUMN conductor_color_status TEXT NOT NULL DEFAULT 'UNKNOWN'")
        db.execSQL("ALTER TABLE main_panel_inspections ADD COLUMN exposed_parts_or_damaged_insulation TEXT NOT NULL DEFAULT 'UNKNOWN'")
        db.execSQL("ALTER TABLE main_panel_inspections ADD COLUMN wiring_risks_notes TEXT")
        db.execSQL("ALTER TABLE main_panel_inspections ADD COLUMN protection_conductor_check_result TEXT NOT NULL DEFAULT 'NOT_VERIFIED'")
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS main_panel_measurements (
                id TEXT NOT NULL PRIMARY KEY,
                inspection_id TEXT NOT NULL,
                section TEXT NOT NULL,
                type TEXT NOT NULL,
                value REAL,
                unit TEXT NOT NULL,
                origin TEXT NOT NULL,
                sort_order INTEGER NOT NULL,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL,
                is_deleted INTEGER NOT NULL
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS index_main_panel_measurements_inspection_id ON main_panel_measurements(inspection_id)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_main_panel_measurements_inspection_id_section ON main_panel_measurements(inspection_id, section)")
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS main_panel_circuits (
                id TEXT NOT NULL PRIMARY KEY,
                inspection_id TEXT NOT NULL,
                sort_order INTEGER NOT NULL,
                destination TEXT NOT NULL,
                destination_other TEXT,
                breaker_amps INTEGER,
                breaker_other_amps INTEGER,
                breaker_curve TEXT NOT NULL,
                conductor_section_mm2 REAL,
                conductor_other_section_mm2 REAL,
                conductor_material TEXT NOT NULL,
                conductor_material_other TEXT,
                consumption_amps REAL,
                consumption_origin TEXT NOT NULL,
                notes TEXT,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL,
                is_deleted INTEGER NOT NULL
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS index_main_panel_circuits_inspection_id ON main_panel_circuits(inspection_id)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_main_panel_circuits_inspection_id_sort_order ON main_panel_circuits(inspection_id, sort_order)")
    }
}
