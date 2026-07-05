package ru.plumsoftware.finance.presentation.analytics

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.patrykandpatrick.vico.compose.pie.PieSize.Outer.Companion.Fill
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.domain.model.CategorySpending
import ru.plumsoftware.finance.ui.theme.Dimens
import ru.plumsoftware.finance.ui.theme.IosGreen
import ru.plumsoftware.finance.ui.theme.IosOrange
import ru.plumsoftware.finance.ui.theme.IosPurple
import ru.plumsoftware.finance.ui.theme.IosRed
import ru.plumsoftware.finance.ui.theme.IosViolet

// ── Donut segment data ────────────────────────────────────────────────────────

internal data class DonutSegment(
    val color: Color,
    val fraction: Float,
)

// ── SavingsRateRing ───────────────────────────────────────────────────────────
// Redesigned: thinner arc, animated fill, cleaner typography

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

    // Animate fill on entry
    val animatedRate = remember { Animatable(0f) }
    LaunchedEffect(savingsRate) {
        animatedRate.animateTo(
            targetValue = savingsRate.coerceIn(0f, 1f),
            animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        )
    }

    Box(
        modifier = modifier.size(130.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(130.dp)) {
            val stroke = 11.dp.toPx()
            val diameter = size.minDimension - stroke * 1.2f
            val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
            val arcSize = Size(diameter, diameter)

            // Track
            drawArc(
                color = colors.surfaceVariant,
                startAngle = ringStartAngle,
                sweepAngle = ringSweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
            // Fill
            if (animatedRate.value > 0f) {
                drawArc(
                    color = colors.secondary,
                    startAngle = ringStartAngle,
                    sweepAngle = ringSweep * animatedRate.value,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round),
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "$savingsPercent%",
                style = typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface,
            )
            Text(
                text = stringResource(R.string.saved),
                style = typography.labelSmall,
                color = colors.onSurfaceVariant,
                fontSize = 11.sp,
            )
        }
    }
}

// ── LegendItem ────────────────────────────────────────────────────────────────

@Composable
internal fun LegendItem(
    color: Color,
    label: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
        )
    }
}

// ── CategoryLegendRow ─────────────────────────────────────────────────────────

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
            .padding(vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(9.dp)
                .clip(CircleShape)
                .background(color),
        )
        Text(
            text = name,
            style = typography.bodySmall,
            color = colors.onSurface,
            fontWeight = FontWeight.Medium,
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
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(color.copy(alpha = 0.12f))
                .padding(horizontal = 6.dp, vertical = 2.dp),
        ) {
            Text(
                text = percent,
                style = typography.labelSmall,
                color = color,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
            )
        }
    }
}

// ── CategoryDonutChart ────────────────────────────────────────────────────────
// Redesigned: thinner stroke, gaps between segments, animated draw

@Composable
internal fun CategoryDonutChart(
    segments: List<DonutSegment>,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme

    val animatedProgress = remember { Animatable(0f) }
    LaunchedEffect(segments) {
        animatedProgress.snapTo(0f)
        animatedProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        )
    }

    Box(
        modifier = modifier.size(160.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(160.dp)) {
            val strokeWidth = 22.dp.toPx()
            val gapAngle = 2f // degrees gap between segments
            val diameter = size.minDimension - strokeWidth * 1.1f
            val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
            val arcSize = Size(diameter, diameter)

            if (segments.isEmpty()) {
                drawArc(
                    color = colors.surfaceVariant,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
                )
                return@Canvas
            }

            val progress = animatedProgress.value
            var startAngle = -90f
            segments.forEach { segment ->
                val fullSweep = 360f * segment.fraction
                val sweep = (fullSweep - gapAngle).coerceAtLeast(0f) * progress
                if (sweep > 0f) {
                    drawArc(
                        color = segment.color,
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
                    )
                }
                startAngle += fullSweep
            }
        }
    }
}

// ── ExpenseColumnChart ────────────────────────────────────────────────────────
// Uses Vico — unchanged logic, improved column style

@Composable
internal fun ExpenseColumnChart(
    dailyBars: List<AnalyticsDailyBar>,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val modelProducer = remember { CartesianChartModelProducer() }
    val labels = remember(dailyBars) { dailyBars.map { it.label } }

    if (dailyBars.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(160.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.no_data),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant,
            )
        }
        return
    }

    LaunchedEffect(dailyBars) {
        val values = dailyBars.map { it.expenseMinor / 100.0 }
        if (values.isNotEmpty()) {
            modelProducer.runTransaction { columnSeries { series(values) } }
        }
    }

    val bottomFormatter = CartesianValueFormatter { _, value, _ ->
        labels.getOrNull(value.toInt()) ?: ""
    }
    val startFormatter = CartesianValueFormatter { _, value, _ ->
        if (value >= 1000) "${(value / 1000).toInt()}k₽" else "${value.toInt()}₽"
    }

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberColumnCartesianLayer(
                columnProvider = ColumnCartesianLayer.ColumnProvider.series(
                    rememberLineComponent(
                        fill = Fill(colors.error),
                        // Narrower columns — 10dp instead of 16dp
                        thickness = 10.dp,
                        shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp),
                    ),
                ),
            ),
            startAxis = VerticalAxis.rememberStart(valueFormatter = startFormatter),
            bottomAxis = HorizontalAxis.rememberBottom(valueFormatter = bottomFormatter),
        ),
        modelProducer = modelProducer,
        modifier = modifier
            .height(170.dp)
            .padding(Dimens.SpacingM),
        scrollState = rememberVicoScrollState(scrollEnabled = dailyBars.size > 7),
    )
}

// ── BalanceTrendLineChart ─────────────────────────────────────────────────────

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
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(140.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.no_data),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant,
            )
        }
        return
    }

    LaunchedEffect(trendValues) {
        if (trendValues.isNotEmpty()) {
            modelProducer.runTransaction { lineSeries { series(trendValues) } }
        }
    }

    val bottomFormatter = CartesianValueFormatter { _, value, _ ->
        labels.getOrNull(value.toInt()) ?: ""
    }
    val startFormatter = CartesianValueFormatter { _, value, _ ->
        if (value >= 1000) "${(value / 1000).toInt()}k₽" else "${value.toInt()}₽"
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
                                    listOf(
                                        colors.primary.copy(alpha = 0.25f),
                                        Color.Transparent,
                                    ),
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

// ── Helpers ───────────────────────────────────────────────────────────────────

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
