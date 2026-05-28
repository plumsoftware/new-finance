package ru.plumsoftware.finance.presentation.settings

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import org.koin.androidx.compose.koinViewModel
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.domain.model.ThemeMode
import ru.plumsoftware.finance.ui.components.IosCard
import ru.plumsoftware.finance.ui.components.ios.IosSwitch
import ru.plumsoftware.finance.ui.components.ios.IosThemePicker
import ru.plumsoftware.finance.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onOpenCategories: () -> Unit = {},
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
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
        containerColor = Color(0xFFF2F2F7),
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(
                start = Dimens.paddingLarge,
                end = Dimens.paddingLarge,
                top = 20.dp,
                bottom = 0.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(Dimens.paddingLarge),
        ) {
            item {
                Text(
                    text = stringResource(R.string.settings_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
            item(key = "theme_$selectedThemeIndex") {
                Text(stringResource(R.string.settings_theme), fontWeight = FontWeight.SemiBold)
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
                    text = "ДАННЫЕ",
                    color = Color(0xFF8E8E93),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp,
                )
                Spacer(Modifier.height(8.dp))
                IosCard {
                    SettingsRow(
                        icon = Icons.Outlined.Category,
                        label = "Категории",
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
                                Text(stringResource(R.string.settings_biometric))
                                Text(
                                    stringResource(R.string.settings_biometric_hint),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
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
                    stringResource(R.string.settings_local_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF8E8E93),
                modifier = Modifier
                    .size(24.dp)
                    .padding(end = 8.dp),
            )
            Text(label, fontSize = 17.sp, color = Color.Black)
        }
        Icon(Icons.Outlined.KeyboardArrowRight, contentDescription = null, tint = Color(0xFFC7C7CC))
    }
}
