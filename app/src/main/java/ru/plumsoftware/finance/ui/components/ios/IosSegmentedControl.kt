package ru.plumsoftware.finance.ui.components.ios

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import ru.plumsoftware.finance.ui.theme.Dimens

@Composable
fun IosSegmentedControl(
    labels: List<String>,
    selectedIndex: Int,
    onSelectIndex: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (labels.isEmpty()) return
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val safeIndex = selectedIndex.coerceIn(0, labels.lastIndex.coerceAtLeast(0))
    val animatedIndex by animateFloatAsState(
        targetValue = safeIndex.toFloat(),
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "ios_segmented_offset",
    )
    val density = LocalDensity.current
    val shape = RoundedCornerShape(Dimens.RadiusS)
    val activeShape = RoundedCornerShape(Dimens.SpacingXs)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(Dimens.segmentedHeight)
            .clip(shape)
            .background(colors.outline),
    ) {
        val innerWidth = maxWidth - Dimens.SpacingXxs
        val segmentWidth = innerWidth / labels.size
        val indicatorOffsetPx = with(density) { (segmentWidth * animatedIndex).toPx() }

        Box(
            modifier = Modifier
                .padding(Dimens.SpacingXxs / 2)
                .offset { IntOffset(indicatorOffsetPx.roundToInt(), 0) }
                .width(segmentWidth)
                .height(Dimens.segmentedThumbHeight)
                .shadow(elevation = Dimens.SpacingXxs / 2, shape = activeShape, clip = false)
                .clip(activeShape)
                .background(colors.surface),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.SpacingXxs / 2),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            labels.forEachIndexed { index, label ->
                val isSelected = index == safeIndex
                val interactionSource = MutableInteractionSource()
                val isPressed by interactionSource.collectIsPressedAsState()
                val scale by animateFloatAsState(
                    targetValue = if (!isSelected && isPressed) 0.96f else 1f,
                    animationSpec = tween(durationMillis = 120),
                    label = "ios_segmented_scale_$index",
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(Dimens.segmentedThumbHeight)
                        .scale(scale)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                        ) { onSelectIndex(index) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = label,
                        style = if (isSelected) typography.labelMedium else typography.bodyMedium,
                        color = if (isSelected) colors.onSurface else colors.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
