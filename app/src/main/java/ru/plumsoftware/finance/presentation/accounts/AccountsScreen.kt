package ru.plumsoftware.finance.presentation.accounts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.domain.model.AccountWithBalance
import ru.plumsoftware.finance.presentation.common.MoneyFormat
import ru.plumsoftware.finance.ui.components.IosCard
import ru.plumsoftware.finance.ui.components.ios.IosEditorTopBar
import ru.plumsoftware.finance.ui.components.ios.IosTextButton
import ru.plumsoftware.finance.ui.theme.Dimens
import ru.plumsoftware.finance.ui.theme.IosRed

@Composable
fun AccountsScreen(
    onBack: () -> Unit,
    onAdd: () -> Unit,
    onEdit: (Long) -> Unit,
    viewModel: AccountsViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = {
            IosEditorTopBar(
                title = stringResource(R.string.accounts_title),
                backLabel = stringResource(R.string.accounts_back_settings),
                onBack = onBack,
                actionLabel = stringResource(R.string.accounts_add),
                onAction = onAdd,
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(Dimens.paddingMedium),
            verticalArrangement = Arrangement.spacedBy(Dimens.spacingList),
        ) {
            item {
                IosCard {
                    Text(
                        text = stringResource(R.string.accounts_total),
                        style = typography.labelSmall,
                        color = colors.onSurfaceVariant,
                    )
                    Text(
                        text = state.totalBalanceLabel,
                        style = typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            items(state.accounts, key = { it.account.id }) { row ->
                AccountRow(
                    row = row,
                    isSelected = row.account.id == state.selectedAccountId,
                    onSelect = { viewModel.selectAccount(row.account.id) },
                    onEdit = { onEdit(row.account.id) },
                    onDelete = { viewModel.deleteAccount(row.account.id) },
                )
            }
        }
    }
}

@Composable
private fun AccountRow(
    row: AccountWithBalance,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val account = row.account
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        Dialog(onDismissRequest = { showDeleteConfirm = false }) {
            Surface(
                shape = RoundedCornerShape(Dimens.cornerRadiusCard),
                color = colors.surface,
            ) {
                Column(modifier = Modifier.padding(Dimens.paddingMedium + Dimens.paddingMicro)) {
                    Text(
                        text = stringResource(R.string.account_delete_title, account.name),
                        style = typography.titleMedium,
                    )
                    Text(
                        text = stringResource(R.string.account_delete_message),
                        style = typography.bodyMedium,
                        color = colors.onSurfaceVariant,
                        modifier = Modifier.padding(top = Dimens.paddingSmall),
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = Dimens.paddingMedium),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        TextButton(onClick = { showDeleteConfirm = false }) {
                            Text(
                                text = stringResource(R.string.cancel),
                                color = colors.primary,
                            )
                        }
                        TextButton(
                            onClick = {
                                showDeleteConfirm = false
                                onDelete()
                            },
                        ) {
                            Text(
                                text = stringResource(R.string.account_delete_confirm),
                                color = IosRed,
                            )
                        }
                    }
                }
            }
        }
    }

    IosCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onSelect)
                .padding(vertical = Dimens.paddingMicro),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.spacingRow),
            ) {
                Text(account.emoji, style = typography.headlineSmall)
                Column {
                    Row(horizontalArrangement = Arrangement.spacedBy(Dimens.paddingMicro)) {
                        Text(account.name, style = typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                        if (account.isDefault) {
                            Text(
                                text = stringResource(R.string.account_default_badge),
                                style = typography.labelSmall,
                                color = colors.primary,
                            )
                        }
                    }
                    Text(
                        text = MoneyFormat.format(row.calculatedBalanceMinor, account.currencyCode),
                        style = typography.bodyMedium,
                        color = colors.onSurfaceVariant,
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isSelected) {
                    Text(
                        text = stringResource(R.string.checkmark),
                        color = colors.primary,
                        style = typography.titleMedium,
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = colors.onSurfaceVariant,
                    modifier = Modifier
                        .clickable(onClick = onEdit)
                        .padding(start = Dimens.paddingSmall),
                )
            }
        }
        if (isSelected && !account.isDefault) {
            HorizontalDivider(color = colors.outline.copy(alpha = 0.4f))
            IosTextButton(
                text = stringResource(R.string.delete),
                onClick = { showDeleteConfirm = true },
                color = colors.error,
                style = typography.labelLarge,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                contentPadding = PaddingValues(vertical = Dimens.spacingRow),
            )
        }
    }
}
