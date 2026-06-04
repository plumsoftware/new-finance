package ru.plumsoftware.finance.presentation.goals

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.domain.model.Goal
import ru.plumsoftware.finance.domain.model.daysLeft
import ru.plumsoftware.finance.domain.model.isOverdue
import ru.plumsoftware.finance.domain.model.progress
import ru.plumsoftware.finance.domain.model.remainingMinor
import ru.plumsoftware.finance.presentation.common.MoneyFormat
import ru.plumsoftware.finance.ui.components.FinanceNumPad
import ru.plumsoftware.finance.ui.components.IosCard
import ru.plumsoftware.finance.ui.components.IosPrimaryButton
import ru.plumsoftware.finance.ui.components.ios.IosEditorTopBar
import ru.plumsoftware.finance.ui.theme.Dimens
import ru.plumsoftware.finance.ui.theme.IosRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalDetailScreen(
    goalId: Long,
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
    onDeleted: () -> Unit,
    viewModel: GoalDetailViewModel = koinViewModel { parametersOf(goalId) },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val goal = state.goal
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (state.showCelebration && goal != null) {
        GoalCelebrationDialog(
            title = androidx.compose.ui.res.stringResource(R.string.goal_completed_title),
            message = androidx.compose.ui.res.stringResource(
                R.string.goal_completed_message,
                MoneyFormat.format(goal.targetAmountMinor, state.currencyCode),
                goal.name,
            ),
            onDismiss = viewModel::dismissCelebration,
        )
    }

    if (showDeleteDialog && goal != null) {
        GoalDeleteDialog(
            goalName = goal.name,
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                showDeleteDialog = false
                viewModel.deleteGoal(onDeleted)
            },
        )
    }

    val selectedDeposit = state.selectedDeposit
    if (state.showDepositDetail && selectedDeposit != null && goal != null) {
        GoalDepositDetailSheet(
            deposit = selectedDeposit,
            goal = goal,
            currencyCode = state.currencyCode,
            accountName = selectedDeposit.accountId?.let { state.accountNames[it] },
            onDismiss = viewModel::closeDepositDetail,
            onDelete = { viewModel.deleteDeposit(selectedDeposit) },
        )
    }

    if (state.showDepositSheet && goal != null) {
        val goalColor = colorFromHexOrDefault(goal.colorHex, colors.primary)
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = viewModel::closeDepositSheet,
            sheetState = sheetState,
            containerColor = colors.background,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.spacingSection)
                    .padding(top = Dimens.paddingSmall)
                    .padding(bottom = Dimens.bottomSheetBottomPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = androidx.compose.ui.res.stringResource(R.string.goal_deposit_title),
                    style = typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(Dimens.paddingMicro))
                Text(
                    text = goal.name,
                    style = typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                )
                Spacer(Modifier.height(Dimens.paddingMedium))
                Text(
                    text = MoneyFormat.formatEntryDisplay(state.amountDigits, state.currencyCode),
                    style = typography.displaySmall,
                    color = goalColor,
                )
                Spacer(Modifier.height(Dimens.spacingList))

                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.paddingSmall)) {
                    listOf(
                        R.string.goal_deposit_quick_10 to 0.1f,
                        R.string.goal_deposit_quick_25 to 0.25f,
                        R.string.goal_deposit_quick_50 to 0.5f,
                    ).forEach { (labelRes, fraction) ->
                        val quickAmount = (goal.remainingMinor * fraction).toLong().coerceAtLeast(0L)
                        Box(
                            modifier = Modifier
                                .background(goalColor.copy(alpha = 0.1f), RoundedCornerShape(Dimens.cornerRadiusChip))
                                .clickable { viewModel.setDepositAmount(quickAmount) }
                                .padding(horizontal = Dimens.spacingRow + Dimens.paddingMicro, vertical = Dimens.paddingSmall - Dimens.paddingMicro / 2),
                        ) {
                            Text(
                                text = androidx.compose.ui.res.stringResource(labelRes),
                                style = typography.labelLarge,
                                color = goalColor,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
                Spacer(Modifier.height(Dimens.spacingList))
                FinanceNumPad(
                    onDigit = viewModel::appendDepositDigit,
                    onBackspace = viewModel::backspaceDeposit,
                )
                Spacer(Modifier.height(Dimens.spacingList))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.surface, RoundedCornerShape(Dimens.cornerRadiusChip))
                        .padding(horizontal = Dimens.spacingRow + Dimens.paddingMicro, vertical = Dimens.spacingList),
                ) {
                    if (state.depositNote.isEmpty()) {
                        Text(
                            text = androidx.compose.ui.res.stringResource(R.string.goal_deposit_note_placeholder),
                            color = colors.outlineVariant,
                            style = typography.bodyMedium,
                        )
                    }
                    BasicTextField(
                        value = state.depositNote,
                        onValueChange = viewModel::setDepositNote,
                        textStyle = TextStyle(
                            color = colors.onSurface,
                            fontSize = typography.bodyMedium.fontSize,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                Spacer(Modifier.height(Dimens.spacingList))
                IosPrimaryButton(
                    text = androidx.compose.ui.res.stringResource(R.string.goal_deposit_confirm),
                    onClick = viewModel::confirmDeposit,
                    loading = state.isSaving,
                    enabled = state.amountDigits.isNotBlank(),
                )
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = {
            IosEditorTopBar(
                title = androidx.compose.ui.res.stringResource(R.string.goal_detail_title),
                backLabel = androidx.compose.ui.res.stringResource(R.string.goals_back_list),
                onBack = onBack,
                actionLabel = androidx.compose.ui.res.stringResource(R.string.goal_detail_edit),
                onAction = { goal?.let { onEdit(it.id) } },
                actionEnabled = goal != null,
            )
        },
    ) { padding ->
        if (goal == null) return@Scaffold
        val goalColor = colorFromHexOrDefault(goal.colorHex, colors.primary)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            androidx.compose.foundation.lazy.LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = Dimens.paddingMedium, vertical = Dimens.spacingRow),
                verticalArrangement = Arrangement.spacedBy(Dimens.spacingList),
            ) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = Dimens.paddingLarge, bottom = Dimens.paddingSmall),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(
                            modifier = Modifier.size(120.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(
                                progress = { goal.progress },
                                modifier = Modifier.fillMaxSize(),
                                color = goalColor,
                                trackColor = colors.outline,
                                strokeWidth = Dimens.paddingSmall,
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(goal.emoji, style = typography.displaySmall)
                                Text(
                                    text = "${(goal.progress * 100).toInt()}%",
                                    style = typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                        Spacer(Modifier.height(Dimens.spacingList))
                        Text(
                            text = MoneyFormat.format(goal.savedAmountMinor, state.currencyCode),
                            style = typography.displaySmall,
                            color = goalColor,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = androidx.compose.ui.res.stringResource(
                                R.string.goal_saved_of,
                                MoneyFormat.format(goal.savedAmountMinor, state.currencyCode),
                                MoneyFormat.format(goal.targetAmountMinor, state.currencyCode),
                            ),
                            style = typography.bodyMedium,
                            color = colors.onSurfaceVariant,
                        )
                    }
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.spacingList),
                    ) {
                        IosCard(modifier = Modifier.weight(1f)) {
                            Text(
                                text = androidx.compose.ui.res.stringResource(R.string.goal_detail_remaining),
                                style = typography.labelSmall,
                                color = colors.onSurfaceVariant,
                            )
                            Text(
                                text = MoneyFormat.format(goal.remainingMinor, state.currencyCode),
                                style = typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = IosRed,
                            )
                        }
                        IosCard(modifier = Modifier.weight(1f)) {
                            Text(
                                text = androidx.compose.ui.res.stringResource(R.string.goal_section_deadline),
                                style = typography.labelSmall,
                                color = colors.onSurfaceVariant,
                            )
                            val left = goal.daysLeft
                            Text(
                                text = left?.let { androidx.compose.ui.res.stringResource(R.string.goal_days_left, it) }
                                    ?: androidx.compose.ui.res.stringResource(R.string.goal_no_deadline),
                                style = typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = goalDeadlineColor(goal.isOverdue, left, colors.onSurface),
                            )
                        }
                    }
                }
                if (goal.daysLeft != null && (goal.daysLeft ?: 0) > 0 && !goal.isCompleted) {
                    item {
                        val days = goal.daysLeft ?: 1
                        val dailyNeeded = (goal.remainingMinor / days).coerceAtLeast(0L)
                        IosCard {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("💡", style = typography.titleMedium)
                                Spacer(Modifier.width(Dimens.spacingRow))
                                Text(
                                    text = androidx.compose.ui.res.stringResource(
                                        R.string.goal_detail_daily_needed,
                                        MoneyFormat.format(dailyNeeded, state.currencyCode),
                                    ),
                                    style = typography.bodyMedium,
                                    color = colors.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
                item {
                    IosPrimaryButton(
                        text = androidx.compose.ui.res.stringResource(R.string.goal_deposit_button),
                        onClick = viewModel::openDepositSheet,
                    )
                }
                item {
                    Text(
                        text = androidx.compose.ui.res.stringResource(R.string.goal_detail_deposited),
                        style = typography.labelSmall,
                        color = colors.onSurfaceVariant,
                        modifier = Modifier.padding(start = Dimens.spacingRow, top = Dimens.paddingSmall, bottom = Dimens.paddingMicro),
                    )
                }
                item {
                    IosCard {
                        if (state.deposits.isEmpty()) {
                            Text(
                                text = androidx.compose.ui.res.stringResource(R.string.goal_detail_history_empty),
                                style = typography.bodyMedium,
                                color = colors.onSurfaceVariant,
                            )
                        } else {
                            state.deposits.forEachIndexed { index, deposit ->
                                val depositCurrency = deposit.currencyCode.ifBlank { state.currencyCode }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.openDepositDetail(deposit) }
                                        .padding(vertical = Dimens.spacingList),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column {
                                        Text(
                                            text = SimpleDateFormat("d MMM, HH:mm", Locale("ru"))
                                                .format(Date(deposit.createdAtMillis)),
                                            style = typography.bodyLarge,
                                        )
                                        deposit.note?.let {
                                            Text(
                                                text = it,
                                                style = typography.bodySmall,
                                                color = colors.onSurfaceVariant,
                                            )
                                        }
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(Dimens.paddingMicro),
                                    ) {
                                        Text(
                                            text = "+${MoneyFormat.format(deposit.amountMinor, depositCurrency)}",
                                            style = typography.bodyLarge,
                                            fontWeight = FontWeight.SemiBold,
                                            color = goalColor,
                                        )
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                            contentDescription = null,
                                            tint = colors.onSurfaceVariant.copy(alpha = 0.4f),
                                            modifier = Modifier.size(Dimens.iconSizeSmall),
                                        )
                                    }
                                }
                                if (index < state.deposits.lastIndex) {
                                    HorizontalDivider(color = colors.outline.copy(alpha = 0.5f))
                                }
                            }
                        }
                    }
                }
            }
            TextButton(
                onClick = { showDeleteDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Dimens.paddingSmall),
            ) {
                Text(
                    text = androidx.compose.ui.res.stringResource(R.string.goal_detail_delete),
                    color = IosRed,
                    style = typography.bodyLarge,
                )
            }
        }
    }
}

