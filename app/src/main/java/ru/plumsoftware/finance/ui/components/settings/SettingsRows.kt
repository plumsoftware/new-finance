package ru.plumsoftware.finance.ui.components.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import ru.plumsoftware.finance.ui.components.ios.IosSwitch
import ru.plumsoftware.finance.ui.theme.Dimens

@Composable
fun SettingsRow(
    icon: ImageVector,
    iconBackground: Color,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    trailing: @Composable () -> Unit = {},
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val clickableModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else {
        Modifier
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = Dimens.RowHeight)
            .then(clickableModifier)
            .padding(horizontal = Dimens.SpacingM, vertical = Dimens.SpacingXs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(Dimens.RadiusS))
                .background(iconBackground),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(Dimens.IconSizeS),
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = Dimens.SpacingM),
        ) {
            Text(
                text = title,
                style = typography.bodyLarge,
                color = colors.onSurface,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = typography.bodySmall,
                    color = colors.onSurfaceVariant,
                )
            }
        }
        trailing()
    }
}

@Composable
fun SettingsNavRow(
    icon: ImageVector,
    iconBackground: Color,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    trailing: @Composable () -> Unit = {
        val colors = MaterialTheme.colorScheme
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
            contentDescription = null,
            tint = colors.onSurfaceVariant,
            modifier = Modifier.size(Dimens.IconSizeS),
        )
    },
) {
    SettingsRow(
        icon = icon,
        iconBackground = iconBackground,
        title = title,
        subtitle = subtitle,
        modifier = modifier,
        onClick = onClick,
        trailing = trailing,
    )
}

@Composable
fun SettingsToggleRow(
    icon: ImageVector,
    iconBackground: Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    SettingsRow(
        icon = icon,
        iconBackground = iconBackground,
        title = title,
        subtitle = subtitle,
        modifier = modifier.heightIn(min = 60.dp),
        trailing = {
            IosSwitch(
                checked = checked,
                onCheckedChange = onToggle,
                enabled = enabled,
            )
        },
    )
}

@Composable
fun SettingsRowDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier.padding(start = 52.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        thickness = 1.dp,
    )
}
