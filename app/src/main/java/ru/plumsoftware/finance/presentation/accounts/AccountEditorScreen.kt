package ru.plumsoftware.finance.presentation.accounts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.domain.model.AccountType
import ru.plumsoftware.finance.domain.model.localizedNameRes
import ru.plumsoftware.finance.ui.components.CurrencyPickerSheet
import ru.plumsoftware.finance.ui.components.IosCard
import ru.plumsoftware.finance.ui.components.IosPrimaryButton
import ru.plumsoftware.finance.ui.components.ios.IosChip
import ru.plumsoftware.finance.ui.components.ios.IosEditorTopBar
import ru.plumsoftware.finance.ui.theme.Dimens

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AccountEditorScreen(
    accountId: Long?,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: AccountEditorViewModel = koinViewModel { parametersOf(accountId) },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    var showCurrencyPicker by remember { mutableStateOf(false) }

    if (showCurrencyPicker) {
        CurrencyPickerSheet(
            selectedCode = state.currencyCode,
            onSelect = viewModel::setCurrency,
            onDismiss = { showCurrencyPicker = false },
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = {
            IosEditorTopBar(
                title = stringResource(
                    if (state.accountId > 0L) R.string.account_edit_title else R.string.account_create_title,
                ),
                backLabel = stringResource(R.string.accounts_title),
                onBack = onBack,
                actionLabel = stringResource(R.string.done),
                onAction = { viewModel.save(onSaved) },
                actionEnabled = state.name.isNotBlank(),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(Dimens.paddingMedium),
            verticalArrangement = Arrangement.spacedBy(Dimens.spacingList),
        ) {
            IosCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    EditorField(
                        label = stringResource(R.string.account_field_name),
                        value = state.name,
                        onValueChange = viewModel::setName,
                        placeholder = stringResource(R.string.account_field_name_hint),
                    )
                    HorizontalDivider(
                        color = colors.outline.copy(alpha = 0.35f),
                        modifier = Modifier.padding(vertical = Dimens.paddingSmall),
                    )
                    EditorField(
                        label = stringResource(R.string.account_field_currency),
                        value = state.currencyCode,
                        onClick = { showCurrencyPicker = true },
                        readOnly = true,
                    )
                    HorizontalDivider(
                        color = colors.outline.copy(alpha = 0.35f),
                        modifier = Modifier.padding(vertical = Dimens.paddingSmall),
                    )
                    Text(
                        text = stringResource(R.string.account_field_type),
                        style = typography.labelSmall,
                        color = colors.onSurfaceVariant,
                    )
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = Dimens.paddingSmall),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.paddingSmall),
                        verticalArrangement = Arrangement.spacedBy(Dimens.paddingSmall),
                    ) {
                        AccountType.entries.forEach { type ->
                            IosChip(
                                text = stringResource(type.localizedNameRes()),
                                selected = state.type == type,
                                onClick = { viewModel.setType(type) },
                            )
                        }
                    }
                }
            }
            IosPrimaryButton(
                text = stringResource(R.string.save),
                onClick = { viewModel.save(onSaved) },
                loading = state.isSaving,
                enabled = state.name.isNotBlank(),
            )
            if (state.isDefault) {
                Text(
                    text = stringResource(R.string.account_cannot_delete_default),
                    style = typography.bodySmall,
                    color = colors.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = Dimens.paddingMicro),
                )
            }
        }
    }
}

@Composable
private fun EditorField(
    label: String,
    value: String,
    onValueChange: ((String) -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    placeholder: String = "",
    readOnly: Boolean = false,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier,
            )
            .padding(vertical = Dimens.paddingSmall),
    ) {
        Text(label, style = typography.labelSmall, color = colors.onSurfaceVariant)
        if (readOnly || onClick != null) {
            Text(
                text = value.ifBlank { placeholder },
                style = typography.bodyLarge,
                modifier = Modifier.padding(top = Dimens.paddingMicro),
            )
        } else {
            BasicTextField(
                value = value,
                onValueChange = { onValueChange?.invoke(it) },
                textStyle = typography.bodyLarge.copy(color = colors.onSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Dimens.paddingMicro),
            )
        }
    }
}
