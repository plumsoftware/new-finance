package ru.plumsoftware.finance.ui.ds

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.progressSemantics
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.plumsoftware.finance.ui.theme.FinanceTheme
import ru.plumsoftware.finance.ui.theme.FinanceType

// region Кнопки (§7)

@Composable
fun ButtonPrimary(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    height: Dp = 54.dp,
    color: Color = FinanceTheme.colors.primary,
) {
    Box(
        modifier = modifier
            .heightIn(min = height)
            .alpha(if (enabled) 1f else 0.4f)
            .clip(RoundedCornerShape(height / 2))
            .background(color)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = FinanceType.title.copy(fontSize = 16.sp),
            color = Color.White,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun ButtonTonal(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 50.dp,
    enabled: Boolean = true,
    compact: Boolean = false,
) {
    val c = FinanceTheme.colors
    Box(
        modifier = modifier
            .heightIn(min = if (compact) 36.dp else height)
            .alpha(if (enabled) 1f else 0.4f)
            .clip(RoundedCornerShape(50))
            .background(c.primaryTonalBg)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = if (compact) 14.dp else 20.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = if (compact) FinanceType.bodySmall.copy(fontWeight = FontWeight.SemiBold) else FinanceType.title,
            color = c.primaryTonalText,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun ButtonOutlined(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 50.dp,
    enabled: Boolean = true,
) {
    val c = FinanceTheme.colors
    Box(
        modifier = modifier
            .heightIn(min = height)
            .alpha(if (enabled) 1f else 0.4f)
            .clip(RoundedCornerShape(50))
            .border(1.dp, c.outline, RoundedCornerShape(50))
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = text, style = FinanceType.title, color = c.primaryTonalText, textAlign = TextAlign.Center)
    }
}

/** Текстовая кнопка (например, «Удалить цель»). */
@Composable
fun TextAction(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = FinanceTheme.colors.primary,
) {
    Box(
        modifier = modifier
            .heightIn(min = 44.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = text, style = FinanceType.title, color = color)
    }
}

// endregion

// region Сегмент-контролы (§7)

/** Тёмный сегмент: белая подложка, активный `ink` (Аналитика). */
@Composable
fun SegmentedInk(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = FinanceTheme.colors
    SegmentedBase(
        options = options,
        selectedIndex = selectedIndex,
        onSelect = onSelect,
        modifier = modifier,
        trackColor = c.surface,
        activeColor = c.ink,
        activeText = Color.White,
        inactiveText = c.textPrimary,
        outerRadius = 20.dp,
        innerRadius = 16.dp,
        height = 44.dp,
        shadow = false,
    )
}

/** Светлый сегмент: трек `#F2F3F7`/`#E3E4EA`, активный белый с тенью. */
@Composable
fun SegmentedLight(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    onBackground: Boolean = false,
    activeTextColor: Color = FinanceTheme.colors.textPrimary,
) {
    val c = FinanceTheme.colors
    SegmentedBase(
        options = options,
        selectedIndex = selectedIndex,
        onSelect = onSelect,
        modifier = modifier,
        trackColor = if (onBackground) c.segmentTrackOnBg else c.segmentTrackOnSurface,
        activeColor = c.surface,
        activeText = activeTextColor,
        inactiveText = c.textSecondary,
        outerRadius = 14.dp,
        innerRadius = 11.dp,
        height = 40.dp,
        shadow = true,
    )
}

@Composable
private fun SegmentedBase(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier,
    trackColor: Color,
    activeColor: Color,
    activeText: Color,
    inactiveText: Color,
    outerRadius: Dp,
    innerRadius: Dp,
    height: Dp,
    shadow: Boolean,
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(trackColor, RoundedCornerShape(outerRadius))
            .padding(4.dp),
    ) {
        val segW = maxWidth / options.size.coerceAtLeast(1)
        val offset by animateDpAsState(segW * selectedIndex, tween(200), label = "seg")
        Box(
            Modifier
                .offset(x = offset)
                .width(segW)
                .fillMaxHeight()
                .then(if (shadow) Modifier.shadow(1.5.dp, RoundedCornerShape(innerRadius)) else Modifier)
                .background(activeColor, RoundedCornerShape(innerRadius)),
        )
        Row(Modifier.fillMaxWidth().fillMaxHeight()) {
            options.forEachIndexed { i, label ->
                val selected = i == selectedIndex
                val color by animateColorAsState(if (selected) activeText else inactiveText, tween(200), label = "segc")
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(innerRadius))
                        .selectable(selected = selected, role = Role.Tab, onClick = { onSelect(i) }),
                    contentAlignment = Alignment.Center,
                ) {
                    FitText(
                        text = label,
                        style = FinanceType.bodySmall.copy(
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        ),
                        color = color,
                        modifier = Modifier.padding(horizontal = 4.dp),
                    )
                }
            }
        }
    }
}

