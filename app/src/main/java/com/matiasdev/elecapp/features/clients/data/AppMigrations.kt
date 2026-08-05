package com.matiasdev.elecapp.features.clients.data

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE clients ADD COLUMN address TEXT")
        db.execSQL("ALTER TABLE clients ADD COLUMN locality TEXT")
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS visits (
                id TEXT NOT NULL,
                client_id TEXT NOT NULL,
                scheduled_at INTEGER NOT NULL,
                estimated_duration_minutes INTEGER,
                reason TEXT NOT NULL,
                notes TEXT,
                status TEXT NOT NULL,
                started_at INTEGER,
                completed_at INTEGER,
                completion_notes TEXT,
                pending_work_notes TEXT,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL,
                is_deleted INTEGER NOT NULL,
                PRIMARY KEY(id)
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS index_visits_client_id ON visits(client_id)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_visits_scheduled_at ON visits(scheduled_at)")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS visit_reminders (
                id TEXT NOT NULL,
                visit_id TEXT NOT NULL,
                minutes_before INTEGER NOT NULL,
                enabled INTEGER NOT NULL,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL,
                PRIMARY KEY(id)
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS index_visit_reminders_visit_id ON visit_reminders(visit_id)")
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS electrical_inspections (
                id TEXT NOT NULL,
                visit_id TEXT NOT NULL,
                status TEXT NOT NULL,
                inspection_type TEXT NOT NULL,
                general_condition TEXT NOT NULL,
                supply_type TEXT NOT NULL,
                property_type TEXT NOT NULL,
                visit_reason_snapshot TEXT NOT NULL,
                client_name_snapshot TEXT NOT NULL,
                address_snapshot TEXT NOT NULL,
                locality_snapshot TEXT NOT NULL,
                technician_name TEXT,
                access_limitations TEXT,
                original_technical_comment TEXT,
                final_client_report TEXT,
                started_at INTEGER NOT NULL,
                completed_at INTEGER,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL,
                is_deleted INTEGER NOT NULL,
                PRIMARY KEY(id)
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS index_electrical_inspections_visit_id ON electrical_inspections(visit_id)")
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS pillar_inspections (
                inspection_id TEXT NOT NULL,
                pillar_exists INTEGER,
                accessible TEXT NOT NULL,
                general_condition TEXT NOT NULL,
                main_breaker_present TEXT NOT NULL,
                main_breaker_amps INTEGER,
                conductor_section_mm2 REAL,
                conductor_material TEXT NOT NULL,
                conductor_condition TEXT NOT NULL,
                neutral_identified TEXT NOT NULL,
                grounding_visible TEXT NOT NULL,
                protection_compatibility TEXT NOT NULL,
                notes TEXT,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL,
                PRIMARY KEY(inspection_id)
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS index_pillar_inspections_inspection_id ON pillar_inspections(inspection_id)")
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS main_panel_inspections (
                inspection_id TEXT NOT NULL,
                accessible TEXT NOT NULL,
                general_condition TEXT NOT NULL,
                differential_present TEXT NOT NULL,
                differential_rated_amps INTEGER,
                differential_sensitivity_ma INTEGER,
                differential_test_result TEXT NOT NULL,
                circuit_count INTEGER,
                circuits_identified TEXT NOT NULL,
                neutral_bar_present TEXT NOT NULL,
                ground_bar_present TEXT NOT NULL,
                neutral_and_ground_separated TEXT NOT NULL,
                improvised_connections TEXT NOT NULL,
                mixed_or_incorrect_colors TEXT NOT NULL,
                overheating_signs TEXT NOT NULL,
                protection_compatibility TEXT NOT NULL,
                notes TEXT,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL,
                PRIMARY KEY(inspection_id)
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS index_main_panel_inspections_inspection_id ON main_panel_inspections(inspection_id)")
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS inspection_findings (
                id TEXT NOT NULL,
                inspection_id TEXT NOT NULL,
                category TEXT NOT NULL,
                severity TEXT NOT NULL,
                title TEXT NOT NULL,
                description TEXT NOT NULL,
                recommendation TEXT,
                sort_order INTEGER NOT NULL,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL,
                is_deleted INTEGER NOT NULL,
                PRIMARY KEY(id)
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS index_inspection_findings_inspection_id ON inspection_findings(inspection_id)")
        db.execSQL(
            """
            CREATE INDEX IF NOT EXISTS index_inspection_findings_inspection_id_sort_order
            ON inspection_findings(inspection_id, sort_order)
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS inspection_unverified_items (
                id TEXT NOT NULL,
                inspection_id TEXT NOT NULL,
                type TEXT NOT NULL,
                description TEXT,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL,
                is_deleted INTEGER NOT NULL,
                PRIMARY KEY(id)
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS index_inspection_unverified_items_inspection_id ON inspection_unverified_items(inspection_id)")
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE visits ADD COLUMN started_at INTEGER")
        db.execSQL("ALTER TABLE visits ADD COLUMN completed_at INTEGER")
        db.execSQL("ALTER TABLE visits ADD COLUMN completion_notes TEXT")
        db.execSQL("ALTER TABLE visits ADD COLUMN pending_work_notes TEXT")
    }
}

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS quotes (
                id TEXT NOT NULL,
                client_id TEXT NOT NULL,
                visit_id TEXT,
                inspection_id TEXT,
                quote_number TEXT NOT NULL,
                title TEXT NOT NULL,
                description TEXT,
                status TEXT NOT NULL,
                currency TEXT NOT NULL,
                subtotal_amount INTEGER NOT NULL,
                discount_type TEXT NOT NULL,
                discount_value INTEGER NOT NULL,
                total_amount INTEGER NOT NULL,
                valid_until INTEGER,
                payment_terms TEXT,
                general_notes TEXT,
                client_message TEXT,
                sent_at INTEGER,
                approved_at INTEGER,
                rejected_at INTEGER,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL,
                is_deleted INTEGER NOT NULL,
                PRIMARY KEY(id)
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS index_quotes_client_id ON quotes(client_id)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_quotes_visit_id ON quotes(visit_id)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_quotes_inspection_id ON quotes(inspection_id)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_quotes_status ON quotes(status)")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_quotes_quote_number ON quotes(quote_number)")
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS quote_items (
                id TEXT NOT NULL,
                quote_id TEXT NOT NULL,
                type TEXT NOT NULL,
                description TEXT NOT NULL,
                quantity REAL NOT NULL,
                unit TEXT NOT NULL,
                custom_unit_label TEXT,
                unit_price_amount INTEGER NOT NULL,
                line_total_amount INTEGER NOT NULL,
                sort_order INTEGER NOT NULL,
                notes TEXT,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL,
                is_deleted INTEGER NOT NULL,
                PRIMARY KEY(id)
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS index_quote_items_quote_id ON quote_items(quote_id)")
        db.execSQL(
            """
            CREATE INDEX IF NOT EXISTS index_quote_items_quote_id_sort_order
            ON quote_items(quote_id, sort_order)
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS material_lists (
                id TEXT NOT NULL,
                client_id TEXT NOT NULL,
                visit_id TEXT,
                inspection_id TEXT,
                quote_id TEXT,
                title TEXT NOT NULL,
                status TEXT NOT NULL,
                purchase_responsibility TEXT NOT NULL,
                introduction TEXT,
                notes TEXT,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL,
                delivered_at INTEGER,
                is_deleted INTEGER NOT NULL,
                PRIMARY KEY(id)
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS index_material_lists_client_id ON material_lists(client_id)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_material_lists_visit_id ON material_lists(visit_id)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_material_lists_inspection_id ON material_lists(inspection_id)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_material_lists_quote_id ON material_lists(quote_id)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_material_lists_status ON material_lists(status)")
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS material_items (
                id TEXT NOT NULL,
                material_list_id TEXT NOT NULL,
                description TEXT NOT NULL,
                quantity REAL NOT NULL,
                unit TEXT NOT NULL,
                custom_unit_label TEXT,
                specifications TEXT,
                preferred_brand TEXT,
                alternative_allowed INTEGER NOT NULL,
                estimated_unit_price_amount INTEGER,
                actual_unit_price_amount INTEGER,
                sort_order INTEGER NOT NULL,
                notes TEXT,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL,
                is_deleted INTEGER NOT NULL,
                PRIMARY KEY(id)
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS index_material_items_material_list_id ON material_items(material_list_id)")
        db.execSQL(
            """
            CREATE INDEX IF NOT EXISTS index_material_items_material_list_id_sort_order
            ON material_items(material_list_id, sort_order)
            """.trimIndent(),
        )
    }
}
