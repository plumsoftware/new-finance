package ru.plumsoftware.finance.presentation.accounts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.domain.model.AccountType
import ru.plumsoftware.finance.domain.model.localizedNameRes
import ru.plumsoftware.finance.domain.util.SupportedCurrencies
import ru.plumsoftware.finance.presentation.common.Money
import ru.plumsoftware.finance.presentation.dashboard.AmountEntrySheet
import ru.plumsoftware.finance.presentation.goals.colorFromHexOrDefault
import ru.plumsoftware.finance.presentation.goals.goalColorPalette
import ru.plumsoftware.finance.ui.components.CurrencyPickerSheet
import ru.plumsoftware.finance.ui.ds.BottomActionBar
import ru.plumsoftware.finance.ui.ds.CardDivider
import ru.plumsoftware.finance.ui.ds.EmojiPicker
import ru.plumsoftware.finance.ui.ds.FCard
import ru.plumsoftware.finance.ui.ds.FChip
import ru.plumsoftware.finance.ui.ds.FormTextField
import ru.plumsoftware.finance.ui.ds.FormValueRow
import ru.plumsoftware.finance.ui.ds.SectionHeader
import ru.plumsoftware.finance.ui.ds.SubScreenAppBar
import ru.plumsoftware.finance.ui.ds.VSpace
import ru.plumsoftware.finance.ui.theme.FinanceTheme
import ru.plumsoftware.finance.ui.theme.FinanceType

private val AccountEmojis = listOf("💳", "🏦", "💵", "💰", "🪙", "🐷", "💼", "👛", "🏠", "🚗", "✈️", "🎁")

/** Новый счёт / изменение счёта: название, тип, валюта, начальный баланс, иконка и цвет. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AccountEditorScreen(
    accountId: Long?,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: AccountEditorViewModel = koinViewModel { parametersOf(accountId) },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val c = FinanceTheme.colors
    var showCurrency by rememberSaveable { mutableStateOf(false) }
    var showBalance by rememberSaveable { mutableStateOf(false) }
    val accent = colorFromHexOrDefault(state.colorHex, c.primary)

    if (showCurrency) {
        CurrencyPickerSheet(
            selectedCode = state.currencyCode,
            onSelect = viewModel::setCurrency,
            onDismiss = { showCurrency = false },
        )
    }
    if (showBalance) {
        AmountEntrySheet(
            title = stringResource(R.string.account_initial_balance),
            message = stringResource(R.string.account_initial_balance_hint),
            confirmLabel = stringResource(R.string.save),
            dismissLabel = stringResource(R.string.cancel),
            initialMinor = state.initialBalanceMinor,
            currencyCode = state.currencyCode,
            allowZero = true,
            onConfirm = {
                viewModel.setInitialBalance(it)
                showBalance = false
            },
            onDismiss = { showBalance = false },
        )
    }

    Column(Modifier.fillMaxSize().background(c.bg).imePadding()) {
        SubScreenAppBar(
            title = stringResource(if (state.accountId > 0L) R.string.account_edit_title else R.string.account_create_title),
            onBack = onBack,
        )
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Column(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier.size(84.dp).background(accent.copy(alpha = 0x22 / 255f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) { Text(state.emoji, fontSize = 40.sp) }
                VSpace(8.dp)
                Text(
                    state.name.ifBlank { stringResource(R.string.account_field_name_hint) },
                    style = FinanceType.title,
                    color = if (state.name.isBlank()) c.textSecondary else c.textPrimary,
                )
            }

            SectionHeader(stringResource(R.string.account_section_main))
            FCard {
                FormTextField(state.name, viewModel::setName, stringResource(R.string.account_field_name_hint))
                CardDivider(Modifier.padding(vertical = 8.dp))
                val currency = SupportedCurrencies.find(state.currencyCode)
                FormValueRow(
                    stringResource(R.string.account_field_currency),
                    "${Money.symbol(state.currencyCode)} ${currency?.let { stringResource(it.nameRes) } ?: state.currencyCode}",
                    onClick = { showCurrency = true },
                )
                CardDivider()
                FormValueRow(
                    stringResource(R.string.account_initial_balance),
                    Money.format(state.initialBalanceMinor, state.currencyCode, forceFraction = true),
                    onClick = { showBalance = true },
                )
            }

            SectionHeader(stringResource(R.string.account_field_type))
            FCard {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AccountType.entries.forEach { type ->
                        FChip(
                            stringResource(type.localizedNameRes()),
                            selected = state.type == type,
                            onClick = { viewModel.setType(type) },
                            selectedBg = c.primary,
                            unselectedBg = c.bg,
                        )
                    }
                }
            }

            SectionHeader(stringResource(R.string.goal_section_icon))
            FCard { EmojiPicker((listOf(state.emoji) + AccountEmojis).distinct().take(12), state.emoji, viewModel::setEmoji) }

            SectionHeader(stringResource(R.string.goal_section_color))
            FCard {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    goalColorPalette.take(15).forEach { hex ->
                        val color = colorFromHexOrDefault(hex, c.primary)
                        val selected = state.colorHex.equals(hex, ignoreCase = true)
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

            if (state.isDefault) {
                Text(
                    stringResource(R.string.account_cannot_delete_default),
                    style = FinanceType.caption,
                    color = c.textSecondary,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
            }
            VSpace(16.dp)
        }
        BottomActionBar(
            text = stringResource(R.string.save),
            onClick = { viewModel.save(onSaved) },
            enabled = state.name.isNotBlank() && !state.isSaving,
        )
    }
}
