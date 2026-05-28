package ru.plumsoftware.finance.presentation.onboarding.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import ru.plumsoftware.finance.ui.theme.Dimens

@Composable
fun OnboardingPageIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Dimens.paddingSmall),
    ) {
        repeat(pageCount) { index ->
            val selected = index == currentPage
            Box(
                modifier = Modifier
                    .size(
                        if (selected) Dimens.pageIndicatorDotActive else Dimens.pageIndicatorDotInactive,
                    )
                    .clip(CircleShape)
                    .background(
                        if (selected) {
                            colors.secondary
                        } else {
                            colors.onSurface.copy(alpha = 0.35f)
                        },
                    ),
            )
        }
    }
}
