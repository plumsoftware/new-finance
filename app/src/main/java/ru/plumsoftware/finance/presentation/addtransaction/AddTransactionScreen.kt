package ru.plumsoftware.finance.presentation.addtransaction

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import org.koin.androidx.compose.koinViewModel
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.domain.model.TransactionType
import ru.plumsoftware.finance.presentation.common.Money
import ru.plumsoftware.finance.presentation.common.NBSP
import ru.plumsoftware.finance.ui.ds.AmountInput
import ru.plumsoftware.finance.ui.ds.AmountKeypad
import ru.plumsoftware.finance.ui.ds.ButtonPrimary
import ru.plumsoftware.finance.ui.ds.FChip
import ru.plumsoftware.finance.ui.ds.HSpace
import ru.plumsoftware.finance.ui.ds.Kopi
import ru.plumsoftware.finance.ui.ds.KopiImage
import ru.plumsoftware.finance.ui.ds.LocalMascotSnackbar
import ru.plumsoftware.finance.ui.ds.SegmentedLight
import ru.plumsoftware.finance.ui.ds.SubScreenAppBar
import ru.plumsoftware.finance.ui.ds.VSpace
import ru.plumsoftware.finance.ui.theme.FinanceTheme
import ru.plumsoftware.finance.ui.theme.FinanceType

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddTransactionScreen(
    onBack: () -> Unit,
    onCreateCategory: (TransactionType) -> Unit = {},
    viewModel: AddTransactionViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val saved by viewModel.saved.collectAsStateWithLifecycle()
    val snackbar = LocalMascotSnackbar.current
    val c = FinanceTheme.colors

    // Скан чека: камера → QR ФНС (§6.2).
    val scanPrompt = stringResource(R.string.add_scan_prompt)
    val scanner = rememberLauncherForActivityResult(ScanContract()) { result -> viewModel.onScanResult(result.contents) }
    val startScan = {
        viewModel.setScanning(true)
        scanner.launch(
            ScanOptions()
                .setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                .setPrompt(scanPrompt)
                .setBeepEnabled(false)
                .setOrientationLocked(false),
        )
    }
    var autoScanDone by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (viewModel.startWithScan && !autoScanDone) {
            autoScanDone = true
            startScan()
        }
    }

    LaunchedEffect(saved) {
        val result = saved ?: return@LaunchedEffect
        snackbar.show(result.message, if (result.overLimit) Kopi.THINKING else Kopi.HAPPY)
        onBack()
    }
    LaunchedEffect(state.errorMessage) {
        val msg = state.errorMessage ?: return@LaunchedEffect
        snackbar.show(msg, Kopi.THINKING)
        viewModel.clearError()
    }

    val isIncome = state.type == TransactionType.INCOME
    Column(
        Modifier
            .fillMaxSize()
            .background(c.bg)
            .navigationBarsPadding()
            .imePadding(),
    ) {
        SubScreenAppBar(
            title = stringResource(if (state.isEdit) R.string.add_title_edit else R.string.add_title),
            onBack = onBack,
            backIcon = R.drawable.ic_close,
            backDescription = stringResource(R.string.add_close),
        )
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            SegmentedLight(
                options = listOf(stringResource(R.string.add_expense), stringResource(R.string.add_income)),
                selectedIndex = if (isIncome) 1 else 0,
                onSelect = { viewModel.setType(if (it == 1) TransactionType.INCOME else TransactionType.EXPENSE) },
                onBackground = true,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            Box(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 86.dp)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                if (state.isScanning) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        KopiImage(Kopi.THINKING, 44.dp)
                        HSpace(8.dp)
                        Text(stringResource(R.string.add_scan_reading), style = FinanceType.title, color = c.textSecondary)
                    }
                } else {
                    Text(
                        text = "${AmountInput.display(state.input)}$NBSP${Money.symbol(state.currencyCode)}",
                        style = FinanceType.displayAmount,
                        color = if (isIncome) c.successText else c.textPrimary,
                        maxLines = 1,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            // Быстрый ввод: «📷 Чек» + недавние шаблоны.
            val scanChip: @Composable () -> Unit = {
                FChip(
                    text = "📷 ${stringResource(R.string.home_quick_scan)}",
                    selected = true,
                    onClick = { startScan() },
                    selectedBg = c.primary,
                )
            }
            if (state.expandRecent && state.templates.isNotEmpty()) {
                FlowRow(
                    Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    scanChip()
                    state.templates.forEach { t -> TemplateChip(t, state.currencyCode) { viewModel.applyTemplate(t) } }
                }
            } else {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    item { scanChip() }
                    items(state.templates) { t -> TemplateChip(t, state.currencyCode) { viewModel.applyTemplate(t) } }
                }
            }
            VSpace(12.dp)

            // Категории.
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(state.categories, key = { it.id }) { cat ->
                    FChip(
                        text = "${cat.icon} ${cat.name}",
                        selected = cat.id == state.selectedCategoryId,
                        onClick = { viewModel.selectCategory(cat.id) },
                        selectedBg = c.primary,
                    )
                }
                item {
                    FChip(
                        text = "＋",
                        selected = false,
                        onClick = { onCreateCategory(state.type) },
                        border = c.outline,
                    )
                }
            }
            VSpace(12.dp)

            // Счёт + заметка.
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AccountPicker(state, onSelect = viewModel::selectAccount)
                NoteField(state.note, viewModel::setNote, Modifier.weight(1f))
            }
            VSpace(12.dp)
        }

        Column(Modifier.padding(horizontal = 16.dp)) {
            AmountKeypad(onKey = viewModel::onKey)
            VSpace(12.dp)
            ButtonPrimary(
                text = if (state.amountMinor > 0) {
                    stringResource(R.string.add_save_amount, Money.format(state.amountMinor, state.currencyCode))
                } else {
                    stringResource(R.string.save)
                },
                onClick = viewModel::save,
                enabled = state.canSave,
                modifier = Modifier.fillMaxWidth(),
            )
            VSpace(12.dp)
        }
    }
}

