package ru.plumsoftware.finance.data.local.converter

import androidx.room.TypeConverter
import ru.plumsoftware.finance.domain.model.CategoryType
import ru.plumsoftware.finance.domain.model.NotificationSource
import ru.plumsoftware.finance.domain.model.SmartAssetStatus
import ru.plumsoftware.finance.domain.model.SmartAssetTrackingMode
import ru.plumsoftware.finance.domain.model.TransactionType

class EnumConverters {
    @TypeConverter
    fun fromTransactionType(value: TransactionType): String = value.name

    @TypeConverter
    fun toTransactionType(value: String): TransactionType = TransactionType.valueOf(value)

    @TypeConverter
    fun fromCategoryType(value: CategoryType): String = value.name

    @TypeConverter
    fun toCategoryType(value: String): CategoryType = CategoryType.valueOf(value)

    @TypeConverter
    fun fromSmartAssetTrackingMode(value: SmartAssetTrackingMode): String = value.name

    @TypeConverter
    fun toSmartAssetTrackingMode(value: String): SmartAssetTrackingMode =
        SmartAssetTrackingMode.valueOf(value)

    @TypeConverter
    fun fromSmartAssetStatus(value: SmartAssetStatus): String = value.name

    @TypeConverter
    fun toSmartAssetStatus(value: String): SmartAssetStatus = SmartAssetStatus.valueOf(value)

    @TypeConverter
    fun fromNotificationSource(value: NotificationSource): String = value.name

    @TypeConverter
    fun toNotificationSource(value: String): NotificationSource = NotificationSource.valueOf(value)
}
