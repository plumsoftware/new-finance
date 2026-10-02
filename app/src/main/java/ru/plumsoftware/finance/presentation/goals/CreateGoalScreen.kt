package ru.plumsoftware.finance.presentation.goals

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.ads.InterstitialPlacement
import ru.plumsoftware.finance.presentation.common.DateFmt
import ru.plumsoftware.finance.presentation.common.Money
import ru.plumsoftware.finance.presentation.dashboard.AmountEntrySheet
import ru.plumsoftware.finance.ui.ads.InterstitialAdEffect
import ru.plumsoftware.finance.ui.ds.BottomActionBar
import ru.plumsoftware.finance.ui.ds.EmojiPicker
import ru.plumsoftware.finance.ui.ds.FormSwitchRow
import ru.plumsoftware.finance.ui.ds.FormTextField
import ru.plumsoftware.finance.ui.ds.FormValueRow
import ru.plumsoftware.finance.ui.ds.CardDivider
import ru.plumsoftware.finance.ui.ds.FCard
import ru.plumsoftware.finance.ui.ds.FSwitch
import ru.plumsoftware.finance.ui.ds.SectionHeader
import ru.plumsoftware.finance.ui.ds.SubScreenAppBar
import ru.plumsoftware.finance.ui.ds.VSpace
import ru.plumsoftware.finance.ui.theme.FinanceTheme
import ru.plumsoftware.finance.ui.theme.FinanceType
import java.util.Calendar

private enum class GoalAmountField { TARGET, SAVED }

/**
 * Новая цель / изменение цели. Кнопка «Готово» — в нижней закреплённой панели,
 * сетка иконок — в прокручиваемой области (ТЗ §2 п.10).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateGoalScreen(
    onBack: () -> Unit,
    @Suppress("UNUSED_PARAMETER") backLabel: String = "",
    viewModel: CreateGoalViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val c = FinanceTheme.colors
    val context = LocalContext.current
    var amountField by rememberSaveable { mutableStateOf<GoalAmountField?>(null) }
    val accent = colorFromHexOrDefault(state.colorHex, c.primary)
    val targetMinor = Money.fromMajor(state.targetDigits.toDoubleOrNull() ?: 0.0, state.currencyCode)
    val savedMinor = Money.fromMajor(state.savedDigits.toDoubleOrNull() ?: 0.0, state.currencyCode)

    InterstitialAdEffect(placement = InterstitialPlacement.GOAL, trigger = state.saved, onContinue = onBack)

    amountField?.let { field ->
        AmountEntrySheet(
            title = stringResource(if (field == GoalAmountField.TARGET) R.string.goal_field_target else R.string.goal_field_saved),
            message = null,
            confirmLabel = stringResource(R.string.save),
            dismissLabel = stringResource(R.string.cancel),
            initialMinor = if (field == GoalAmountField.TARGET) targetMinor else savedMinor,
            currencyCode = state.currencyCode,
            allowZero = field == GoalAmountField.SAVED,
            onConfirm = { minor ->
                val digits = Money.toMajor(minor, state.currencyCode).stripTrailingZeros().toPlainString()
                if (field == GoalAmountField.TARGET) viewModel.setTargetDigits(digits) else viewModel.setSavedDigits(digits)
                amountField = null
            },
            onDismiss = { amountField = null },
        )
    }

    val openDatePicker = {
        val cal = Calendar.getInstance().apply { timeInMillis = state.deadlineMillis ?: System.currentTimeMillis() }
        DatePickerDialog(
            context,
            { _, y, m, d -> viewModel.setDeadline(Calendar.getInstance().apply { set(y, m, d, 12, 0, 0) }.timeInMillis) },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH),
        ).apply { datePicker.minDate = System.currentTimeMillis() }.show()
    }

    Column(Modifier.fillMaxSize().background(c.bg).imePadding()) {
        SubScreenAppBar(
            title = stringResource(if (state.isEdit) R.string.goal_edit_title else R.string.goal_create_title),
            onBack = onBack,
        )
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Превью.
            Column(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier.size(84.dp).background(accent.copy(alpha = 0x22 / 255f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) { Text(state.emoji, fontSize = 40.sp) }
                VSpace(8.dp)
                Text(
                    state.name.ifBlank { stringResource(R.string.goal_field_name_placeholder) },
                    style = FinanceType.title,
                    color = if (state.name.isBlank()) c.textSecondary else c.textPrimary,
                )
            }

            SectionHeader(stringResource(R.string.goal_section_main))
            FCard {
                FormTextField(state.name, viewModel::setName, stringResource(R.string.goal_field_name_placeholder))
                CardDivider(Modifier.padding(vertical = 8.dp))
                FormTextField(state.note, viewModel::setNote, stringResource(R.string.goal_field_note_placeholder))
            }

            SectionHeader(stringResource(R.string.goal_section_finance))
            FCard {
                FormValueRow(
                    stringResource(R.string.goal_field_target),
                    if (targetMinor > 0) Money.formatRounded(targetMinor, state.currencyCode) else stringResource(R.string.period_choose),
                    onClick = { amountField = GoalAmountField.TARGET },
                    valueColor = if (targetMinor > 0) c.primary else c.textSecondary,
                )
                CardDivider()
                FormValueRow(
                    stringResource(R.string.goal_field_saved),
                    Money.formatRounded(savedMinor, state.currencyCode),
                    onClick = { amountField = GoalAmountField.SAVED },
                )
            }

            SectionHeader(stringResource(R.string.goal_section_icon))
            FCard {
                EmojiPicker(viewModel.availableEmojis(), state.emoji, viewModel::setEmoji)
            }

            SectionHeader(stringResource(R.string.goal_section_color))
            FCard {
                FlowRow(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    goalColorPalette.forEach { hex ->
                        val color = colorFromHexOrDefault(hex, c.primary)
                        val selected = state.colorHex == hex
                        Box(
                            Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(color)
                                .then(if (selected) Modifier.border(3.dp, c.surface, CircleShape) else Modifier)
                                .clickable(role = Role.RadioButton) { viewModel.setColor(hex) }
                                .semantics {
                                    this.selected = selected
                                    contentDescription = hex
                                },
                        )
                    }
                }
            }

            SectionHeader(stringResource(R.string.goal_section_deadline))
            FCard {
                FormSwitchRow(stringResource(R.string.goal_field_deadline_toggle), state.hasDeadline) { enabled ->
                    viewModel.toggleDeadline(enabled)
                    if (enabled) openDatePicker()
                }
                val deadline = state.deadlineMillis
                if (state.hasDeadline && deadline != null) {
                    CardDivider()
                    val date = DateFmt.toLocalDate(deadline)
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .heightIn(min = 52.dp)
                            .clickable(role = Role.Button, onClick = openDatePicker),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(stringResource(R.string.goal_deadline_label), style = FinanceType.body, color = c.textPrimary, modifier = Modifier.weight(1f))
                        Text("${DateFmt.dayMonth(date)} ${date.year}", style = FinanceType.body, color = c.primary)
                    }
                }
                CardDivider()
                FormSwitchRow(stringResource(R.string.goal_field_show_on_home), state.showOnHome, onChange = viewModel::setShowOnHome)
            }
            VSpace(16.dp)
        }

        BottomActionBar(
            text = stringResource(if (state.isEdit) R.string.save else R.string.goal_save_button),
            onClick = viewModel::save,
            enabled = state.canSave && !state.isSaving,
        )
    }
}
