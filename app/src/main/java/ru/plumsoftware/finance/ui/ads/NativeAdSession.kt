package ru.plumsoftware.finance.ui.ads

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.yandex.mobile.ads.nativeads.NativeAd
import com.yandex.mobile.ads.nativeads.NativeAdView

/**
 * Кэш нативной рекламы на главном экране в рамках сессии приложения.
 * Переживает уход с главного экрана и возврат по табам без повторной загрузки.
 */
object NativeAdSession {
    var dismissed by mutableStateOf(false)
        private set

    /** Инкремент при обновлении кэша — для синхронизации Compose после фоновой загрузки. */
    var cacheRevision by mutableStateOf(0)
        private set

    var closeDelayCompleted = false

    private var cachedAd: NativeAd? = null
    private var cachedView: NativeAdView? = null
    private var cachedAdUnitId: String? = null
    private var isLoading = false
    private var failedAdUnitId: String? = null

    fun getCached(adUnitId: String): Pair<NativeAd, NativeAdView>? {
        if (dismissed || cachedAdUnitId != adUnitId) return null
        val ad = cachedAd ?: return null
        val view = cachedView ?: return null
        return ad to view
    }

    fun beginLoad(adUnitId: String): Boolean {
        if (dismissed) return false
        if (getCached(adUnitId) != null) return false
        if (failedAdUnitId == adUnitId) return false
        if (isLoading) return false
        isLoading = true
        cachedAdUnitId = adUnitId
        return true
    }

    fun cache(adUnitId: String, ad: NativeAd, view: NativeAdView) {
        cachedAdUnitId = adUnitId
        cachedAd = ad
        cachedView = view
        failedAdUnitId = null
        isLoading = false
        cacheRevision++
    }

    fun onLoadFailed(adUnitId: String) {
        isLoading = false
        failedAdUnitId = adUnitId
        cacheRevision++
    }

    fun dismiss() {
        dismissed = true
        clearCache()
    }

    private fun clearCache() {
        cachedAd = null
        cachedView = null
        cachedAdUnitId = null
        isLoading = false
        closeDelayCompleted = false
        cacheRevision++
    }
}
