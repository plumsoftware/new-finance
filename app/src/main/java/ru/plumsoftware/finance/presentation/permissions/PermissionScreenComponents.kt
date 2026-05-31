package ru.plumsoftware.finance.presentation.permissions

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.domain.model.PermissionIconKind
import ru.plumsoftware.finance.domain.model.PermissionItem
import ru.plumsoftware.finance.ui.components.AppCard
import ru.plumsoftware.finance.ui.theme.Dimens

@Composable
fun PermissionScreenRow(
    item: PermissionItem,
    onRequest: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val icon = item.iconKind.toImageVector()
    val iconColor = Color(item.iconColorArgb)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Dimens.SpacingM),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(Dimens.RadiusM))
                    .background(iconColor),
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
                    text = stringResource(item.titleRes),
                    style = typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = stringResource(item.descriptionRes),
                    style = typography.bodySmall,
                    color = colors.onSurfaceVariant,
                )
            }

            if (item.isGranted) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(colors.secondary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        tint = colors.secondary,
                        modifier = Modifier.size(16.dp),
                    )
                }
            } else {
                OutlinedButton(
                    onClick = onRequest,
                    modifier = Modifier.height(32.dp),
                    shape = RoundedCornerShape(Dimens.RadiusPill),
                    contentPadding = PaddingValues(
                        horizontal = Dimens.SpacingM,
                        vertical = 0.dp,
                    ),
                    border = BorderStroke(Dimens.borderThin, colors.primary),
                ) {
                    Text(
                        text = stringResource(R.string.perm_allow),
                        style = typography.labelMedium,
                        color = colors.primary,
                    )
                }
            }
        }

        AnimatedVisibility(visible = !item.isGranted) {
            Text(
                text = stringResource(item.rationaleRes),
                style = typography.bodySmall,
                color = colors.onSurfaceVariant,
                modifier = Modifier.padding(
                    start = 44.dp + Dimens.SpacingM,
                    top = Dimens.SpacingXxs,
                ),
            )
        }
    }
}

@Composable
fun AllGrantedBanner() {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    AppCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(Dimens.SpacingM),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(colors.secondary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.VerifiedUser,
                    contentDescription = null,
                    tint = colors.secondary,
                    modifier = Modifier.size(Dimens.IconSizeM),
                )
            }
            Column(modifier = Modifier.padding(start = Dimens.SpacingM)) {
                Text(
                    text = stringResource(R.string.perm_all_granted_title),
                    style = typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = stringResource(R.string.perm_all_granted_desc),
                    style = typography.bodySmall,
                    color = colors.onSurfaceVariant,
                )
            }
        }
    }
}

private fun PermissionIconKind.toImageVector(): ImageVector = when (this) {
    PermissionIconKind.NOTIFICATIONS -> Icons.Rounded.Notifications
    PermissionIconKind.STORAGE -> Icons.Rounded.FolderOpen
}
