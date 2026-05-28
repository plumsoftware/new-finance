package ru.plumsoftware.finance.presentation.onboarding.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.ui.theme.Dimens

@Composable
fun OnboardingControlChartIllustration(modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val shapes = MaterialTheme.shapes
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(Dimens.illustrationHeight),
        shape = shapes.medium,
        color = colors.surface,
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(Dimens.paddingLarge),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(R.string.onboarding_demo_balance),
                style = typography.bodyMedium,
                color = colors.onSurfaceVariant,
            )
            Text(
                text = stringResource(R.string.onboarding_demo_amount),
                style = typography.displayLarge,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.paddingSmall),
                verticalAlignment = Alignment.Bottom,
            ) {
                listOf(0.35f, 0.5f, 0.42f, 0.68f, 0.55f, 0.82f, 0.75f).forEach { fraction ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height((Dimens.illustrationHeight.value * fraction * 0.35f).dp)
                            .clip(
                                RoundedCornerShape(
                                    topStart = Dimens.illustrationBarRadius,
                                    topEnd = Dimens.illustrationBarRadius,
                                ),
                            )
                            .background(colors.secondary.copy(alpha = 0.85f)),
                    )
                }
            }
        }
    }
}

@Composable
fun OnboardingIncomeExpenseIllustration(modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(Dimens.illustrationHeight),
        verticalArrangement = Arrangement.spacedBy(Dimens.paddingMedium),
    ) {
        FinanceSummaryRow(
            label = stringResource(R.string.income),
            amount = "+45 200 ₽",
            accentColor = colors.tertiary,
        )
        FinanceSummaryRow(
            label = stringResource(R.string.expense),
            amount = "–28 150 ₽",
            accentColor = colors.error,
        )
    }
}

@Composable
private fun FinanceSummaryRow(
    label: String,
    amount: String,
    accentColor: androidx.compose.ui.graphics.Color,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val shapes = MaterialTheme.shapes
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = shapes.medium,
        color = colors.surface,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.paddingLarge),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = typography.titleMedium,
            )
            Text(
                text = amount,
                style = typography.titleMedium,
                color = accentColor,
            )
        }
    }
}

@Composable
fun OnboardingSmartSavingsIllustration(modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val shapes = MaterialTheme.shapes
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(Dimens.illustrationHeight),
        shape = shapes.medium,
        color = colors.surface,
    ) {
        Column(
            modifier = Modifier.padding(Dimens.paddingLarge),
            verticalArrangement = Arrangement.spacedBy(Dimens.paddingMedium),
        ) {
            Text(
                text = stringResource(R.string.onboarding_demo_mug),
                style = typography.titleMedium,
            )
            Text(
                text = stringResource(R.string.onboarding_demo_payback),
                style = typography.bodyMedium,
                color = colors.onSurfaceVariant,
            )
            LinearProgressIndicator(
                progress = { 0.8f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.paddingSmall)
                    .clip(shapes.extraSmall),
                color = colors.tertiary,
                trackColor = colors.onSurfaceVariant.copy(alpha = 0.2f),
                strokeCap = StrokeCap.Round,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "1 500 ₽",
                    style = typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                )
                Text(
                    text = stringResource(R.string.onboarding_demo_daily),
                    style = typography.bodyMedium,
                    color = colors.tertiary,
                )
            }
            Surface(
                shape = shapes.small,
                color = colors.secondary.copy(alpha = 0.12f),
            ) {
                Text(
                    text = stringResource(R.string.smart_record_usage),
                    modifier = Modifier.padding(
                        horizontal = Dimens.paddingMedium,
                        vertical = Dimens.paddingSmall,
                    ),
                    style = typography.labelLarge,
                    color = colors.secondary,
                )
            }
        }
    }
}

@Composable
fun OnboardingWelcomeIllustration(modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val shapes = MaterialTheme.shapes
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(Dimens.illustrationHeight),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier
                .width(Dimens.illustrationBarWidth)
                .height(Dimens.illustrationBarWidth),
            shape = shapes.large,
            color = colors.surface,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.checkmark),
                    style = typography.displayLarge,
                    color = colors.tertiary,
                )
            }
        }
    }
}

@Composable
fun OnboardingIllustrationWithMascot(
    illustration: @Composable () -> Unit,
    mascotRes: Int,
    mascotAlignment: Alignment = Alignment.BottomEnd,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(Dimens.illustrationHeight + Dimens.mascotOnboarding / 2),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimens.illustrationHeight)
                .align(Alignment.TopCenter),
        ) {
            illustration()
        }
        OnboardingMascot(
            mascotRes = mascotRes,
            modifier = Modifier
                .align(mascotAlignment)
                .offset(
                    x = if (mascotAlignment == Alignment.BottomStart) Dimens.paddingMedium else (-Dimens.paddingMedium),
                    y = Dimens.paddingSmall,
                ),
            sizeDp = Dimens.mascotOnboarding,
        )
    }
}
