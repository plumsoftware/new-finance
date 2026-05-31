package ru.plumsoftware.finance.presentation.permissions

import android.Manifest
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.presentation.common.buildPermissionsToRequest
import ru.plumsoftware.finance.presentation.common.isNotificationPermissionGranted
import ru.plumsoftware.finance.presentation.common.isStoragePermissionGranted
import ru.plumsoftware.finance.presentation.common.openAppSettings
import ru.plumsoftware.finance.ui.components.AppCard
import ru.plumsoftware.finance.ui.components.PermissionStatusRow
import ru.plumsoftware.finance.ui.components.PrimaryButton
import ru.plumsoftware.finance.ui.theme.Dimens

private data class PermissionRowState(val isGranted: Boolean = false)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionsBottomSheet(
    onDismiss: (dontShowAgain: Boolean) -> Unit,
) {
    val context = LocalContext.current
    val activity = context as Activity
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isLightTheme = !isSystemInDarkTheme()

    var notifState by remember {
        mutableStateOf(PermissionRowState(isGranted = isNotificationPermissionGranted(context)))
    }
    var storageState by remember {
        mutableStateOf(PermissionRowState(isGranted = isStoragePermissionGranted(context)))
    }
    var showSettingsHint by remember { mutableStateOf(false) }
    var dontShowAgain by remember { mutableStateOf(false) }

    fun refreshPermissionStates() {
        notifState = notifState.copy(isGranted = isNotificationPermissionGranted(context))
        storageState = storageState.copy(isGranted = isStoragePermissionGranted(context))
    }

    fun dismissSheet() {
        onDismiss(dontShowAgain)
    }

    LaunchedEffect(Unit) {
        refreshPermissionStates()
    }

    val multiplePermissionsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { results ->
        results.forEach { (permission, granted) ->
            when (permission) {
                Manifest.permission.POST_NOTIFICATIONS ->
                    notifState = notifState.copy(isGranted = granted)
                Manifest.permission.WRITE_EXTERNAL_STORAGE ->
                    storageState = storageState.copy(isGranted = granted)
            }
        }
        val anyPermanentlyDenied = results.any { (perm, granted) ->
            !granted && !ActivityCompat.shouldShowRequestPermissionRationale(activity, perm)
        }
        if (anyPermanentlyDenied) {
            showSettingsHint = true
        }
    }

    val permissionsToRequest = remember(notifState, storageState) {
        buildPermissionsToRequest(context)
    }
    val allGranted = notifState.isGranted && storageState.isGranted

    LaunchedEffect(allGranted) {
        if (allGranted) {
            dismissSheet()
        }
    }

    ModalBottomSheet(
        onDismissRequest = { dismissSheet() },
        sheetState = sheetState,
        containerColor = colors.surface,
        shape = RoundedCornerShape(
            topStart = Dimens.RadiusXl,
            topEnd = Dimens.RadiusXl,
        ),
        dragHandle = {},
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.illustrationHeight)
                    .background(colors.background),
            ) {
                Image(
                    painter = painterResource(R.drawable.mascot_permissions),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.Center),
                )

                if (isLightTheme) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .align(Alignment.BottomCenter)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        colors.surface,
                                    ),
                                ),
                            ),
                    )
                }
            }

            Text(
                text = stringResource(R.string.permissions_title),
                style = typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = Dimens.SpacingL),
            )
            Spacer(Modifier.height(Dimens.SpacingXs))
            Text(
                text = stringResource(R.string.permissions_subtitle),
                style = typography.bodyMedium,
                color = colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = Dimens.SpacingXxl),
            )

            Spacer(Modifier.height(Dimens.SpacingXl))

            AppCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.SpacingL),
            ) {
                PermissionStatusRow(
                    icon = Icons.Rounded.Notifications,
                    iconBg = Color(0xFF007AFF),
                    title = stringResource(R.string.perm_notif_title),
                    description = stringResource(R.string.perm_notif_desc),
                    isGranted = notifState.isGranted,
                )

                HorizontalDivider(
                    modifier = Modifier.padding(start = 72.dp),
                    color = colors.surfaceVariant,
                )

                PermissionStatusRow(
                    icon = Icons.Rounded.FolderOpen,
                    iconBg = Color(0xFFFF9500),
                    title = stringResource(R.string.perm_storage_title),
                    description = stringResource(R.string.perm_storage_desc),
                    isGranted = storageState.isGranted,
                )
            }

            AnimatedVisibility(
                visible = showSettingsHint,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimens.SpacingL)
                        .padding(top = Dimens.SpacingS)
                        .clip(RoundedCornerShape(Dimens.RadiusM))
                        .background(colors.error.copy(alpha = 0.08f))
                        .padding(Dimens.SpacingM),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        tint = colors.error,
                        modifier = Modifier.size(Dimens.IconSizeM),
                    )
                    Text(
                        text = stringResource(R.string.permissions_settings_hint),
                        style = typography.bodySmall,
                        color = colors.error,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = Dimens.SpacingS),
                    )
                    TextButton(
                        onClick = {
                            context.openAppSettings()
                            refreshPermissionStates()
                        },
                    ) {
                        Text(
                            text = stringResource(R.string.permissions_open_settings),
                            color = colors.error,
                            style = typography.labelMedium,
                        )
                    }
                }
            }

            Spacer(Modifier.height(Dimens.SpacingL))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .toggleable(
                        value = dontShowAgain,
                        role = Role.Checkbox,
                        onValueChange = { dontShowAgain = it },
                    )
                    .padding(horizontal = Dimens.SpacingL),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(
                    checked = dontShowAgain,
                    onCheckedChange = { dontShowAgain = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = colors.primary,
                    ),
                )
                Text(
                    text = stringResource(R.string.permissions_dont_show_again),
                    style = typography.bodyMedium,
                    color = colors.onSurface,
                    modifier = Modifier.padding(start = Dimens.SpacingXs),
                )
            }

            Spacer(Modifier.height(Dimens.SpacingM))

            PrimaryButton(
                text = stringResource(
                    when {
                        allGranted -> R.string.permissions_continue
                        showSettingsHint -> R.string.permissions_open_settings
                        else -> R.string.permissions_allow_all
                    },
                ),
                onClick = {
                    when {
                        allGranted -> dismissSheet()
                        showSettingsHint -> {
                            context.openAppSettings()
                            refreshPermissionStates()
                        }
                        permissionsToRequest.isNotEmpty() ->
                            multiplePermissionsLauncher.launch(permissionsToRequest.toTypedArray())
                        else -> dismissSheet()
                    }
                },
                modifier = Modifier.padding(horizontal = Dimens.SpacingL),
            )

            Spacer(Modifier.height(Dimens.SpacingS))

            TextButton(
                onClick = { dismissSheet() },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = stringResource(R.string.permissions_skip),
                    style = typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                )
            }

            Spacer(Modifier.height(Dimens.SpacingM))
        }
    }
}
