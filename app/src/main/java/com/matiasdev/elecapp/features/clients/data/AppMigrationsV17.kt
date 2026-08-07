package com.matiasdev.elecapp.features.clients.data

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_16_17 = object : Migration(16, 17) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS grounding_inspections (
                inspection_id TEXT NOT NULL,
                electrode_present TEXT NOT NULL,
                inspection_chamber_accessible TEXT NOT NULL,
                main_ground_conductor_present TEXT NOT NULL,
                protective_conductor_continuity TEXT NOT NULL,
                resistance_ohms REAL,
                resistance_origin TEXT NOT NULL,
                notes TEXT,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL,
                PRIMARY KEY(inspection_id)
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS index_grounding_inspections_inspection_id ON grounding_inspections(inspection_id)")
        db.execSQL(
            """
            UPDATE electrical_rule_configs
            SET numeric_value = 16.0, config_version = 2
            WHERE code = 'MAX_CURRENT_COPPER_2_5_MM2' AND numeric_value = 20.0
            """.trimIndent(),
        )
    }
}