@Composable
private fun GoalCelebrationDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(Dimens.cornerRadiusCard),
            color = colors.surface,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimens.paddingMedium + Dimens.paddingMicro),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("🎉", style = typography.displaySmall)
                Spacer(Modifier.height(Dimens.paddingSmall))
                Text(
                    text = title,
                    style = typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = message,
                    style = typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = Dimens.paddingSmall),
                )
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.padding(top = Dimens.paddingMedium),
                ) {
                    Text(
                        text = androidx.compose.ui.res.stringResource(R.string.goal_completed_ok),
                        color = colors.primary,
                    )
                }
            }
        }
    }
}

@Composable
private fun GoalDeleteDialog(
    goalName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(Dimens.cornerRadiusCard),
            color = colors.surface,
        ) {
            Column(modifier = Modifier.padding(Dimens.paddingMedium + Dimens.paddingMicro)) {
                Text(
                    text = androidx.compose.ui.res.stringResource(R.string.goal_delete_title, goalName),
                    style = typography.titleMedium,
                )
                Text(
                    text = androidx.compose.ui.res.stringResource(R.string.goal_delete_message),
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
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = androidx.compose.ui.res.stringResource(R.string.goal_delete_cancel),
                            color = colors.primary,
                        )
                    }
                    TextButton(onClick = onConfirm) {
                        Text(
                            text = androidx.compose.ui.res.stringResource(R.string.goal_delete_confirm),
                            color = IosRed,
                        )
                    }
                }
            }
        }
    }
}
