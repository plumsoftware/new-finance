package ru.plumsoftware.finance.presentation.limits

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import org.koin.androidx.compose.koinViewModel
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.domain.model.CategoryWithSpending
import ru.plumsoftware.finance.domain.model.LimitStatus
import ru.plumsoftware.finance.presentation.common.MoneyFormat
import ru.plumsoftware.finance.ui.components.AppCard
import ru.plumsoftware.finance.ui.components.PrimaryButton
import ru.plumsoftware.finance.ui.components.SectionLabel
import ru.plumsoftware.finance.ui.components.ios.IosEditorTopBar
import ru.plumsoftware.finance.ui.theme.Dimens

private val LimitWarningOrange = Color(0xFFFF9500)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LimitsScreen(
    navController: NavController,
    viewModel: LimitsViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val categories = state.categoriesWithSpending
    val colors = MaterialTheme.colorScheme

    var showSheet by remember { mutableStateOf(false) }
    var sheetCategory by remember { mutableStateOf<CategoryWithSpending?>(null) }
    var limitInput by remember { mutableStateOf("") }

    fun showLimitSheet(item: CategoryWithSpending) {
        sheetCategory = item
        limitInput = item.limit?.let { formatLimitInput(it) } ?: ""
        showSheet = true
    }

    if (showSheet && sheetCategory != null) {
        LimitEditSheet(
            item = sheetCategory!!,
            limitInput = limitInput,
            currencyCode = state.currencyCode,
            onInputChange = { limitInput = it.filter { c -> c.isDigit() || c == '.' } },
            onSave = {
                limitInput.toDoubleOrNull()?.let { amount ->
                    viewModel.setLimit(sheetCategory!!.category.id, amount)
                }
                showSheet = false
            },
            onRemove = {
                viewModel.removeLimit(sheetCategory!!.category.id)
                showSheet = false
            },
            onDismiss = { showSheet = false },
        )
    }

    val sorted = remember(categories) {
        categories.sortedByDescending { it.status.ordinal }
    }
    val exceededCount = categories.count { it.status == LimitStatus.EXCEEDED }
    val warningCount = categories.count { it.status == LimitStatus.WARNING }
    val okCount = categories.count { it.status == LimitStatus.OK }
    val withLimits = sorted.filter { it.limit != null }
    val noLimits = sorted.filter { it.limit == null }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = {
            IosEditorTopBar(
                title = stringResource(R.string.limits),
                backLabel = stringResource(R.string.categories_back_settings),
                onBack = navController::popBackStack,
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(
                horizontal = Dimens.SpacingL,
                vertical = Dimens.SpacingXs,
            ),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingXs),
        ) {
            item {
                LimitsSummaryBanner(
                    okCount = okCount,
                    warningCount = warningCount,
                    exceededCount = exceededCount,
                    modifier = Modifier.padding(
                        top = Dimens.SpacingM,
                        bottom = Dimens.SpacingXs,
                    ),
                )
            }

            if (withLimits.isNotEmpty()) {
                item { SectionLabel(text = stringResource(R.string.limits_set)) }
                items(withLimits, key = { it.category.id }) { item ->
                    LimitCategoryCard(
                        item = item,
                        currencyCode = state.currencyCode,
                        onEditClick = { showLimitSheet(item) },
                    )
                }
            }

            if (noLimits.isNotEmpty()) {
                item {
                    SectionLabel(
                        text = stringResource(R.string.limits_not_set),
                        modifier = Modifier.padding(top = Dimens.SpacingM),
                    )
                }
                items(noLimits, key = { it.category.id }) { item ->
                    NoLimitCategoryRow(
                        item = item,
                        onAddClick = { showLimitSheet(item) },
                    )
                }
            }
        }
    }
}

