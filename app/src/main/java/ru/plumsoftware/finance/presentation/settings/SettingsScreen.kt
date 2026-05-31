package ru.plumsoftware.finance.presentation.settings

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.PieChart
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import ru.plumsoftware.finance.BuildConfig
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.presentation.common.BiometricHelper
import ru.plumsoftware.finance.ui.components.AppCard
import ru.plumsoftware.finance.ui.components.SectionLabel
import ru.plumsoftware.finance.ui.components.ThemeToggle
import ru.plumsoftware.finance.ui.components.settings.SettingsNavRow
import ru.plumsoftware.finance.ui.components.settings.SettingsRow
import ru.plumsoftware.finance.ui.components.settings.SettingsRowDivider
import ru.plumsoftware.finance.ui.components.settings.SettingsToggleRow
import ru.plumsoftware.finance.ui.theme.AccentBlue
import ru.plumsoftware.finance.ui.theme.Dimens
import ru.plumsoftware.finance.ui.theme.IncomeGreen
import ru.plumsoftware.finance.ui.theme.IosOrange
import ru.plumsoftware.finance.ui.theme.IosPurple
import ru.plumsoftware.finance.ui.theme.TextPrimaryL

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onOpenCategories: () -> Unit = {},
    onOpenLimits: () -> Unit = {},
    onOpenRecurring: () -> Unit = {},
    onOpenExport: () -> Unit = {},
    onOpenAbout: () -> Unit = {},
    onRateApp: () -> Unit = {},
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val biometricHelper = remember(activity) {
        activity?.let { BiometricHelper(it) }
    }
    var showBiometricUnavailableDialog by remember { mutableStateOf(false) }
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    if (showBiometricUnavailableDialog) {
        AlertDialog(
            onDismissRequest = { showBiometricUnavailableDialog = false },
            title = { Text(stringResource(R.string.biometric_unavailable_title)) },
            text = { Text(stringResource(R.string.biometric_unavailable_body)) },
            confirmButton = {
                TextButton(onClick = { showBiometricUnavailableDialog = false }) {
                    Text(stringResource(R.string.action_apply))
                }
            },
        )
    }

    val openStore = {
        onRateApp()
        runCatching {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=${context.packageName}"),
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = colors.background,
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = Dimens.statusBarInset,
                bottom = Dimens.SpacingL,
            ),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingM),
        ) {
            item {
                Text(
                    text = stringResource(R.string.settings),
                    style = typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface,
                    modifier = Modifier.padding(horizontal = Dimens.SpacingL),
                )
            }

            item {
                SectionLabel(text = stringResource(R.string.settings_appearance))
                AppCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimens.SpacingL),
                ) {
                    SettingsRow(
                        icon = Icons.Rounded.DarkMode,
                        iconBackground = Color(0xFF5856D6),
                        title = stringResource(R.string.theme),
                    )
                    ThemeToggle(
                        selected = state.themeMode,
                        onSelect = viewModel::setThemeMode,
                        modifier = Modifier.padding(
                            start = Dimens.SpacingL,
                            end = Dimens.SpacingL,
                            bottom = Dimens.SpacingM,
                        ),
                    )
                    SettingsRowDivider()
                    SettingsRow(
                        icon = Icons.Rounded.Language,
                        iconBackground = AccentBlue,
                        title = stringResource(R.string.currency),
                        trailing = {
                            Text(
                                text = currencyDisplayLabel(state.currencyCode),
                                style = typography.bodyLarge,
                                color = colors.primary,
                            )
                        },
                    )
                }
            }

            item {
                SectionLabel(text = stringResource(R.string.settings_data))
                AppCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimens.SpacingL),
                ) {
                    SettingsNavRow(
                        icon = Icons.Rounded.Category,
                        iconBackground = IosOrange,
                        title = stringResource(R.string.categories),
                        onClick = onOpenCategories,
                    )
                    SettingsRowDivider()
                    SettingsNavRow(
                        icon = Icons.Rounded.PieChart,
                        iconBackground = AccentBlue,
                        title = stringResource(R.string.limits),
                        onClick = onOpenLimits,
                    )
                    SettingsRowDivider()
                    SettingsNavRow(
                        icon = Icons.Rounded.Repeat,
                        iconBackground = IncomeGreen,
                        title = stringResource(R.string.recurring_transactions),
                        onClick = onOpenRecurring,
                    )
                    SettingsRowDivider()
                    SettingsNavRow(
                        icon = Icons.Rounded.FileDownload,
                        iconBackground = IosPurple,
                        title = stringResource(R.string.export_data),
                        onClick = onOpenExport,
                    )
                }
            }

            item {
                SectionLabel(text = stringResource(R.string.settings_security))
                AppCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimens.SpacingL),
                ) {
                    SettingsToggleRow(
                        icon = Icons.Rounded.Fingerprint,
                        iconBackground = TextPrimaryL,
                        title = stringResource(R.string.biometrics),
                        subtitle = stringResource(R.string.biometrics_desc),
                        checked = state.biometricEnabled,
                        onToggle = { enabled ->
                            val helper = biometricHelper
                            if (helper == null) return@SettingsToggleRow
                            if (enabled) {
                                if (helper.isAvailable()) {
                                    helper.authenticate(
                                        onSuccess = { viewModel.setBiometric(true) },
                                        onError = { /* ignore */ },
                                    )
                                } else {
                                    showBiometricUnavailableDialog = true
                                }
                            } else {
                                helper.authenticate(
                                    onSuccess = { viewModel.setBiometric(false) },
                                    onError = { /* ignore cancel */ },
                                )
                            }
                        },
                        enabled = biometricHelper?.isAvailable() != false,
                    )
                }
            }

            item {
                SectionLabel(text = stringResource(R.string.settings_app))
                AppCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimens.SpacingL),
                ) {
                    SettingsNavRow(
                        icon = Icons.Rounded.Star,
                        iconBackground = IosOrange,
                        title = stringResource(R.string.rate_app),
                        onClick = { openStore() },
                    )
                    SettingsRowDivider()
                    SettingsNavRow(
                        icon = Icons.Rounded.Info,
                        iconBackground = AccentBlue,
                        title = stringResource(R.string.about),
                        onClick = onOpenAbout,
                    )
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimens.SpacingL, vertical = Dimens.SpacingXl),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(Dimens.SpacingXs),
                ) {
                    Text(
                        text = stringResource(R.string.data_stored_locally),
                        style = typography.bodySmall,
                        color = colors.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = "v${BuildConfig.VERSION_NAME}",
                        style = typography.labelSmall,
                        color = colors.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

private fun currencyDisplayLabel(currencyCode: String): String = when (currencyCode.uppercase()) {
    "RUB" -> "₽"
    "USD" -> "$"
    "EUR" -> "€"
    "GBP" -> "£"
    else -> currencyCode
}
