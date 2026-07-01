package ru.plumsoftware.finance.presentation.export

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.SaveAlt
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import ru.plumsoftware.finance.navigation.popBackStackOrHome
import org.koin.androidx.compose.koinViewModel
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.data.util.startOfDayMillis
import ru.plumsoftware.finance.domain.model.ExportFormat
import ru.plumsoftware.finance.domain.model.ExportPeriod
import ru.plumsoftware.finance.domain.model.ExportState
import ru.plumsoftware.finance.ui.components.AppCard
import ru.plumsoftware.finance.ui.components.SectionLabel
import ru.plumsoftware.finance.ui.components.ios.IosEditorTopBar
import ru.plumsoftware.finance.ui.theme.Dimens
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportScreen(
    navController: NavController,
    viewModel: ExportViewModel = koinViewModel(),
) {
    val exportState by viewModel.exportState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    var selectedFormat by rememberSaveable { mutableStateOf(ExportFormat.BACKUP) }
    var selectedPeriod by rememberSaveable { mutableStateOf(ExportPeriod.ALL_TIME) }
    var customStartMillis by rememberSaveable { mutableStateOf<Long?>(null) }
    var customEndMillis by rememberSaveable { mutableStateOf<Long?>(null) }
    var showDateRangePicker by rememberSaveable { mutableStateOf(false) }

    val customRangeActive = selectedPeriod == ExportPeriod.CUSTOM &&
        customStartMillis != null &&
        customEndMillis != null

    val canExport = exportState !is ExportState.Loading &&
        (selectedPeriod != ExportPeriod.CUSTOM || customRangeActive)

    val isLoading = exportState is ExportState.Loading
    val successMsg = stringResource(R.string.export_saved_success)
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(exportState) {
        when (val state = exportState) {
            is ExportState.Success -> {
                val uri = state.shareUri
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
                    viewModel.resetState()
                } else {
                    snackbarHostState.showSnackbar(
                        message = successMsg,
                        duration = SnackbarDuration.Short,
                    )
                    viewModel.resetState()
                }
            }
            else -> Unit
        }
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
            shape = RoundedCornerShape(
                topStart = Dimens.RadiusXl,
                topEnd = Dimens.RadiusXl,
            ),
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
                            label = {
                                Text(
                                    text = stringResource(labelRes),
                                    style = typography.bodySmall,
                                )
                            },
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
                        .padding(
                            horizontal = Dimens.SpacingL,
                            vertical = Dimens.SpacingM,
                        ),
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
                        Text(
                            text = stringResource(R.string.action_cancel),
                            color = colors.onSurface,
                        )
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
                        Text(
                            text = stringResource(R.string.action_apply),
                            color = Color.White,
                        )
                    }
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = colors.background,
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(bottom = Dimens.SpacingM),
            ) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = colors.onSurface,
                    contentColor = colors.surface,
                    shape = RoundedCornerShape(Dimens.RadiusM),
                )
            }
        },
        topBar = {
            IosEditorTopBar(
                title = stringResource(R.string.export_data),
                backLabel = stringResource(R.string.categories_back_settings),
                onBack = navController::popBackStackOrHome,
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(
                horizontal = Dimens.SpacingL,
                vertical = Dimens.SpacingM,
            ),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingL),
        ) {
            item {
                SectionLabel(text = stringResource(R.string.export_section_format))
                AppCard(modifier = Modifier.fillMaxWidth()) {
                    ExportFormat.entries.forEachIndexed { index, format ->
                        ExportRadioRow(
                            label = stringResource(format.labelRes),
                            selected = selectedFormat == format,
                            onClick = { selectedFormat = format },
                        )
                        if (index < ExportFormat.entries.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(start = Dimens.SpacingM),
                                color = colors.surfaceVariant,
                            )
                        }
                    }
                }
            }

            item {
                SectionLabel(text = stringResource(R.string.export_section_period))
                AppCard(modifier = Modifier.fillMaxWidth()) {
                    ExportPeriod.entries.forEachIndexed { index, period ->
                        ExportRadioRow(
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

                Text(
                    text = stringResource(R.string.export_period_hint),
                    style = typography.bodySmall,
                    color = colors.onSurfaceVariant,
                    modifier = Modifier.padding(top = Dimens.SpacingXs),
                )

                if (customRangeActive) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = Dimens.SpacingXs)
                            .clip(RoundedCornerShape(Dimens.RadiusM))
                            .background(colors.primary.copy(alpha = 0.08f))
                            .clickable { showDateRangePicker = true }
                            .padding(
                                horizontal = Dimens.SpacingM,
                                vertical = Dimens.SpacingXs,
                            ),
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

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimens.SpacingL),
                    verticalArrangement = Arrangement.spacedBy(Dimens.SpacingS),
                ) {
                    Button(
                        onClick = {
                            viewModel.saveBackup(
                                format = selectedFormat,
                                period = selectedPeriod,
                                customStartMillis = customStartMillis,
                                customEndMillis = customEndMillis,
                            )
                        },
                        enabled = canExport && !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(Dimens.ButtonHeight),
                        shape = RoundedCornerShape(Dimens.RadiusL),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primary,
                        ),
                        elevation = ButtonDefaults.buttonElevation(0.dp),
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.SaveAlt,
                                contentDescription = null,
                                modifier = Modifier.size(Dimens.IconSizeM),
                            )
                            Spacer(Modifier.width(Dimens.SpacingXs))
                            Text(
                                text = stringResource(R.string.export_save_backup),
                                style = typography.titleMedium,
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            viewModel.exportAndShare(
                                format = selectedFormat,
                                period = selectedPeriod,
                                customStartMillis = customStartMillis,
                                customEndMillis = customEndMillis,
                            )
                        },
                        enabled = canExport && !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(Dimens.ButtonHeight),
                        shape = RoundedCornerShape(Dimens.RadiusL),
                        border = BorderStroke(1.dp, colors.primary),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Share,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(Dimens.IconSizeM),
                        )
                        Spacer(Modifier.width(Dimens.SpacingXs))
                        Text(
                            text = stringResource(R.string.export_share),
                            color = colors.primary,
                            style = typography.titleMedium,
                        )
                    }
                }

                if (exportState is ExportState.Error) {
                    Text(
                        text = (exportState as ExportState.Error).message.ifBlank {
                            stringResource(R.string.export_error_generic)
                        },
                        style = typography.bodySmall,
                        color = colors.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = Dimens.SpacingS),
                    )
                }

                Text(
                    text = stringResource(R.string.export_hint),
                    style = typography.bodySmall,
                    color = colors.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Dimens.SpacingM),
                )
            }
        }
    }
}

@Composable
private fun ExportRadioRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(
                horizontal = Dimens.SpacingM,
                vertical = Dimens.SpacingS, // Чуть больше вертикального пространства для стиля iOS
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween, // Текст слева, галочка справа
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = colors.onSurface,
            modifier = Modifier.weight(1f)
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
    SimpleDateFormat(pattern, Locale.US).format(Date(millis))
