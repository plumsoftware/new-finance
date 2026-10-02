package ru.plumsoftware.finance.ui.ads

import android.app.Activity
import android.view.ViewGroup
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.luminance
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.yandex.mobile.ads.common.AdRequest
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.nativeads.NativeAd
import com.yandex.mobile.ads.nativeads.NativeAdLoadListener
import com.yandex.mobile.ads.nativeads.NativeAdLoader
import com.yandex.mobile.ads.nativeads.NativeAdView
import kotlinx.coroutines.delay
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.ui.theme.Dimens
import kotlin.time.Duration.Companion.milliseconds

private const val CLOSE_DELAY_MS = 3_000L
private const val EXIT_ANIMATION_MS = 200L

@Composable
fun NativeAdContainer(
    adUnitId: String,
    modifier: Modifier = Modifier,
) {
    if (adUnitId.isBlank()) return

    val context = LocalContext.current
    val activity = context as? Activity ?: return
    val colors = MaterialTheme.colorScheme
    val isDarkTheme = colors.background.luminance() < 0.5f
    val cacheRevision = NativeAdSession.cacheRevision

    var nativeAd by remember(adUnitId) {
        mutableStateOf(NativeAdSession.getCached(adUnitId)?.first)
    }
    var boundView by remember(adUnitId) {
        mutableStateOf(NativeAdSession.getCached(adUnitId)?.second)
    }
    var visible by remember { mutableStateOf(true) }
    var closeEnabled by remember { mutableStateOf(NativeAdSession.closeDelayCompleted) }
    var closeProgress by remember {
        mutableFloatStateOf(if (NativeAdSession.closeDelayCompleted) 1f else 0f)
    }

    LaunchedEffect(cacheRevision, adUnitId) {
        NativeAdSession.getCached(adUnitId)?.let { (ad, view) ->
            nativeAd = ad
            boundView = view
        }
    }

    DisposableEffect(adUnitId) {
        if (!NativeAdSession.beginLoad(adUnitId)) {
            onDispose { }
            return@DisposableEffect onDispose { }
        }

        val loader = NativeAdLoader(activity)
        loader.loadAd(
            AdRequest.Builder(adUnitId).build(),
            object : NativeAdLoadListener {
                override fun onAdLoaded(ad: NativeAd) {
                    if (NativeAdSession.dismissed) {
                        NativeAdSession.onLoadFailed(adUnitId)
                        return
                    }
                    val view = inflateAndBindNativeAd(activity, ad)
                    if (view != null) {
                        NativeAdSession.cache(adUnitId, ad, view)
                        nativeAd = ad
                        boundView = view
                    } else {
                        NativeAdSession.onLoadFailed(adUnitId)
                        nativeAd = null
                        boundView = null
                    }
                }

                override fun onAdFailedToLoad(error: AdRequestError) {
                    NativeAdSession.onLoadFailed(adUnitId)
                    nativeAd = null
                    boundView = null
                }
            },
        )
        onDispose { }
    }

    val ad = nativeAd
    val view = boundView

    LaunchedEffect(ad, view) {
        if (ad == null || view == null || !visible) return@LaunchedEffect
        if (NativeAdSession.closeDelayCompleted) {
            closeEnabled = true
            closeProgress = 1f
            return@LaunchedEffect
        }
        closeEnabled = false
        closeProgress = 0f
        val steps = 30
        val stepDelay = CLOSE_DELAY_MS / steps
        repeat(steps) { step ->
            delay(stepDelay.milliseconds)
            closeProgress = (step + 1) / steps.toFloat()
        }
        NativeAdSession.closeDelayCompleted = true
        closeEnabled = true
    }

    LaunchedEffect(visible) {
        if (!visible) {
            delay(EXIT_ANIMATION_MS.milliseconds)
            NativeAdSession.dismiss()
        }
    }

    AnimatedVisibility(
        visible = visible && ad != null && view != null,
        enter = fadeIn(tween(300)) + expandVertically(tween(300)),
        exit = fadeOut(tween(EXIT_ANIMATION_MS.toInt())) + shrinkVertically(tween(EXIT_ANIMATION_MS.toInt())),
        modifier = modifier,
    ) {
        if (ad == null || view == null) return@AnimatedVisibility

        val isImageLayout = view.getTag(R.id.native_ad_is_image_layout) as? Boolean == true

        Box(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Dimens.RadiusL))
                    .background(colors.surface),
                factory = {
                    (view.parent as? ViewGroup)?.removeView(view)
                    view
                },
                update = { nativeAdView ->
                    bindNativeAd(nativeAdView, ad, isImageLayout, isDarkTheme)
                },
            )

            NativeAdCloseControl(
                isImageLayout = isImageLayout,
                closeEnabled = closeEnabled,
                progress = closeProgress,
                onClose = { visible = false },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(
                        top = NativeAdControlMetrics.inset,
                        end = NativeAdControlMetrics.inset,
                    ),
            )
        }
    }
}

internal fun markNativeAdLayout(nativeAdView: NativeAdView, isImageLayout: Boolean) {
    nativeAdView.setTag(R.id.native_ad_is_image_layout, isImageLayout)
}
