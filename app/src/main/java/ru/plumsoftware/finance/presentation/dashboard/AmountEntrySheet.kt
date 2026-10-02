package ru.plumsoftware.finance.presentation.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.plumsoftware.finance.presentation.common.Money
import ru.plumsoftware.finance.presentation.common.NBSP
import ru.plumsoftware.finance.ui.ds.AmountInput
import ru.plumsoftware.finance.ui.ds.AmountKeypad
import ru.plumsoftware.finance.ui.ds.ButtonOutlined
import ru.plumsoftware.finance.ui.ds.ButtonPrimary
import ru.plumsoftware.finance.ui.ds.FBottomSheet
import ru.plumsoftware.finance.ui.ds.VSpace
import ru.plumsoftware.finance.ui.theme.FinanceTheme
import ru.plumsoftware.finance.ui.theme.FinanceType

/**
 * Нижняя панель ввода суммы с клавиатурой §6.2 (начальный баланс §6.14, бюджет месяца, лимит категории).
 */
@Composable
fun AmountEntrySheet(
    title: String,
    message: String?,
    confirmLabel: String,
    dismissLabel: String,
    onConfirm: (Long) -> Unit,
    onDismiss: () -> Unit,
    initialMinor: Long = 0L,
    currencyCode: String = "RUB",
    allowZero: Boolean = false,
    secondaryAction: Pair<String, () -> Unit>? = null,
) {
    val c = FinanceTheme.colors
    var input by rememberSaveable { mutableStateOf(AmountInput.fromMinor(initialMinor)) }
    val minor = AmountInput.toMinor(input)
    FBottomSheet(onDismiss = onDismiss) {
        Column(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(title, style = FinanceType.titleLarge, color = c.textPrimary, textAlign = TextAlign.Center)
            if (message != null) {
                VSpace(6.dp)
                Text(message, style = FinanceType.bodySmall, color = c.textSecondary, textAlign = TextAlign.Center)
            }
            VSpace(16.dp)
            Text(
                "${AmountInput.display(input)}$NBSP${Money.symbol(currencyCode)}",
                style = FinanceType.displayAmount,
                color = c.textPrimary,
                maxLines = 1,
            )
            VSpace(16.dp)
            AmountKeypad(onKey = { input = AmountInput.press(input, it) })
            VSpace(16.dp)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ButtonOutlined(dismissLabel, onDismiss, Modifier.weight(1f), height = 54.dp)
                ButtonPrimary(
                    confirmLabel,
                    onClick = { onConfirm(minor) },
                    modifier = Modifier.weight(1f),
                    enabled = allowZero || minor > 0,
                )
            }
            if (secondaryAction != null) {
                VSpace(4.dp)
                ru.plumsoftware.finance.ui.ds.TextAction(secondaryAction.first, secondaryAction.second, color = c.dangerText)
            }
            VSpace(8.dp)
        }
    }
}
