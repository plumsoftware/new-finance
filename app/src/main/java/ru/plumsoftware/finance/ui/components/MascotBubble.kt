package ru.plumsoftware.finance.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import ru.plumsoftware.finance.ui.theme.Dimens

private val MascotBubbleMaxWidth = 260.dp

@Composable
fun MascotBubble(
    @DrawableRes mascotRes: Int,
    message: String,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(mascotRes),
            contentDescription = null,
            modifier = Modifier.size(Dimens.emojiPickerSize),
            contentScale = ContentScale.Fit,
        )
        Surface(
            modifier = Modifier.widthIn(max = MascotBubbleMaxWidth),
            shape = MaterialTheme.shapes.medium,
            color = colors.surface,
        ) {
            Text(
                text = message,
                modifier = Modifier.padding(Dimens.SpacingM),
                style = typography.bodyMedium,
                color = colors.onSurfaceVariant,
            )
        }
    }
}
