package ru.plumsoftware.finance.presentation.onboarding.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.ui.components.AppCard
import ru.plumsoftware.finance.ui.theme.Dimens

@Composable
fun OnboardingControlChartIllustration(modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    AppCard(
        modifier = modifier
            .fillMaxWidth()
            .height(Dimens.illustrationHeight),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(Dimens.SpacingXl),
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
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingXs),
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
                            .background(colors.primary.copy(alpha = 0.85f)),
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
        verticalArrangement = Arrangement.spacedBy(Dimens.SpacingM),
    ) {
        FinanceSummaryRow(
            label = stringResource(R.string.income),
            amount = stringResource(R.string.onboarding_demo_income_amount),
            accentColor = colors.secondary,
        )
        FinanceSummaryRow(
            label = stringResource(R.string.expense),
            amount = stringResource(R.string.onboarding_demo_expense_amount),
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
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.SpacingXl),
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
    AppCard(
        modifier = modifier
            .fillMaxWidth()
            .height(Dimens.illustrationHeight),
    ) {
        Column(
            modifier = Modifier.padding(Dimens.SpacingXl),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingM),
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
                    .height(Dimens.SpacingXs)
                    .clip(shapes.extraSmall),
                color = colors.secondary,
                trackColor = colors.onSurfaceVariant.copy(alpha = 0.2f),
                strokeCap = StrokeCap.Round,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(R.string.onboarding_demo_saved_amount),
                    style = typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                )
                Text(
                    text = stringResource(R.string.onboarding_demo_daily),
                    style = typography.bodyMedium,
                    color = colors.secondary,
                )
            }
            Surface(
                shape = shapes.small,
                color = colors.primary.copy(alpha = 0.12f),
            ) {
                Text(
                    text = stringResource(R.string.smart_record_usage),
                    modifier = Modifier.padding(
                        horizontal = Dimens.SpacingM,
                        vertical = Dimens.SpacingXs,
                    ),
                    style = typography.labelLarge,
                    color = colors.primary,
                )
            }
        }
    }
}

@Composable
fun OnboardingGoalsIllustration(modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    AppCard(
        modifier = modifier
            .fillMaxWidth()
            .height(Dimens.illustrationHeight),
    ) {
        Column(
            modifier = Modifier.padding(Dimens.SpacingXl),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingM),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingS),
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(colors.primary.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.onboarding_demo_goal_emoji),
                        style = typography.titleLarge,
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.onboarding_demo_goal_name),
                        style = typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = stringResource(R.string.onboarding_demo_goal_progress),
                        style = typography.bodySmall,
                        color = colors.onSurfaceVariant,
                    )
                }
            }
            LinearProgressIndicator(
                progress = { 0.375f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.SpacingXs)
                    .clip(RoundedCornerShape(Dimens.RadiusPill)),
                color = colors.primary,
                trackColor = colors.onSurfaceVariant.copy(alpha = 0.2f),
                strokeCap = StrokeCap.Round,
            )
            Text(
                text = stringResource(R.string.percent_short, 38),
                style = typography.labelMedium,
                color = colors.primary,
            )
        }
    }
}

@Composable
fun OnboardingAchievementsIllustration(modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(Dimens.illustrationHeight),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpacingM),
    ) {
        Surface(
            shape = RoundedCornerShape(Dimens.RadiusPill),
            color = Color(0xFFFF3B30).copy(alpha = 0.10f),
        ) {
            Text(
                text = stringResource(R.string.onboarding_demo_streak),
                modifier = Modifier.padding(horizontal = Dimens.SpacingM, vertical = Dimens.SpacingS),
                style = typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFFF3B30),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingS),
        ) {
            OnboardingAchievementChip(
                emoji = "🚀",
                title = stringResource(R.string.onboarding_demo_achievement_1),
                modifier = Modifier.weight(1f),
            )
            OnboardingAchievementChip(
                emoji = "🦉",
                title = stringResource(R.string.onboarding_demo_achievement_2),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun OnboardingAchievementChip(
    emoji: String,
    title: String,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    AppCard(
        modifier = modifier.border(
            width = 1.dp,
            color = Color(0xFFFFD700).copy(alpha = 0.25f),
            shape = RoundedCornerShape(Dimens.RadiusL),
        ),
    ) {
        Column(
            modifier = Modifier.padding(Dimens.SpacingM),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingXs),
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFFFFD700).copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = emoji, style = typography.titleMedium)
            }
            Text(
                text = title,
                style = typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = colors.onSurface,
                maxLines = 2,
            )
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
        AppCard(
            modifier = Modifier
                .width(Dimens.illustrationBarWidth)
                .height(Dimens.illustrationBarWidth),
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.checkmark),
                    style = typography.displayLarge,
                    color = colors.secondary,
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
                    x = if (mascotAlignment == Alignment.BottomStart) {
                        Dimens.SpacingM
                    } else {
                        -Dimens.SpacingM
                    },
                    y = Dimens.SpacingXs,
                ),
            sizeDp = Dimens.mascotOnboarding,
        )
    }
}
