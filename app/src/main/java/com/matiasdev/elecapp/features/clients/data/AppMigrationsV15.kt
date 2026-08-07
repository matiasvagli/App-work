package com.matiasdev.elecapp.features.clients.data

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_14_15 = object : Migration(14, 15) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE inspection_findings ADD COLUMN source_type TEXT NOT NULL DEFAULT 'MANUAL'")
        db.execSQL("ALTER TABLE inspection_findings ADD COLUMN source_section TEXT")
        db.execSQL("ALTER TABLE inspection_findings ADD COLUMN source_entity_id TEXT")
        db.execSQL("ALTER TABLE inspection_findings ADD COLUMN source_value REAL")
        db.execSQL("ALTER TABLE inspection_findings ADD COLUMN source_unit TEXT")
        db.execSQL("ALTER TABLE inspection_findings ADD COLUMN rule_code TEXT")
        db.execSQL("ALTER TABLE inspection_findings ADD COLUMN review_status TEXT NOT NULL DEFAULT 'CONFIRMED'")
        db.execSQL("ALTER TABLE inspection_findings ADD COLUMN include_in_report INTEGER NOT NULL DEFAULT 1")
        db.execSQL("ALTER TABLE inspection_findings ADD COLUMN technician_notes TEXT")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_inspection_findings_inspection_id_source_entity_id ON inspection_findings(inspection_id, source_entity_id)")
    }
}
