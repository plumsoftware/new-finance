package ru.plumsoftware.finance.ui.ads

import android.content.Context
import android.graphics.Outline
import android.view.LayoutInflater
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.ImageView
import android.widget.TextView
import com.yandex.mobile.ads.common.AdBindingResult
import com.yandex.mobile.ads.nativeads.MediaView
import com.yandex.mobile.ads.nativeads.NativeAd
import com.yandex.mobile.ads.nativeads.NativeAdView
import com.yandex.mobile.ads.nativeads.NativeAdViewBinder
import ru.plumsoftware.finance.R

internal fun NativeAd.shouldUseImageLayout(): Boolean {
    val assets = adAssets ?: return false
    if (assets.image != null) return true
    val media = assets.media ?: return false
    if (media.hasVideo) return true
    return media.aspectRatio >= 1.3f
}

internal fun inflateAndBindNativeAd(
    context: Context,
    nativeAd: NativeAd,
): NativeAdView? {
    val layoutCandidates = if (nativeAd.shouldUseImageLayout()) {
        listOf(
            R.layout.view_native_ad_image to true,
            R.layout.view_native_ad_compact to false,
        )
    } else {
        listOf(
            R.layout.view_native_ad_compact to false,
            R.layout.view_native_ad_image to true,
        )
    }

    for ((layoutId, isImageLayout) in layoutCandidates) {
        val nativeAdView = LayoutInflater.from(context)
            .inflate(layoutId, null) as NativeAdView
        if (bindNativeAd(nativeAdView, nativeAd, isImageLayout)) {
            markNativeAdLayout(nativeAdView, isImageLayout)
            return nativeAdView
        }
    }
    return null
}

internal fun bindNativeAd(
    nativeAdView: NativeAdView,
    nativeAd: NativeAd,
    isImageLayout: Boolean,
    isDarkTheme: Boolean = false,
): Boolean {
    val media = nativeAdView.findViewById<MediaView>(R.id.native_ad_media)
    val icon = nativeAdView.findViewById<ImageView>(R.id.native_ad_icon)
    val favicon = nativeAdView.findViewById<ImageView>(R.id.native_ad_favicon)
    val title = nativeAdView.findViewById<TextView>(R.id.native_ad_title)
    val body = nativeAdView.findViewById<TextView>(R.id.native_ad_body)
    val callToAction = nativeAdView.findViewById<TextView>(R.id.native_ad_call_to_action)
    val warning = nativeAdView.findViewById<TextView>(R.id.native_ad_warning)
    val domain = nativeAdView.findViewById<TextView>(R.id.native_ad_domain)
    val sponsored = nativeAdView.findViewById<TextView>(R.id.native_ad_sponsored)
    val feedback = nativeAdView.findViewById<ImageView>(R.id.native_ad_feedback)
    val age = nativeAdView.findViewById<TextView>(R.id.native_ad_age)
    val price = nativeAdView.findViewById<TextView>(R.id.native_ad_price)

    val binder = NativeAdViewBinder.Builder(nativeAdView)
        .setMediaView(media)
        .setIconView(icon)
        .setFaviconView(favicon)
        .setTitleView(title)
        .setBodyView(body)
        .setCallToActionView(callToAction)
        .setWarningView(warning)
        .setDomainView(domain)
        .setSponsoredView(sponsored)
        .setFeedbackView(feedback)
        .setAgeView(age)
        .setPriceView(price)
        .build()

    val bound = when (nativeAd.bindNativeAd(binder)) {
        is AdBindingResult.Failure -> false
        AdBindingResult.Success -> true
    }
    if (!bound) return false

    applyNativeAdStyle(
        nativeAdView = nativeAdView,
        context = nativeAdView.context,
        isImageLayout = isImageLayout,
        isDarkTheme = isDarkTheme,
    )
    return true
}

