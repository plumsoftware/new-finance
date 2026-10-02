package ru.plumsoftware.finance.presentation.accounts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.domain.model.AccountWithBalance
import ru.plumsoftware.finance.presentation.common.Money
import ru.plumsoftware.finance.presentation.goals.colorFromHexOrDefault
import ru.plumsoftware.finance.ui.ds.CardDivider
import ru.plumsoftware.finance.ui.ds.EmojiBadge
import ru.plumsoftware.finance.ui.ds.HSpace
import ru.plumsoftware.finance.ui.ds.IconButton44
import ru.plumsoftware.finance.ui.ds.InkCard
import ru.plumsoftware.finance.ui.ds.LocalAmountVisibility
import ru.plumsoftware.finance.ui.ds.SubScreenAppBar
import ru.plumsoftware.finance.ui.ds.SwipeHint
import ru.plumsoftware.finance.ui.ds.SwipeHintButton
import ru.plumsoftware.finance.ui.ds.rememberSwipeHint
import ru.plumsoftware.finance.ui.ds.masked
import ru.plumsoftware.finance.ui.theme.FinanceTheme
import ru.plumsoftware.finance.ui.theme.FinanceType

/** Счета: общий баланс, выбор текущего счёта, редактирование, удаление свайпом. */
@Composable
fun AccountsScreen(
    onBack: () -> Unit,
    onAdd: () -> Unit,
    onEdit: (Long) -> Unit,
    viewModel: AccountsViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val c = FinanceTheme.colors
    var toDelete by remember { mutableStateOf<AccountWithBalance?>(null) }
    val showSwipeHint = rememberSwipeHint(SwipeHint.ACCOUNTS, hasItems = state.accounts.size > 1)

    toDelete?.let { row ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            containerColor = c.surface,
            title = { Text(stringResource(R.string.account_delete_title, row.account.name), style = FinanceType.titleLarge, color = c.textPrimary) },
            text = { Text(stringResource(R.string.account_delete_message), style = FinanceType.bodySmall, color = c.textSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteAccount(row.account.id)
                    toDelete = null
                }) { Text(stringResource(R.string.account_delete_confirm), color = c.dangerText) }
            },
            dismissButton = { TextButton(onClick = { toDelete = null }) { Text(stringResource(R.string.cancel), color = c.textSecondary) } },
        )
    }

    Column(Modifier.fillMaxSize().background(c.bg)) {
        SubScreenAppBar(
            title = stringResource(R.string.accounts_title),
            onBack = onBack,
            actions = {
                SwipeHintButton(showSwipeHint)
                IconButton44(R.drawable.ic_add, stringResource(R.string.accounts_add), onAdd)
            },
        )
        LazyColumn(
            Modifier.fillMaxSize().navigationBarsPadding(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                InkCard(radius = 24.dp) {
                    Text(stringResource(R.string.accounts_total), style = FinanceType.bodySmall, color = c.onInkSecondary)
                    val visibility = LocalAmountVisibility.current
                    Text(
                        masked(Money.format(state.totalBalanceMinor, state.currencyCode, forceFraction = true)),
                        style = FinanceType.headlineLarge,
                        color = Color.White,
                        modifier = Modifier.clickable(enabled = visibility.hidden, onClick = visibility.toggle),
                    )
                    Text(
                        pluralStringResource(R.plurals.pl_accounts, state.accounts.size, state.accounts.size),
                        style = FinanceType.caption,
                        color = c.onInkSecondary,
                    )
                }
            }
            item {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(c.surface),
                ) {
                    state.accounts.forEachIndexed { i, row ->
                        if (i > 0) CardDivider(Modifier.padding(start = 70.dp))
                        AccountRow(
                            row = row,
                            isSelected = row.account.id == state.selectedAccountId,
                            onSelect = { viewModel.selectAccount(row.account.id) },
                            onEdit = { onEdit(row.account.id) },
                            onDeleteRequest = { toDelete = row },
                        )
                    }
                }
            }
            item {
                Text(
                    stringResource(R.string.accounts_hint),
                    style = FinanceType.caption,
                    color = c.textSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountRow(
    row: AccountWithBalance,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDeleteRequest: () -> Unit,
) {
    val c = FinanceTheme.colors
    val account = row.account
    val swipe = rememberSwipeToDismissBoxState()
    LaunchedEffect(swipe.currentValue) {
        if (swipe.currentValue == SwipeToDismissBoxValue.EndToStart) {
            onDeleteRequest()
            swipe.reset()
        }
    }
    SwipeToDismissBox(
        state = swipe,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = !account.isDefault,
        backgroundContent = {
            Box(Modifier.fillMaxSize().background(c.danger).padding(horizontal = 20.dp), contentAlignment = Alignment.CenterEnd) {
                Text(stringResource(R.string.delete), style = FinanceType.title, color = Color.White)
            }
        },
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .background(c.surface)
                .heightIn(min = 68.dp)
                .clickable(role = Role.RadioButton, onClick = onSelect)
                .padding(start = 16.dp, end = 4.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            EmojiBadge(account.emoji, colorFromHexOrDefault(account.colorHex, c.primary), size = 42.dp)
            HSpace(12.dp)
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        account.name,
                        style = FinanceType.bodyMedium,
                        color = c.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    if (account.isDefault) {
                        HSpace(6.dp)
                        Text(
                            stringResource(R.string.account_default_badge),
                            style = FinanceType.caption.copy(fontWeight = FontWeight.SemiBold),
                            color = c.primaryTonalText,
                            modifier = Modifier
                                .background(c.primaryTonalBg, RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                        )
                    }
                }
                Text(
                    masked(Money.format(row.calculatedBalanceMinor, account.currencyCode, forceFraction = true)),
                    style = FinanceType.caption,
                    color = c.textSecondary,
                )
            }
            if (isSelected) {
                Icon(
                    painterResource(R.drawable.ic_check),
                    contentDescription = stringResource(R.string.accounts_selected),
                    tint = c.primary,
                    modifier = Modifier.size(22.dp),
                )
            }
            IconButton44(R.drawable.ic_edit, stringResource(R.string.accounts_edit), onEdit, tint = c.textSecondary)
        }
    }
}
