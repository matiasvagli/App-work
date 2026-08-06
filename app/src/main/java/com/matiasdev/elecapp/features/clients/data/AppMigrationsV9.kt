package com.matiasdev.elecapp.features.clients.data

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE visits ADD COLUMN attention_type TEXT")
        db.execSQL("ALTER TABLE visits ADD COLUMN parent_visit_id TEXT")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_visits_parent_visit_id ON visits(parent_visit_id)")
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS visit_completions (
                id TEXT NOT NULL,
                visit_id TEXT NOT NULL,
                diagnosis TEXT,
                work_performed TEXT NOT NULL,
                pending_work TEXT,
                requires_follow_up INTEGER NOT NULL,
                follow_up_suggested_at INTEGER,
                internal_notes TEXT,
                customer_notes TEXT,
                completed_at INTEGER NOT NULL,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL,
                is_deleted INTEGER NOT NULL,
                PRIMARY KEY(id)
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_visit_completions_visit_id ON visit_completions(visit_id)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_visit_completions_completed_at ON visit_completions(completed_at)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_visit_completions_is_deleted ON visit_completions(is_deleted)")
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS service_receipts (
                id TEXT NOT NULL,
                receipt_number INTEGER,
                client_id TEXT NOT NULL,
                visit_id TEXT,
                quote_id TEXT,
                issued_at INTEGER,
                title TEXT NOT NULL,
                description TEXT,
                status TEXT NOT NULL,
                subtotal_labor_cents INTEGER NOT NULL,
                subtotal_materials_cents INTEGER NOT NULL,
                subtotal_other_cents INTEGER NOT NULL,
                discount_cents INTEGER NOT NULL,
                total_cents INTEGER NOT NULL,
                notes TEXT,
                internal_notes TEXT,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL,
                is_deleted INTEGER NOT NULL,
                PRIMARY KEY(id)
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_service_receipts_receipt_number ON service_receipts(receipt_number)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_service_receipts_client_id ON service_receipts(client_id)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_service_receipts_visit_id ON service_receipts(visit_id)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_service_receipts_quote_id ON service_receipts(quote_id)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_service_receipts_issued_at ON service_receipts(issued_at)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_service_receipts_status ON service_receipts(status)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_service_receipts_is_deleted ON service_receipts(is_deleted)")
        createReceiptItems(db)
        createPayments(db)
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS receipt_sequence (
                id TEXT NOT NULL,
                next_number INTEGER NOT NULL,
                updated_at INTEGER NOT NULL,
                PRIMARY KEY(id)
            )
            """.trimIndent(),
        )
    }

    private fun createReceiptItems(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS service_receipt_items (
                id TEXT NOT NULL,
                receipt_id TEXT NOT NULL,
                type TEXT NOT NULL,
                description TEXT NOT NULL,
                quantity_millis INTEGER NOT NULL,
                unit_price_cents INTEGER NOT NULL,
                total_cents INTEGER NOT NULL,
                source_type TEXT NOT NULL,
                source_id TEXT,
                supplied_by TEXT NOT NULL,
                is_chargeable INTEGER NOT NULL,
                sort_order INTEGER NOT NULL,
                notes TEXT,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL,
                is_deleted INTEGER NOT NULL,
                PRIMARY KEY(id)
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS index_service_receipt_items_receipt_id ON service_receipt_items(receipt_id)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_service_receipt_items_type ON service_receipt_items(type)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_service_receipt_items_source_id ON service_receipt_items(source_id)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_service_receipt_items_sort_order ON service_receipt_items(sort_order)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_service_receipt_items_is_deleted ON service_receipt_items(is_deleted)")
    }

    private fun createPayments(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS payments (
                id TEXT NOT NULL,
                client_id TEXT NOT NULL,
                visit_id TEXT,
                service_receipt_id TEXT,
                amount_cents INTEGER NOT NULL,
                method TEXT NOT NULL,
                paid_at INTEGER NOT NULL,
                reference TEXT,
                notes TEXT,
                status TEXT NOT NULL,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL,
                is_deleted INTEGER NOT NULL,
                PRIMARY KEY(id)
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS index_payments_client_id ON payments(client_id)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_payments_visit_id ON payments(visit_id)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_payments_service_receipt_id ON payments(service_receipt_id)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_payments_paid_at ON payments(paid_at)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_payments_method ON payments(method)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_payments_status ON payments(status)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_payments_is_deleted ON payments(is_deleted)")
    }
}