internal fun applyNativeAdStyle(
    nativeAdView: NativeAdView,
    context: Context,
    isImageLayout: Boolean,
    isDarkTheme: Boolean,
) {
    val textColors = nativeAdTextColors(isDarkTheme)
    val icon = nativeAdView.findViewById<ImageView>(R.id.native_ad_icon)
    val media = nativeAdView.findViewById<MediaView>(R.id.native_ad_media)
    val title = nativeAdView.findViewById<TextView>(R.id.native_ad_title)
    val body = nativeAdView.findViewById<TextView>(R.id.native_ad_body)
    val callToAction = nativeAdView.findViewById<TextView>(R.id.native_ad_call_to_action)
    val warning = nativeAdView.findViewById<TextView>(R.id.native_ad_warning)
    val domain = nativeAdView.findViewById<TextView>(R.id.native_ad_domain)
    val sponsored = nativeAdView.findViewById<TextView>(R.id.native_ad_sponsored)
    val feedback = nativeAdView.findViewById<ImageView>(R.id.native_ad_feedback)

    val cornerRadiusPx = 5f * context.resources.displayMetrics.density
    val outlineProvider = object : ViewOutlineProvider() {
        override fun getOutline(view: View, outline: Outline) {
            outline.setRoundRect(0, 0, view.width, view.height, cornerRadiusPx)
        }
    }

    icon?.let { view ->
        view.clipToOutline = true
        view.outlineProvider = outlineProvider
    }
    if (!isImageLayout) {
        val hasIcon = icon?.drawable != null
        if (hasIcon) {
            media?.visibility = View.GONE
        } else {
            icon?.visibility = View.GONE
            media?.let { view ->
                view.visibility = View.VISIBLE
                view.clipToOutline = true
                view.outlineProvider = outlineProvider
            }
        }
    }

    sponsored?.apply {
        text = context.getString(R.string.ad_label)
        setTextColor(textColors.secondary)
    }

    val advertiser = domain?.text?.toString()?.trim().orEmpty()
    domain?.setTextColor(textColors.secondary)
    domain?.text = when {
        advertiser.isBlank() -> context.getString(
            R.string.ad_domain_separator,
            context.getString(R.string.ad_by_yandex),
        )
        else -> context.getString(R.string.ad_domain_separator, advertiser)
    }

    title?.apply {
        text = text?.toString()
            ?.removePrefix("[Demo Ad]")
            ?.removePrefix("[Demo Ad] ")
            ?.trim()
        setTextColor(textColors.primary)
    }

    body?.apply {
        visibility = if (text.isNullOrBlank() || !isImageLayout) View.GONE else View.VISIBLE
        setTextColor(textColors.secondary)
    }
    warning?.apply {
        visibility = if (text.isNullOrBlank()) View.GONE else View.VISIBLE
        setTextColor(textColors.secondary)
    }
    callToAction?.visibility = if (callToAction.text.isNullOrBlank()) View.GONE else View.VISIBLE

    feedback?.apply {
        alpha = 1f
        setBackgroundResource(
            if (isImageLayout) {
                R.drawable.bg_native_ad_feedback_circle
            } else {
                R.drawable.bg_native_ad_feedback_circle_compact
            },
        )
        scaleType = ImageView.ScaleType.CENTER_INSIDE
        val paddingPx = (3f * context.resources.displayMetrics.density).toInt()
        setPadding(paddingPx, paddingPx, paddingPx, paddingPx)
        elevation = 2f * context.resources.displayMetrics.density
    }

    callToAction?.setBackgroundResource(
        if (isImageLayout) {
            R.drawable.bg_native_ad_cta_solid
        } else {
            R.drawable.bg_native_ad_cta_light
        },
    )
    if (!isImageLayout) {
        callToAction?.setTextColor(0xFF007AFF.toInt())
    } else {
        callToAction?.setTextColor(0xFFFFFFFF.toInt())
    }
}

private data class NativeAdTextColors(
    val primary: Int,
    val secondary: Int,
)

private fun nativeAdTextColors(isDarkTheme: Boolean): NativeAdTextColors = if (isDarkTheme) {
    NativeAdTextColors(
        primary = 0xFFFFFFFF.toInt(),
        secondary = 0xFF8E8E93.toInt(),
    )
} else {
    NativeAdTextColors(
        primary = 0xFF1C1C1E.toInt(),
        secondary = 0xFF8E8E93.toInt(),
    )
}
