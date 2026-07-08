package ru.plumsoftware.finance.presentation.dashboard

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.DonutSmall
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Badge
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import ru.plumsoftware.finance.AppConfig
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.ui.ads.NativeAdContainer
import ru.plumsoftware.finance.ui.ads.NativeAdSession
import ru.plumsoftware.finance.domain.model.AppSettings
import ru.plumsoftware.finance.domain.model.Goal
import ru.plumsoftware.finance.domain.model.SmartAsset
import ru.plumsoftware.finance.domain.model.SmartAssetStatus
import ru.plumsoftware.finance.domain.model.StreakData
import ru.plumsoftware.finance.domain.model.daysLeft
import ru.plumsoftware.finance.domain.model.isOverdue
import ru.plumsoftware.finance.domain.model.progress
import ru.plumsoftware.finance.presentation.achievements.AchievementKeys
import ru.plumsoftware.finance.presentation.notifications.NotificationsViewModel
import ru.plumsoftware.finance.presentation.common.MoneyFormat
import ru.plumsoftware.finance.presentation.common.hasPendingPermissions
import ru.plumsoftware.finance.presentation.permissions.PermissionsBottomSheet
import ru.plumsoftware.finance.domain.model.Account
import ru.plumsoftware.finance.domain.repository.AccountRepository
import ru.plumsoftware.finance.domain.repository.SettingsRepository
import ru.plumsoftware.finance.ui.components.AppCard
import ru.plumsoftware.finance.ui.components.MascotImage
import ru.plumsoftware.finance.ui.components.SectionLabel
import ru.plumsoftware.finance.ui.components.ios.IosTextButton
import ru.plumsoftware.finance.ui.theme.Dimens
import ru.plumsoftware.finance.ui.theme.IosGreen
import ru.plumsoftware.finance.ui.theme.MascotEmotion
import ru.plumsoftware.finance.ui.theme.MascotSize
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.sp
import ru.plumsoftware.finance.data.util.startOfDayMillis
import ru.plumsoftware.finance.domain.model.*
import ru.plumsoftware.finance.presentation.export.ExportViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeScreen(
    onOpenSmartSavingsClick: () -> Unit = {},
    onOpenGoalsClick: () -> Unit = {},
    onOpenHistoryClick: () -> Unit = {},
    onOpenAnalyticsClick: () -> Unit = {},
    onOpenLimitsClick: () -> Unit = {},
    onOpenAchievementsClick: () -> Unit = {},
    onOpenNotificationsClick: () -> Unit = {},
    onCreateAssetClick: () -> Unit = {},
    onCreateGoalClick: () -> Unit = {},
    onGoalClick: (Long) -> Unit = {},
    viewModel: DashboardViewModel = koinViewModel(),
    notificationsViewModel: NotificationsViewModel = koinViewModel(),
    exportViewModel: ExportViewModel = koinViewModel(),
    settingsRepository: SettingsRepository = koinInject(),
    accountRepository: AccountRepository = koinInject(),
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
    val settings by settingsRepository.settings.collectAsStateWithLifecycle(initialValue = AppSettings())
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val unreadCount by notificationsViewModel.unreadCount.collectAsStateWithLifecycle()
    val exportState by exportViewModel.exportState.collectAsStateWithLifecycle()

    val snackbarHost = remember { SnackbarHostState() }
    var showPermissionsSheet by remember { mutableStateOf(false) }
    var showInitialBalanceDialog by remember { mutableStateOf(false) }
    var showEditBalanceDialog by remember { mutableStateOf(false) }
    var initialBalanceDigits by remember { mutableStateOf("") }
    var editBalanceDigits by remember { mutableStateOf("") }
    var isSavingInitialBalance by remember { mutableStateOf(false) }
    var isSavingEditBalance by remember { mutableStateOf(false) }
    var settingsLoaded by remember { mutableStateOf(false) }
    var permissionResumeTick by remember { mutableIntStateOf(0) }

    // Переменные для переключения накоплений и выбора счетов
    var includeSavings by rememberSaveable { mutableStateOf(true) }
    var targetAccountIdForEdit by remember { mutableStateOf<Long?>(null) }

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { state.accountsData.size + 1 }
    )

    // Переменные для Share Bottom Sheet
    var showShareSheet by rememberSaveable { mutableStateOf(false) }
    var selectedFormat by rememberSaveable { mutableStateOf(ExportFormat.PDF) }
    var selectedPeriod by rememberSaveable { mutableStateOf(ExportPeriod.THIS_MONTH) }
    var customStartMillis by rememberSaveable { mutableStateOf<Long?>(null) }
    var customEndMillis by rememberSaveable { mutableStateOf<Long?>(null) }
    var showDateRangePicker by rememberSaveable { mutableStateOf(false) }

    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    LaunchedEffect(exportState) {
        when (val expState = exportState) {
            is ExportState.Success -> {
                val uri = expState.shareUri
                if (uri != null) {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = selectedFormat.mimeType
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(
                        Intent.createChooser(
                            shareIntent,
                            context.getString(R.string.export_share_title),
                        ),
                    )
                    exportViewModel.resetState()
                    showShareSheet = false
                }
            }
            is ExportState.Error -> {
                val errorMsg = expState.message.ifBlank { context.getString(R.string.export_error_generic) }
                snackbarHost.showSnackbar(errorMsg)
                exportViewModel.resetState()
            }
            else -> Unit
        }
    }

    DisposableEffect(lifecycleOwner, settings.permissionsPromptHidden) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    if (!settings.permissionsPromptHidden && hasPendingPermissions(context)) {
                        showPermissionsSheet = true
                    }
                }
                Lifecycle.Event.ON_RESUME -> permissionResumeTick++
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(settings.permissionsPromptHidden, permissionResumeTick) {
        if (settings.permissionsPromptHidden || !hasPendingPermissions(context)) {
            showPermissionsSheet = false
        } else if (hasPendingPermissions(context)) {
            showPermissionsSheet = true
        }
    }

    LaunchedEffect(Unit) {
        settingsRepository.settings.first()
        settingsLoaded = true
    }

    // Исправленная логика инициализации баланса (предотвращает мгновенное закрытие диалога)
    LaunchedEffect(
        settings.initialBalancePromptCompleted,
        settingsLoaded,
        state.isLoading,
        showPermissionsSheet,
    ) {
        if (!settingsLoaded || state.isLoading) {
            showInitialBalanceDialog = false
            return@LaunchedEffect
        }
        if (settings.initialBalancePromptCompleted) {
            showInitialBalanceDialog = false
            return@LaunchedEffect
        }

        val permissionsReady = !hasPendingPermissions(context) && !showPermissionsSheet
        if (!permissionsReady) {
            showInitialBalanceDialog = false
            return@LaunchedEffect
        }

        val account = accountRepository.getDefault() ?: return@LaunchedEffect
        val alreadyConfigured = account.initialBalanceMinor != 0L || state.totalBalanceMinor != 0L
        if (alreadyConfigured) {
            settingsRepository.update { it.copy(initialBalancePromptCompleted = true) }
            showInitialBalanceDialog = false
            return@LaunchedEffect
        }

        showInitialBalanceDialog = true
    }

    if (showInitialBalanceDialog) {
        InitialBalanceDialog(
            title = stringResource(R.string.initial_balance_prompt_title),
            message = stringResource(R.string.initial_balance_prompt_message),
            currencyCode = state.currencyCode,
            amountDigits = initialBalanceDigits,
            isSaving = isSavingInitialBalance,
            dismissLabel = stringResource(R.string.initial_balance_prompt_skip),
            onDigit = { digit ->
                initialBalanceDigits = normalizeBalanceDigits(initialBalanceDigits + digit)
            },
            onBackspace = {
                initialBalanceDigits = initialBalanceDigits.dropLast(1)
            },
            onSave = {
                scope.launch {
                    isSavingInitialBalance = true
                    val amountMinor = MoneyFormat.majorDigitsToMinor(
                        initialBalanceDigits.ifBlank { "0" },
                        state.currencyCode,
                    )
                    val account = accountRepository.getDefault()
                    if (account != null && amountMinor > 0L) {
                        accountRepository.upsert(
                            account.copy(initialBalanceMinor = amountMinor),
                        )
                    }
                    settingsRepository.update {
                        it.copy(initialBalancePromptCompleted = true)
                    }
                    isSavingInitialBalance = false
                    showInitialBalanceDialog = false
                    initialBalanceDigits = ""
                }
            },
            onDismiss = {
                scope.launch {
                    settingsRepository.update {
                        it.copy(initialBalancePromptCompleted = true)
                    }
                    showInitialBalanceDialog = false
                    initialBalanceDigits = ""
                }
            },
        )
    }

    if (showEditBalanceDialog) {
        InitialBalanceDialog(
            title = stringResource(R.string.edit_balance_dialog_title),
            message = stringResource(R.string.edit_balance_dialog_message),
            currencyCode = state.currencyCode,
            amountDigits = editBalanceDigits,
            isSaving = isSavingEditBalance,
            dismissLabel = stringResource(R.string.cancel),
            onDigit = { digit ->
                editBalanceDigits = normalizeBalanceDigits(editBalanceDigits + digit)
            },
            onBackspace = {
                editBalanceDigits = editBalanceDigits.dropLast(1)
            },
            onSave = {
                scope.launch {
                    isSavingEditBalance = true
                    val enteredMinor = MoneyFormat.majorDigitsToMinor(
                        editBalanceDigits.ifBlank { "0" },
                        state.currencyCode,
                    )
                    val accountId = targetAccountIdForEdit ?: settings.selectedAccountId
                    val account = accountRepository.getById(accountId)
                        ?: accountRepository.getDefault()
                    if (account != null) {
                        val currentBalance = state.accountsData.find { it.accountId == account.id }?.balanceMinor ?: 0L
                        val transactionsDelta = currentBalance - account.initialBalanceMinor
                        val newInitialBalance = (enteredMinor - transactionsDelta).coerceAtLeast(0L)
                        accountRepository.upsert(
                            account.copy(initialBalanceMinor = newInitialBalance),
                        )
                    }
                    isSavingEditBalance = false
                    showEditBalanceDialog = false
                    editBalanceDigits = ""
                }
            },
            onDismiss = {
                showEditBalanceDialog = false
                editBalanceDigits = ""
            },
        )
    }

    if (showPermissionsSheet) {
        PermissionsBottomSheet(
            onDismiss = { dontShowAgain ->
                showPermissionsSheet = false
                if (dontShowAgain) {
                    scope.launch {
                        settingsRepository.update { it.copy(permissionsPromptHidden = true) }
                    }
                }
            },
        )
    }

    val dateRangePickerState = rememberDateRangePickerState(
        initialSelectedStartDateMillis = customStartMillis,
        initialSelectedEndDateMillis = customEndMillis,
        initialDisplayMode = DisplayMode.Picker,
    )

    if (showDateRangePicker) {
        ModalBottomSheet(
            onDismissRequest = { showDateRangePicker = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = colors.surface,
            shape = RoundedCornerShape(topStart = Dimens.RadiusXl, topEnd = Dimens.RadiusXl),
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(top = Dimens.SpacingS)
                        .size(width = Dimens.bottomSheetHandleWidth, height = Dimens.bottomSheetHandleHeight)
                        .clip(RoundedCornerShape(Dimens.RadiusPill))
                        .background(colors.surfaceVariant),
                )
            },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .heightIn(min = 580.dp)
                    .navigationBarsPadding(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = Dimens.SpacingL,
                            end = Dimens.SpacingM,
                            top = Dimens.SpacingM,
                            bottom = Dimens.SpacingXs,
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.pick_date_range),
                        style = typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = { showDateRangePicker = false }) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = stringResource(R.string.action_cancel),
                            tint = colors.onSurfaceVariant,
                        )
                    }
                }

                DateRangePicker(
                    state = dateRangePickerState,
                    title = null,
                    headline = null,
                    showModeToggle = false,
                    colors = DatePickerDefaults.colors(
                        containerColor = colors.surface,
                        titleContentColor = colors.onSurfaceVariant,
                        headlineContentColor = colors.onSurface,
                        weekdayContentColor = colors.onSurfaceVariant,
                        selectedDayContainerColor = colors.primary,
                        selectedDayContentColor = Color.White,
                        dayInSelectionRangeContainerColor = colors.primary.copy(alpha = 0.12f),
                        dayInSelectionRangeContentColor = colors.primary,
                        todayContentColor = colors.primary,
                        todayDateBorderColor = colors.primary,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                )

                val todayStart = startOfDayMillis(System.currentTimeMillis())
                val presets = listOf(
                    R.string.preset_week to (startOfDayOffset(Calendar.DAY_OF_YEAR, -7) to todayStart),
                    R.string.preset_month to (startOfDayOffset(Calendar.MONTH, -1) to todayStart),
                    R.string.preset_3m to (startOfDayOffset(Calendar.MONTH, -3) to todayStart),
                    R.string.preset_year to (startOfDayOffset(Calendar.YEAR, -1) to todayStart),
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = Dimens.SpacingL),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingXs),
                    modifier = Modifier.padding(bottom = Dimens.SpacingS),
                ) {
                    items(presets, key = { it.first }) { (labelRes, range) ->
                        FilterChip(
                            selected = false,
                            onClick = {
                                dateRangePickerState.setSelection(
                                    startOfDayMillis(range.first),
                                    startOfDayMillis(range.second),
                                )
                            },
                            label = { Text(text = stringResource(labelRes), style = typography.bodySmall) },
                            shape = RoundedCornerShape(Dimens.RadiusPill),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = false,
                                borderColor = colors.surfaceVariant,
                                selectedBorderColor = Color.Transparent,
                            ),
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = colors.surfaceVariant,
                                labelColor = colors.onSurfaceVariant,
                            ),
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimens.SpacingL, vertical = Dimens.SpacingM),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingM),
                ) {
                    OutlinedButton(
                        onClick = { showDateRangePicker = false },
                        modifier = Modifier
                            .weight(1f)
                            .height(Dimens.ButtonHeight),
                        shape = RoundedCornerShape(Dimens.RadiusL),
                        border = BorderStroke(Dimens.borderThin, colors.surfaceVariant),
                    ) {
                        Text(text = stringResource(R.string.action_cancel), color = colors.onSurface)
                    }

                    Button(
                        onClick = {
                            val startMs = dateRangePickerState.selectedStartDateMillis
                            val endMs = dateRangePickerState.selectedEndDateMillis
                            if (startMs != null && endMs != null) {
                                customStartMillis = startOfDayMillis(startMs)
                                customEndMillis = startOfDayMillis(endMs)
                            }
                            showDateRangePicker = false
                        },
                        enabled = dateRangePickerState.selectedStartDateMillis != null &&
                                dateRangePickerState.selectedEndDateMillis != null,
                        modifier = Modifier
                            .weight(1f)
                            .height(Dimens.ButtonHeight),
                        shape = RoundedCornerShape(Dimens.RadiusL),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primary,
                            disabledContainerColor = colors.primary.copy(alpha = 0.3f),
                        ),
                        elevation = ButtonDefaults.buttonElevation(0.dp),
                    ) {
                        Text(text = stringResource(R.string.action_apply), color = Color.White)
                    }
                }
            }
        }
    }

    if (showShareSheet) {
        ModalBottomSheet(
            onDismissRequest = { showShareSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false),
            containerColor = colors.surface,
            shape = RoundedCornerShape(topStart = Dimens.RadiusXl, topEnd = Dimens.RadiusXl),
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(top = Dimens.SpacingS)
                        .size(width = Dimens.bottomSheetHandleWidth, height = Dimens.bottomSheetHandleHeight)
                        .clip(RoundedCornerShape(Dimens.RadiusPill))
                        .background(colors.surfaceVariant),
                )
            },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = Dimens.SpacingL, vertical = Dimens.SpacingM),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpacingL),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = stringResource(R.string.export_share_title),
                        style = typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    IconButton(onClick = { showShareSheet = false }) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = stringResource(R.string.action_cancel),
                            tint = colors.onSurfaceVariant,
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpacingXs)) {
                    SectionLabel(text = stringResource(R.string.export_section_format), withBottomSpacing = false)
                    AppCard(modifier = Modifier.fillMaxWidth()) {
                        val availableFormats = ExportFormat.entries.filter { it != ExportFormat.JSON }
                        availableFormats.forEachIndexed { index, format ->
                            ShareSelectionRow(
                                label = stringResource(format.labelRes),
                                selected = selectedFormat == format,
                                onClick = { selectedFormat = format },
                            )
                            if (index < availableFormats.lastIndex) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(start = Dimens.SpacingM),
                                    color = colors.surfaceVariant,
                                )
                            }
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpacingXs)) {
                    SectionLabel(text = stringResource(R.string.export_section_period), withBottomSpacing = false)
                    AppCard(modifier = Modifier.fillMaxWidth()) {
                        ExportPeriod.entries.forEachIndexed { index, period ->
                            ShareSelectionRow(
                                label = stringResource(period.labelRes),
                                selected = selectedPeriod == period,
                                onClick = {
                                    selectedPeriod = period
                                    if (period == ExportPeriod.CUSTOM) {
                                        showDateRangePicker = true
                                    }
                                },
                            )
                            if (index < ExportPeriod.entries.lastIndex) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(start = Dimens.SpacingM),
                                    color = colors.surfaceVariant,
                                )
                            }
                        }
                    }

                    if (selectedPeriod == ExportPeriod.CUSTOM && customStartMillis != null && customEndMillis != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = Dimens.SpacingXs)
                                .clip(RoundedCornerShape(Dimens.RadiusM))
                                .background(colors.primary.copy(alpha = 0.08f))
                                .clickable { showDateRangePicker = true }
                                .padding(horizontal = Dimens.SpacingM, vertical = Dimens.SpacingXs),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.DateRange,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(Dimens.IconSizeS),
                            )
                            Text(
                                text = stringResource(
                                    R.string.date_range_label,
                                    formatEpochMillis(customStartMillis!!, "d MMM"),
                                    formatEpochMillis(customEndMillis!!, "d MMM yyyy"),
                                ),
                                style = typography.bodySmall,
                                color = colors.primary,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = Dimens.SpacingXs),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            IconButton(
                                onClick = {
                                    selectedPeriod = ExportPeriod.THIS_MONTH
                                    customStartMillis = null
                                    customEndMillis = null
                                },
                                modifier = Modifier.size(Dimens.IconSizeL),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = stringResource(R.string.cd_clear_date_range),
                                    tint = colors.primary,
                                    modifier = Modifier.size(Dimens.IconSizeS),
                                )
                            }
                        }
                    }
                }

                val isExporting = exportState is ExportState.Loading
                Button(
                    onClick = {
                        exportViewModel.exportAndShare(
                            format = selectedFormat,
                            period = selectedPeriod,
                            customStartMillis = customStartMillis,
                            customEndMillis = customEndMillis,
                        )
                    },
                    enabled = !isExporting && (selectedPeriod != ExportPeriod.CUSTOM || (customStartMillis != null && customEndMillis != null)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimens.ButtonHeight),
                    shape = RoundedCornerShape(Dimens.RadiusL),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                    elevation = ButtonDefaults.buttonElevation(0.dp),
                ) {
                    if (isExporting) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.Share,
                            contentDescription = null,
                            modifier = Modifier.size(Dimens.IconSizeM),
                        )
                        Spacer(Modifier.width(Dimens.SpacingXs))
                        Text(
                            text = stringResource(R.string.export_share),
                            style = typography.titleMedium,
                        )
                    }
                }
            }
        }
    }

    LaunchedEffect(state.snackbarMessage) {
        state.snackbarMessage?.let {
            snackbarHost.showSnackbar(it)
            viewModel.clearSnackbar()
        }
    }

    val listState = rememberSaveable(saver = androidx.compose.foundation.lazy.LazyListState.Saver) {
        androidx.compose.foundation.lazy.LazyListState()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = colors.background,
        snackbarHost = { SnackbarHost(snackbarHost) },
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = Dimens.statusBarInset,
                bottom = Dimens.SpacingM,
            ),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingL),
        ) {
            item {
                GreetingHeader(
                    unreadCount = unreadCount,
                    onNotificationsClick = onOpenNotificationsClick,
                    onShareClick = { showShareSheet = true },
                )
            }
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(Dimens.SpacingL)
                ) {
                    AccountsCarousel(
                        accountsData = state.accountsData,
                        totalGoalSavingsMinor = state.totalGoalSavingsMinor,
                        defaultCurrencyCode = state.currencyCode,
                        includeSavings = includeSavings,
                        onIncludeSavingsChange = { includeSavings = it },
                        pagerState = pagerState,
                        onMonthClick = onOpenAnalyticsClick,
                        onBalanceClick = { cardData ->
                            if (cardData != null) {
                                targetAccountIdForEdit = cardData.accountId
                                editBalanceDigits = MoneyFormat.minorToMajorDigits(
                                    amountMinor = cardData.balanceMinor,
                                    currencyCode = cardData.currencyCode,
                                ).takeIf { it != "0" }.orEmpty()
                                showEditBalanceDialog = true
                            } else {
                                scope.launch {
                                    val errorMsg = try {
                                        context.getString(R.string.edit_balance_combined_error)
                                    } catch (e: Exception) {
                                        "Выберите конкретный счет для изменения баланса"
                                    }
                                    snackbarHost.showSnackbar(errorMsg)
                                }
                            }
                        },
                    )

                    // Реклама выводится внутри общего контейнера. Если она скрыта или
                    // имеет нулевую высоту, LazyColumn не будет дублировать SpacingL.
                    if (!NativeAdSession.dismissed) {
                        NativeAdContainer(adUnitId = AppConfig.nativeHome)
                    }
                }
            }
            item {
                HomeQuickActionsRow(
                    onLimitsClick = onOpenLimitsClick,
                    onOperationsClick = onOpenHistoryClick,
                    onAchievementsClick = onOpenAchievementsClick,
                    hasBudgetWarnings = state.hasBudgetWarnings,
                    operationCount = state.todayOperationsCount,
                    unlockedAchievementsCount = state.unlockedAchievementsCount,
                    streak = state.streak,
                )
            }
            item {
                Column {
                    SectionWithAction(
                        title = stringResource(R.string.goals_home_section),
                        actionLabel = stringResource(R.string.dashboard_see_all),
                        onActionClick = onOpenGoalsClick,
                    )
                    state.featuredGoal?.let { featured ->
                        HomeFeaturedGoalCard(
                            goal = featured,
                            currencyCode = state.currencyCode,
                            onClick = { onGoalClick(featured.id) },
                        )
                    } ?: HomeCreateGoalCard(onClick = onCreateGoalClick)
                }
            }
            item {
                AnimatedVisibility(visible = state.hasBudgetWarnings) {
                    BudgetWarningBanner(
                        onClick = onOpenLimitsClick,
                        onDismiss = viewModel::dismissWarning,
                    )
                }
            }
            if (state.insights.isNotEmpty()) {
                item {
                    InsightsSection(
                        insights = state.insights,
                        categoryMap = state.categoryMap,
                        currencyCode = state.currencyCode,
                    )
                }
            }
            item {
                Column {
                    SectionWithAction(
                        title = stringResource(R.string.smart_savings),
                        actionLabel = stringResource(R.string.dashboard_see_all),
                        onActionClick = onOpenSmartSavingsClick,
                    )
                    if (state.smartAssets.isEmpty()) {
                        EmptyAssetCard(onClick = onCreateAssetClick)
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = Dimens.SpacingL),
                            horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingS),
                        ) {
                            state.smartAssets.forEach { asset ->
                                AssetMiniCard(
                                    asset = asset,
                                    currencyCode = state.currencyCode,
                                    onRecordUsage = { viewModel.recordSmartUsage(asset.id) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AccountsCarousel(
    accountsData: List<AccountDashboardData>,
    totalGoalSavingsMinor: Long, // <-- Принимаем накопления по целям
    defaultCurrencyCode: String,
    includeSavings: Boolean,
    onIncludeSavingsChange: (Boolean) -> Unit,
    pagerState: PagerState,
    onMonthClick: () -> Unit,
    onBalanceClick: (AccountDashboardData?) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpacingS)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = Dimens.SpacingL),
            pageSpacing = Dimens.SpacingS,
        ) { page ->
            val cardData = if (page == 0) {
                val activeAccounts = if (includeSavings) {
                    accountsData
                } else {
                    accountsData.filter { !it.isSavings }
                }
                // Накопления с целей прибавляются к общему балансу только если includeSavings == true
                val goalSavings = if (includeSavings) totalGoalSavingsMinor else 0L

                AccountDashboardData(
                    accountId = null,
                    name = stringResource(R.string.total_budget),
                    balanceMinor = activeAccounts.sumOf { it.balanceMinor } + goalSavings, // <-- Суммируем счета и цели
                    currencyCode = defaultCurrencyCode,
                    monthIncomeMinor = activeAccounts.sumOf { it.monthIncomeMinor },
                    monthExpenseMinor = activeAccounts.sumOf { it.monthExpenseMinor },
                    isSavings = false
                )
            } else {
                accountsData.getOrNull(page - 1)
            }

            if (cardData != null) {
                val context = LocalContext.current
                val animatedBalance by animateFloatAsState(
                    targetValue = cardData.balanceMinor.toFloat(),
                    animationSpec = tween(durationMillis = 400),
                    label = "home_balance_${page}",
                )
                val savingsRate = if (cardData.monthIncomeMinor > 0) {
                    ((cardData.monthIncomeMinor - cardData.monthExpenseMinor).coerceAtLeast(0L).toFloat() / cardData.monthIncomeMinor)
                        .coerceIn(0f, 1f)
                } else {
                    0f
                }
                val savingsPercent = (savingsRate * 100).roundToInt()
                val monthLabel = SimpleDateFormat("LLLL yyyy", Locale.getDefault())
                    .format(Date())
                    .replaceFirstChar { it.uppercase() }

                val balanceCardScale = 0.855f
                val balancePadding = Dimens.SpacingL * balanceCardScale

                AppCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onBalanceClick(if (page == 0) null else cardData) },
                ) {
                    Column(
                        modifier = Modifier.padding(balancePadding),
                        verticalArrangement = Arrangement.spacedBy(Dimens.SpacingS * balanceCardScale),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingXs)
                            ) {
                                Text(
                                    text = cardData.name,
                                    style = typography.labelSmall,
                                    color = colors.onSurfaceVariant,
                                )
                                if (cardData.isSavings) {
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                colors.secondary.copy(alpha = 0.15f),
                                                RoundedCornerShape(Dimens.RadiusPill)
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = stringResource(R.string.savings_label),
                                            style = typography.labelSmall.copy(fontSize = 9.sp),
                                            color = colors.secondary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            if (page == 0) {
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(Dimens.RadiusPill))
                                        .background(colors.surfaceVariant.copy(alpha = 0.5f))
                                        .clickable { onIncludeSavingsChange(!includeSavings) }
                                        .padding(horizontal = Dimens.SpacingS, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (includeSavings) Icons.Rounded.CheckCircle else Icons.Rounded.Circle,
                                        contentDescription = null,
                                        tint = if (includeSavings) colors.primary else colors.onSurfaceVariant.copy(alpha = 0.4f),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = if (includeSavings) stringResource(R.string.with_savings) else stringResource(R.string.without_savings),
                                        style = typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = colors.primary,
                                    )
                                }
                            } else {
                                MonthSelector(
                                    monthLabel = monthLabel,
                                    onClick = onMonthClick,
                                )
                            }
                        }
                        Text(
                            text = MoneyFormat.format(animatedBalance.roundToInt().toLong(), cardData.currencyCode),
                            style = typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = colors.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        HorizontalDivider(
                            color = colors.surfaceVariant,
                            thickness = 1.dp,
                            modifier = Modifier.padding(vertical = Dimens.SpacingS * balanceCardScale),
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            FinanceStat(
                                icon = Icons.Rounded.ArrowUpward,
                                tint = colors.secondary,
                                label = stringResource(R.string.income),
                                value = MoneyFormat.formatWithSignPrefix(
                                    context,
                                    cardData.monthIncomeMinor,
                                    cardData.currencyCode,
                                    isPositive = true,
                                ),
                            )
                            FinanceStat(
                                icon = Icons.Rounded.ArrowDownward,
                                tint = colors.error,
                                label = stringResource(R.string.expenses),
                                value = MoneyFormat.format(cardData.monthExpenseMinor, cardData.currencyCode),
                            )
                        }
                        if (savingsRate > 0f) {
                            LinearProgressIndicator(
                                progress = { savingsRate },
                                color = colors.secondary,
                                trackColor = colors.surfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(Dimens.RadiusPill)),
                            )
                            Text(
                                text = stringResource(R.string.savings_rate_saved, savingsPercent),
                                style = typography.labelSmall,
                                color = colors.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }

        val pageCount = accountsData.size + 1
        if (pageCount > 1) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                repeat(pageCount) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (isSelected) 8.dp else 6.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) colors.primary else colors.onSurfaceVariant.copy(alpha = 0.3f)
                            )
                    )
                }
            }
        }
    }
}


