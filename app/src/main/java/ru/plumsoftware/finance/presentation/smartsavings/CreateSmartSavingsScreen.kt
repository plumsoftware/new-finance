package ru.plumsoftware.finance.presentation.smartsavings

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.presentation.common.MoneyFormat
import ru.plumsoftware.finance.ui.components.ios.IosAlertDialog
import ru.plumsoftware.finance.ui.components.ios.IosSwitch
import ru.plumsoftware.finance.ui.components.ios.IosTextButton
import ru.plumsoftware.finance.ui.theme.Dimens
import ru.plumsoftware.finance.ui.theme.ExtendedTypography
import ru.plumsoftware.finance.ui.theme.Inter28Family

private enum class AmountField { PURCHASE, SAVING }

private val createAssetNumPadKeys = listOf(
    listOf("1", "2", "3"),
    listOf("4", "5", "6"),
    listOf("7", "8", "9"),
    listOf(".", "0", "⌫"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSmartSavingsScreen(
    onBack: () -> Unit,
    onCreated: () -> Unit,
    viewModel: CreateSmartSavingsViewModel,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var activeField by remember { mutableStateOf<Int?>(null) }
    val dismissNumPad = { activeField = null }
    val scrollState = rememberScrollState()
    val purchaseBringIntoView = remember { BringIntoViewRequester() }
    val savingBringIntoView = remember { BringIntoViewRequester() }
    val onCreatedUpdated by rememberUpdatedState(onCreated)
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val formCardShape = RoundedCornerShape(Dimens.cornerRadiusList)
    val canSave = state.name.isNotBlank() &&
        MoneyFormat.majorDigitsToMinor(state.purchaseDigits, state.currencyCode) > 0L
    val showNumPad = activeField != null
    val dismissTapInteraction = remember { MutableInteractionSource() }
    val dismissTapModifier = if (showNumPad) {
        Modifier.clickable(
            indication = null,
            interactionSource = dismissTapInteraction,
            onClick = dismissNumPad,
        )
    } else {
        Modifier
    }

    val activeAmountLabel = when (activeField) {
        AmountField.PURCHASE.ordinal -> stringResource(R.string.smart_purchase_cost_label)
        AmountField.SAVING.ordinal -> stringResource(R.string.smart_saving_per_use)
        else -> ""
    }
    val activeAmountDigits = when (activeField) {
        AmountField.PURCHASE.ordinal -> state.purchaseDigits
        AmountField.SAVING.ordinal -> state.savingPerUseDigits
        else -> ""
    }

    LaunchedEffect(state.saved) {
        if (state.saved) onCreatedUpdated()
    }

    LaunchedEffect(activeField) {
        when (activeField) {
            AmountField.PURCHASE.ordinal -> {
                delay(220)
                purchaseBringIntoView.bringIntoView()
            }
            AmountField.SAVING.ordinal -> {
                delay(220)
                savingBringIntoView.bringIntoView()
            }
        }
    }

    state.errorMessage?.let { msg ->
        IosAlertDialog(message = msg, onDismiss = viewModel::clearError)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = {
            CreateAssetTopBar(
                title = if (state.isEditMode) {
                    stringResource(R.string.smart_edit_title)
                } else {
                    stringResource(R.string.smart_new_asset)
                },
                actionLabel = if (state.isEditMode) {
                    stringResource(R.string.save)
                } else {
                    stringResource(R.string.create)
                },
                onCancel = {
                    dismissNumPad()
                    onBack()
                },
                onAction = {
                    dismissNumPad()
                    viewModel.save()
                },
                actionEnabled = canSave && !state.isSaving,
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = Dimens.formHorizontalInset),
            ) {
                Spacer(Modifier.height(Dimens.headerContentGap))
                SectionTitle(
                    text = stringResource(R.string.smart_section_main),
                    onClick = if (showNumPad) dismissNumPad else null,
                )
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = formCardShape,
                    color = colors.surface,
                    shadowElevation = 0.dp,
                    tonalElevation = 0.dp,
                ) {
                    Column {
                        IosFormTextField(
                            label = stringResource(R.string.smart_name),
                            value = state.name,
                            onValueChange = viewModel::setName,
                            placeholder = stringResource(R.string.smart_name_placeholder),
                            onFocus = dismissNumPad,
                        )
                        FormRowDivider()
                        IosFormTextField(
                            label = stringResource(R.string.smart_note),
                            value = state.note,
                            onValueChange = viewModel::setNote,
                            placeholder = stringResource(R.string.smart_note_optional),
                            onFocus = dismissNumPad,
                        )
                    }
                }

                Spacer(
                    Modifier
                        .height(Dimens.sectionGapLarge)
                        .then(dismissTapModifier),
                )
                SectionTitle(
                    text = stringResource(R.string.smart_section_icon),
                    onClick = if (showNumPad) dismissNumPad else null,
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(Dimens.paddingSmall),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    items(SMART_EMOJI_PRESETS) { emoji ->
                        CreateAssetIconCell(
                            emoji = emoji,
                            selected = state.icon == emoji,
                            onClick = {
                                dismissNumPad()
                                viewModel.setIcon(emoji)
                            },
                        )
                    }
                }

                Spacer(
                    Modifier
                        .height(Dimens.sectionGapLarge)
                        .then(dismissTapModifier),
                )
                SectionTitle(
                    text = stringResource(R.string.smart_section_finance),
                    onClick = if (showNumPad) dismissNumPad else null,
                )
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = formCardShape,
                    color = colors.surface,
                    shadowElevation = 0.dp,
                    tonalElevation = 0.dp,
                ) {
                    Column {
                        IosAmountSelectorRow(
                            label = stringResource(R.string.smart_purchase_cost_label),
                            digits = state.purchaseDigits,
                            currencyCode = state.currencyCode,
                            selected = activeField == AmountField.PURCHASE.ordinal,
                            onClick = {
                                activeField = if (activeField == AmountField.PURCHASE.ordinal) {
                                    null
                                } else {
                                    AmountField.PURCHASE.ordinal
                                }
                            },
                            modifier = Modifier.bringIntoViewRequester(purchaseBringIntoView),
                        )
                        FormRowDivider()
                        IosAmountSelectorRow(
                            label = stringResource(R.string.smart_saving_per_use),
                            digits = state.savingPerUseDigits,
                            currencyCode = state.currencyCode,
                            selected = activeField == AmountField.SAVING.ordinal,
                            onClick = {
                                activeField = if (activeField == AmountField.SAVING.ordinal) {
                                    null
                                } else {
                                    AmountField.SAVING.ordinal
                                }
                            },
                            modifier = Modifier.bringIntoViewRequester(savingBringIntoView),
                        )
                    }
                }

                Spacer(
                    Modifier
                        .height(Dimens.sectionGapLarge)
                        .then(dismissTapModifier),
                )
                SectionTitle(
                    text = stringResource(R.string.smart_section_settings),
                    onClick = if (showNumPad) dismissNumPad else null,
                )
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = formCardShape,
                    color = colors.surface,
                    shadowElevation = 0.dp,
                    tonalElevation = 0.dp,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(Dimens.formRowHeight)
                            .padding(horizontal = Dimens.formHorizontalInset),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.smart_record_as_expense),
                            style = typography.bodyLarge,
                            color = colors.onSurface,
                            modifier = Modifier
                                .weight(1f)
                                .then(dismissTapModifier),
                        )
                        IosSwitch(
                            checked = state.recordPurchaseExpense,
                            onCheckedChange = { checked ->
                                dismissNumPad()
                                viewModel.setRecordPurchaseExpense(checked)
                            },
                        )
                    }
                }
                Text(
                    text = stringResource(R.string.smart_record_as_expense_hint),
                    style = typography.bodySmall,
                    color = colors.onSurfaceVariant,
                    modifier = Modifier
                        .padding(top = Dimens.paddingSmall)
                        .then(dismissTapModifier),
                )
                if (showNumPad) {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(Dimens.sectionGapLarge)
                            .then(dismissTapModifier),
                    )
                } else {
                    Spacer(Modifier.height(Dimens.paddingMedium))
                }
            }

            AnimatedVisibility(
                visible = showNumPad,
                enter = slideInVertically(
                    initialOffsetY = { fullHeight -> fullHeight },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMedium,
                    ),
                ) + fadeIn(animationSpec = tween(200)),
                exit = slideOutVertically(
                    targetOffsetY = { fullHeight -> fullHeight },
                    animationSpec = tween(220),
                ) + fadeOut(animationSpec = tween(180)),
            ) {
                Column {
                    CreateAssetAmountBar(
                        fieldLabel = activeAmountLabel,
                        digits = activeAmountDigits,
                        currencyCode = state.currencyCode,
                    )
                    CreateAssetNumPad(
                        onDigit = { d ->
                            when (activeField) {
                                AmountField.PURCHASE.ordinal -> viewModel.appendPurchaseDigit(d)
                                AmountField.SAVING.ordinal -> viewModel.appendSavingDigit(d)
                            }
                        },
                        onBackspace = {
                            when (activeField) {
                                AmountField.PURCHASE.ordinal -> viewModel.backspacePurchase()
                                AmountField.SAVING.ordinal -> viewModel.backspaceSaving()
                            }
                        },
                        modifier = Modifier.padding(horizontal = Dimens.formHorizontalInset),
                    )
                    Spacer(Modifier.height(Dimens.spacingList))
                }
            }
            CreateAssetPrimaryButton(
                text = if (state.isEditMode) stringResource(R.string.save) else stringResource(R.string.create),
                onClick = {
                    dismissNumPad()
                    viewModel.save()
                },
                loading = state.isSaving,
                enabled = canSave,
                modifier = Modifier.padding(horizontal = Dimens.paddingMedium),
            )
            Spacer(
                Modifier
                    .navigationBarsPadding()
                    .height(Dimens.paddingMedium),
            )
        }
    }
}

