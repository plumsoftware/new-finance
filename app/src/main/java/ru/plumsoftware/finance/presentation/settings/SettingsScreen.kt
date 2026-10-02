package ru.plumsoftware.finance.presentation.settings

import android.app.TimePickerDialog
import android.text.format.DateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import org.koin.androidx.compose.koinViewModel
import ru.plumsoftware.finance.BuildConfig
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.data.report.ReportKind
import ru.plumsoftware.finance.domain.achievements.AchievementId
import ru.plumsoftware.finance.domain.analytics.DateRange
import ru.plumsoftware.finance.domain.model.ThemeMode
import ru.plumsoftware.finance.domain.util.SupportedCurrencies
import ru.plumsoftware.finance.presentation.common.BiometricHelper
import ru.plumsoftware.finance.presentation.common.DateFmt
import ru.plumsoftware.finance.presentation.common.Money
import ru.plumsoftware.finance.presentation.export.ReportExportSheet
import ru.plumsoftware.finance.presentation.importdata.ImportPickerSheet
import ru.plumsoftware.finance.ui.components.CurrencyPickerSheet
import ru.plumsoftware.finance.ui.ds.CardDivider
import ru.plumsoftware.finance.ui.ds.Kopi
import ru.plumsoftware.finance.ui.ds.KopiImage
import ru.plumsoftware.finance.ui.ds.RootBottomInset
import ru.plumsoftware.finance.ui.ds.SectionHeader
import ru.plumsoftware.finance.ui.ds.SegmentedLight
import ru.plumsoftware.finance.ui.ds.SettingsRow
import ru.plumsoftware.finance.ui.ds.VSpace
import ru.plumsoftware.finance.ui.theme.FinanceTheme
import ru.plumsoftware.finance.ui.theme.FinanceType
import ru.plumsoftware.finance.util.openStoreListing
import java.time.LocalDate
import java.time.LocalTime

/** Цвета плиток настроек (§3.6). */
private object Tiles {
    val Theme = Color(0xFF5856D6)
    val Currency = Color(0xFF007AFF)
    val Accounts = Color(0xFF007AFF)
    val Categories = Color(0xFFFF9500)
    val Limits = Color(0xFF0A84FF)
    val Goals = Color(0xFF5856D6)
    val Achievements = Color(0xFFE0A100)
    val Recurring = Color(0xFF34C759)
    val Export = Color(0xFF5E5CE6)
    val Import = Color(0xFF5E5CE6)
    val Bell = Color(0xFFFF3B30)
    val Alert = Color(0xFFFF9500)
    val Calendar = Color(0xFF34C759)
    val Permissions = Color(0xFF34C759)
    val Biometry = Color(0xFF1C1C1E)
    val EyeOff = Color(0xFF8E8E93)
    val Rate = Color(0xFFFF9500)
    val About = Color(0xFF007AFF)
}

