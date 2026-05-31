package ru.plumsoftware.finance.presentation.analytics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.columnSeries
import com.patrykandpatrick.vico.compose.cartesian.data.lineSeries
import com.patrykandpatrick.vico.compose.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.domain.model.CategorySpending
import ru.plumsoftware.finance.presentation.common.MoneyFormat
import ru.plumsoftware.finance.ui.theme.Dimens
import ru.plumsoftware.finance.ui.theme.IosOrange
import ru.plumsoftware.finance.ui.theme.IosPurple
import ru.plumsoftware.finance.ui.theme.IosViolet
import kotlin.math.roundToInt

internal data class DonutSegment(
    val color: Color,
    val fraction: Float,
)

@Composable
internal fun SavingsRateRing(
    savingsPercent: Int,
    savingsRate: Float,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val ringStartAngle = 150f
    val ringSweep = 240f

    Box(
        modifier = modifier.size(120.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(120.dp)) {
            val stroke = 10.dp.toPx()
            val diameter = size.minDimension - stroke
            val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
            val arcSize = Size(diameter, diameter)
            drawArc(
                color = colors.surfaceVariant,
                startAngle = ringStartAngle,
                sweepAngle = ringSweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
            drawArc(
                color = colors.secondary,
                startAngle = ringStartAngle,
                sweepAngle = ringSweep * savingsRate.coerceIn(0f, 1f),
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.percent_short, savingsPercent),
                style = typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface,
            )
            Text(
                text = stringResource(R.string.saved),
                style = typography.labelSmall,
                color = colors.onSurfaceVariant,
            )
        }
    }
}

@Composable
internal fun LegendItem(
    color: Color,
    label: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingXxs),
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
internal fun CategoryLegendRow(
    color: Color,
    name: String,
    amount: String,
    percent: String,
    modifier: Modifier = Modifier,
) {
    val typography = MaterialTheme.typography
    val colors = MaterialTheme.colorScheme

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Dimens.SpacingXs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color),
        )
        Text(
            text = name,
            style = typography.bodySmall,
            color = colors.onSurface,
            modifier = Modifier
                .weight(1f)
                .padding(start = Dimens.SpacingS),
            maxLines = 1,
        )
        Text(
            text = amount,
            style = typography.bodySmall,
            color = colors.onSurface,
            modifier = Modifier.padding(end = Dimens.SpacingS),
        )
        Text(
            text = percent,
            style = typography.bodySmall,
            color = colors.onSurfaceVariant,
        )
    }
}

@Composable
internal fun CategoryDonutChart(
    segments: List<DonutSegment>,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(140.dp)) {
            val stroke = 16.dp.toPx()
            val diameter = size.minDimension - stroke
            val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
            val arcSize = Size(diameter, diameter)
            var start = -90f
            segments.forEach { segment ->
                val sweep = 360f * segment.fraction
                if (sweep > 0f) {
                    drawArc(
                        color = segment.color,
                        startAngle = start,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = stroke, cap = StrokeCap.Butt),
                    )
                    start += sweep
                }
            }
            if (segments.isEmpty()) {
                drawArc(
                    color = colors.surfaceVariant,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Butt),
                )
            }
        }
    }
}

