package ru.plumsoftware.finance.presentation.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.domain.model.Category
import ru.plumsoftware.finance.domain.model.Insight
import ru.plumsoftware.finance.domain.model.InsightSeverity
import ru.plumsoftware.finance.domain.model.InsightType
import ru.plumsoftware.finance.presentation.common.MoneyFormat
import ru.plumsoftware.finance.ui.components.AppCard
import ru.plumsoftware.finance.ui.components.SectionLabel
import ru.plumsoftware.finance.ui.theme.Dimens

private val InsightWarningOrange = Color(0xFFFF9500)

@Composable
fun InsightsSection(
    insights: List<Insight>,
    categoryMap: Map<Long, Category>,
    currencyCode: String,
) {
    if (insights.isEmpty()) return

    SectionLabel(text = stringResource(R.string.insights))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = Dimens.SpacingL),
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingS),
    ) {
        insights.forEach { insight ->
            InsightCard(
                insight = insight,
                categoryMap = categoryMap,
                currencyCode = currencyCode,
            )
        }
    }
}

@Composable
private fun InsightCard(
    insight: Insight,
    categoryMap: Map<Long, Category>,
    currencyCode: String,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val severityColor = insightSeverityColor(insight)
    val severityLabel = insightSeverityLabel(insight.severity)

    AppCard(modifier = Modifier.width(Dimens.insightCardWidth)) {
        Column(modifier = Modifier.padding(Dimens.SpacingM)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = insightEmoji(insight.type),
                    fontSize = 24.sp,
                )
                Spacer(modifier = Modifier.width(Dimens.SpacingXs))
                Box(
                    modifier = Modifier
                        .background(
                            color = severityColor.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(Dimens.RadiusPill),
                        )
                        .padding(
                            horizontal = Dimens.SpacingS,
                            vertical = Dimens.SpacingXxs,
                        ),
                ) {
                    Text(
                        text = severityLabel,
                        style = typography.labelSmall,
                        color = severityColor,
                    )
                }
            }
            Spacer(modifier = Modifier.height(Dimens.SpacingS))
            Text(
                text = insightTitle(insight, categoryMap, currencyCode),
                style = typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = colors.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = insightDescription(insight, currencyCode),
                style = typography.bodySmall,
                color = colors.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun insightSeverityColor(insight: Insight): Color {
    val colors = MaterialTheme.colorScheme
    return when (insight.type) {
        InsightType.CATEGORY_SPIKE -> when (insight.severity) {
            InsightSeverity.HIGH -> colors.error
            InsightSeverity.MEDIUM -> InsightWarningOrange
            InsightSeverity.LOW -> colors.onSurfaceVariant
        }
        InsightType.GOOD_SAVINGS -> colors.secondary
        InsightType.LARGE_EXPENSE -> colors.error
        InsightType.STREAK -> colors.primary
    }
}

@Composable
private fun insightSeverityLabel(severity: InsightSeverity): String = when (severity) {
    InsightSeverity.HIGH -> stringResource(R.string.insight_severity_high)
    InsightSeverity.MEDIUM -> stringResource(R.string.insight_severity_medium)
    InsightSeverity.LOW -> stringResource(R.string.insight_severity_low)
}

private fun insightEmoji(type: InsightType): String = when (type) {
    InsightType.CATEGORY_SPIKE -> "📈"
    InsightType.GOOD_SAVINGS -> "🎉"
    InsightType.LARGE_EXPENSE -> "💸"
    InsightType.STREAK -> "🔥"
}

@Composable
private fun insightTitle(
    insight: Insight,
    categoryMap: Map<Long, Category>,
    currencyCode: String,
): String = when (insight.type) {
    InsightType.CATEGORY_SPIKE -> {
        val categoryName = insight.categoryId?.let { categoryMap[it]?.name }
            ?: stringResource(R.string.transaction_default)
        stringResource(R.string.insight_spike_title, categoryName, insight.value.toInt())
    }
    InsightType.GOOD_SAVINGS -> stringResource(R.string.insight_savings_title)
    InsightType.LARGE_EXPENSE -> {
        val amount = MoneyFormat.format(insight.value.toLong(), currencyCode)
        stringResource(R.string.insight_large_expense, amount)
    }
    InsightType.STREAK -> stringResource(R.string.insights)
}

@Composable
private fun insightDescription(insight: Insight, currencyCode: String): String = when (insight.type) {
    InsightType.CATEGORY_SPIKE -> stringResource(R.string.insight_spike_desc)
    InsightType.GOOD_SAVINGS -> stringResource(
        R.string.insight_savings_desc,
        insight.value.toInt(),
    )
    InsightType.LARGE_EXPENSE -> stringResource(R.string.insight_large_desc)
    InsightType.STREAK -> ""
}
