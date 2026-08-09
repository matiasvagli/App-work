package com.matiasdev.elecapp.features.clients.data

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Registra cuándo el técnico revisó la sección de hallazgos.
 *
 * Hasta v19 la sección se marcaba completa solo si había al menos un hallazgo, así que
 * "no pasé por hallazgos" y "pasé y no encontré nada" se veían igual: incompletas. Pero
 * no encontrar nada es un resultado válido de una inspección — el mejor, de hecho — y
 * distinto de no haber mirado.
 *
 * Nullable: los relevamientos anteriores a v20 quedan sin revisar y se comportan como antes.
 */
val MIGRATION_19_20 = object : Migration(19, 20) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE electrical_inspections ADD COLUMN findings_reviewed_at INTEGER")
    }
}
