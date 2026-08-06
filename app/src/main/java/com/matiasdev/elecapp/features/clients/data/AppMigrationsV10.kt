package com.matiasdev.elecapp.features.clients.data

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.matiasdev.elecapp.features.electricalrules.data.createElectricalRuleConfigsTable

val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.createElectricalRuleConfigsTable()
    }
}