@Composable
fun DashboardScreen(
    onOpenSmartSavingsClick: () -> Unit = {},
    onOpenGoalsClick: () -> Unit = {},
    onOpenHistoryClick: () -> Unit = {},
    onOpenAnalyticsClick: () -> Unit = {},
    onOpenLimitsClick: () -> Unit = {},
    onOpenAchievementsClick: () -> Unit = {},
    onOpenNotificationsClick: () -> Unit = {},
    onCreateAssetClick: () -> Unit = {},
    onCreateGoalClick: () -> Unit = {},
    onGoalClick: (Long) -> Unit = {},
    viewModel: DashboardViewModel = koinViewModel(),
    exportViewModel: ExportViewModel = koinViewModel(),
) {
    HomeScreen(
        onOpenSmartSavingsClick = onOpenSmartSavingsClick,
        onOpenGoalsClick = onOpenGoalsClick,
        onOpenHistoryClick = onOpenHistoryClick,
        onOpenAnalyticsClick = onOpenAnalyticsClick,
        onOpenLimitsClick = onOpenLimitsClick,
        onOpenAchievementsClick = onOpenAchievementsClick,
        onOpenNotificationsClick = onOpenNotificationsClick,
        onCreateAssetClick = onCreateAssetClick,
        onCreateGoalClick = onCreateGoalClick,
        onGoalClick = onGoalClick,
        viewModel = viewModel,
        exportViewModel = exportViewModel,
    )
}

