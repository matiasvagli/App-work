package com.matiasdev.elecapp.features.clients.data

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Guarda los informes de la atención como snapshot en `visit_completions`.
 *
 * Hasta v18 el informe técnico se regeneraba en vivo desde los datos actuales,
 * incluidos los umbrales editables de `electrical_rule_configs`. Eso hacía que
 * un informe ya entregado cambiara de conclusión si después se ajustaba un
 * umbral. Congelarlo al cerrar la atención evita esa mutación retroactiva; la
 * corrección de datos se resuelve regenerando explícitamente, no en silencio.
 *
 * Las tres columnas son nullable: las atenciones cerradas antes de v19 quedan
 * sin snapshot y se muestran como "informe no generado".
 */
val MIGRATION_18_19 = object : Migration(18, 19) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE visit_completions ADD COLUMN technical_report_snapshot TEXT")
        db.execSQL("ALTER TABLE visit_completions ADD COLUMN client_report TEXT")
        db.execSQL("ALTER TABLE visit_completions ADD COLUMN reports_generated_at INTEGER")
    }
}