@Composable
internal fun ExpenseColumnChart(
    dailyBars: List<AnalyticsDailyBar>,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val modelProducer = remember { CartesianChartModelProducer() }
    val labels = remember(dailyBars) { dailyBars.map { it.label } }

    if (dailyBars.isEmpty()) {
        Text(
            text = stringResource(R.string.no_data),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurfaceVariant,
            modifier = modifier.padding(Dimens.SpacingM),
        )
        return
    }

    LaunchedEffect(dailyBars) {
        val values = dailyBars.map { it.expenseMinor / 100.0 }
        if (values.isNotEmpty()) {
            modelProducer.runTransaction {
                columnSeries { series(values) }
            }
        }
    }

    val bottomFormatter = CartesianValueFormatter { _, value, _ ->
        labels.getOrNull(value.toInt()) ?: ""
    }
    val startFormatter = CartesianValueFormatter { _, value, _ ->
        "${value.toInt()}₽"
    }

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberColumnCartesianLayer(
                columnProvider = ColumnCartesianLayer.ColumnProvider.series(
                    rememberLineComponent(
                        fill = Fill(colors.error),
                        thickness = 16.dp,
                        shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp),
                    ),
                ),
            ),
            startAxis = VerticalAxis.rememberStart(valueFormatter = startFormatter),
            bottomAxis = HorizontalAxis.rememberBottom(valueFormatter = bottomFormatter),
        ),
        modelProducer = modelProducer,
        modifier = modifier
            .height(180.dp)
            .padding(Dimens.SpacingM),
        scrollState = rememberVicoScrollState(scrollEnabled = dailyBars.size > 7),
    )
}

@Composable
internal fun BalanceTrendLineChart(
    dailyBars: List<AnalyticsDailyBar>,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val modelProducer = remember { CartesianChartModelProducer() }
    val labels = remember(dailyBars) { dailyBars.map { it.label } }
    val trendValues = remember(dailyBars) { buildBalanceTrendValues(dailyBars) }

    if (dailyBars.isEmpty()) {
        Text(
            text = stringResource(R.string.no_data),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurfaceVariant,
            modifier = modifier.padding(Dimens.SpacingM),
        )
        return
    }

    LaunchedEffect(trendValues) {
        if (trendValues.isNotEmpty()) {
            modelProducer.runTransaction {
                lineSeries { series(trendValues) }
            }
        }
    }

    val bottomFormatter = CartesianValueFormatter { _, value, _ ->
        labels.getOrNull(value.toInt()) ?: ""
    }
    val startFormatter = CartesianValueFormatter { _, value, _ ->
        "${value.toInt()}₽"
    }

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberLineCartesianLayer(
                lineProvider = LineCartesianLayer.LineProvider.series(
                    LineCartesianLayer.rememberLine(
                        fill = LineCartesianLayer.LineFill.single(Fill(colors.primary)),
                        areaFill = LineCartesianLayer.AreaFill.single(
                            Fill(
                                Brush.verticalGradient(
                                    listOf(colors.primary.copy(alpha = 0.3f), Color.Transparent),
                                ),
                            ),
                        ),
                    ),
                ),
            ),
            startAxis = VerticalAxis.rememberStart(valueFormatter = startFormatter),
            bottomAxis = HorizontalAxis.rememberBottom(valueFormatter = bottomFormatter),
        ),
        modelProducer = modelProducer,
        modifier = modifier
            .height(150.dp)
            .padding(Dimens.SpacingM),
        scrollState = rememberVicoScrollState(scrollEnabled = dailyBars.size > 7),
    )
}

internal fun buildBalanceTrendValues(dailyBars: List<AnalyticsDailyBar>): List<Double> {
    var cumulative = 0.0
    return dailyBars.map { bar ->
        cumulative += (bar.incomeMinor - bar.expenseMinor) / 100.0
        cumulative
    }
}

internal fun categoryDonutSegments(
    categories: List<CategorySpending>,
    schemeColors: androidx.compose.material3.ColorScheme,
): List<DonutSegment> {
    val palette = listOf(
        schemeColors.error,
        schemeColors.primary,
        schemeColors.secondary,
        IosPurple,
        IosOrange,
        IosViolet,
    )
    val total = categories.sumOf { it.amountMinor }.coerceAtLeast(1L)
    return categories.mapIndexed { index, item ->
        DonutSegment(
            color = item.category.colorArgb?.let { Color(it.toInt()) }
                ?: palette[index % palette.size],
            fraction = item.amountMinor.toFloat() / total.toFloat(),
        )
    }
}
