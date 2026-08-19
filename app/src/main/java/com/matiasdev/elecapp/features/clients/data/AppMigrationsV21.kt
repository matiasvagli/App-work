package com.matiasdev.elecapp.features.clients.data

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Documentos PDF de consulta importados por el técnico (listas de precios, normas, catálogos).
 *
 * Solo se guarda el metadato: el archivo vive en almacenamiento interno, no en la base.
 * Tabla nueva, sin tocar datos existentes.
 */
val MIGRATION_20_21 = object : Migration(20, 21) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS reference_documents (
                id TEXT NOT NULL,
                title TEXT NOT NULL,
                file_name TEXT NOT NULL,
                source_url TEXT,
                size_bytes INTEGER NOT NULL,
                imported_at INTEGER NOT NULL,
                is_deleted INTEGER NOT NULL,
                PRIMARY KEY(id)
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS index_reference_documents_imported_at ON reference_documents(imported_at)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_reference_documents_is_deleted ON reference_documents(is_deleted)")
    }
}
