package ru.plumsoftware.finance.domain.model

data class SmartAsset(
    val id: Long = 0,
    val name: String,
    val icon: String,
    val purchaseCostMinor: Long,
    val alternativeCostMinor: Long,
    val trackingMode: SmartAssetTrackingMode,
    val status: SmartAssetStatus,
    val totalSavedMinor: Long,
    val totalUses: Int,
    val purchasedAtMillis: Long,
    val isActive: Boolean,
    val note: String?,
    val createdAtMillis: Long,
) {
    val paybackProgress: Float
        get() = if (purchaseCostMinor <= 0) 1f
        else (totalSavedMinor.toFloat() / purchaseCostMinor).coerceIn(0f, 1f)

    val isPaidOff: Boolean
        get() = totalSavedMinor >= purchaseCostMinor
}

data class SmartAssetUsage(
    val id: Long = 0,
    val smartAssetId: Long,
    val savedAmountMinor: Long,
    val usedAtMillis: Long,
    val note: String?,
)

data class SmartAssetWithUsages(
    val asset: SmartAsset,
    val usages: List<SmartAssetUsage>,
)
