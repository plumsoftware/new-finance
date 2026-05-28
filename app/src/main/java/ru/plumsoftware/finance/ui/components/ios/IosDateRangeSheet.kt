package ru.plumsoftware.finance.ui.components.ios

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.data.util.endOfDayMillis
import ru.plumsoftware.finance.data.util.startOfDayMillis
import ru.plumsoftware.finance.ui.theme.Dimens
import java.util.Calendar

private enum class QuickRangePreset {
    THIS_WEEK,
    THIS_MONTH,
    LAST_MONTH,
    THIS_YEAR,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IosDateRangeSheet(
    onDismiss: () -> Unit,
    onConfirm: (startMillis: Long, endMillis: Long) -> Unit,
    initialStartMillis: Long? = null,
    initialEndMillis: Long? = null,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val shapes = MaterialTheme.shapes
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val pickerState = rememberDateRangePickerState(
        initialSelectedStartDateMillis = initialStartMillis,
        initialSelectedEndDateMillis = initialEndMillis,
    )

    LaunchedEffect(Unit) {
        sheetState.expand()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = Dimens.paddingLarge),
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = Dimens.paddingSmall)
                    .height(Dimens.bottomSheetHandleHeight)
                    .fillMaxWidth(0.14f)
                    .background(colors.outlineVariant, RoundedCornerShape(Dimens.cornerRadiusHandle)),
            )
            Text(
                text = stringResource(R.string.period_select_title),
                style = typography.titleLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.paddingLarge, vertical = Dimens.spacingRow),
            )
            QuickRanges(
                onRange = { start, end ->
                    onConfirm(start, end)
                    onDismiss()
                },
            )
            Spacer(modifier = Modifier.height(Dimens.paddingSmall))
            HorizontalDivider(
                color = colors.outline,
                thickness = Dimens.dividerThickness,
                modifier = Modifier.padding(horizontal = Dimens.paddingMedium),
            )
            Spacer(modifier = Modifier.height(Dimens.paddingMicro + 2.dp))
            DateRangePicker(
                state = pickerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.dateRangeCalendarHeight),
                title = null,
                headline = null,
                showModeToggle = false,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = colors.secondary,
                    dayInSelectionRangeContainerColor = colors.secondary.copy(alpha = 0.2f),
                ),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.paddingLarge),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IosTextButton(
                    text = stringResource(R.string.cancel),
                    onClick = onDismiss,
                )
                Spacer(modifier = Modifier.weight(1f))
                val canConfirm = pickerState.selectedStartDateMillis != null &&
                    pickerState.selectedEndDateMillis != null
                IosTextButton(
                    text = stringResource(R.string.done),
                    onClick = {
                        val start = pickerState.selectedStartDateMillis
                        val end = pickerState.selectedEndDateMillis
                        if (start != null && end != null) {
                            onConfirm(startOfDayMillis(start), endOfDayMillis(end))
                        }
                    },
                    enabled = canConfirm,
                    color = if (canConfirm) colors.secondary else colors.outlineVariant,
                    style = typography.labelLarge,
                )
            }
        }
    }
}

@Composable
private fun QuickRanges(
    onRange: (Long, Long) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val items = listOf(
        QuickRangePreset.THIS_WEEK to stringResource(R.string.period_this_week),
        QuickRangePreset.THIS_MONTH to stringResource(R.string.period_this_month),
        QuickRangePreset.LAST_MONTH to stringResource(R.string.period_last_month),
        QuickRangePreset.THIS_YEAR to stringResource(R.string.period_this_year),
    )
    Column(
        modifier = Modifier.padding(horizontal = Dimens.paddingMedium),
        verticalArrangement = Arrangement.spacedBy(Dimens.paddingSmall),
    ) {
        items.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.paddingSmall)) {
                row.forEach { (preset, label) ->
                    Surface(
                        shape = RoundedCornerShape(Dimens.cornerRadiusChip),
                        color = colors.surfaceVariant,
                        modifier = Modifier
                            .weight(1f)
                            .height(Dimens.dateRangePresetHeight)
                            .clickable {
                                val cal = Calendar.getInstance()
                                when (preset) {
                                    QuickRangePreset.THIS_WEEK -> {
                                        cal.firstDayOfWeek = Calendar.MONDAY
                                        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                                        val start = startOfDayMillis(cal.timeInMillis)
                                        cal.add(Calendar.DAY_OF_YEAR, 6)
                                        val end = endOfDayMillis(cal.timeInMillis)
                                        onRange(start, end)
                                    }
                                    QuickRangePreset.THIS_MONTH -> {
                                        cal.set(Calendar.DAY_OF_MONTH, 1)
                                        val start = startOfDayMillis(cal.timeInMillis)
                                        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
                                        onRange(start, endOfDayMillis(cal.timeInMillis))
                                    }
                                    QuickRangePreset.LAST_MONTH -> {
                                        cal.add(Calendar.MONTH, -1)
                                        cal.set(Calendar.DAY_OF_MONTH, 1)
                                        val start = startOfDayMillis(cal.timeInMillis)
                                        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
                                        onRange(start, endOfDayMillis(cal.timeInMillis))
                                    }
                                    QuickRangePreset.THIS_YEAR -> {
                                        cal.set(Calendar.DAY_OF_YEAR, 1)
                                        val start = startOfDayMillis(cal.timeInMillis)
                                        cal.set(Calendar.DAY_OF_YEAR, cal.getActualMaximum(Calendar.DAY_OF_YEAR))
                                        onRange(start, endOfDayMillis(cal.timeInMillis))
                                    }
                                }
                            },
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text(label, color = colors.onSurface, style = typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}
