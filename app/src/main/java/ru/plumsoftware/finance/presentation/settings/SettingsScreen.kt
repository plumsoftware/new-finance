package ru.plumsoftware.finance.presentation.settings

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.domain.model.ThemeMode
import ru.plumsoftware.finance.ui.components.IosCard
import ru.plumsoftware.finance.ui.components.ios.IosSwitch
import ru.plumsoftware.finance.ui.components.ios.IosThemePicker
import ru.plumsoftware.finance.ui.theme.Dimens

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onOpenCategories: () -> Unit = {},
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val themeLabels = listOf(
        stringResource(R.string.theme_system),
        stringResource(R.string.theme_light),
        stringResource(R.string.theme_dark),
    )
    val themeIndexFromState = when (state.themeMode) {
        ThemeMode.SYSTEM -> 0
        ThemeMode.LIGHT -> 1
        ThemeMode.DARK -> 2
    }
    var selectedThemeIndex by rememberSaveable { mutableIntStateOf(themeIndexFromState) }
    LaunchedEffect(state.themeMode) {
        selectedThemeIndex = themeIndexFromState
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = colors.background,
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = Dimens.paddingLarge,
                end = Dimens.paddingLarge,
                top = Dimens.statusBarInset,
                bottom = Dimens.paddingMicro,
            ),
            verticalArrangement = Arrangement.spacedBy(Dimens.paddingMedium),
        ) {
            item {
                Text(
                    text = stringResource(R.string.settings_title),
                    style = typography.headlineLarge,
                )
            }
            item(key = "theme_$selectedThemeIndex") {
                Text(
                    text = stringResource(R.string.settings_theme),
                    style = typography.titleMedium,
                )
                Spacer(modifier = Modifier.height(Dimens.paddingSmall))
                IosThemePicker(
                    labels = themeLabels,
                    selectedIndex = selectedThemeIndex,
                    onSelectIndex = { index ->
                        selectedThemeIndex = index
                        val mode = when (index) {
                            1 -> ThemeMode.LIGHT
                            2 -> ThemeMode.DARK
                            else -> ThemeMode.SYSTEM
                        }
                        viewModel.setThemeMode(mode)
                    },
                )
            }
            item {
                Text(
                    text = stringResource(R.string.settings_data_section),
                    style = typography.labelSmall,
                    color = colors.onSurfaceVariant,
                )
                Spacer(Modifier.height(Dimens.paddingSmall))
                IosCard {
                    SettingsRow(
                        icon = Icons.Outlined.Category,
                        label = stringResource(R.string.settings_categories),
                        onClick = onOpenCategories,
                    )
                }
            }
            if (state.biometricAvailable) {
                item {
                    IosCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column {
                                Text(
                                    text = stringResource(R.string.settings_biometric),
                                    style = typography.bodyLarge,
                                )
                                Text(
                                    text = stringResource(R.string.settings_biometric_hint),
                                    style = typography.bodySmall,
                                    color = colors.onSurfaceVariant,
                                )
                            }
                            IosSwitch(
                                checked = state.biometricEnabled,
                                onCheckedChange = viewModel::setBiometric,
                            )
                        }
                    }
                }
            }
            item {
                Text(
                    text = stringResource(R.string.settings_local_note),
                    style = typography.bodySmall,
                    color = colors.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun SettingsRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = Dimens.paddingMedium, vertical = Dimens.spacingList + Dimens.paddingMicro),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.onSurfaceVariant,
                modifier = Modifier
                    .size(Dimens.iconSizeStandard)
                    .padding(end = Dimens.paddingSmall),
            )
            Text(
                text = label,
                style = typography.bodyLarge,
                color = colors.onSurface,
            )
        }
        Icon(
            Icons.Outlined.KeyboardArrowRight,
            contentDescription = null,
            tint = colors.outlineVariant,
        )
    }
}
