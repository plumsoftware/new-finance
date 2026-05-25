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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.plumsoftware.finance.ui.theme.Dimens
import ru.plumsoftware.finance.ui.theme.IosBlue
import ru.plumsoftware.finance.ui.theme.IosGreen
import ru.plumsoftware.finance.ui.theme.IosRed
import ru.plumsoftware.finance.ui.theme.LightTextSecondary

@Composable
fun OnboardingControlChartIllustration(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(Dimens.illustrationHeight),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(Dimens.paddingLarge),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Баланс",
                style = MaterialTheme.typography.bodyMedium,
                color = LightTextSecondary,
            )
            Text(
                text = "120 000 ₽",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
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
                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                            .background(IosBlue.copy(alpha = 0.85f)),
                    )
                }
            }
        }
    }
}

@Composable
fun OnboardingIncomeExpenseIllustration(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(Dimens.illustrationHeight),
        verticalArrangement = Arrangement.spacedBy(Dimens.paddingMedium),
    ) {
        FinanceSummaryRow(
            label = "Доходы",
            amount = "+45 200 ₽",
            accentColor = IosGreen,
        )
        FinanceSummaryRow(
            label = "Расходы",
            amount = "−28 150 ₽",
            accentColor = IosRed,
        )
    }
}

@Composable
private fun FinanceSummaryRow(
    label: String,
    amount: String,
    accentColor: androidx.compose.ui.graphics.Color,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
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
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = amount,
                style = MaterialTheme.typography.titleMedium,
                color = accentColor,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
fun OnboardingSmartSavingsIllustration(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(Dimens.illustrationHeight),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier.padding(Dimens.paddingLarge),
            verticalArrangement = Arrangement.spacedBy(Dimens.paddingMedium),
        ) {
            Text(
                text = "Термокружка",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Окупаемость 80%",
                style = MaterialTheme.typography.bodyMedium,
                color = LightTextSecondary,
            )
            LinearProgressIndicator(
                progress = { 0.8f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(MaterialTheme.shapes.extraSmall),
                color = IosGreen,
                trackColor = LightTextSecondary.copy(alpha = 0.2f),
                strokeCap = StrokeCap.Round,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "1 500 ₽",
                    style = MaterialTheme.typography.bodyMedium,
                    color = LightTextSecondary,
                )
                Text(
                    text = "+150 ₽ / день",
                    style = MaterialTheme.typography.bodyMedium,
                    color = IosGreen,
                    fontWeight = FontWeight.Medium,
                )
            }
            Surface(
                shape = MaterialTheme.shapes.small,
                color = IosBlue.copy(alpha = 0.12f),
            ) {
                Text(
                    text = "+ Я сэкономил сегодня",
                    modifier = Modifier.padding(
                        horizontal = Dimens.paddingMedium,
                        vertical = Dimens.paddingSmall,
                    ),
                    style = MaterialTheme.typography.labelLarge,
                    color = IosBlue,
                )
            }
        }
    }
}

@Composable
fun OnboardingWelcomeIllustration(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(Dimens.illustrationHeight),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier
                .width(160.dp)
                .height(160.dp),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "✓",
                    style = MaterialTheme.typography.displayLarge,
                    color = IosGreen,
                    fontWeight = FontWeight.Bold,
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