@Composable
private fun GreetingHeader(
    unreadCount: Int,
    onNotificationsClick: () -> Unit,
    onShareClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.SpacingL),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                text = greetingWithTime(),
                style = typography.bodyMedium,
                color = colors.onSurfaceVariant,
            )
            Text(
                text = stringResource(R.string.dashboard_title),
                style = typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface,
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingXs),
        ) {
            IconButton(onClick = onShareClick) {
                Icon(
                    imageVector = Icons.Outlined.Share,
                    contentDescription = stringResource(R.string.export_share),
                    tint = colors.onSurfaceVariant,
                    modifier = Modifier.size(Dimens.IconSizeM),
                )
            }

            BadgedBox(
                badge = {
                    if (unreadCount > 0) {
                        Badge {
                            Text(
                                text = if (unreadCount > 99) {
                                    stringResource(R.string.badge_count_overflow)
                                } else {
                                    unreadCount.toString()
                                },
                                style = typography.labelSmall,
                            )
                        }
                    }
                },
            ) {
                IconButton(onClick = onNotificationsClick) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = stringResource(R.string.cd_notifications),
                        tint = colors.onSurfaceVariant,
                        modifier = Modifier.size(Dimens.IconSizeM),
                    )
                }
            }
        }
    }
}

@Composable
private fun ShareSelectionRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = Dimens.SpacingM, vertical = Dimens.SpacingS),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = colors.onSurface,
        )
        if (selected) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(Dimens.IconSizeM),
            )
        }
    }
}

