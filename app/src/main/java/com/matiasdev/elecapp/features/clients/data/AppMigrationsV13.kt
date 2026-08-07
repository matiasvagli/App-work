package com.matiasdev.elecapp.features.clients.data

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_12_13 = object : Migration(12, 13) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE pillar_inspections ADD COLUMN property_type TEXT")
        db.execSQL("ALTER TABLE pillar_inspections ADD COLUMN property_type_other TEXT")
        db.execSQL("ALTER TABLE pillar_inspections ADD COLUMN supply_type TEXT")
        db.execSQL("ALTER TABLE pillar_inspections ADD COLUMN main_breaker_other_amps INTEGER")
        db.execSQL("ALTER TABLE pillar_inspections ADD COLUMN differential_present TEXT NOT NULL DEFAULT 'UNKNOWN'")
        db.execSQL("ALTER TABLE pillar_inspections ADD COLUMN differential_rated_amps INTEGER")
        db.execSQL("ALTER TABLE pillar_inspections ADD COLUMN differential_other_rated_amps INTEGER")
        db.execSQL("ALTER TABLE pillar_inspections ADD COLUMN differential_sensitivity_ma INTEGER")
        db.execSQL("ALTER TABLE pillar_inspections ADD COLUMN differential_other_sensitivity_ma INTEGER")
        db.execSQL("ALTER TABLE pillar_inspections ADD COLUMN differential_test_result TEXT NOT NULL DEFAULT 'NOT_TESTED'")
        db.execSQL("ALTER TABLE pillar_inspections ADD COLUMN conductor_other_section_mm2 REAL")
        db.execSQL("ALTER TABLE pillar_inspections ADD COLUMN conductor_material_other TEXT")
        db.execSQL("ALTER TABLE pillar_inspections ADD COLUMN protection_compatibility_notes TEXT")
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS pillar_measurements (
                id TEXT NOT NULL PRIMARY KEY,
                inspection_id TEXT NOT NULL,
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
        db.execSQL("CREATE INDEX IF NOT EXISTS index_pillar_measurements_inspection_id ON pillar_measurements(inspection_id)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_pillar_measurements_inspection_id_type ON pillar_measurements(inspection_id, type)")
    }
}