@Composable
private fun TemplateChip(t: RecentTemplate, currency: String, onClick: () -> Unit) {
    FChip(
        text = "${t.emoji} ${t.title} · ${Money.format(t.amountMinor, currency)}",
        selected = false,
        onClick = onClick,
    )
}

@Composable
private fun AccountPicker(state: AddTransactionUiState, onSelect: (Long) -> Unit) {
    val c = FinanceTheme.colors
    var expanded by remember { mutableStateOf(false) }
    val current = state.accounts.find { it.id == state.selectedAccountId }
    Box {
        Row(
            Modifier
                .height(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(c.surface)
                .clickable(role = Role.Button) { if (state.accounts.size > 1) expanded = true }
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "${current?.emoji ?: "💳"} ${current?.name.orEmpty()}",
                style = FinanceType.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = c.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(max = 140.dp),
            )
            if (state.accounts.size > 1) {
                HSpace(4.dp)
                Icon(painterResource(R.drawable.ic_chevron_down), null, tint = c.textSecondary, modifier = Modifier.size(16.dp))
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, containerColor = c.surface) {
            state.accounts.forEach { acc ->
                DropdownMenuItem(
                    text = { Text("${acc.emoji} ${acc.name}", style = FinanceType.body, color = c.textPrimary) },
                    onClick = {
                        expanded = false
                        onSelect(acc.id)
                    },
                )
            }
        }
    }
}

@Composable
private fun NoteField(value: String, onChange: (String) -> Unit, modifier: Modifier) {
    val c = FinanceTheme.colors
    Box(
        modifier
            .height(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(c.surface)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        if (value.isEmpty()) {
            Text(stringResource(R.string.add_note_hint), style = FinanceType.bodySmall, color = c.textSecondary)
        }
        BasicTextField(
            value = value,
            onValueChange = onChange,
            singleLine = true,
            textStyle = FinanceType.bodySmall.copy(color = c.textPrimary, fontSize = 14.sp),
            cursorBrush = SolidColor(c.primary),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
