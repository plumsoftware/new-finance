package ru.plumsoftware.finance.ads

import android.app.Activity
import android.app.Application
import com.yandex.mobile.ads.common.AdError
import com.yandex.mobile.ads.common.AdRequest
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import com.yandex.mobile.ads.interstitial.InterstitialAd
import com.yandex.mobile.ads.interstitial.InterstitialAdEventListener
import com.yandex.mobile.ads.interstitial.InterstitialAdLoadListener
import com.yandex.mobile.ads.interstitial.InterstitialAdLoader
import ru.plumsoftware.finance.AppConfig

class InterstitialAdManager(
    application: Application,
) {
    private val loader = InterstitialAdLoader(application)
    private var loadedAd: InterstitialAd? = null
    private var loadedAdUnitId: String? = null
    private var lastShownAtMs: Long = 0L

    fun preload(adUnitId: String) {
        if (adUnitId.isBlank()) return
        if (loadedAd != null && loadedAdUnitId == adUnitId) return
        val request = AdRequest.Builder(adUnitId).build()
        loader.loadAd(
            request,
            object : InterstitialAdLoadListener {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    clearLoaded()
                    loadedAd = interstitialAd
                    loadedAdUnitId = adUnitId
                }

                override fun onAdFailedToLoad(error: AdRequestError) {
                    clearLoaded()
                }
            },
        )
    }

    /**
     * Shows an interstitial if cooldown elapsed and ad is loaded.
     * Always invokes [onFinished] when the user may continue (skip, fail, or dismiss).
     */
    fun tryShow(
        activity: Activity,
        placement: InterstitialPlacement,
        onFinished: () -> Unit,
    ) {
        val adUnitId = placement.adUnitId()
        if (adUnitId.isBlank()) {
            onFinished()
            return
        }
        val now = System.currentTimeMillis()
        if (now - lastShownAtMs < AppConfig.INTERSTITIAL_COOLDOWN_MS) {
            onFinished()
            preload(adUnitId)
            return
        }
        val ad = loadedAd
        if (ad == null || loadedAdUnitId != adUnitId) {
            onFinished()
            preload(adUnitId)
            return
        }
        clearLoaded()
        ad.setAdEventListener(object : InterstitialAdEventListener {
            override fun onAdShown() = Unit

            override fun onAdFailedToShow(error: AdError) {
                preload(adUnitId)
                onFinished()
            }

            override fun onAdDismissed() {
                lastShownAtMs = System.currentTimeMillis()
                preload(adUnitId)
                onFinished()
            }

            override fun onAdClicked() = Unit

            override fun onAdImpression(impressionData: ImpressionData?) = Unit
        })
        ad.show(activity)
    }

    fun destroy() {
        clearLoaded()
    }

    private fun clearLoaded() {
        loadedAd?.setAdEventListener(null)
        loadedAd = null
        loadedAdUnitId = null
    }
}
