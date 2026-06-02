package ru.plumsoftware.finance.ui.ads

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import org.koin.compose.koinInject
import ru.plumsoftware.finance.ads.InterstitialAdManager
import ru.plumsoftware.finance.ads.InterstitialPlacement

/**
 * Runs [onContinue] after an interstitial (if cooldown and ad allow), or immediately otherwise.
 */
@Composable
fun InterstitialAdEffect(
    placement: InterstitialPlacement,
    trigger: Boolean,
    onContinue: () -> Unit,
    manager: InterstitialAdManager = koinInject(),
) {
    val context = LocalContext.current

    LaunchedEffect(placement) {
        manager.preload(placement.adUnitId())
    }

    LaunchedEffect(trigger) {
        if (!trigger) return@LaunchedEffect
        val activity = context as? Activity
        if (activity == null) {
            onContinue()
            return@LaunchedEffect
        }
        manager.tryShow(activity, placement, onContinue)
    }
}
