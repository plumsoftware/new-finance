package ru.plumsoftware.finance.presentation.security

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.ui.theme.Dimens

@Composable
fun BiometricLockOverlay(
    onUnlockClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Rounded.Fingerprint,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = colors.primary,
            )
            Spacer(Modifier.height(Dimens.SpacingL))
            Text(
                text = stringResource(R.string.biometric_locked),
                style = typography.titleMedium,
                color = colors.onSurface,
            )
            Spacer(Modifier.height(Dimens.SpacingXs))
            Text(
                text = stringResource(R.string.biometric_tap_hint),
                style = typography.bodyMedium,
                color = colors.onSurfaceVariant,
            )
            Spacer(Modifier.height(Dimens.SpacingXl))
            OutlinedButton(onClick = onUnlockClick) {
                Icon(
                    imageVector = Icons.Rounded.Fingerprint,
                    contentDescription = null,
                    modifier = Modifier.size(Dimens.IconSizeM),
                )
                Spacer(Modifier.width(Dimens.SpacingXs))
                Text(stringResource(R.string.biometric_unlock))
            }
        }
    }
}
