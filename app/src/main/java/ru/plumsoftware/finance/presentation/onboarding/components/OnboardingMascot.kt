package ru.plumsoftware.finance.presentation.onboarding.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import ru.plumsoftware.finance.ui.theme.Dimens

@Composable
fun OnboardingMascot(
    @DrawableRes mascotRes: Int,
    modifier: Modifier = Modifier,
    sizeDp: androidx.compose.ui.unit.Dp = Dimens.mascotOnboarding,
) {
    Image(
        painter = painterResource(mascotRes),
        contentDescription = null,
        modifier = modifier.size(sizeDp),
        contentScale = ContentScale.Fit,
    )
}
