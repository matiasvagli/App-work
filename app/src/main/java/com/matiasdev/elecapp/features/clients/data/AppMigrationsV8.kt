package com.matiasdev.elecapp.features.clients.data

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS visit_work_sessions (
                id TEXT NOT NULL,
                visit_id TEXT NOT NULL,
                started_at INTEGER NOT NULL,
                ended_at INTEGER,
                status TEXT NOT NULL,
                notes TEXT,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL,
                is_deleted INTEGER NOT NULL,
                PRIMARY KEY(id)
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS index_visit_work_sessions_visit_id ON visit_work_sessions(visit_id)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_visit_work_sessions_status ON visit_work_sessions(status)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_visit_work_sessions_started_at ON visit_work_sessions(started_at)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_visit_work_sessions_is_deleted ON visit_work_sessions(is_deleted)")
    }
}
