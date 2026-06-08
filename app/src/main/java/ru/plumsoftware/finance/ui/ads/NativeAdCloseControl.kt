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

private val NativeAdCloseCircleImage = Color(0xE6FFFFFF)
private val NativeAdCloseCircleCompact = Color(0xFFF2F2F7)
private val NativeAdCloseIcon = Color(0xFF1C1C1E)
private val NativeAdCloseIconDisabled = Color(0x591C1C1E)
private val NativeAdCloseProgressTrack = Color(0x1F000000)

@Composable
fun NativeAdCloseControl(
    isImageLayout: Boolean,
    closeEnabled: Boolean,
    progress: Float,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val circleColor = if (isImageLayout) {
        NativeAdCloseCircleImage
    } else {
        NativeAdCloseCircleCompact
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
                trackColor = NativeAdCloseProgressTrack,
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
                tint = if (closeEnabled) NativeAdCloseIcon.copy(alpha = 0.7f) else NativeAdCloseIconDisabled,
            )
        }
    }
}
