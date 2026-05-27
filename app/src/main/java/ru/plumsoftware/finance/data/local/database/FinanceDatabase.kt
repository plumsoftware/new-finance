package ru.plumsoftware.finance.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.runBlocking
import ru.plumsoftware.finance.data.local.converter.EnumConverters
import ru.plumsoftware.finance.data.local.dao.CategoryDao
import ru.plumsoftware.finance.data.local.dao.NotificationDao
import ru.plumsoftware.finance.data.local.dao.SmartAssetDao
import ru.plumsoftware.finance.data.local.dao.TransactionDao
import ru.plumsoftware.finance.data.local.entity.CategoryEntity
import ru.plumsoftware.finance.data.local.entity.NotificationEntity
import ru.plumsoftware.finance.data.local.entity.SmartAssetEntity
import ru.plumsoftware.finance.data.local.entity.SmartAssetUsageEntity
import ru.plumsoftware.finance.data.local.entity.TransactionEntity

@Database(
    entities = [
        CategoryEntity::class,
        TransactionEntity::class,
        SmartAssetEntity::class,
        SmartAssetUsageEntity::class,
        NotificationEntity::class,
    ],
    version = 3,
    exportSchema = false,
)
@TypeConverters(EnumConverters::class)
abstract class FinanceDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun smartAssetDao(): SmartAssetDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        private const val DATABASE_NAME = "finance.db"

        fun create(context: Context): FinanceDatabase {
            lateinit var database: FinanceDatabase
            database = Room.databaseBuilder(context, FinanceDatabase::class.java, DATABASE_NAME)
                .addCallback(
                    object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            runBlocking {
                                seed(database)
                            }
                        }
                    },
                )
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
            return database
        }

        private suspend fun seed(database: FinanceDatabase) {
            val categoryDao = database.categoryDao()
            if (categoryDao.count() == 0) {
                categoryDao.insertAll(DefaultCategories.all())
            }
        }
    }
}