@Composable
private fun LimitsSummaryBanner(
    okCount: Int,
    warningCount: Int,
    exceededCount: Int,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme

    AppCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.SpacingM),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SummaryStatItem(
                count = okCount,
                label = R.string.limit_status_ok,
                color = colors.secondary,
                modifier = Modifier.weight(1f),
            )
            VerticalDivider(
                modifier = Modifier.height(36.dp),
                color = colors.surfaceVariant,
            )
            SummaryStatItem(
                count = warningCount,
                label = R.string.limit_status_warning,
                color = LimitWarningOrange,
                modifier = Modifier.weight(1f),
            )
            VerticalDivider(
                modifier = Modifier.height(36.dp),
                color = colors.surfaceVariant,
            )
            SummaryStatItem(
                count = exceededCount,
                label = R.string.limit_status_exceeded,
                color = colors.error,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun SummaryStatItem(
    count: Int,
    @StringRes label: Int,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = color,
        )
        Text(
            text = stringResource(label),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun LimitCategoryCard(
    item: CategoryWithSpending,
    currencyCode: String,
    onEditClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val category = item.category
    val categoryColor = category.colorArgb?.let { Color(it.toInt()) } ?: colors.onSurfaceVariant
    val statusColor = item.status.color()

    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(Dimens.SpacingM)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(Dimens.RadiusM))
                        .background(categoryColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(category.icon, fontSize = 22.sp)
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = Dimens.SpacingM),
                ) {
                    Text(
                        text = category.name,
                        style = typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                    )
                    if (item.status != LimitStatus.OK) {
                        Spacer(Modifier.height(Dimens.SpacingXxs))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(Dimens.RadiusPill))
                                .background(statusColor.copy(alpha = 0.12f))
                                .padding(
                                    horizontal = Dimens.SpacingS,
                                    vertical = 2.dp,
                                ),
                        ) {
                            Text(
                                text = stringResource(item.status.labelRes),
                                style = typography.labelSmall,
                                color = statusColor,
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier.size(Dimens.IconSizeL),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.EditNote,
                        contentDescription = stringResource(R.string.cd_edit_limit),
                        tint = colors.primary,
                        modifier = Modifier.size(Dimens.IconSizeM),
                    )
                }
            }

            Spacer(Modifier.height(Dimens.SpacingM))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(
                        R.string.spent_amount,
                        item.spentThisMonth.formatMoney(currencyCode),
                    ),
                    style = typography.bodyMedium,
                    color = statusColor,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = stringResource(
                        R.string.limit_of,
                        item.limit!!.formatMoney(currencyCode),
                    ),
                    style = typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                )
            }

            Spacer(Modifier.height(Dimens.SpacingXs))

            LinearProgressIndicator(
                progress = { item.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(Dimens.RadiusPill)),
                color = statusColor,
                trackColor = colors.surfaceVariant,
            )

            Spacer(Modifier.height(Dimens.SpacingXs))

            val remainingText = when (item.status) {
                LimitStatus.EXCEEDED -> stringResource(
                    R.string.limit_overspend,
                    (item.spentThisMonth - item.limit!!).formatMoney(currencyCode),
                )
                else -> stringResource(
                    R.string.limit_remaining,
                    (item.limit!! - item.spentThisMonth).coerceAtLeast(0.0)
                        .formatMoney(currencyCode),
                )
            }
            Text(
                text = remainingText,
                style = typography.labelSmall,
                color = colors.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun NoLimitCategoryRow(
    item: CategoryWithSpending,
    onAddClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    AppCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .padding(Dimens.SpacingM)
                .height(Dimens.RowHeight - Dimens.SpacingM),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(Dimens.RadiusM))
                    .background(colors.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                Text(item.category.icon, fontSize = 22.sp)
            }

            Text(
                text = item.category.name,
                style = typography.bodyLarge,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = Dimens.SpacingM),
            )

            TextButton(onClick = onAddClick) {
                Text(
                    text = stringResource(R.string.set_limit),
                    style = typography.bodyMedium,
                    color = colors.primary,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LimitEditSheet(
    item: CategoryWithSpending,
    limitInput: String,
    currencyCode: String,
    onInputChange: (String) -> Unit,
    onSave: () -> Unit,
    onRemove: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val focusManager = LocalFocusManager.current
    val category = item.category
    val categoryColor = category.colorArgb?.let { Color(it.toInt()) } ?: colors.onSurfaceVariant
    val currencySymbol = MoneyFormat.symbol(currencyCode)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        shape = RoundedCornerShape(
            topStart = Dimens.RadiusXl,
            topEnd = Dimens.RadiusXl,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.SpacingL)
                .navigationBarsPadding(),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(Dimens.RadiusM))
                        .background(categoryColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(category.icon, fontSize = 24.sp)
                }
                Column(modifier = Modifier.padding(start = Dimens.SpacingM)) {
                    Text(
                        text = category.name,
                        style = typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = stringResource(R.string.monthly_limit_label),
                        style = typography.bodySmall,
                        color = colors.onSurfaceVariant,
                    )
                }
            }

            Spacer(Modifier.height(Dimens.SpacingXl))

            OutlinedTextField(
                value = limitInput,
                onValueChange = onInputChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.limit_amount_label)) },
                suffix = { Text(currencySymbol) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() },
                ),
                singleLine = true,
                shape = RoundedCornerShape(Dimens.RadiusM),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.primary,
                    unfocusedBorderColor = colors.surfaceVariant,
                ),
            )

            Spacer(Modifier.height(Dimens.SpacingXs))
            Text(
                text = stringResource(
                    R.string.spent_this_month_hint,
                    item.spentThisMonth.formatMoney(currencyCode),
                ),
                style = typography.bodySmall,
                color = colors.onSurfaceVariant,
                modifier = Modifier.padding(start = Dimens.SpacingXs),
            )

            Spacer(Modifier.height(Dimens.SpacingM))
            Text(
                text = stringResource(R.string.quick_presets),
                style = typography.labelMedium,
                color = colors.onSurfaceVariant,
            )
            Spacer(Modifier.height(Dimens.SpacingXs))
            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingXs)) {
                listOf("3 000", "5 000", "10 000", "20 000").forEach { preset ->
                    LimitPresetChip(
                        label = "$preset $currencySymbol",
                        selected = limitInput == preset.replace(" ", ""),
                        onClick = { onInputChange(preset.replace(" ", "")) },
                    )
                }
            }

            Spacer(Modifier.height(Dimens.SpacingXl))

            PrimaryButton(
                text = stringResource(R.string.save_limit),
                enabled = limitInput.isNotBlank() &&
                    limitInput.toDoubleOrNull() != null &&
                    limitInput.toDouble() > 0,
                onClick = onSave,
            )

            if (item.limit != null) {
                Spacer(Modifier.height(Dimens.SpacingS))
                TextButton(
                    onClick = onRemove,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = stringResource(R.string.remove_limit),
                        style = typography.bodyMedium,
                        color = colors.error,
                    )
                }
            }

            Spacer(Modifier.height(Dimens.SpacingM))
        }
    }
}