@Composable
private fun HomeStreakInfo(
    streak: StreakData,
    modifier: Modifier = Modifier,
) {
    if (streak.currentStreak == 0) return
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Dimens.SpacingXxs),
    ) {
        Text(
            text = stringResource(
                R.string.streak_fire_format,
                streak.currentStreak,
                pluralStringResource(R.plurals.days_count, streak.currentStreak),
            ),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = when {
                streak.currentStreak >= 30 -> Color(0xFFFF9500)
                streak.currentStreak >= 7 -> Color(0xFFFF3B30)
                else -> MaterialTheme.colorScheme.onSurface
            },
        )
        Text(
            text = if (streak.todayHasActivity) {
                stringResource(R.string.streak_today_done)
            } else {
                stringResource(R.string.streak_today_pending)
            },
            style = MaterialTheme.typography.labelSmall,
            color = if (streak.todayHasActivity) {
                Color(0xFF34C759)
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            },
        )
    }
}

@Composable
private fun greetingWithTime(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when {
        hour in 5..11 -> stringResource(R.string.greeting_morning)
        hour in 12..17 -> stringResource(R.string.greeting_afternoon)
        else -> stringResource(R.string.greeting_evening)
    }
}

@Composable
private fun HomeQuickActionsRow(
    onLimitsClick: () -> Unit,
    onOperationsClick: () -> Unit,
    onAchievementsClick: () -> Unit,
    hasBudgetWarnings: Boolean,
    operationCount: Int,
    unlockedAchievementsCount: Int,
    streak: StreakData,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Max)
            .padding(horizontal = Dimens.SpacingL),
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingS),
    ) {
        HomeAchievementsQuickCard(
            streak = streak,
            unlockedCount = unlockedAchievementsCount,
            onClick = onAchievementsClick,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingS),
        ) {
            HomeQuickActionCard(
                title = stringResource(R.string.home_all_operations),
                subtitle = stringResource(R.string.home_operations_count, operationCount),
                icon = Icons.Outlined.History,
                onClick = onOperationsClick,
            )
            HomeLimitsQuickCard(
                hasBudgetWarnings = hasBudgetWarnings,
                onClick = onLimitsClick,
            )
        }
    }
}

