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

import android.util.Log

class InterstitialAdManager(
    application: Application,
) {
    private companion object {
        const val TAG = "YandexAds"
    }

    private val loader = InterstitialAdLoader(application)
    private var loadedAd: InterstitialAd? = null
    private var loadedAdUnitId: String? = null
    private var lastShownAtMs: Long = 0L

    fun preload(adUnitId: String) {
        if (adUnitId.isBlank()) {
            Log.d(TAG, "preload: skipped, adUnitId is blank")
            return
        }
        if (loadedAd != null && loadedAdUnitId == adUnitId) {
            Log.d(TAG, "preload: skipped, ad already loaded for $adUnitId")
            return
        }
        Log.d(TAG, "preload: start loading, adUnitId=$adUnitId")
        val request = AdRequest.Builder(adUnitId).build()
        loader.loadAd(
            request,
            object : InterstitialAdLoadListener {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    Log.d(TAG, "onAdLoaded: adUnitId=$adUnitId")
                    clearLoaded()
                    loadedAd = interstitialAd
                    loadedAdUnitId = adUnitId
                }

                override fun onAdFailedToLoad(error: AdRequestError) {
                    Log.e(
                        TAG,
                        "onAdFailedToLoad: adUnitId=$adUnitId, code=${error.code}, description=${error.description}",
                    )
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
        Log.d(TAG, "tryShow: placement=$placement, adUnitId=$adUnitId")
        if (adUnitId.isBlank()) {
            Log.d(TAG, "tryShow: skipped, adUnitId is blank")
            onFinished()
            return
        }
        val now = System.currentTimeMillis()
        val sinceLastShow = now - lastShownAtMs
        if (sinceLastShow < AppConfig.INTERSTITIAL_COOLDOWN_MS) {
            Log.d(
                TAG,
                "tryShow: skipped, cooldown (passed=${sinceLastShow}ms, required=${AppConfig.INTERSTITIAL_COOLDOWN_MS}ms)",
            )
            onFinished()
            preload(adUnitId)
            return
        }
        val ad = loadedAd
        if (ad == null || loadedAdUnitId != adUnitId) {
            Log.d(
                TAG,
                "tryShow: skipped, ad not ready (loaded=${ad != null}, loadedAdUnitId=$loadedAdUnitId, expected=$adUnitId)",
            )
            onFinished()
            preload(adUnitId)
            return
        }
        clearLoaded()
        ad.setAdEventListener(object : InterstitialAdEventListener {
            override fun onAdShown() {
                Log.d(TAG, "onAdShown: adUnitId=$adUnitId")
            }

            override fun onAdFailedToShow(error: AdError) {
                Log.e(TAG, "onAdFailedToShow: adUnitId=$adUnitId, description=${error.description}")
                preload(adUnitId)
                onFinished()
            }

            override fun onAdDismissed() {
                Log.d(TAG, "onAdDismissed: adUnitId=$adUnitId")
                lastShownAtMs = System.currentTimeMillis()
                preload(adUnitId)
                onFinished()
            }

            override fun onAdClicked() {
                Log.d(TAG, "onAdClicked: adUnitId=$adUnitId")
            }

            override fun onAdImpression(impressionData: ImpressionData?) {
                Log.d(TAG, "onAdImpression: adUnitId=$adUnitId, rawData=${impressionData?.rawData}")
            }
        })
        Log.d(TAG, "tryShow: showing ad, adUnitId=$adUnitId")
        ad.show(activity)
    }

    fun destroy() {
        Log.d(TAG, "destroy")
        clearLoaded()
    }

    private fun clearLoaded() {
        loadedAd?.setAdEventListener(null)
        loadedAd = null
        loadedAdUnitId = null
    }
}
