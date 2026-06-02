package ru.plumsoftware.finance.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.plumsoftware.finance.ui.theme.MascotSize
import ru.plumsoftware.finance.ui.theme.Dimens

@Composable
fun MascotEmptyState(
    @DrawableRes mascotRes: Int,
    title: String,
    subtitle: String,
    mascotPhrase: String? = null,
    modifier: Modifier = Modifier,
    mascotSize: Dp = MascotSize.Large,
    verticalPadding: Dp = 40.dp,
    action: (@Composable () -> Unit)? = null,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.SpacingXxl, vertical = verticalPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(mascotRes),
            contentDescription = null,
            modifier = Modifier.size(mascotSize),
            contentScale = ContentScale.Fit,
        )
        if (mascotPhrase != null) {
            Spacer(Modifier.height(Dimens.SpacingXs))
            Box(
                modifier = Modifier
                    .background(colors.surface, RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = Dimens.SpacingXs),
            ) {
                Text(
                    text = mascotPhrase,
                    style = typography.bodySmall,
                    color = colors.onSurface.copy(alpha = 0.55f),
                    fontStyle = FontStyle.Italic,
                    textAlign = TextAlign.Center,
                )
            }
        }
        Spacer(Modifier.height(Dimens.SpacingM))
        Text(
            text = title,
            style = typography.titleLarge,
            color = colors.onBackground,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(Dimens.SpacingXxs))
        Text(
            text = subtitle,
            style = typography.bodyMedium,
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (action != null) {
            Spacer(Modifier.height(Dimens.SpacingXl))
            action()
        }
    }
}
