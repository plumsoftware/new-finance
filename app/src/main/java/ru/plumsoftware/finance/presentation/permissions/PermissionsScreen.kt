package ru.plumsoftware.finance.presentation.permissions

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import org.koin.androidx.compose.koinViewModel
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.domain.model.PermissionItem
import ru.plumsoftware.finance.ui.components.AppCard
import ru.plumsoftware.finance.ui.components.SectionLabel
import ru.plumsoftware.finance.ui.components.ios.IosEditorTopBar
import ru.plumsoftware.finance.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionsScreen(
    navController: NavController,
    viewModel: PermissionsViewModel = koinViewModel(),
) {
    val items by viewModel.items.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = context as Activity
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refresh(activity)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(Unit) {
        viewModel.refresh(activity)
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) {
        viewModel.refresh(activity)
    }

    val denied = items.filter { !it.isGranted }
    val granted = items.filter { it.isGranted }

    Scaffold(
        topBar = {
            IosEditorTopBar(
                title = stringResource(R.string.permissions),
                backLabel = stringResource(R.string.categories_back_settings),
                onBack = navController::popBackStack,
            )
        },
        containerColor = colors.background,
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding() + Dimens.SpacingM,
                bottom = Dimens.SpacingXxl,
                start = Dimens.SpacingL,
                end = Dimens.SpacingL,
            ),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingXs),
        ) {
            if (denied.isEmpty()) {
                item { AllGrantedBanner() }
            }

            if (denied.isNotEmpty()) {
                item {
                    SectionLabel(text = stringResource(R.string.perm_section_required))
                }
                item {
                    AppCard(modifier = Modifier.fillMaxWidth()) {
                        denied.forEachIndexed { index, item ->
                            PermissionScreenRow(
                                item = item,
                                onRequest = { requestPermission(item, launcher) },
                            )
                            if (index < denied.lastIndex) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(start = 72.dp),
                                    color = colors.surfaceVariant,
                                )
                            }
                        }
                    }
                }

                if (denied.any { it.isPermanentlyDenied }) {
                    item {
                        Row(
                            modifier = Modifier.padding(
                                start = Dimens.SpacingM,
                                top = Dimens.SpacingXxs,
                            ),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = null,
                                tint = colors.onSurfaceVariant,
                                modifier = Modifier.size(14.dp),
                            )
                            Spacer(Modifier.width(Dimens.SpacingXxs))
                            Text(
                                text = stringResource(R.string.perm_permanently_denied_hint),
                                style = typography.bodySmall,
                                color = colors.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            if (granted.isNotEmpty()) {
                item {
                    SectionLabel(
                        text = stringResource(R.string.perm_section_granted),
                        modifier = Modifier.padding(
                            top = if (denied.isNotEmpty()) Dimens.SpacingL else Dimens.SpacingXxs,
                        ),
                    )
                }
                item {
                    AppCard(modifier = Modifier.fillMaxWidth()) {
                        granted.forEachIndexed { index, item ->
                            PermissionScreenRow(
                                item = item,
                                onRequest = {},
                            )
                            if (index < granted.lastIndex) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(start = 72.dp),
                                    color = colors.surfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun requestPermission(
    item: PermissionItem,
    launcher: androidx.activity.compose.ManagedActivityResultLauncher<String, Boolean>,
) {
    val permission = item.permission ?: return
    launcher.launch(permission)
}
