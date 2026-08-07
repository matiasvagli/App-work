package com.matiasdev.elecapp.features.clients.data

import org.junit.Assert.assertTrue
import org.junit.Test

class AppMigrationsV18Test {
    @Test
    fun `migration 17 to 18 adds structured visit completion columns`() {
        val db = RecordingDatabaseV16()

        MIGRATION_17_18.migrate(db)

        assertTrue(db.sql.contains("ALTER TABLE visit_completions ADD COLUMN work_type TEXT"))
        assertTrue(db.sql.contains("ALTER TABLE visit_completions ADD COLUMN work_sectors TEXT"))
        assertTrue(db.sql.contains("ALTER TABLE visit_completions ADD COLUMN work_items TEXT"))
        assertTrue(db.sql.contains("ALTER TABLE visit_completions ADD COLUMN work_tests TEXT"))
        assertTrue(db.sql.contains("ALTER TABLE visit_completions ADD COLUMN work_observations TEXT"))
        assertTrue(db.sql.contains("ALTER TABLE visit_completions ADD COLUMN technical_result TEXT"))
    }
}