@Composable
private fun CreateAssetTopBar(
    title: String,
    actionLabel: String,
    onCancel: () -> Unit,
    onAction: () -> Unit,
    actionEnabled: Boolean,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(Dimens.toolbarHeight)
            .background(colors.background)
            .padding(horizontal = Dimens.formHorizontalInset),
    ) {
        IosTextButton(
            text = stringResource(R.string.cancel),
            onClick = onCancel,
            modifier = Modifier.align(Alignment.CenterStart),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(),
        )
        Text(
            text = title,
            style = typography.bodyLarge.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 17.sp,
            ),
            color = colors.onSurface,
            modifier = Modifier.align(Alignment.Center),
        )
        IosTextButton(
            text = actionLabel,
            onClick = onAction,
            enabled = actionEnabled,
            color = if (actionEnabled) colors.secondary else colors.secondary.copy(alpha = 0.3f),
            style = typography.bodyLarge.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 17.sp,
            ),
            modifier = Modifier.align(Alignment.CenterEnd),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(),
        )
    }
}

@Composable
private fun SectionTitle(
    text: String,
    onClick: (() -> Unit)? = null,
) {
    val colors = MaterialTheme.colorScheme
    val interactionSource = remember { MutableInteractionSource() }
    Text(
        text = text.uppercase(),
        style = ExtendedTypography.sectionLabel,
        color = colors.onSurfaceVariant,
        modifier = Modifier
            .padding(bottom = Dimens.paddingSmall)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        indication = null,
                        interactionSource = interactionSource,
                        onClick = onClick,
                    )
                } else {
                    Modifier
                },
            ),
    )
}

