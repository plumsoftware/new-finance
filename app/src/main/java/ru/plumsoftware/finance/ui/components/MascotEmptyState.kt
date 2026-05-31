package ru.plumsoftware.finance.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import ru.plumsoftware.finance.ui.theme.Dimens

/**
 * Пустое состояние с маскотом — использовать редко: один раз на экран / секцию.
 */
@Composable
fun MascotEmptyState(
    @DrawableRes mascotRes: Int,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Dimens.SpacingXxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimens.SpacingM),
    ) {
        Image(
            painter = painterResource(mascotRes),
            contentDescription = null,
            modifier = Modifier.size(Dimens.mascotEmptyState),
            contentScale = ContentScale.Fit,
        )
        Text(
            text = title,
            style = typography.titleMedium,
            color = colors.onBackground,
            textAlign = TextAlign.Center,
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = typography.bodyMedium,
                color = colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = Dimens.SpacingXl),
            )
        }
    }
}
