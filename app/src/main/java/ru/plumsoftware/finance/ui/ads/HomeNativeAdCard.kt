package ru.plumsoftware.finance.ui.ads

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** @see NativeAdContainer */
@Composable
fun HomeNativeAdCard(
    adUnitId: String,
    modifier: Modifier = Modifier,
) {
    NativeAdContainer(
        adUnitId = adUnitId,
        modifier = modifier,
    )
}
