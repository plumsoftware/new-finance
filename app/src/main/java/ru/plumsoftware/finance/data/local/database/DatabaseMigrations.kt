package ru.plumsoftware.finance.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseMigrations {

    val MIGRATION_10_11 = object : Migration(10, 11) {
        override fun migrate(db: SupportSQLiteDatabase) {
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
                 '💳', 0, 0, 1, 0, ${System.currentTimeMillis()})
                """.trimIndent(),
            )

            db.execSQL(
                "ALTER TABLE transactions ADD COLUMN accountId INTEGER NOT NULL DEFAULT 1",
            )
            db.execSQL(
                "ALTER TABLE transactions ADD COLUMN currencyCode TEXT NOT NULL DEFAULT 'RUB'",
            )
            db.execSQL(
                "ALTER TABLE transactions ADD COLUMN originalAmountMinor INTEGER NOT NULL DEFAULT 0",
            )
            db.execSQL(
                "ALTER TABLE transactions ADD COLUMN originalCurrencyCode TEXT",
            )
            db.execSQL(
                "ALTER TABLE transactions ADD COLUMN exchangeRate REAL NOT NULL DEFAULT 1.0",
            )

            db.execSQL(
                """
                UPDATE transactions
                SET accountId = 1,
                    currencyCode = 'RUB',
                    originalAmountMinor = amountMinor,
                    originalCurrencyCode = 'RUB',
                    exchangeRate = 1.0
                """.trimIndent(),
            )

            db.execSQL(
                "ALTER TABLE goals ADD COLUMN currencyCode TEXT NOT NULL DEFAULT 'RUB'",
            )
            db.execSQL(
                "ALTER TABLE goals ADD COLUMN accountId INTEGER",
            )

            db.execSQL(
                "ALTER TABLE goal_deposits ADD COLUMN currencyCode TEXT NOT NULL DEFAULT 'RUB'",
            )
            db.execSQL(
                "ALTER TABLE goal_deposits ADD COLUMN accountId INTEGER",
            )

            db.execSQL(
                "CREATE INDEX IF NOT EXISTS idx_transactions_accountId ON transactions(accountId)",
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS idx_transactions_currency ON transactions(currencyCode)",
            )
        }
    }

    val MIGRATION_11_12 = object : Migration(11, 12) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE transactions ADD COLUMN goalId INTEGER")
            db.execSQL("ALTER TABLE goal_deposits ADD COLUMN transactionId INTEGER")
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS idx_transactions_goalId ON transactions(goalId)",
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS idx_goal_deposits_transactionId ON goal_deposits(transactionId)",
            )
        }
    }
}