@Composable
private fun FormRowDivider() {
    val colors = MaterialTheme.colorScheme
    HorizontalDivider(
        color = colors.outline,
        thickness = Dimens.borderThin,
        modifier = Modifier.padding(start = Dimens.formHorizontalInset),
    )
}

@Composable
private fun IosFormTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    onFocus: () -> Unit = {},
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(Dimens.formRowHeight)
            .padding(horizontal = Dimens.formHorizontalInset),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = typography.bodyLarge,
            color = colors.onSurface,
            modifier = Modifier.weight(0.4f),
        )
        Box(modifier = Modifier.weight(0.6f), contentAlignment = Alignment.CenterEnd) {
            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    color = colors.outlineVariant,
                    style = typography.bodyLarge,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = typography.bodyLarge.copy(
                    color = colors.onSurface,
                    textAlign = TextAlign.End,
                ),
                cursorBrush = SolidColor(colors.tertiary),
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused) onFocus()
                    },
                singleLine = true,
            )
        }
    }
}

@Composable
private fun CreateAssetIconCell(
    emoji: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.08f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "create_asset_icon_scale",
    )
    val cellShape = RoundedCornerShape(Dimens.iconPickerCellRadius)
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(Dimens.iconPickerCellSize)
            .scale(scale)
            .clip(cellShape)
            .background(if (selected) colors.tertiary else colors.outline)
            .clickable(onClick = onClick),
    ) {
        Text(
            text = emoji,
            style = TextStyle(fontSize = 28.sp),
        )
    }
}

