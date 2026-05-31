package ru.plumsoftware.finance.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ru.plumsoftware.finance.ui.theme.Dimens

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
    val typography = MaterialTheme.typography
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpacingXs),
    ) {
        keys.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingXs),
            ) {
                row.forEach { key ->
                    val cellModifier = Modifier
                        .weight(1f)
                        .height(Dimens.numPadKeyHeight)
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
                                    )
                                }
                            } else {
                                NumPadKey(
                                    modifier = cellModifier,
                                    onClick = { onDigit(key) },
                                ) {
                                    Text(
                                        text = key,
                                        style = typography.titleLarge,
                                        color = colors.onSurface,
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
                            )
                        }
                        else -> NumPadKey(
                            modifier = cellModifier,
                            onClick = { onDigit(key) },
                        ) {
                            Text(
                                text = key,
                                style = typography.titleLarge,
                                color = colors.onSurface,
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
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(Dimens.RadiusS),
        color = colors.surface,
        shadowElevation = Dimens.elevationNumPad,
        content = {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                content()
            }
        },
    )
}
