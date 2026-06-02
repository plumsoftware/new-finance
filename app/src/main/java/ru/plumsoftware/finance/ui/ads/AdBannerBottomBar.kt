package ru.plumsoftware.finance.ui.ads

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Максимальная высота рекламного баннера. */
val AdBannerMaxHeight = 70.dp

/** Нижняя панель со sticky-баннером (до [AdBannerMaxHeight]) над системной навигацией. */
@Composable
fun AdBannerBottomBar(
    adUnitId: String,
    modifier: Modifier = Modifier,
) {
    if (adUnitId.isBlank()) return

    StickyAdBanner(
        adUnitId = adUnitId,
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .heightIn(max = AdBannerMaxHeight),
    )
}
