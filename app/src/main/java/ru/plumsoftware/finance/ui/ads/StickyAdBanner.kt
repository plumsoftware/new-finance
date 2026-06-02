package ru.plumsoftware.finance.ui.ads

import android.app.Activity
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.yandex.mobile.ads.banner.BannerAdEventListener
import com.yandex.mobile.ads.banner.BannerAdSize
import com.yandex.mobile.ads.banner.BannerAdView
import com.yandex.mobile.ads.common.AdRequest
import com.yandex.mobile.ads.common.AdRequestError

@Composable
fun StickyAdBanner(
    adUnitId: String,
    modifier: Modifier = Modifier,
) {
    if (adUnitId.isBlank()) return

    val context = LocalContext.current
    val activity = context as? Activity ?: return
    val screenWidthDp = LocalConfiguration.current.screenWidthDp

    val bannerAdSize = remember(screenWidthDp) {
        BannerAdSize.sticky(activity, screenWidthDp)
    }

    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { ctx ->
            BannerAdView(ctx).apply {
                setAdSize(bannerAdSize)
                setBannerAdEventListener(object : BannerAdEventListener {
                    override fun onAdLoaded() = Unit

                    override fun onAdFailedToLoad(error: AdRequestError) = Unit

                    override fun onAdClicked() = Unit

                    override fun onImpression(impressionData: com.yandex.mobile.ads.common.ImpressionData?) =
                        Unit
                })
                loadAd(AdRequest.Builder(adUnitId).build())
            }
        },
        onRelease = { bannerView ->
            bannerView.destroy()
        },
    )
}
