package com.matiasdev.elecapp.features.clients.data

import org.junit.Assert.assertTrue
import org.junit.Test

class AppMigrationsV17Test {
    @Test
    fun `migration 16 to 17 creates grounding storage and updates untouched 2_5 limit`() {
        val db = RecordingDatabaseV16()

        MIGRATION_16_17.migrate(db)

        assertTrue(db.sql.any { it.contains("CREATE TABLE IF NOT EXISTS grounding_inspections") })
        assertTrue(db.sql.contains("CREATE INDEX IF NOT EXISTS index_grounding_inspections_inspection_id ON grounding_inspections(inspection_id)"))
        assertTrue(db.sql.any { it.contains("MAX_CURRENT_COPPER_2_5_MM2") && it.contains("numeric_value = 20.0") })
    }
}
