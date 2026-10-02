package ru.plumsoftware.finance.presentation.onboarding.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.plumsoftware.finance.ui.theme.FinanceTheme

/** Точки-индикатор (§6.14): активная — капсула 20×8dp `primary`, остальные 8dp `outline`. */
@Composable
fun OnboardingPageIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
) {
    val c = FinanceTheme.colors
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(pageCount) { index ->
            val selected = index == currentPage
            val w by animateDpAsState(if (selected) 20.dp else 8.dp, tween(200), label = "dotw")
            val color by animateColorAsState(if (selected) c.primary else c.outline, tween(200), label = "dotc")
            Box(Modifier.width(w).height(8.dp).background(color, RoundedCornerShape(4.dp)))
        }
    }
}
