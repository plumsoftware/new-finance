package ru.plumsoftware.finance.data.util

import ru.plumsoftware.finance.domain.model.SmartAsset
import ru.plumsoftware.finance.domain.model.SmartAssetStatus

object SmartAssetLogic {
    fun resolveStatus(asset: SmartAsset, addedSavingsMinor: Long): SmartAssetStatus {
        val newTotal = asset.totalSavedMinor + addedSavingsMinor
        return when {
            asset.status == SmartAssetStatus.PROFIT -> SmartAssetStatus.PROFIT
            newTotal >= asset.purchaseCostMinor -> SmartAssetStatus.PROFIT
            else -> SmartAssetStatus.PAYING_OFF
        }
    }
}