@Composable
private fun LimitPresetChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val shape = RoundedCornerShape(Dimens.RadiusPill)
    val backgroundColor = if (selected) {
        colors.primary.copy(alpha = 0.12f)
    } else {
        colors.surface
    }
    val borderModifier = if (selected) {
        Modifier
    } else {
        Modifier.border(Dimens.borderThin, colors.surfaceVariant, shape)
    }
    val textColor = if (selected) colors.primary else colors.onSurfaceVariant

    Box(
        modifier = Modifier
            .clip(shape)
            .then(borderModifier)
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = Dimens.SpacingS, vertical = Dimens.SpacingXxs),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = typography.bodySmall,
            color = textColor,
        )
    }
}

@Composable
private fun LimitStatus.color(): Color {
    val colors = MaterialTheme.colorScheme
    return when (this) {
        LimitStatus.NONE -> colors.onSurfaceVariant
        LimitStatus.OK -> colors.secondary
        LimitStatus.WARNING -> LimitWarningOrange
        LimitStatus.EXCEEDED -> colors.error
    }
}

private val LimitStatus.labelRes: Int
    @StringRes get() = when (this) {
        LimitStatus.NONE -> R.string.limit_status_ok
        LimitStatus.OK -> R.string.limit_status_ok
        LimitStatus.WARNING -> R.string.limit_status_warning
        LimitStatus.EXCEEDED -> R.string.limit_status_exceeded
    }

private fun formatLimitInput(value: Double): String {
    return if (value % 1.0 == 0.0) {
        value.toLong().toString()
    } else {
        value.toString()
    }
}
