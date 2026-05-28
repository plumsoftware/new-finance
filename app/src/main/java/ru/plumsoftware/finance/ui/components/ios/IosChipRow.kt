package ru.plumsoftware.finance.ui.components.ios

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.plumsoftware.finance.ui.theme.Dimens

@Composable
fun IosChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = if (selected) colors.secondary.copy(alpha = 0.14f) else colors.surface,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = Dimens.spacingRow + 4.dp, vertical = Dimens.spacingRow),
            style = typography.bodyMedium,
            color = if (selected) colors.secondary else colors.onSurfaceVariant,
        )
    }
}

@Composable
fun IosChipRow(
    items: List<String>,
    selected: String,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .padding(vertical = Dimens.paddingMicro),
        horizontalArrangement = Arrangement.spacedBy(Dimens.paddingSmall),
    ) {
        items.forEach { item ->
            IosChip(
                text = item,
                selected = item == selected,
                onClick = { onSelected(item) },
            )
        }
    }
}
