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

    @Test
    fun migrate11To12_addsGoalDepositTransactionLinks() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val dbPath = context.getDatabasePath("migration_manual_test_11_12.db")
        if (dbPath.exists()) dbPath.delete()

        val openHelper = FrameworkSQLiteOpenHelper(
            context,
            dbPath.absolutePath,
            object : FrameworkSQLiteOpenHelper.Callback(11) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    createVersion11Schema(db)
                }

                override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
            },
        )
        val db = openHelper.writableDatabase
        db.execSQL(
            """
            INSERT INTO transactions
            (type, amountMinor, categoryId, smartAssetId, note, dateMillis, createdAtMillis,
             accountId, currencyCode, originalAmountMinor, originalCurrencyCode, exchangeRate)
            VALUES ('EXPENSE', 50000, NULL, NULL, 'Test', 1700000000000, 1700000000000,
                    1, 'RUB', 50000, 'RUB', 1.0)
            """.trimIndent(),
        )
        db.execSQL(
            """
            INSERT INTO goal_deposits
            (goalId, amountMinor, note, createdAtMillis, currencyCode, accountId)
            VALUES (1, 10000, 'Old deposit', 1700000000000, 'RUB', 1)
            """.trimIndent(),
        )
        db.close()

        val migratedDb = openHelper.writableDatabase
        DatabaseMigrations.MIGRATION_11_12.migrate(migratedDb)
        migratedDb.version = 12

        migratedDb.query("PRAGMA table_info(transactions)").use { cursor ->
            val columns = mutableSetOf<String>()
            while (cursor.moveToNext()) {
                columns += cursor.getString(cursor.getColumnIndexOrThrow("name"))
            }
            assertTrue(columns.contains("goalId"))
        }

        migratedDb.query("PRAGMA table_info(goal_deposits)").use { cursor ->
            val columns = mutableSetOf<String>()
            while (cursor.moveToNext()) {
                columns += cursor.getString(cursor.getColumnIndexOrThrow("name"))
            }
            assertTrue(columns.contains("transactionId"))
        }

        migratedDb.query("SELECT amountMinor FROM transactions").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(50000L, cursor.getLong(0))
        }

        migratedDb.query("SELECT amountMinor FROM goal_deposits").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(10000L, cursor.getLong(0))
        }

        migratedDb.close()
        openHelper.close()
        dbPath.delete()
    }

    private fun createVersion11Schema(db: SupportSQLiteDatabase) {
        createVersion10Schema(db)
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS accounts (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                type TEXT NOT NULL DEFAULT 'DEBIT',
                currencyCode TEXT NOT NULL DEFAULT 'RUB',
                colorHex TEXT NOT NULL DEFAULT '#007AFF',
                emoji TEXT NOT NULL DEFAULT '💳',
                initialBalanceMinor INTEGER NOT NULL DEFAULT 0,
                sortOrder INTEGER NOT NULL DEFAULT 0,
                isDefault INTEGER NOT NULL DEFAULT 0,
                isArchived INTEGER NOT NULL DEFAULT 0,
                createdAtMillis INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            INSERT OR IGNORE INTO accounts
            (id, name, type, currencyCode, colorHex, emoji,
             initialBalanceMinor, sortOrder, isDefault,
             isArchived, createdAtMillis)
            VALUES
            (1, 'Основной счёт', 'DEBIT', 'RUB', '#007AFF',
             '💳', 0, 0, 1, 0, 0)
            """.trimIndent(),
        )
        db.execSQL("ALTER TABLE transactions ADD COLUMN accountId INTEGER NOT NULL DEFAULT 1")
        db.execSQL("ALTER TABLE transactions ADD COLUMN currencyCode TEXT NOT NULL DEFAULT 'RUB'")
        db.execSQL("ALTER TABLE transactions ADD COLUMN originalAmountMinor INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE transactions ADD COLUMN originalCurrencyCode TEXT")
        db.execSQL("ALTER TABLE transactions ADD COLUMN exchangeRate REAL NOT NULL DEFAULT 1.0")
        db.execSQL("ALTER TABLE goals ADD COLUMN currencyCode TEXT NOT NULL DEFAULT 'RUB'")
        db.execSQL("ALTER TABLE goals ADD COLUMN accountId INTEGER")
        db.execSQL("ALTER TABLE goal_deposits ADD COLUMN currencyCode TEXT NOT NULL DEFAULT 'RUB'")
        db.execSQL("ALTER TABLE goal_deposits ADD COLUMN accountId INTEGER")
        db.execSQL(
            """
            INSERT INTO goals
            (id, name, emoji, targetAmountMinor, savedAmountMinor, colorHex, deadline, note,
             showOnHome, isCompleted, createdAtMillis, currencyCode, accountId)
            VALUES (1, 'Vacation', '🏖️', 100000, 10000, '#007AFF', NULL, NULL, 0, 0, 0, 'RUB', 1)
            """.trimIndent(),
        )
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
