package ru.plumsoftware.finance.presentation.recurring

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.data.util.startOfDayMillis
import ru.plumsoftware.finance.domain.model.Category
import ru.plumsoftware.finance.domain.model.CategoryType
import ru.plumsoftware.finance.domain.model.RecurringFrequency
import ru.plumsoftware.finance.domain.model.RecurringTransaction
import ru.plumsoftware.finance.presentation.common.MoneyFormat
import ru.plumsoftware.finance.ui.components.PrimaryButton
import ru.plumsoftware.finance.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecurringSheet(
    categories: List<Category>,
    currencyCode: String,
    onDismiss: () -> Unit,
    onSave: (RecurringTransaction) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val focusManager = LocalFocusManager.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val currencySymbol = MoneyFormat.symbol(currencyCode)

    var title by rememberSaveable { mutableStateOf("") }
    var amountInput by rememberSaveable { mutableStateOf("") }
    var isIncome by rememberSaveable { mutableStateOf(false) }
    var selectedCategoryId by rememberSaveable { mutableStateOf<Long?>(null) }
    var selectedFrequency by remember { mutableStateOf(RecurringFrequency.MONTHLY) }
    var selectedDayOfMonth by rememberSaveable { mutableIntStateOf(1) }

    val filteredCategories = remember(categories, isIncome) {
        val type = if (isIncome) CategoryType.INCOME else CategoryType.EXPENSE
        categories.filter { it.type == type }
    }

    val amountMinor = MoneyFormat.majorDigitsToMinor(amountInput, currencyCode)
    val canSave = title.isNotBlank() &&
        amountMinor > 0L &&
        selectedCategoryId != null

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
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
            Text(
                text = stringResource(R.string.add_recurring),
                style = typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            Spacer(Modifier.height(Dimens.SpacingL))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.recurring_title)) },
                singleLine = true,
                shape = RoundedCornerShape(Dimens.RadiusM),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.primary,
                    unfocusedBorderColor = colors.surfaceVariant,
                ),
            )

            Spacer(Modifier.height(Dimens.SpacingM))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingM),
            ) {
                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it.filter { c -> c.isDigit() || c == '.' || c == ',' } },
                    modifier = Modifier.weight(1f),
                    label = { Text(stringResource(R.string.amount)) },
                    suffix = { Text(currencySymbol) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() },
                    ),
                    shape = RoundedCornerShape(Dimens.RadiusM),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.primary,
                        unfocusedBorderColor = colors.surfaceVariant,
                    ),
                )

                TypeToggle(
                    isIncome = isIncome,
                    onToggle = { income ->
                        isIncome = income
                        val type = if (income) CategoryType.INCOME else CategoryType.EXPENSE
                        if (selectedCategoryId != null &&
                            categories.none { it.id == selectedCategoryId && it.type == type }
                        ) {
                            selectedCategoryId = null
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(Dimens.ButtonHeight),
                )
            }

            Spacer(Modifier.height(Dimens.SpacingM))

            Text(
                text = stringResource(R.string.category),
                style = typography.labelMedium,
                color = colors.onSurfaceVariant,
            )
            Spacer(Modifier.height(Dimens.SpacingXs))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingXs),
            ) {
                items(filteredCategories, key = { it.id }) { category ->
                    FilterChip(
                        selected = selectedCategoryId == category.id,
                        onClick = { selectedCategoryId = category.id },
                        label = {
                            Text(
                                text = "${category.icon} ${category.name}",
                                style = typography.bodySmall,
                            )
                        },
                        shape = RoundedCornerShape(Dimens.RadiusPill),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedCategoryId == category.id,
                            borderColor = colors.surfaceVariant,
                            selectedBorderColor = Color.Transparent,
                        ),
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = colors.surfaceVariant,
                            labelColor = colors.onSurfaceVariant,
                            selectedContainerColor = colors.primary.copy(alpha = 0.12f),
                            selectedLabelColor = colors.primary,
                        ),
                    )
                }
            }

            Spacer(Modifier.height(Dimens.SpacingM))

            Text(
                text = stringResource(R.string.frequency),
                style = typography.labelMedium,
                color = colors.onSurfaceVariant,
            )
            Spacer(Modifier.height(Dimens.SpacingXs))
            FrequencyToggle(
                selected = selectedFrequency,
                onSelect = { selectedFrequency = it },
            )

            AnimatedVisibility(visible = selectedFrequency == RecurringFrequency.MONTHLY) {
                Column {
                    Spacer(Modifier.height(Dimens.SpacingM))
                    Text(
                        text = stringResource(R.string.day_of_month),
                        style = typography.labelMedium,
                        color = colors.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(Dimens.SpacingXs))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingXs),
                    ) {
                        items((1..31).toList()) { day ->
                            FilterChip(
                                selected = selectedDayOfMonth == day,
                                onClick = { selectedDayOfMonth = day },
                                label = {
                                    Text(
                                        text = day.toString(),
                                        style = typography.bodySmall,
                                    )
                                },
                                shape = RoundedCornerShape(Dimens.RadiusPill),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = selectedDayOfMonth == day,
                                    borderColor = colors.surfaceVariant,
                                    selectedBorderColor = Color.Transparent,
                                ),
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = colors.surfaceVariant,
                                    labelColor = colors.onSurfaceVariant,
                                    selectedContainerColor = colors.primary.copy(alpha = 0.12f),
                                    selectedLabelColor = colors.primary,
                                ),
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(Dimens.SpacingXl))

            PrimaryButton(
                text = stringResource(R.string.save),
                enabled = canSave,
                onClick = {
                    onSave(
                        RecurringTransaction(
                            title = title.trim(),
                            amountMinor = amountMinor,
                            categoryId = selectedCategoryId!!,
                            isIncome = isIncome,
                            frequency = selectedFrequency,
                            dayOfMonth = if (selectedFrequency == RecurringFrequency.MONTHLY) {
                                selectedDayOfMonth
                            } else {
                                null
                            },
                            nextDateMillis = startOfDayMillis(System.currentTimeMillis()),
                            isActive = true,
                        ),
                    )
                },
            )

            Spacer(Modifier.height(Dimens.SpacingM))
        }
    }
}

