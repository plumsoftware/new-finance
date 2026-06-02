package ru.plumsoftware.finance.ui.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import ru.plumsoftware.finance.ui.theme.MascotEmotion
import ru.plumsoftware.finance.ui.theme.toDrawableRes

@Composable
fun MascotImage(
    emotion: MascotEmotion,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    Image(
        painter = painterResource(emotion.toDrawableRes()),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = ContentScale.Fit,
    )
}
