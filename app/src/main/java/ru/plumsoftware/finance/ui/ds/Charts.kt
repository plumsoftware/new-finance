package ru.plumsoftware.finance.ui.ds

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import ru.plumsoftware.finance.ui.theme.FinanceTheme
import ru.plumsoftware.finance.ui.theme.FinanceType

/** Состояние дня в полосе месяца (§8.2). */
enum class DayState { NORMAL, OVER, TODAY, FUTURE }

/**
 * Полоса дней месяца (§6.1.2): N колонок, промежуток 3dp, высота 22dp, радиус 3dp.
 */
@Composable
fun DayStrip(
    days: List<DayState>,
    modifier: Modifier = Modifier,
    labels: Triple<String, String, String>? = null,
    summary: String? = null,
) {
    val c = FinanceTheme.colors
    Column(
        modifier
            .fillMaxWidth()
            .then(if (summary != null) Modifier.clearAndSetSemantics { contentDescription = summary } else Modifier),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(22.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            days.forEach { state ->
                val color = when (state) {
                    DayState.NORMAL -> c.success
                    DayState.OVER -> c.onInkWarning
                    DayState.TODAY -> Color.White
                    DayState.FUTURE -> Color.White.copy(alpha = 0.14f)
                }
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(color, RoundedCornerShape(3.dp)),
                )
            }
        }
        if (labels != null) {
            VSpace(6.dp)
            Row(Modifier.fillMaxWidth()) {
                Text(labels.first, style = FinanceType.micro, color = c.onInkSecondary)
                Text(
                    labels.second,
                    style = FinanceType.micro,
                    color = c.onInkSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
                Text(labels.third, style = FinanceType.micro, color = c.onInkSecondary)
            }
        }
    }
}

/** Тип столбца графика Аналитики (§6.4 п.4). */
enum class BarKind { DEFAULT, CURRENT, HIGH, EMPTY }

data class ChartBar(
    val value: Long,
    val kind: BarKind,
    val axisLabel: String?,
    val tooltip: String,
)

/**
 * Столбчатый график (§6.4): высота 120dp, промежуток 3dp, радиус 4dp. Тап — подсказка с датой и суммой.
 */
@Composable
fun BarChart(
    bars: List<ChartBar>,
    summary: String,
    modifier: Modifier = Modifier,
    height: Dp = 120.dp,
) {
    val c = FinanceTheme.colors
    var selected by remember(bars) { mutableStateOf<Int?>(null) }
    val max = bars.maxOfOrNull { it.value }?.takeIf { it > 0 } ?: 1L
    Column(modifier.fillMaxWidth().semantics { contentDescription = summary }) {
        Box(Modifier.fillMaxWidth().height(height + 28.dp)) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(height)
                    .align(Alignment.BottomCenter),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                bars.forEachIndexed { i, bar ->
                    val target = if (bar.value <= 0) 0.04f else (bar.value.toFloat() / max).coerceIn(0.04f, 1f)
                    val h by animateFloatAsState(target, tween(350), label = "bar")
                    val color = when (bar.kind) {
                        BarKind.CURRENT -> c.primary
                        BarKind.HIGH -> c.warning
                        BarKind.EMPTY -> c.trackMuted
                        BarKind.DEFAULT -> c.barDefault
                    }
                    Box(
                        Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                            ) { selected = if (selected == i) null else i },
                        contentAlignment = Alignment.BottomCenter,
                    ) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(h)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (selected == null || selected == i) color else color.copy(alpha = 0.5f)),
                        )
                    }
                }
            }
            selected?.let { i ->
                val frac = (i + 0.5f) / bars.size
                Box(
                    Modifier
                        .zIndex(1f)
                        .fillMaxWidth()
                        .align(Alignment.TopStart),
                ) {
                    Text(
                        text = bars[i].tooltip,
                        style = FinanceType.captionBold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .align(
                                when {
                                    frac < 0.25f -> Alignment.TopStart
                                    frac > 0.75f -> Alignment.TopEnd
                                    else -> Alignment.TopCenter
                                },
                            )
                            .background(c.toastBg, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }
        }
        VSpace(6.dp)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            bars.forEach { bar ->
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    if (bar.axisLabel != null) {
                        Text(
                            bar.axisLabel,
                            style = FinanceType.axis,
                            color = c.textSecondary,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Visible,
                        )
                    }
                }
            }
        }
    }
}

/** Легенда: цветной квадрат + подпись. */
@Composable
fun LegendDot(color: Color, label: String, modifier: Modifier = Modifier, textColor: Color = FinanceTheme.colors.textSecondary) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).background(color, RoundedCornerShape(2.dp)))
        HSpace(6.dp)
        Text(label, style = FinanceType.caption, color = textColor)
    }
}
