package ru.plumsoftware.finance.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.plumsoftware.finance.data.local.converter.EnumConverters
import ru.plumsoftware.finance.data.local.dao.CategoryDao
import ru.plumsoftware.finance.data.local.dao.GoalDao
import ru.plumsoftware.finance.data.local.dao.NotificationDao
import ru.plumsoftware.finance.data.local.dao.RecurringTransactionDao
import ru.plumsoftware.finance.data.local.dao.SmartAssetDao
import ru.plumsoftware.finance.data.local.dao.StreakDao
import ru.plumsoftware.finance.data.local.dao.TransactionDao
import ru.plumsoftware.finance.data.local.dao.AchievementUnlockDao
import ru.plumsoftware.finance.data.local.entity.AchievementUnlockEntity
import ru.plumsoftware.finance.data.local.entity.CategoryEntity
import ru.plumsoftware.finance.data.local.entity.GoalDepositEntity
import ru.plumsoftware.finance.data.local.entity.GoalEntity
import ru.plumsoftware.finance.data.local.entity.NotificationEntity
import ru.plumsoftware.finance.data.local.entity.RecurringTransactionEntity
import ru.plumsoftware.finance.data.local.entity.SmartAssetEntity
import ru.plumsoftware.finance.data.local.entity.SmartAssetUsageEntity
import ru.plumsoftware.finance.data.local.entity.StreakDataEntity
import ru.plumsoftware.finance.data.local.entity.TransactionEntity

@Database(
    entities = [
        CategoryEntity::class,
        TransactionEntity::class,
        SmartAssetEntity::class,
        SmartAssetUsageEntity::class,
        NotificationEntity::class,
        RecurringTransactionEntity::class,
        GoalEntity::class,
        GoalDepositEntity::class,
        StreakDataEntity::class,
        AchievementUnlockEntity::class,
    ],
    version = 10,
    exportSchema = false,
)
@TypeConverters(EnumConverters::class)
abstract class FinanceDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun smartAssetDao(): SmartAssetDao
    abstract fun notificationDao(): NotificationDao
    abstract fun recurringTransactionDao(): RecurringTransactionDao
    abstract fun goalDao(): GoalDao
    abstract fun streakDao(): StreakDao
    abstract fun achievementUnlockDao(): AchievementUnlockDao

    companion object {
        private const val DATABASE_NAME = "finance.db"

        fun create(context: Context): FinanceDatabase {
            lateinit var database: FinanceDatabase
            database = Room.databaseBuilder(context, FinanceDatabase::class.java, DATABASE_NAME)
                .addCallback(
                    object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                seed(database, context)
                            }
                        }

                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                seed(database, context)
                            }
                        }
                    },
                )
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
            return database
        }

        private suspend fun seed(database: FinanceDatabase, context: Context) {
            val categoryDao = database.categoryDao()
            val transactionDao = database.transactionDao()

            CategoryDeduplicator.deduplicate(categoryDao, transactionDao)

            val existingKeys = categoryDao.getAllSync()
                .map { CategoryDeduplicator.categoryKey(it) }
                .toSet()
            val missing = DefaultCategories.all(context)
                .filter { CategoryDeduplicator.categoryKey(it) !in existingKeys }
            if (missing.isNotEmpty()) {
                categoryDao.insertAll(missing)
            }
        }
    }
}
