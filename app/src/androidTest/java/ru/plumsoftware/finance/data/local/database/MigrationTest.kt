package ru.plumsoftware.finance.data.local.database

import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MigrationTest {

    @Test
    fun migrate10To11_preservesTransactionsAndSeedsDefaultAccount() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val dbPath = context.getDatabasePath("migration_manual_test.db")
        if (dbPath.exists()) dbPath.delete()

        val openHelper = FrameworkSQLiteOpenHelper(
            context,
            dbPath.absolutePath,
            object : FrameworkSQLiteOpenHelper.Callback(10) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    createVersion10Schema(db)
                }

                override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
            },
        )
        val db = openHelper.writableDatabase
        db.execSQL(
            """
            INSERT INTO transactions (type, amountMinor, categoryId, smartAssetId, note, dateMillis, createdAtMillis)
            VALUES ('EXPENSE', 15000, NULL, NULL, 'Coffee', 1700000000000, 1700000000000)
            """.trimIndent(),
        )
        db.close()

        val migratedDb = openHelper.writableDatabase
        DatabaseMigrations.MIGRATION_10_11.migrate(migratedDb)
        migratedDb.version = 11

        migratedDb.query("SELECT COUNT(*) FROM accounts WHERE id = 1 AND isDefault = 1").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(1, cursor.getInt(0))
        }

        migratedDb.query("SELECT accountId, currencyCode, amountMinor FROM transactions").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(1L, cursor.getLong(cursor.getColumnIndexOrThrow("accountId")))
            assertEquals("RUB", cursor.getString(cursor.getColumnIndexOrThrow("currencyCode")))
            assertEquals(15000L, cursor.getLong(cursor.getColumnIndexOrThrow("amountMinor")))
        }

        migratedDb.close()
        openHelper.close()
        dbPath.delete()
    }

    private fun createVersion10Schema(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS categories (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                type TEXT NOT NULL,
                icon TEXT NOT NULL,
                colorArgb INTEGER,
                isHidden INTEGER NOT NULL,
                isSystem INTEGER NOT NULL,
                sortOrder INTEGER NOT NULL,
                monthlyLimitMinor INTEGER
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS transactions (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                type TEXT NOT NULL,
                amountMinor INTEGER NOT NULL,
                categoryId INTEGER,
                smartAssetId INTEGER,
                note TEXT,
                dateMillis INTEGER NOT NULL,
                createdAtMillis INTEGER NOT NULL
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS goals (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                emoji TEXT NOT NULL,
                targetAmountMinor INTEGER NOT NULL,
                savedAmountMinor INTEGER NOT NULL,
                colorHex TEXT NOT NULL,
                deadline INTEGER,
                note TEXT,
                showOnHome INTEGER NOT NULL,
                isCompleted INTEGER NOT NULL,
                createdAtMillis INTEGER NOT NULL
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS goal_deposits (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                goalId INTEGER NOT NULL,
                amountMinor INTEGER NOT NULL,
                note TEXT,
                createdAtMillis INTEGER NOT NULL
            )
            """.trimIndent(),
        )
    }
}