@Composable
private fun HomeLimitsQuickCard(
    hasBudgetWarnings: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    AppCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier.padding(Dimens.SpacingM),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingXxs),
        ) {
            Icon(
                imageVector = Icons.Rounded.DonutSmall,
                contentDescription = null,
                tint = if (hasBudgetWarnings) colors.error else colors.primary,
                modifier = Modifier.size(Dimens.IconSizeM),
            )
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpacingXxs)) {
                Text(
                    text = stringResource(R.string.limits),
                    style = typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = if (hasBudgetWarnings) {
                        stringResource(R.string.budget_warning_title)
                    } else {
                        stringResource(R.string.current_month_limits)
                    },
                    style = typography.bodySmall,
                    color = if (hasBudgetWarnings) colors.error else colors.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun HomeAchievementsQuickCard(
    streak: StreakData,
    unlockedCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val streakBackground = when {
        streak.currentStreak >= 30 -> Color(0xFFFF9500).copy(alpha = 0.12f)
        streak.currentStreak >= 7 -> Color(0xFFFF3B30).copy(alpha = 0.10f)
        streak.currentStreak > 0 -> colors.surface
        else -> Color.Transparent
    }
    AppCard(
        modifier = modifier,
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .clip(RoundedCornerShape(Dimens.RadiusL))
                .background(streakBackground)
                .padding(Dimens.SpacingM),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            MascotImage(
                emotion = MascotEmotion.HAPPY,
                modifier = Modifier.size(MascotSize.Medium),
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Dimens.SpacingXxs),
            ) {
                Text(
                    text = stringResource(R.string.achievements_title),
                    style = typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (streak.currentStreak > 0) {
                    HomeStreakInfo(streak = streak)
                } else {
                    Text(
                        text = stringResource(
                            R.string.home_achievements_count,
                            unlockedCount,
                            AchievementKeys.TOTAL_COUNT,
                        ),
                        style = typography.bodySmall,
                        color = colors.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeQuickActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    AppCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier.padding(Dimens.SpacingM),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingXxs),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(Dimens.IconSizeM),
            )
            Text(
                text = title,
                style = typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = subtitle,
                style = typography.bodySmall,
                color = colors.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun MonthSelector(
    monthLabel: String,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    Row(
        modifier = Modifier.clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingXxs),
    ) {
        Text(
            text = monthLabel,
            style = typography.bodyMedium,
            color = colors.primary,
        )
        Icon(
            imageVector = Icons.Outlined.KeyboardArrowDown,
            contentDescription = null,
            tint = colors.primary,
            modifier = Modifier.size(Dimens.IconSizeS),
        )
    }
}

@Composable
private fun FinanceStat(
    icon: ImageVector,
    tint: Color,
    label: String,
    value: String,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpacingXxs)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingXxs),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(Dimens.IconSizeS),
            )
            Text(
                text = value,
                style = typography.bodyMedium,
                color = tint,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Text(
            text = label,
            style = typography.labelSmall,
            color = colors.onSurfaceVariant,
        )
    }
}

@Composable
private fun BudgetWarningBanner(
    onClick: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.SpacingL)
            .clickable(onClick = onClick),
        containerColor = colors.error.copy(alpha = 0.08f),
        borderColor = colors.error.copy(alpha = 0.3f),
    ) {
        Row(
            modifier = Modifier.padding(Dimens.SpacingM),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.WarningAmber,
                contentDescription = null,
                tint = colors.error,
                modifier = Modifier.size(Dimens.IconSizeM),
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = Dimens.SpacingS),
            ) {
                Text(
                    text = stringResource(R.string.budget_warning_title),
                    style = typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.error,
                )
                Text(
                    text = stringResource(R.string.budget_warning_body),
                    style = typography.bodySmall,
                    color = colors.onSurfaceVariant,
                )
            }
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = stringResource(R.string.cd_close),
                    tint = colors.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun SectionWithAction(
    title: String,
    actionLabel: String,
    onActionClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = Dimens.SpacingS, bottom = Dimens.SpacingXs),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SectionLabel(text = title, withBottomSpacing = false)
        IosTextButton(
            text = actionLabel,
            onClick = onActionClick,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun EmptyAssetCard(onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
    val cornerRadius = Dimens.RadiusL

    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.SpacingL)
            .drawBehind {
                drawRoundRect(
                    color = colors.primary.copy(alpha = 0.45f),
                    cornerRadius = CornerRadius(cornerRadius.toPx()),
                    style = Stroke(width = 2.dp.toPx(), pathEffect = dashEffect),
                )
            },
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.padding(Dimens.SpacingM),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingM),
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(Dimens.IconSizeM),
            )
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpacingXxs)) {
                Text(
                    text = stringResource(R.string.add_first_asset),
                    style = typography.bodyLarge,
                    color = colors.primary,
                )
                Text(
                    text = stringResource(R.string.track_roi),
                    style = typography.bodySmall,
                    color = colors.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun HomeCreateGoalCard(onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.SpacingL),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.padding(Dimens.SpacingM),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingM),
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(colors.primary.copy(alpha = 0.14f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    tint = colors.primary,
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.goals_add_button),
                    style = typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onSurface,
                )
                Text(
                    text = stringResource(R.string.goals_empty_subtitle),
                    style = typography.bodySmall,
                    color = colors.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = colors.outlineVariant,
                modifier = Modifier.size(Dimens.IconSizeS),
            )
        }
    }
}

