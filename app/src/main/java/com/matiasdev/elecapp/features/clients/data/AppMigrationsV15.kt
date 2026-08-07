package com.matiasdev.elecapp.features.clients.data

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_14_15 = object : Migration(14, 15) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS inspection_findings_new (
                id TEXT NOT NULL,
                inspection_id TEXT NOT NULL,
                category TEXT NOT NULL,
                severity TEXT NOT NULL,
                title TEXT NOT NULL,
                description TEXT NOT NULL,
                recommendation TEXT,
                source_type TEXT NOT NULL,
                source_section TEXT,
                source_entity_id TEXT,
                source_value REAL,
                source_unit TEXT,
                rule_code TEXT,
                review_status TEXT NOT NULL,
                include_in_report INTEGER NOT NULL,
                technician_notes TEXT,
                sort_order INTEGER NOT NULL,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL,
                is_deleted INTEGER NOT NULL,
                PRIMARY KEY(id)
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            INSERT INTO inspection_findings_new (
                id,
                inspection_id,
                category,
                severity,
                title,
                description,
                recommendation,
                source_type,
                source_section,
                source_entity_id,
                source_value,
                source_unit,
                rule_code,
                review_status,
                include_in_report,
                technician_notes,
                sort_order,
                created_at,
                updated_at,
                is_deleted
            )
            SELECT
                id,
                inspection_id,
                category,
                severity,
                title,
                description,
                recommendation,
                'MANUAL',
                NULL,
                NULL,
                NULL,
                NULL,
                NULL,
                'CONFIRMED',
                1,
                NULL,
                sort_order,
                created_at,
                updated_at,
                is_deleted
            FROM inspection_findings
            """.trimIndent(),
        )
        db.execSQL("DROP TABLE inspection_findings")
        db.execSQL("ALTER TABLE inspection_findings_new RENAME TO inspection_findings")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_inspection_findings_inspection_id ON inspection_findings(inspection_id)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_inspection_findings_inspection_id_sort_order ON inspection_findings(inspection_id, sort_order)")
    }
}