@Composable
private fun TypeToggle(
    isIncome: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val options = listOf(false to R.string.type_expense, true to R.string.type_income)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(Dimens.RadiusPill))
            .background(colors.surfaceVariant)
            .padding(3.dp),
    ) {
        Row(Modifier.fillMaxSize()) {
            options.forEach { (income, labelRes) ->
                val isSelected = isIncome == income
                val backgroundColor by animateColorAsState(
                    targetValue = if (isSelected) colors.surface else Color.Transparent,
                    animationSpec = tween(200),
                    label = "type_tab_bg",
                )
                val textColor by animateColorAsState(
                    targetValue = if (isSelected) colors.onSurface else colors.onSurfaceVariant,
                    label = "type_tab_text",
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(Dimens.RadiusPill))
                        .background(backgroundColor)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) { onToggle(income) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(labelRes),
                        style = typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = textColor,
                        maxLines = 1,
                        softWrap = false,
                    )
                }
            }
        }
    }
}

@Composable
private fun FrequencyToggle(
    selected: RecurringFrequency,
    onSelect: (RecurringFrequency) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val options = RecurringFrequency.entries

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(36.dp)
            .clip(RoundedCornerShape(Dimens.RadiusPill))
            .background(colors.surfaceVariant)
            .padding(3.dp),
    ) {
        Row(Modifier.fillMaxSize()) {
            options.forEach { frequency ->
                val isSelected = frequency == selected
                val backgroundColor by animateColorAsState(
                    targetValue = if (isSelected) colors.surface else Color.Transparent,
                    animationSpec = tween(200),
                    label = "freq_tab_bg",
                )
                val textColor by animateColorAsState(
                    targetValue = if (isSelected) colors.onSurface else colors.onSurfaceVariant,
                    label = "freq_tab_text",
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(Dimens.RadiusPill))
                        .background(backgroundColor)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) { onSelect(frequency) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(frequency.labelRes),
                        style = typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = textColor,
                        maxLines = 1,
                        softWrap = false,
                    )
                }
            }
        }
    }
}
