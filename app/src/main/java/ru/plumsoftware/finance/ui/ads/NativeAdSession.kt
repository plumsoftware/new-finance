package ru.plumsoftware.finance.ui.ads

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.yandex.mobile.ads.nativeads.NativeAd
import com.yandex.mobile.ads.nativeads.NativeAdView

/**
 * Кэш нативной рекламы на главном экране в рамках сессии приложения.
 * Переживает уход с главного экрана и возврат по табам без повторной загрузки.
 */
object NativeAdSession {
    // Глобальный флаг скрытия (для обратной совместимости)
    var dismissed by mutableStateOf(false)
        private set

    /** Инкремент при обновлении кэша — для синхронизации Compose после фоновой загрузки. */
    var cacheRevision by mutableStateOf(0)
        private set

    var closeDelayCompleted = false

    // Независимые кэши и состояния для каждого adUnitId
    private val dismissedStates = mutableStateMapOf<String, Boolean>()
    private val cachedAds = mutableMapOf<String, NativeAd>()
    private val cachedViews = mutableMapOf<String, NativeAdView>()
    private val loadingStates = mutableStateMapOf<String, Boolean>()
    private val failedAdUnitIds = mutableStateMapOf<String, Boolean>()

    /** Проверка, скрыта ли реклама для конкретного рекламного блока */
    fun isDismissed(adUnitId: String): Boolean {
        return dismissed || (dismissedStates[adUnitId] == true)
    }

    fun getCached(adUnitId: String): Pair<NativeAd, NativeAdView>? {
        if (isDismissed(adUnitId)) return null
        val ad = cachedAds[adUnitId] ?: return null
        val view = cachedViews[adUnitId] ?: return null
        return ad to view
    }

    fun beginLoad(adUnitId: String): Boolean {
        if (isDismissed(adUnitId)) return false
        if (getCached(adUnitId) != null) return false
        if (failedAdUnitIds[adUnitId] == true) return false
        if (loadingStates[adUnitId] == true) return false
        loadingStates[adUnitId] = true
        return true
    }

    fun cache(adUnitId: String, ad: NativeAd, view: NativeAdView) {
        cachedAds[adUnitId] = ad
        cachedViews[adUnitId] = view
        failedAdUnitIds[adUnitId] = false
        loadingStates[adUnitId] = false
        cacheRevision++
    }

    fun onLoadFailed(adUnitId: String) {
        loadingStates[adUnitId] = false
        failedAdUnitIds[adUnitId] = true
        cacheRevision++
    }

    /** Скрытие рекламы для конкретного экрана */
    fun dismiss(adUnitId: String) {
        dismissedStates[adUnitId] = true
        clearCache(adUnitId)
    }

    /** Полное глобальное скрытие всей рекламы */
    fun dismiss() {
        dismissed = true
        cachedAds.keys.toList().forEach { id ->
            dismissedStates[id] = true
            clearCache(id)
        }
        clearGlobalCache()
    }

    fun clearCache(adUnitId: String) {
        cachedAds.remove(adUnitId)
        cachedViews.remove(adUnitId)
        loadingStates.remove(adUnitId)
        failedAdUnitIds.remove(adUnitId)
        cacheRevision++
    }

    private fun clearGlobalCache() {
        cachedAds.clear()
        cachedViews.clear()
        loadingStates.clear()
        failedAdUnitIds.clear()
        closeDelayCompleted = false
        cacheRevision++
    }
}