@Composable
private fun IosAmountSelectorRow(
    label: String,
    digits: String,
    currencyCode: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val hasValue = MoneyFormat.majorDigitsToMinor(digits, currencyCode) > 0L
    val valueColor = when {
        hasValue -> colors.tertiary
        else -> colors.onSurfaceVariant
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(Dimens.formRowHeight)
            .clickable(onClick = onClick)
            .padding(horizontal = Dimens.formHorizontalInset),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = typography.bodyLarge,
            color = colors.onSurface,
        )
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = MoneyFormat.formatEntryDisplay(digits, currencyCode),
                style = typography.bodyLarge,
                color = valueColor,
            )
            if (selected) {
                Box(
                    modifier = Modifier
                        .padding(top = Dimens.paddingMicro)
                        .height(Dimens.borderThin)
                        .width(Dimens.iconSizeStandard)
                        .background(colors.tertiary),
                )
            }
        }
    }
}

@Composable
private fun CreateAssetAmountBar(
    fieldLabel: String,
    digits: String,
    currencyCode: String,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(Dimens.amountPreviewBarHeight)
            .background(colors.background)
            .padding(horizontal = Dimens.formHorizontalInset),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = fieldLabel,
            style = typography.bodySmall,
            color = colors.onSurfaceVariant,
        )
        AnimatedContent(
            targetState = MoneyFormat.formatEntryDisplay(digits, currencyCode),
            transitionSpec = {
                (slideInVertically(animationSpec = tween(120)) { it / 2 } + fadeIn(tween(120)))
                    .togetherWith(slideOutVertically(animationSpec = tween(100)) { -it / 2 } + fadeOut(tween(80)))
            },
            label = "create_asset_amount_preview",
        ) { value ->
            Text(
                text = value,
                style = ExtendedTypography.amountPreview,
                color = colors.onSurface,
            )
        }
    }
}

@Composable
private fun CreateAssetNumPad(
    onDigit: (String) -> Unit,
    onBackspace: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val keyStyle = TextStyle(
        fontFamily = Inter28Family,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        color = colors.onSurface,
    )
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.background),
        verticalArrangement = Arrangement.spacedBy(Dimens.numPadGapVertical),
    ) {
        createAssetNumPadKeys.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.paddingSmall),
            ) {
                row.forEach { key ->
                    val cellModifier = Modifier
                        .weight(1f)
                        .height(Dimens.numPadKeyHeightCompact)
                    when (key) {
                        "⌫" -> CreateAssetNumPadKey(
                            modifier = cellModifier,
                            onClick = onBackspace,
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Backspace,
                                contentDescription = null,
                                tint = colors.onSurfaceVariant,
                                modifier = Modifier.size(Dimens.iconSizeSmall),
                            )
                        }
                        "." -> CreateAssetNumPadKey(
                            modifier = cellModifier,
                            onClick = { onDigit(key) },
                        ) {
                            Text(
                                text = key,
                                style = keyStyle.copy(color = colors.onSurfaceVariant),
                            )
                        }
                        else -> CreateAssetNumPadKey(
                            modifier = cellModifier,
                            onClick = { onDigit(key) },
                        ) {
                            Text(text = key, style = keyStyle)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CreateAssetNumPadKey(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressProgress by animateFloatAsState(
        targetValue = if (isPressed) 1f else 0f,
        animationSpec = tween(durationMillis = 80),
        label = "create_asset_numpad_press",
    )
    val keyShape = RoundedCornerShape(Dimens.cornerRadiusChip)
    Box(
        modifier = modifier
            .clip(keyShape)
            .background(lerp(colors.surface, colors.outline, pressProgress))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Composable
private fun CreateAssetPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed && enabled && !loading) 0.97f else 1f,
        animationSpec = if (isPressed) {
            tween(durationMillis = 80)
        } else {
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium,
            )
        },
        label = "create_asset_button_scale",
    )
    val shape = RoundedCornerShape(Dimens.cornerRadiusList)
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier
            .fillMaxWidth()
            .height(Dimens.createActionHeight)
            .scale(pressScale),
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = colors.tertiary,
            contentColor = colors.onSecondary,
            disabledContainerColor = colors.tertiary.copy(alpha = 0.25f),
            disabledContentColor = colors.tertiary.copy(alpha = 0.6f),
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            disabledElevation = 0.dp,
            focusedElevation = 0.dp,
            hoveredElevation = 0.dp,
        ),
        interactionSource = interactionSource,
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(Dimens.iconSizeStandard),
                strokeWidth = Dimens.borderThin + 1.5.dp,
                color = colors.onSecondary,
            )
        } else {
            Text(
                text = text,
                style = typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp,
                ),
            )
        }
    }
}
