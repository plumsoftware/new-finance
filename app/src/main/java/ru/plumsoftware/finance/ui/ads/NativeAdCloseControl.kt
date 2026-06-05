package ru.plumsoftware.finance.ui.ads

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.ui.theme.IosBlue

@Composable
fun NativeAdCloseControl(
    isImageLayout: Boolean,
    closeEnabled: Boolean,
    progress: Float,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val circleColor = if (isImageLayout) {
        Color(0xE6FFFFFF)
    } else {
        Color(0xFFF2F2F7)
    }

    Box(
        modifier = modifier.size(NativeAdControlMetrics.size),
        contentAlignment = Alignment.Center,
    ) {
        if (!closeEnabled) {
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.matchParentSize(),
                color = IosBlue,
                trackColor = colors.onSurface.copy(alpha = 0.12f),
                strokeWidth = 2.dp,
            )
        }

        Box(
            modifier = Modifier
                .matchParentSize()
                .alpha(if (closeEnabled) 1f else 0.45f)
                .clip(CircleShape)
                .background(circleColor)
                .clickable(
                    enabled = closeEnabled,
                    onClick = onClose,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Close,
                contentDescription = stringResource(R.string.ad_close),
                modifier = Modifier.size(12.dp),
                tint = colors.onSurface.copy(alpha = if (closeEnabled) 0.7f else 0.35f),
            )
        }
    }
}
