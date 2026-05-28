package ru.plumsoftware.finance.ui.components.ios

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import ru.plumsoftware.finance.ui.theme.Dimens

@Composable
fun IosThemePicker(
    labels: List<String>,
    selectedIndex: Int,
    onSelectIndex: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val safeIndex = selectedIndex.coerceIn(0, labels.lastIndex.coerceAtLeast(0))
    val trackColor = colors.surfaceVariant
    val shape = RoundedCornerShape(Dimens.cornerRadiusSegment)
    val innerShape = RoundedCornerShape(Dimens.cornerRadiusSegmentInner)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(Dimens.segmentedHeight)
            .clip(shape)
            .background(trackColor),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.paddingMicro / 2),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            labels.forEachIndexed { index, label ->
                val isSelected = index == safeIndex
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(Dimens.segmentedThumbHeight)
                        .clip(innerShape)
                        .background(
                            if (isSelected) colors.secondary.copy(alpha = 0.2f)
                            else androidx.compose.ui.graphics.Color.Transparent,
                        )
                        .clickable(
                            interactionSource = MutableInteractionSource(),
                            indication = null,
                        ) { onSelectIndex(index) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = label,
                        style = typography.bodyMedium,
                        color = if (isSelected) {
                            colors.secondary
                        } else {
                            colors.onSurfaceVariant
                        },
                    )
                }
            }
        }
    }
}
