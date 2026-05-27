package ru.plumsoftware.finance.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.domain.model.Category
import ru.plumsoftware.finance.domain.model.ThemeMode
import ru.plumsoftware.finance.ui.components.IosCard
import ru.plumsoftware.finance.ui.components.ios.IosChip
import ru.plumsoftware.finance.ui.components.ios.IosSwitch
import ru.plumsoftware.finance.ui.components.ios.IosThemePicker
import ru.plumsoftware.finance.ui.components.ios.IosTopBar
import ru.plumsoftware.finance.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
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
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { IosTopBar(title = stringResource(R.string.settings_title)) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(
                horizontal = Dimens.paddingLarge,
                vertical = Dimens.paddingSmall,
            ),
            verticalArrangement = Arrangement.spacedBy(Dimens.paddingLarge),
        ) {
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
                Text(stringResource(R.string.settings_categories_expense), fontWeight = FontWeight.SemiBold)
            }
            items(state.expenseCategories, key = { "e${it.id}" }) { cat ->
                CategoryRow(cat) { viewModel.toggleCategoryHidden(cat.id, !cat.isHidden) }
            }
            item {
                Text(stringResource(R.string.settings_categories_income), fontWeight = FontWeight.SemiBold)
            }
            items(state.incomeCategories, key = { "i${it.id}" }) { cat ->
                CategoryRow(cat) { viewModel.toggleCategoryHidden(cat.id, !cat.isHidden) }
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
private fun CategoryRow(category: Category, onToggle: () -> Unit) {
    IosCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("${category.icon} ${category.name}")
            IosChip(
                text = if (category.isHidden) {
                    stringResource(R.string.category_hidden)
                } else {
                    stringResource(R.string.category_visible)
                },
                selected = !category.isHidden,
                onClick = onToggle,
            )
        }
    }
}
