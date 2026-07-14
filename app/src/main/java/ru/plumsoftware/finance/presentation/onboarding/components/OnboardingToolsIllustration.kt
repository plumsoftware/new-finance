package ru.plumsoftware.finance.presentation.onboarding.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.ui.components.AppCard
import ru.plumsoftware.finance.ui.theme.Dimens

@Composable
fun OnboardingToolsIllustration(modifier: Modifier = Modifier) {
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
                .padding(Dimens.SpacingL),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingM),
        ) {
            // Заголовок блока
            Text(
                text = stringResource(R.string.onboarding_demo_tools_label),
                style = typography.labelSmall,
                color = colors.onSurfaceVariant,
                letterSpacing = 0.5.sp,
            )

            // Три строки — три калькулятора
            listOf(
                Triple("💳", R.string.tool_credit_title,  0.72f),
                Triple("📈", R.string.tool_deposit_title, 0.55f),
//                Triple("🎯", R.string.tool_goal_calc_title, 0.88f),
            ).forEach { (emoji, labelRes, progress) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(Dimens.RadiusM))
                        .background(colors.surfaceVariant.copy(alpha = 0.6f))
                        .padding(horizontal = Dimens.SpacingM, vertical = Dimens.SpacingS),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingS),
                ) {
                    // Эмодзи в кружке
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(colors.primary.copy(alpha = 0.10f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(emoji, fontSize = 16.sp)
                    }

                    // Название и прогресс-бар (имитирует результат расчёта)
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = stringResource(labelRes),
                            style = typography.labelMedium,
                            color = colors.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .clip(RoundedCornerShape(Dimens.RadiusPill)),
                            color = colors.primary,
                            trackColor = colors.primary.copy(alpha = 0.12f),
                            strokeCap = StrokeCap.Round,
                        )
                    }

                    // Результат-заглушка
                    Text(
                        text = stringResource(R.string.onboarding_demo_tools_arrow),
                        style = typography.labelSmall,
                        color = colors.primary,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}