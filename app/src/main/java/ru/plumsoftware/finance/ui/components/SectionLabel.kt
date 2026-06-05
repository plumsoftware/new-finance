package ru.plumsoftware.finance.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.plumsoftware.finance.ui.theme.Dimens

@Composable
fun SectionLabel(
    text: String,
    modifier: Modifier = Modifier,
    withBottomSpacing: Boolean = true,
) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = 0.5.sp,
        modifier = modifier.padding(
            start = Dimens.SpacingL,
            bottom = if (withBottomSpacing) Dimens.SpacingXs else 0.dp,
        ),
    )
}
