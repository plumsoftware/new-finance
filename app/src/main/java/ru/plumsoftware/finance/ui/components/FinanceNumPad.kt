package ru.plumsoftware.finance.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
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
    modifier: Modifier = Modifier,
) {
    val typography = MaterialTheme.typography
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.paddingSmall),
    ) {
        keys.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.paddingSmall),
            ) {
                row.forEach { key ->
                    val cellModifier = Modifier
                        .weight(1f)
                        .height(Dimens.numPadKeyHeight)
                    when (key) {
                        "" -> Spacer(modifier = cellModifier)
                        "⌫" -> NumPadKey(
                            modifier = cellModifier,
                            onClick = onBackspace,
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Backspace, contentDescription = null)
                        }
                        else -> NumPadKey(
                            modifier = cellModifier,
                            onClick = { onDigit(key) },
                        ) {
                            Text(
                                text = key,
                                style = typography.titleLarge,
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
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surface,
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
