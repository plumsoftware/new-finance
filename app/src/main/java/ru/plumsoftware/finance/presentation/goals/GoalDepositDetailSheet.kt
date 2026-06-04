package ru.plumsoftware.finance.presentation.goals

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.domain.model.Goal
import ru.plumsoftware.finance.domain.model.GoalDeposit
import ru.plumsoftware.finance.presentation.common.MoneyFormat
import ru.plumsoftware.finance.ui.components.ios.IosTextButton
import ru.plumsoftware.finance.ui.theme.Dimens
import ru.plumsoftware.finance.ui.theme.IosRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalDepositDetailSheet(
    deposit: GoalDeposit,
    goal: Goal,
    currencyCode: String,
    accountName: String?,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val goalColor = colorFromHexOrDefault(goal.colorHex, colors.primary)
    val displayCurrency = deposit.currencyCode.ifBlank { currencyCode }

    if (showDeleteConfirm) {
        Dialog(onDismissRequest = { showDeleteConfirm = false }) {
            Surface(
                shape = RoundedCornerShape(Dimens.cornerRadiusCard),
                color = colors.surface,
            ) {
                Column(modifier = Modifier.padding(Dimens.paddingMedium + Dimens.paddingMicro)) {
                    Text(
                        text = stringResource(R.string.goal_deposit_delete_title),
                        style = typography.titleMedium,
                    )
                    Text(
                        text = stringResource(R.string.goal_deposit_delete_message),
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
                                text = stringResource(R.string.goal_deposit_delete_cancel),
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
                                text = stringResource(R.string.goal_deposit_delete_confirm),
                                color = IosRed,
                            )
                        }
                    }
                }
            }
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.spacingSection)
                .padding(bottom = Dimens.bottomSheetBottomPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(goalColor.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(goal.emoji, fontSize = 32.sp)
            }
            Spacer(Modifier.height(Dimens.spacingList))
            Text(
                text = goal.name,
                style = typography.bodyMedium,
                color = colors.onSurfaceVariant,
            )
            Spacer(Modifier.height(Dimens.paddingMicro))
            Text(
                text = "+${MoneyFormat.format(deposit.amountMinor, displayCurrency)}",
                style = typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = goalColor,
            )
            Spacer(Modifier.height(Dimens.spacingList))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.surface, RoundedCornerShape(Dimens.cornerRadiusCard)),
            ) {
                DepositDetailRow(
                    label = stringResource(R.string.goal_deposit_detail_date),
                    value = SimpleDateFormat("d MMMM yyyy, HH:mm", Locale("ru"))
                        .format(Date(deposit.createdAtMillis)),
                )
                HorizontalDivider(
                    color = colors.outline.copy(alpha = 0.5f),
                    modifier = Modifier.padding(start = Dimens.spacingSection),
                )
                DepositDetailRow(
                    label = stringResource(R.string.goal_deposit_detail_goal),
                    value = goal.name,
                )
                HorizontalDivider(
                    color = colors.outline.copy(alpha = 0.5f),
                    modifier = Modifier.padding(start = Dimens.spacingSection),
                )
                DepositDetailRow(
                    label = stringResource(R.string.goal_deposit_detail_currency),
                    value = displayCurrency,
                )
                if (!deposit.note.isNullOrBlank()) {
                    HorizontalDivider(
                        color = colors.outline.copy(alpha = 0.5f),
                        modifier = Modifier.padding(start = Dimens.spacingSection),
                    )
                    DepositDetailRow(
                        label = stringResource(R.string.goal_deposit_detail_note),
                        value = deposit.note,
                    )
                }
                accountName?.let { name ->
                    HorizontalDivider(
                        color = colors.outline.copy(alpha = 0.5f),
                        modifier = Modifier.padding(start = Dimens.spacingSection),
                    )
                    DepositDetailRow(
                        label = stringResource(R.string.goal_deposit_detail_account),
                        value = name,
                    )
                }
            }
            Spacer(Modifier.height(Dimens.spacingList))
            IosTextButton(
                text = stringResource(R.string.goal_deposit_delete_button),
                onClick = { showDeleteConfirm = true },
                color = IosRed,
                style = typography.bodyLarge,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                contentPadding = PaddingValues(vertical = Dimens.spacingRow),
            )
            IosTextButton(
                text = stringResource(R.string.cancel),
                onClick = onDismiss,
                color = colors.onSurfaceVariant,
                style = typography.bodyMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                contentPadding = PaddingValues(bottom = Dimens.SpacingXs),
            )
        }
    }
}

@Composable
private fun DepositDetailRow(label: String, value: String) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.spacingSection, vertical = Dimens.spacingRow),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = typography.bodyMedium,
            color = colors.onSurfaceVariant,
            modifier = Modifier.weight(0.4f),
        )
        Text(
            text = value,
            style = typography.bodyMedium,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(0.6f),
        )
    }
}