@Composable
private fun HomeFeaturedGoalCard(
    goal: Goal,
    currencyCode: String,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val leftDays = goal.daysLeft
    val deadlineText = when {
        leftDays == null -> stringResource(R.string.goal_no_deadline)
        goal.isOverdue -> stringResource(R.string.goal_days_overdue, kotlin.math.abs(leftDays))
        else -> stringResource(R.string.goal_days_left, leftDays)
    }
    val accent = if (goal.isCompleted) IosGreen else colors.primary
    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.SpacingL),
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier.padding(Dimens.SpacingM),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingS),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(colors.primary.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = goal.emoji, style = typography.titleLarge)
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = Dimens.SpacingS),
                ) {
                    Text(
                        text = goal.name,
                        style = typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = stringResource(
                            R.string.goal_saved_of,
                            MoneyFormat.format(goal.savedAmountMinor, currencyCode),
                            MoneyFormat.format(goal.targetAmountMinor, currencyCode),
                        ),
                        style = typography.bodySmall,
                        color = colors.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            LinearProgressIndicator(
                progress = { goal.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.progressHeightThin)
                    .clip(RoundedCornerShape(Dimens.RadiusPill)),
                color = accent,
                trackColor = colors.outline,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(R.string.percent_short, (goal.progress * 100).roundToInt()),
                    style = typography.labelSmall,
                    color = accent,
                )
                Text(
                    text = deadlineText,
                    style = typography.labelSmall,
                    color = colors.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun AssetMiniCard(
    asset: SmartAsset,
    currencyCode: String,
    onRecordUsage: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val progress = asset.paybackProgress.coerceIn(0f, 1f)
    val progressColor = if (asset.status == SmartAssetStatus.PROFIT) {
        colors.secondary
    } else {
        colors.primary
    }

    AppCard(
        modifier = Modifier.width(Dimens.smartCardWidth),
    ) {
        Column(
            modifier = Modifier.padding(Dimens.SpacingM),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingS),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingS),
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(progressColor.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = asset.icon, style = typography.titleMedium)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = asset.name,
                        style = typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = stringResource(R.string.percent_short, (progress * 100).roundToInt()),
                        style = typography.labelSmall,
                        color = colors.onSurfaceVariant,
                    )
                }
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.progressHeightThin)
                    .clip(RoundedCornerShape(Dimens.RadiusPill)),
                color = progressColor,
                trackColor = colors.surfaceVariant,
                strokeCap = StrokeCap.Round,
            )
            Text(
                text = stringResource(
                    R.string.dashboard_saved_amount,
                    MoneyFormat.format(asset.totalSavedMinor, currencyCode),
                ),
                style = typography.bodySmall,
                color = colors.secondary,
            )
            OutlinedButton(
                onClick = onRecordUsage,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(Dimens.RadiusS),
                border = BorderStroke(Dimens.borderThin, colors.primary),
                contentPadding = PaddingValues(
                    horizontal = Dimens.SpacingS,
                    vertical = Dimens.SpacingS,
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = colors.primary,
                ),
            ) {
                Text(
                    text = stringResource(R.string.dashboard_record),
                    style = typography.labelMedium,
                )
            }
        }
    }
}

private fun normalizeBalanceDigits(raw: String): String {
    val filtered = raw.filter { it.isDigit() || it == '.' }
    if (filtered.isEmpty()) return ""
    if (filtered == ".") return "0."
    val dotIndex = filtered.indexOf('.')
    return if (dotIndex >= 0) {
        val intPart = filtered.substring(0, dotIndex).filter { it.isDigit() }
        val fracPart = filtered.substring(dotIndex + 1).filter { it.isDigit() }.take(2)
        val safeInt = intPart.ifEmpty { "0" }.trimStart('0').ifEmpty { "0" }.take(9)
        "$safeInt.$fracPart"
    } else {
        filtered.filter { it.isDigit() }.trimStart('0').ifEmpty { "0" }.take(9)
    }
}

private fun startOfDayOffset(field: Int, amount: Int): Long {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    calendar.add(field, amount)
    return calendar.timeInMillis
}

private fun formatEpochMillis(millis: Long, pattern: String): String =
    SimpleDateFormat(pattern, Locale.getDefault()).format(Date(millis))