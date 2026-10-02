package ru.plumsoftware.finance.ui.ds

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.domain.analytics.DateRange
import ru.plumsoftware.finance.presentation.common.DateFmt
import ru.plumsoftware.finance.ui.theme.FinanceTheme
import ru.plumsoftware.finance.ui.theme.FinanceType
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

/** Логика выбора диапазона (§6.5 п.6). */
object RangeSelection {
    /** Первый тап — начало (конец сбрасывается), второй — конец; если раньше начала — меняются местами. */
    fun tap(start: LocalDate?, end: LocalDate?, day: LocalDate): Pair<LocalDate?, LocalDate?> = when {
        start == null || end != null -> day to null
        day.isBefore(start) -> day to start
        else -> start to day
    }
}

@Composable
fun PeriodPickerSheet(
    initial: DateRange?,
    minDate: LocalDate,
    onApply: (DateRange) -> Unit,
    onDismiss: () -> Unit,
    today: LocalDate = LocalDate.now(),
) {
    val c = FinanceTheme.colors
    var start by remember { mutableStateOf(initial?.start) }
    var end by remember { mutableStateOf(initial?.end) }
    var month by remember { mutableStateOf(YearMonth.from(initial?.end ?: today)) }
    val minMonth = YearMonth.from(minOf(minDate, today))
    val maxMonth = YearMonth.from(today)

    FBottomSheet(onDismiss = onDismiss) {
        Column(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp),
        ) {
            Text(stringResource(R.string.period_sheet_title), style = FinanceType.titleLarge.copy(fontWeight = FontWeight.Bold), color = c.textPrimary)
            VSpace(14.dp)
            val presets = listOf(
                stringResource(R.string.period_preset_7) to DateRange(today.minusDays(6), today),
                stringResource(R.string.period_preset_30) to DateRange(today.minusDays(29), today),
                stringResource(R.string.period_preset_3m) to DateRange(today.minusMonths(3).plusDays(1), today),
                stringResource(R.string.period_preset_ytd) to DateRange(LocalDate.of(today.year, 1, 1), today),
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(presets) { (label, range) ->
                    FChip(
                        text = label,
                        selected = start == range.start && end == range.end,
                        onClick = {
                            start = range.start
                            end = range.end
                            month = YearMonth.from(range.end)
                        },
                        selectedBg = c.primaryTonalBg,
                        selectedText = c.primaryTonalText,
                        border = c.outline,
                        height = 34.dp,
                    )
                }
            }
            VSpace(14.dp)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DateBox(stringResource(R.string.period_start), start, active = end == null || start == null, Modifier.weight(1f))
                DateBox(stringResource(R.string.period_end), end, active = start != null && end == null, Modifier.weight(1f))
            }
            VSpace(14.dp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton44(
                    R.drawable.ic_chevron_left,
                    stringResource(R.string.period_prev_month),
                    onClick = { if (month > minMonth) month = month.minusMonths(1) },
                    modifier = Modifier.alpha(if (month > minMonth) 1f else 0.3f),
                )
                Text(
                    DateFmt.monthYearCapitalized(month.atDay(1)),
                    style = FinanceType.title,
                    color = c.textPrimary,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
                IconButton44(
                    R.drawable.ic_chevron_right,
                    stringResource(R.string.period_next_month),
                    onClick = { if (month < maxMonth) month = month.plusMonths(1) },
                    modifier = Modifier.alpha(if (month < maxMonth) 1f else 0.3f),
                )
            }
            CalendarGrid(month, start, end, today) { day ->
                val (s, e) = RangeSelection.tap(start, end, day)
                start = s
                end = e
            }
            VSpace(14.dp)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ButtonOutlined(stringResource(R.string.cancel), onDismiss, Modifier.weight(1f), height = 50.dp)
                ButtonPrimary(
                    stringResource(R.string.period_apply),
                    onClick = { start?.let { s -> onApply(DateRange(s, end ?: s)) } },
                    enabled = start != null,
                    modifier = Modifier.weight(1f),
                    height = 50.dp,
                )
            }
            VSpace(12.dp)
        }
    }
}

@Composable
private fun DateBox(label: String, date: LocalDate?, active: Boolean, modifier: Modifier) {
    val c = FinanceTheme.colors
    Column(
        modifier
            .border(if (active) 1.5.dp else 1.dp, if (active) c.primary else c.outline, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Text(label, style = FinanceType.caption, color = c.textSecondary)
        Text(
            date?.let { DateFmt.dayMonth(it) + if (it.year != LocalDate.now().year) " ${it.year}" else "" }
                ?: stringResource(R.string.period_choose),
            style = FinanceType.title,
            color = if (date == null) c.textSecondary else c.textPrimary,
        )
    }
}

@Composable
private fun CalendarGrid(month: YearMonth, start: LocalDate?, end: LocalDate?, today: LocalDate, onTap: (LocalDate) -> Unit) {
    val c = FinanceTheme.colors
    val first = month.atDay(1)
    val offset = first.dayOfWeek.value - DayOfWeek.MONDAY.value
    val cells = (0 until offset).map { null } + (1..month.lengthOfMonth()).map { month.atDay(it) }
    Row(Modifier.fillMaxWidth()) {
        (0..6).forEach { i ->
            Text(
                DateFmt.weekdayShort(LocalDate.of(2024, 1, 1).plusDays(i.toLong())),
                style = FinanceType.caption,
                color = c.textSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
        }
    }
    VSpace(4.dp)
    cells.chunked(7).forEach { week ->
        Row(Modifier.fillMaxWidth()) {
            week.forEach { day -> DayCell(day, start, end, today, onTap, Modifier.weight(1f)) }
            repeat(7 - week.size) { Box(Modifier.weight(1f)) }
        }
    }
}

@Composable
private fun DayCell(day: LocalDate?, start: LocalDate?, end: LocalDate?, today: LocalDate, onTap: (LocalDate) -> Unit, modifier: Modifier) {
    val c = FinanceTheme.colors
    Box(modifier.height(40.dp), contentAlignment = Alignment.Center) {
        if (day == null) return@Box
        val isEdge = day == start || day == end
        val inRange = start != null && end != null && day.isAfter(start) && day.isBefore(end)
        val future = day.isAfter(today)
        // Полоса диапазона без скругления; края — полукруглая подложка к соседям.
        if (start != null && end != null && start != end) {
            when {
                inRange -> Box(Modifier.fillMaxWidth().height(40.dp).background(c.primaryTonalBg))
                day == start -> Row(Modifier.fillMaxWidth().height(40.dp)) {
                    Box(Modifier.weight(1f))
                    Box(Modifier.weight(1f).height(40.dp).background(c.primaryTonalBg))
                }
                day == end -> Row(Modifier.fillMaxWidth().height(40.dp)) {
                    Box(Modifier.weight(1f).height(40.dp).background(c.primaryTonalBg))
                    Box(Modifier.weight(1f))
                }
            }
        }
        Box(
            Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (isEdge) c.primary else Color.Transparent)
                .clickable(enabled = !future, role = Role.Button) { onTap(day) },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                day.dayOfMonth.toString(),
                style = FinanceType.body.copy(
                    fontWeight = if (isEdge || day == today) FontWeight.Bold else FontWeight.Normal,
                ),
                color = when {
                    isEdge -> Color.White
                    future -> c.textDisabled
                    day == today -> c.primary
                    else -> c.textPrimary
                },
            )
        }
    }
}
