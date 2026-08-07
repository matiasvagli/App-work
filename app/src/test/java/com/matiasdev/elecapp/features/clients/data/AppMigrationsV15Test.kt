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

class AppMigrationsV15Test {
    @Test
    fun `migration 14 to 15 preserves old findings as manual included findings`() {
        val db = RecordingDatabaseV15()

        MIGRATION_14_15.migrate(db)

        assertTrue(db.sql.any { it.contains("CREATE TABLE IF NOT EXISTS inspection_findings_new") })
        assertTrue(db.sql.any { it.contains("'MANUAL'") && it.contains("'CONFIRMED'") })
        assertTrue(db.sql.contains("DROP TABLE inspection_findings"))
        assertTrue(db.sql.contains("ALTER TABLE inspection_findings_new RENAME TO inspection_findings"))
        assertTrue(db.sql.contains("CREATE INDEX IF NOT EXISTS index_inspection_findings_inspection_id ON inspection_findings(inspection_id)"))
        assertTrue(db.sql.contains("CREATE INDEX IF NOT EXISTS index_inspection_findings_inspection_id_sort_order ON inspection_findings(inspection_id, sort_order)"))
    }
}

private class RecordingDatabaseV15 : SupportSQLiteDatabase {
    val sql = mutableListOf<String>()
    override val isDbLockedByCurrentThread: Boolean = false
    override val isReadOnly: Boolean = false
    override val isOpen: Boolean = true
    override val isWriteAheadLoggingEnabled: Boolean = false
    override val isDatabaseIntegrityOk: Boolean = true
    override var version: Int = 14
    override val maximumSize: Long = 0
    override var pageSize: Long = 0
    override val path: String? = ":memory:"
    override val attachedDbs: List<Pair<String, String>> = emptyList()
    override fun execSQL(sql: String) { this.sql += sql }
    override fun execSQL(sql: String, bindArgs: Array<out Any?>) { this.sql += sql }
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
    private fun <T> unsupported(): T = throw UnsupportedOperationException("Not required")
}
