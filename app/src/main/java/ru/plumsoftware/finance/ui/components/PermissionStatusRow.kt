package ru.plumsoftware.finance.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.plumsoftware.finance.ui.theme.Dimens

@Composable
fun PermissionStatusRow(
    icon: ImageVector,
    iconBg: Color,
    title: String,
    description: String,
    isGranted: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(Dimens.SpacingM),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(Dimens.RadiusM))
                .background(iconBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(Dimens.IconSizeM),
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = Dimens.SpacingM),
        ) {
            Text(
                text = title,
                style = typography.bodyLarge,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = description,
                style = typography.bodySmall,
                color = colors.onSurfaceVariant,
            )
        }

        AnimatedContent(
            targetState = isGranted,
            transitionSpec = {
                scaleIn(initialScale = 0.7f) + fadeIn() togetherWith
                    scaleOut(targetScale = 0.7f) + fadeOut()
            },
            label = "perm_check",
        ) { granted ->
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(
                        if (granted) {
                            colors.secondary.copy(alpha = 0.12f)
                        } else {
                            colors.surfaceVariant
                        },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (granted) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        tint = colors.secondary,
                        modifier = Modifier.size(16.dp),
                    )
                } else {
                    Box(
                        Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(colors.onSurfaceVariant.copy(alpha = 0.4f)),
                    )
                }
            }
        }
    }
}