@Composable
fun SettingsScreen(
    navController: NavController,
    onOpenCategories: () -> Unit = {},
    onOpenAccounts: () -> Unit = {},
    onOpenLimits: () -> Unit = {},
    onOpenGoals: () -> Unit = {},
    onOpenAchievements: () -> Unit = {},
    onOpenRecurring: () -> Unit = {},
    onOpenExport: () -> Unit = {},
    onOpenPermissions: () -> Unit = {},
    onOpenAbout: () -> Unit = {},
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val s = state.settings
    val counts = state.counts
    val c = FinanceTheme.colors
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val lifecycleOwner = LocalLifecycleOwner.current
    val biometricHelper = remember(activity) { activity?.let { BiometricHelper(it) } }

    DisposableEffect(lifecycleOwner, activity) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) activity?.let { viewModel.refreshPermissions(it) }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    var showCurrency by rememberSaveable { mutableStateOf(false) }
    var showImport by rememberSaveable { mutableStateOf(false) }
    var showExport by rememberSaveable { mutableStateOf(false) }
    var showBiometricUnavailable by remember { mutableStateOf(false) }

    if (showCurrency) {
        CurrencyPickerSheet(
            selectedCode = s.defaultCurrencyCode,
            onSelect = {
                viewModel.setCurrency(it)
                showCurrency = false
            },
            onDismiss = { showCurrency = false },
        )
    }
    if (showImport) ImportPickerSheet(navController = navController, onDismiss = { showImport = false })
    if (showExport) {
        val today = LocalDate.now()
        val start = counts.firstOperation ?: today
        ReportExportSheet(
            kind = ReportKind.EXPORT,
            range = DateRange(minOf(start, today), today),
            isWholeMonth = false,
            periodLabel = DateFmt.range(minOf(start, today), today),
            onDismiss = { showExport = false },
            onOpenBackup = {
                showExport = false
                onOpenExport()
            },
        )
    }
    if (showBiometricUnavailable) {
        AlertDialog(
            onDismissRequest = { showBiometricUnavailable = false },
            containerColor = c.surface,
            title = { Text(stringResource(R.string.biometric_unavailable_title)) },
            text = { Text(stringResource(R.string.biometric_unavailable_body)) },
            confirmButton = { TextButton(onClick = { showBiometricUnavailable = false }) { Text(stringResource(R.string.ok)) } },
        )
    }

    val reminderTime = LocalTime.of(s.reminderMinuteOfDay / 60, s.reminderMinuteOfDay % 60)
    val openTimePicker = {
        TimePickerDialog(
            context,
            { _, h, m -> viewModel.setReminderTime(h * 60 + m) },
            reminderTime.hour,
            reminderTime.minute,
            DateFormat.is24HourFormat(context),
        ).show()
    }

    LazyColumn(
        Modifier.fillMaxSize().background(c.bg),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = RootBottomInset),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Text(
                stringResource(R.string.nav_settings),
                style = FinanceType.headline,
                color = c.textPrimary,
                modifier = Modifier.statusBarsPadding().padding(top = 12.dp, bottom = 4.dp),
            )
        }

        item { SectionHeader(stringResource(R.string.settings_group_appearance)) }
        item {
            Group {
                SettingsRow(
                    title = stringResource(R.string.settings_theme),
                    tileIcon = R.drawable.ic_theme,
                    tileColor = Tiles.Theme,
                    showChevron = false,
                    below = {
                        SegmentedLight(
                            options = listOf(
                                stringResource(R.string.settings_theme_system),
                                stringResource(R.string.settings_theme_light),
                                stringResource(R.string.settings_theme_dark),
                            ),
                            selectedIndex = when (s.themeMode) {
                                ThemeMode.SYSTEM -> 0
                                ThemeMode.LIGHT -> 1
                                ThemeMode.DARK -> 2
                            },
                            onSelect = { viewModel.setThemeMode(listOf(ThemeMode.SYSTEM, ThemeMode.LIGHT, ThemeMode.DARK)[it]) },
                            modifier = Modifier.padding(start = 62.dp, end = 14.dp, bottom = 12.dp),
                        )
                    },
                )
                CardDivider()
                val currency = SupportedCurrencies.find(s.defaultCurrencyCode)
                SettingsRow(
                    title = stringResource(R.string.settings_currency),
                    tileIcon = R.drawable.ic_currency,
                    tileColor = Tiles.Currency,
                    value = "${Money.symbol(s.defaultCurrencyCode)} ${currency?.let { stringResource(it.nameRes) } ?: s.defaultCurrencyCode}",
                    onClick = { showCurrency = true },
                )
            }
        }

        item { SectionHeader(stringResource(R.string.settings_group_data)) }
        item {
            Group {
                SettingsRow(stringResource(R.string.settings_accounts), R.drawable.ic_accounts, Tiles.Accounts, value = counts.accounts.toString(), onClick = onOpenAccounts)
                CardDivider()
                SettingsRow(stringResource(R.string.settings_categories), R.drawable.ic_categories, Tiles.Categories, value = counts.categories.toString(), onClick = onOpenCategories)
                CardDivider()
                SettingsRow(
                    stringResource(R.string.limits),
                    R.drawable.ic_limits,
                    Tiles.Limits,
                    value = stringResource(R.string.home_quick_achievements, counts.limitsSet, counts.expenseCategories),
                    onClick = onOpenLimits,
                )
                CardDivider()
                SettingsRow(stringResource(R.string.goals_title), R.drawable.ic_goals, Tiles.Goals, value = counts.goals.toString(), onClick = onOpenGoals)
                CardDivider()
                SettingsRow(
                    stringResource(R.string.achievements_title),
                    R.drawable.ic_achievements,
                    Tiles.Achievements,
                    value = stringResource(R.string.home_quick_achievements, counts.achievements, AchievementId.entries.size),
                    onClick = onOpenAchievements,
                )
                CardDivider()
                SettingsRow(stringResource(R.string.settings_recurring), R.drawable.ic_recurring, Tiles.Recurring, value = counts.recurring.toString(), onClick = onOpenRecurring)
                CardDivider()
                SettingsRow(
                    stringResource(R.string.export_sheet_data_title),
                    R.drawable.ic_export,
                    Tiles.Export,
                    subtitle = stringResource(R.string.settings_export_sub),
                    onClick = { showExport = true },
                )
                CardDivider()
                SettingsRow(
                    stringResource(R.string.settings_import),
                    R.drawable.ic_import,
                    Tiles.Import,
                    subtitle = stringResource(R.string.settings_import_sub),
                    onClick = { showImport = true },
                )
            }
        }

        item { SectionHeader(stringResource(R.string.settings_group_notifications)) }
        item {
            Group {
                SettingsRow(
                    stringResource(R.string.settings_reminder),
                    R.drawable.ic_bell,
                    Tiles.Bell,
                    subtitle = stringResource(R.string.settings_reminder_sub, "%02d:%02d".format(reminderTime.hour, reminderTime.minute)),
                    onSubtitleClick = openTimePicker,
                    onClick = openTimePicker,
                    switchChecked = s.reminderEnabled,
                    onSwitchChange = viewModel::setReminder,
                )
                CardDivider()
                SettingsRow(
                    stringResource(R.string.limits),
                    R.drawable.ic_alert,
                    Tiles.Alert,
                    subtitle = stringResource(R.string.settings_limits_notif_sub),
                    switchChecked = s.limitNotificationsEnabled,
                    onSwitchChange = viewModel::setLimitNotifications,
                )
                CardDivider()
                SettingsRow(
                    stringResource(R.string.settings_payments_notif),
                    R.drawable.ic_calendar,
                    Tiles.Calendar,
                    subtitle = stringResource(R.string.settings_payments_notif_sub),
                    switchChecked = s.recurringNotificationsEnabled,
                    onSwitchChange = viewModel::setRecurringNotifications,
                )
            }
        }

        item { SectionHeader(stringResource(R.string.settings_group_security)) }
        item {
            Group {
                SettingsRow(stringResource(R.string.settings_permissions), R.drawable.ic_permissions, Tiles.Permissions, onClick = onOpenPermissions)
                CardDivider()
                SettingsRow(
                    stringResource(R.string.biometrics),
                    R.drawable.ic_biometry,
                    Tiles.Biometry,
                    subtitle = stringResource(R.string.settings_biometry_sub),
                    switchChecked = s.biometricEnabled,
                    onSwitchChange = { enabled ->
                        val helper = biometricHelper ?: return@SettingsRow
                        if (enabled && !helper.isAvailable()) {
                            showBiometricUnavailable = true
                        } else {
                            helper.authenticate(onSuccess = { viewModel.setBiometric(enabled) }, onError = {})
                        }
                    },
                )
                CardDivider()
                SettingsRow(
                    stringResource(R.string.settings_hide_amounts),
                    R.drawable.ic_eye_off,
                    Tiles.EyeOff,
                    subtitle = stringResource(R.string.settings_hide_amounts_sub),
                    switchChecked = s.hideAmountsOnLaunch,
                    onSwitchChange = viewModel::setHideAmounts,
                )
            }
        }

        item { SectionHeader(stringResource(R.string.settings_group_app)) }
        item {
            Group {
                SettingsRow(stringResource(R.string.settings_rate), R.drawable.ic_rate, Tiles.Rate, onClick = { openStoreListing(context) })
                CardDivider()
                SettingsRow(
                    stringResource(R.string.settings_about),
                    R.drawable.ic_about,
                    Tiles.About,
                    value = "v${BuildConfig.VERSION_NAME}",
                    onClick = onOpenAbout,
                )
            }
        }

        item {
            Column(Modifier.fillMaxWidth().padding(top = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                KopiImage(Kopi.HAPPY, 56.dp)
                VSpace(6.dp)
                Text(stringResource(R.string.settings_footer), style = FinanceType.caption, color = c.textSecondary, textAlign = TextAlign.Center)
                Text(stringResource(R.string.about_version, BuildConfig.VERSION_NAME), style = FinanceType.caption, color = c.textSecondary)
            }
        }
    }
}

@Composable
private fun Group(content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(FinanceTheme.colors.surface),
        content = content,
    )
}
