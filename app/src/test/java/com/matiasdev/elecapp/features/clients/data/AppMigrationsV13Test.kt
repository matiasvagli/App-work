package com.matiasdev.elecapp.features.clients.data

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteTransactionListener
import android.os.CancellationSignal
import android.util.Pair
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteQuery
import androidx.sqlite.db.SupportSQLiteStatement
import java.util.Locale
import org.junit.Assert.assertTrue
import org.junit.Test

class AppMigrationsV13Test {
    @Test
    fun `migration 12 to 13 adds pillar supply protection and measurement schema`() {
        val db = RecordingDatabaseV13()

        MIGRATION_12_13.migrate(db)

        assertTrue(db.sql.contains("ALTER TABLE pillar_inspections ADD COLUMN property_type TEXT"))
        assertTrue(db.sql.contains("ALTER TABLE pillar_inspections ADD COLUMN supply_type TEXT"))
        assertTrue(db.sql.contains("ALTER TABLE pillar_inspections ADD COLUMN differential_present TEXT NOT NULL DEFAULT 'UNKNOWN'"))
        assertTrue(db.sql.any { it.contains("CREATE TABLE IF NOT EXISTS pillar_measurements") })
        assertTrue(db.sql.contains("CREATE INDEX IF NOT EXISTS index_pillar_measurements_inspection_id ON pillar_measurements(inspection_id)"))
        assertTrue(db.sql.contains("CREATE INDEX IF NOT EXISTS index_pillar_measurements_inspection_id_type ON pillar_measurements(inspection_id, type)"))
    }
}

private class RecordingDatabaseV13 : SupportSQLiteDatabase {
    val sql = mutableListOf<String>()
    override val isDbLockedByCurrentThread: Boolean = false
    override val isReadOnly: Boolean = false
    override val isOpen: Boolean = true
    override val isWriteAheadLoggingEnabled: Boolean = false
    override val isDatabaseIntegrityOk: Boolean = true
    override var version: Int = 12
    override val maximumSize: Long = 0
    override var pageSize: Long = 0
    override val path: String? = ":memory:"
    override val attachedDbs: List<Pair<String, String>> = emptyList()

    override fun execSQL(sql: String) {
        this.sql += sql
    }

    override fun execSQL(sql: String, bindArgs: Array<out Any?>) {
        this.sql += sql
    }

    override fun compileStatement(sql: String): SupportSQLiteStatement = unsupported()
    override fun beginTransaction() = Unit
    override fun beginTransactionNonExclusive() = Unit
    override fun beginTransactionWithListener(transactionListener: SQLiteTransactionListener) = Unit
    override fun beginTransactionWithListenerNonExclusive(transactionListener: SQLiteTransactionListener) = Unit
    override fun endTransaction() = Unit
    override fun setTransactionSuccessful() = Unit
    override fun inTransaction(): Boolean = false
    override fun yieldIfContendedSafely(): Boolean = false
    override fun yieldIfContendedSafely(sleepAfterYieldDelayMillis: Long): Boolean = false
    override fun setMaximumSize(numBytes: Long): Long = numBytes
    override fun query(query: String): Cursor = unsupported()
    override fun query(query: String, bindArgs: Array<out Any?>): Cursor = unsupported()
    override fun query(query: SupportSQLiteQuery): Cursor = unsupported()
    override fun query(query: SupportSQLiteQuery, cancellationSignal: CancellationSignal?): Cursor = unsupported()
    override fun insert(table: String, conflictAlgorithm: Int, values: ContentValues): Long = unsupported()
    override fun delete(table: String, whereClause: String?, whereArgs: Array<out Any?>?): Int = unsupported()
    override fun update(table: String, conflictAlgorithm: Int, values: ContentValues, whereClause: String?, whereArgs: Array<out Any?>?): Int = unsupported()
    override fun needUpgrade(newVersion: Int): Boolean = newVersion > version
    override fun setLocale(locale: Locale) = Unit
    override fun setMaxSqlCacheSize(cacheSize: Int) = Unit
    override fun setForeignKeyConstraintsEnabled(enabled: Boolean) = Unit
    override fun enableWriteAheadLogging(): Boolean = false
    override fun disableWriteAheadLogging() = Unit
    override fun close() = Unit

    private fun <T> unsupported(): T {
        throw UnsupportedOperationException("Not required for this migration test")
    }
}
