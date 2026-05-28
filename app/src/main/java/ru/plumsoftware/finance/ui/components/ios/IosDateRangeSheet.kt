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
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.data.util.endOfDayMillis
import ru.plumsoftware.finance.data.util.startOfDayMillis
import ru.plumsoftware.finance.ui.theme.Dimens
import ru.plumsoftware.finance.ui.theme.IosBlue
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IosDateRangeSheet(
    onDismiss: () -> Unit,
    onConfirm: (startMillis: Long, endMillis: Long) -> Unit,
    initialStartMillis: Long? = null,
    initialEndMillis: Long? = null,
) {
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
        containerColor = Color.White,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = Dimens.paddingLarge),
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 8.dp)
                    .height(4.dp)
                    .fillMaxWidth(0.14f)
                    .background(Color(0xFFC7C7CC), RoundedCornerShape(2.dp)),
            )
            Text(
                text = "Выберите период",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.paddingLarge, vertical = 10.dp),
            )
            QuickRanges(
                onRange = { start, end ->
                    onConfirm(start, end)
                    onDismiss()
                },
            )
            Spacer(modifier = Modifier.height(Dimens.paddingSmall))
            HorizontalDivider(color = Color(0xFFE5E5EA), thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(modifier = Modifier.height(6.dp))
            DateRangePicker(
                state = pickerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp),
                title = null,
                headline = null,
                showModeToggle = false,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = IosBlue,
                    dayInSelectionRangeContainerColor = IosBlue.copy(alpha = 0.2f),
                ),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.paddingLarge),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.cancel), color = IosBlue)
                }
                Spacer(modifier = Modifier.weight(1f))
                TextButton(
                    onClick = {
                        val start = pickerState.selectedStartDateMillis
                        val end = pickerState.selectedEndDateMillis
                        if (start != null && end != null) {
                            onConfirm(startOfDayMillis(start), endOfDayMillis(end))
                        }
                    },
                    enabled = pickerState.selectedStartDateMillis != null &&
                        pickerState.selectedEndDateMillis != null,
                ) {
                    Text(
                        stringResource(R.string.done),
                        color = if (pickerState.selectedStartDateMillis != null &&
                            pickerState.selectedEndDateMillis != null
                        ) IosBlue else Color(0xFFC7C7CC),
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickRanges(
    onRange: (Long, Long) -> Unit,
) {
    val items = listOf("Эта неделя", "Этот месяц", "Прошлый месяц", "Этот год")
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { label ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF2F2F7),
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clickable {
                                val cal = Calendar.getInstance()
                                when (label) {
                                    "Эта неделя" -> {
                                        cal.firstDayOfWeek = Calendar.MONDAY
                                        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                                        val start = startOfDayMillis(cal.timeInMillis)
                                        cal.add(Calendar.DAY_OF_YEAR, 6)
                                        val end = endOfDayMillis(cal.timeInMillis)
                                        onRange(start, end)
                                    }
                                    "Этот месяц" -> {
                                        cal.set(Calendar.DAY_OF_MONTH, 1)
                                        val start = startOfDayMillis(cal.timeInMillis)
                                        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
                                        onRange(start, endOfDayMillis(cal.timeInMillis))
                                    }
                                    "Прошлый месяц" -> {
                                        cal.add(Calendar.MONTH, -1)
                                        cal.set(Calendar.DAY_OF_MONTH, 1)
                                        val start = startOfDayMillis(cal.timeInMillis)
                                        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
                                        onRange(start, endOfDayMillis(cal.timeInMillis))
                                    }
                                    "Этот год" -> {
                                        cal.set(Calendar.DAY_OF_YEAR, 1)
                                        val start = startOfDayMillis(cal.timeInMillis)
                                        cal.set(Calendar.DAY_OF_YEAR, cal.getActualMaximum(Calendar.DAY_OF_YEAR))
                                        onRange(start, endOfDayMillis(cal.timeInMillis))
                                    }
                                }
                            },
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text(label, color = Color.Black)
                        }
                    }
                }
            }
        }
    }
}