// endregion

// region Чипы

/** Фильтр-чип (§7 `FilterChip`): 34–36dp, радиус 10. */
@Composable
fun FChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selectedBg: Color = FinanceTheme.colors.ink,
    selectedText: Color = Color.White,
    unselectedBg: Color = FinanceTheme.colors.surface,
    unselectedText: Color = FinanceTheme.colors.textPrimary,
    border: Color? = null,
    height: Dp = 36.dp,
    leading: (@Composable RowScope.() -> Unit)? = null,
    trailing: (@Composable RowScope.() -> Unit)? = null,
) {
    val shape = RoundedCornerShape(10.dp)
    Row(
        modifier = modifier
            .height(height)
            .clip(shape)
            .background(if (selected) selectedBg else unselectedBg, shape)
            .then(if (border != null && !selected) Modifier.border(1.dp, border, shape) else Modifier)
            .selectable(selected = selected, role = Role.Checkbox, onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        leading?.invoke(this)
        Text(
            text = text,
            style = FinanceType.bodySmall.copy(fontWeight = FontWeight.Medium),
            color = if (selected) selectedText else unselectedText,
            maxLines = 1,
        )
        trailing?.invoke(this)
    }
}

// endregion

// region Переключатель (§6.13)

@Composable
fun FSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val c = FinanceTheme.colors
    val track by animateColorAsState(if (checked) c.primary else c.switchTrackOff, tween(200), label = "sw")
    val thumbColor by animateColorAsState(if (checked) Color.White else c.switchThumbOff, tween(200), label = "swt")
    val thumbSize by animateDpAsState(if (checked) 24.dp else 16.dp, tween(200), label = "sws")
    val thumbX by animateDpAsState(if (checked) 52.dp - 4.dp - 24.dp else 8.dp, tween(200), label = "swx")
    Box(
        modifier = modifier
            .size(width = 52.dp, height = 32.dp)
            .alpha(if (enabled) 1f else 0.5f)
            .clip(RoundedCornerShape(16.dp))
            .background(track)
            .toggleable(value = checked, enabled = enabled, role = Role.Switch, onValueChange = onCheckedChange),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            Modifier
                .offset(x = thumbX)
                .size(thumbSize)
                .background(thumbColor, CircleShape),
        )
    }
}

// endregion

// region Прогресс (§7)

/** Линейный прогресс, радиус = height/2, анимация 300–400ms. */
@Composable
fun FProgressBar(
    progress: Float,
    color: Color,
    modifier: Modifier = Modifier,
    height: Dp = 6.dp,
    track: Color = FinanceTheme.colors.trackMuted,
) {
    val p by animateFloatAsState(progress.coerceIn(0f, 1f), tween(350), label = "pb")
    Box(
        modifier = modifier
            .progressSemantics(progress.coerceIn(0f, 1f))
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(height / 2))
            .background(track),
    ) {
        if (p > 0f) {
            Box(
                Modifier
                    .fillMaxWidth(p)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(height / 2))
                    .background(color),
            )
        }
    }
}

/** Кольцо прогресса: начало сверху, по часовой. */
@Composable
fun ProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 52.dp,
    stroke: Dp = 5.dp,
    color: Color = FinanceTheme.colors.primary,
    track: Color = FinanceTheme.colors.trackMuted,
    content: @Composable () -> Unit = {},
) {
    val p by animateFloatAsState(progress.coerceIn(0f, 1f), tween(400), label = "ring")
    Box(modifier = modifier.size(size).progressSemantics(progress.coerceIn(0f, 1f)), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(size)) {
            val s = stroke.toPx()
            val arcSize = Size(this.size.width - s, this.size.height - s)
            val topLeft = Offset(s / 2, s / 2)
            drawArc(track, 0f, 360f, false, topLeft, arcSize, style = Stroke(s))
            if (p > 0f) {
                drawArc(color, -90f, 360f * p, false, topLeft, arcSize, style = Stroke(s, cap = StrokeCap.Round))
            }
        }
        content()
    }
}

/** Составная полоса калькулятора: 12dp, радиус 6, промежуток 3dp. */
@Composable
fun StackedBar(
    parts: List<Pair<Float, Color>>,
    modifier: Modifier = Modifier,
    height: Dp = 12.dp,
) {
    val visible = parts.filter { it.first > 0f }
    val total = visible.sumOf { it.first.toDouble() }.toFloat().takeIf { it > 0f } ?: 1f
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(height),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        visible.forEach { (value, color) ->
            val w by animateFloatAsState(value / total, tween(300), label = "stack")
            Box(
                Modifier
                    .weight(w.coerceAtLeast(0.001f))
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(height / 2))
                    .background(color),
            )
        }
    }
}

// endregion
