package ru.plumsoftware.finance.ui.ads

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Максимальная высота рекламного баннера. */
val AdBannerMaxHeight = 70.dp

/**
 * Нижняя панель со sticky-баннером над системной навигацией.
 *
 * Баннер рисуется **выше** жестовой зоны / кнопок «Назад–Домой–Недавние»:
 * под объявлением добавляется [Spacer] высотой [WindowInsets.navigationBars].
 */
@Composable
fun AdBannerBottomBar(
    adUnitId: String,
    modifier: Modifier = Modifier,
) {
    if (adUnitId.isBlank()) return

    Column(modifier = modifier.fillMaxWidth()) {
        StickyAdBanner(
            adUnitId = adUnitId,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = AdBannerMaxHeight),
        )
        Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
    }
}
