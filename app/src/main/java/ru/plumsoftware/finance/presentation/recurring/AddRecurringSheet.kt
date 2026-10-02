package ru.plumsoftware.finance.presentation.recurring

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.domain.budget.Upcoming
import ru.plumsoftware.finance.domain.model.Category
import ru.plumsoftware.finance.domain.model.CategoryType
import ru.plumsoftware.finance.domain.model.RecurringFrequency
import ru.plumsoftware.finance.domain.model.RecurringTransaction
import ru.plumsoftware.finance.presentation.common.DateFmt
import ru.plumsoftware.finance.presentation.common.Money
import ru.plumsoftware.finance.presentation.common.NBSP
import ru.plumsoftware.finance.ui.ds.AmountInput
import ru.plumsoftware.finance.ui.ds.AmountKeypad
import ru.plumsoftware.finance.ui.ds.ButtonPrimary
import ru.plumsoftware.finance.ui.ds.FBottomSheet
import ru.plumsoftware.finance.ui.ds.FChip
import ru.plumsoftware.finance.ui.ds.FormTextField
import ru.plumsoftware.finance.ui.ds.SectionHeader
import ru.plumsoftware.finance.ui.ds.SegmentedLight
import ru.plumsoftware.finance.ui.ds.VSpace
import ru.plumsoftware.finance.ui.theme.FinanceTheme
import ru.plumsoftware.finance.ui.theme.FinanceType
import java.time.LocalDate

/**
 * Новая повторяющаяся операция: название, расход/доход, сумма с клавиатурой §6.2,
 * категория, периодичность и число месяца. Первое списание — ближайшая выбранная дата.
 */
@Composable
fun AddRecurringSheet(
    categories: List<Category>,
    currencyCode: String,
    onDismiss: () -> Unit,
    onSave: (RecurringTransaction) -> Unit,
) {
    val c = FinanceTheme.colors
    var title by rememberSaveable { mutableStateOf("") }
    var input by rememberSaveable { mutableStateOf("") }
    var isIncome by rememberSaveable { mutableStateOf(false) }
    var categoryId by rememberSaveable { mutableStateOf<Long?>(null) }
    var frequency by rememberSaveable { mutableStateOf(RecurringFrequency.MONTHLY) }
    var day by rememberSaveable { mutableIntStateOf(LocalDate.now().dayOfMonth) }
    val type = if (isIncome) CategoryType.INCOME else CategoryType.EXPENSE
    val filtered = categories.filter { it.type == type }
    val amountMinor = AmountInput.toMinor(input)
    val today = LocalDate.now()
    val firstDate = if (frequency == RecurringFrequency.MONTHLY) Upcoming.firstMonthlyDate(today, day) else today
    val canSave = title.isNotBlank() && amountMinor > 0 && categoryId != null

    FBottomSheet(onDismiss = onDismiss) {
        Column(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            Text(stringResource(R.string.add_recurring), style = FinanceType.titleLarge.copy(fontWeight = FontWeight.Bold), color = c.textPrimary)
            VSpace(12.dp)
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(c.bg, RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 4.dp),
            ) { FormTextField(title, { title = it.take(40) }, stringResource(R.string.recurring_title_hint)) }
            VSpace(12.dp)
            SegmentedLight(
                options = listOf(stringResource(R.string.add_expense), stringResource(R.string.add_income)),
                selectedIndex = if (isIncome) 1 else 0,
                onSelect = { i ->
                    isIncome = i == 1
                    categoryId = null
                },
            )
            Text(
                "${AmountInput.display(input)}$NBSP${Money.symbol(currencyCode)}",
                style = FinanceType.displayAmount,
                color = if (isIncome) c.successText else c.textPrimary,
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(vertical = 10.dp),
                maxLines = 1,
            )

            SectionHeader(stringResource(R.string.category))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(vertical = 4.dp)) {
                items(filtered, key = { it.id }) { cat ->
                    FChip("${cat.icon} ${cat.name}", selected = cat.id == categoryId, onClick = { categoryId = cat.id }, selectedBg = c.primary, unselectedBg = c.bg)
                }
            }

            SectionHeader(stringResource(R.string.frequency))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(vertical = 4.dp)) {
                items(RecurringFrequency.entries) { f ->
                    FChip(stringResource(f.labelRes), selected = f == frequency, onClick = { frequency = f }, selectedBg = c.primary, unselectedBg = c.bg)
                }
            }
            if (frequency == RecurringFrequency.MONTHLY) {
                SectionHeader(stringResource(R.string.day_of_month))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp), contentPadding = PaddingValues(vertical = 4.dp)) {
                    items((1..31).toList()) { d ->
                        FChip(d.toString(), selected = d == day, onClick = { day = d }, selectedBg = c.primary, unselectedBg = c.bg)
                    }
                }
            }
            VSpace(6.dp)
            Text(
                stringResource(R.string.recurring_first_charge, DateFmt.dayMonth(firstDate)),
                style = FinanceType.caption,
                color = c.textSecondary,
            )
            VSpace(12.dp)
            AmountKeypad(onKey = { input = AmountInput.press(input, it) })
            VSpace(12.dp)
            ButtonPrimary(
                text = stringResource(R.string.save),
                enabled = canSave,
                modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp),
                onClick = {
                    onSave(
                        RecurringTransaction(
                            title = title.trim(),
                            amountMinor = amountMinor,
                            categoryId = categoryId!!,
                            isIncome = isIncome,
                            frequency = frequency,
                            dayOfMonth = if (frequency == RecurringFrequency.MONTHLY) day else null,
                            nextDateMillis = DateFmt.startOfDay(firstDate),
                            isActive = true,
                        ),
                    )
                },
            )
            VSpace(12.dp)
        }
    }
}
