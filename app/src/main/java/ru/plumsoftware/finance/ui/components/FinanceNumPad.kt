package ru.plumsoftware.finance.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.lerp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import ru.plumsoftware.finance.ui.theme.Dimens
import ru.plumsoftware.finance.ui.theme.Inter28Family

private val keys = listOf(
    listOf("1", "2", "3"),
    listOf("4", "5", "6"),
    listOf("7", "8", "9"),
    listOf(".", "0", "⌫"),
)

@Composable
fun FinanceNumPad(
    onDigit: (String) -> Unit,
    onBackspace: () -> Unit,
    onCollapse: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val keyStyle = TextStyle(
        fontFamily = Inter28Family,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        color = colors.onSurface,
    )
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.background),
        verticalArrangement = Arrangement.spacedBy(Dimens.numPadGapVertical),
    ) {
        keys.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.paddingSmall),
            ) {
                row.forEach { key ->
                    val cellModifier = Modifier
                        .weight(1f)
                        .height(Dimens.numPadKeyHeightCompact)
                    when (key) {
                        "" -> Spacer(modifier = cellModifier)
                        "." -> {
                            if (onCollapse != null) {
                                NumPadKey(
                                    modifier = cellModifier,
                                    onClick = onCollapse,
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = colors.onSurfaceVariant,
                                        modifier = Modifier.size(Dimens.iconSizeSmall),
                                    )
                                }
                            } else {
                                NumPadKey(
                                    modifier = cellModifier,
                                    onClick = { onDigit(key) },
                                ) {
                                    Text(
                                        text = key,
                                        style = keyStyle.copy(color = colors.onSurfaceVariant),
                                    )
                                }
                            }
                        }
                        "⌫" -> NumPadKey(
                            modifier = cellModifier,
                            onClick = onBackspace,
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Backspace,
                                contentDescription = null,
                                tint = colors.onSurfaceVariant,
                                modifier = Modifier.size(Dimens.iconSizeSmall),
                            )
                        }
                        else -> NumPadKey(
                            modifier = cellModifier,
                            onClick = { onDigit(key) },
                        ) {
                            Text(
                                text = key,
                                style = keyStyle,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NumPadKey(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressProgress by animateFloatAsState(
        targetValue = if (isPressed) 1f else 0f,
        animationSpec = tween(durationMillis = 80),
        label = "finance_numpad_press",
    )
    val keyShape = RoundedCornerShape(Dimens.cornerRadiusChip)
    Box(
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(keyShape)
                .background(lerp(colors.surface, colors.outline, pressProgress))
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick,
                ),
            contentAlignment = Alignment.Center,
        ) {
            content()
        }
    }
}
