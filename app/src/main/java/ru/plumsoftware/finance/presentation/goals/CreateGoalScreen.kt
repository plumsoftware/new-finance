package ru.plumsoftware.finance.presentation.goals

import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.ads.InterstitialPlacement
import ru.plumsoftware.finance.presentation.common.MoneyFormat
import ru.plumsoftware.finance.ui.ads.InterstitialAdEffect
import ru.plumsoftware.finance.ui.components.FinanceNumPad
import ru.plumsoftware.finance.ui.components.IosPrimaryButton
import ru.plumsoftware.finance.ui.components.SectionLabel
import ru.plumsoftware.finance.ui.components.ios.IosEditorTopBar
import ru.plumsoftware.finance.ui.components.ios.IosSwitch
import ru.plumsoftware.finance.ui.theme.Dimens
import ru.plumsoftware.finance.ui.theme.ExtendedTypography
import java.util.Calendar
import androidx.compose.foundation.layout.ColumnScope

private const val TARGET_FIELD = 0
private const val SAVED_FIELD = 1

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateGoalScreen(
    onBack: () -> Unit,
    backLabel: String,
    viewModel: CreateGoalViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val context = LocalContext.current
    var activeField by remember { mutableStateOf<Int?>(null) }
    val animatedColor by animateColorAsState(
        targetValue = colorFromHexOrDefault(state.colorHex, colors.primary),
        animationSpec = tween(300),
        label = "goal_create_color",
    )
    val showNumPad = activeField != null
    val activeAmountLabel = when (activeField) {
        TARGET_FIELD -> stringResource(R.string.goal_field_target)
        SAVED_FIELD -> stringResource(R.string.goal_field_saved)
        else -> ""
    }
    val activeAmountDigits = when (activeField) {
        TARGET_FIELD -> state.targetDigits
        SAVED_FIELD -> state.savedDigits
        else -> ""
    }
    val dismissNumPad = { activeField = null }

    InterstitialAdEffect(
        placement = InterstitialPlacement.GOAL,
        trigger = state.saved,
        onContinue = onBack,
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = {
            IosEditorTopBar(
                title = if (state.isEdit) stringResource(R.string.goal_edit_title) else stringResource(R.string.goal_create_title),
                backLabel = backLabel,
                onBack = {
                    dismissNumPad()
                    onBack()
                },
                actionLabel = stringResource(R.string.goal_save_button),
                actionEnabled = state.canSave,
                onAction = {
                    dismissNumPad()
                    viewModel.save()
                },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Dimens.paddingMedium)
                    .padding(
                        bottom = if (showNumPad) {
                            Dimens.amountPreviewBarHeight +
                                (Dimens.numPadKeyHeightCompact * 4) +
                                (Dimens.numPadGapVertical * 3) +
                                Dimens.iconSizeStandard +
                                Dimens.SpacingXl
                        } else {
                            Dimens.ButtonHeight + Dimens.bottomSheetBottomPadding + Dimens.spacingList
                        },
                    ),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Dimens.spacingSection),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .size(Dimens.avatarSizeLarge)
                            .clip(CircleShape)
                            .background(animatedColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = state.emoji,
                            style = typography.displayMedium,
                        )
                    }
                    Spacer(Modifier.height(Dimens.paddingSmall))
                    Text(
                        text = state.name.ifBlank { stringResource(R.string.goal_field_name_placeholder) },
                        style = typography.bodyLarge,
                        fontWeight = if (state.name.isNotBlank()) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (state.name.isNotBlank()) colors.onSurface else colors.outlineVariant,
                    )
                }

                SectionLabel(text = stringResource(R.string.goal_section_main))
                GoalFormCard {
                    GoalFormTextField(
                        label = stringResource(R.string.goal_field_name),
                        value = state.name,
                        onValueChange = viewModel::setName,
                        placeholder = stringResource(R.string.goal_field_name_placeholder),
                        trailingText = stringResource(R.string.goal_name_counter, state.name.length),
                    )
                    HorizontalDivider(color = colors.outline)
                    GoalFormTextField(
                        label = stringResource(R.string.goal_field_note),
                        value = state.note,
                        onValueChange = viewModel::setNote,
                        placeholder = stringResource(R.string.goal_field_note_placeholder),
                        trailingText = null,
                    )
                    HorizontalDivider(color = colors.outline)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Dimens.paddingMedium, vertical = Dimens.categoryEditorFieldVertical),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.goal_field_show_on_home),
                            style = typography.bodyLarge,
                        )
                        IosSwitch(
                            checked = state.showOnHome,
                            onCheckedChange = viewModel::setShowOnHome,
                        )
                    }
                }

                Spacer(Modifier.height(Dimens.spacingList))
                SectionLabel(text = stringResource(R.string.goal_section_finance))
                GoalFormCard {
                    GoalAmountRow(
                        label = stringResource(R.string.goal_field_target),
                        digits = state.targetDigits,
                        currencyCode = state.currencyCode,
                        selected = activeField == TARGET_FIELD,
                        onClick = {
                            activeField = if (activeField == TARGET_FIELD) null else TARGET_FIELD
                        },
                    )
                    HorizontalDivider(color = colors.outline)
                    GoalAmountRow(
                        label = stringResource(R.string.goal_field_saved),
                        digits = state.savedDigits,
                        currencyCode = state.currencyCode,
                        selected = activeField == SAVED_FIELD,
                        onClick = {
                            activeField = if (activeField == SAVED_FIELD) null else SAVED_FIELD
                        },
                    )
                }

                Spacer(Modifier.height(Dimens.spacingList))
                SectionLabel(text = stringResource(R.string.goal_section_deadline))
                GoalFormCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Dimens.paddingMedium, vertical = Dimens.categoryEditorFieldVertical),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.goal_field_deadline_toggle),
                            style = typography.bodyLarge,
                        )
                        IosSwitch(
                            checked = state.hasDeadline,
                            onCheckedChange = viewModel::toggleDeadline,
                        )
                    }
                    AnimatedVisibility(visible = state.hasDeadline) {
                        Column {
                            HorizontalDivider(color = colors.outline)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val cal = Calendar.getInstance().apply {
                                            timeInMillis = state.deadlineMillis ?: System.currentTimeMillis()
                                        }
                                        DatePickerDialog(
                                            context,
                                            { _, y, m, d ->
                                                val picked = Calendar.getInstance().apply {
                                                    set(Calendar.YEAR, y)
                                                    set(Calendar.MONTH, m)
                                                    set(Calendar.DAY_OF_MONTH, d)
                                                    set(Calendar.HOUR_OF_DAY, 23)
                                                    set(Calendar.MINUTE, 59)
                                                    set(Calendar.SECOND, 59)
                                                }
                                                viewModel.setDeadline(picked.timeInMillis)
                                            },
                                            cal.get(Calendar.YEAR),
                                            cal.get(Calendar.MONTH),
                                            cal.get(Calendar.DAY_OF_MONTH),
                                        ).show()
                                    }
                                    .padding(horizontal = Dimens.paddingMedium, vertical = Dimens.categoryEditorFieldVertical),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    text = stringResource(R.string.goal_field_deadline_value),
                                    style = typography.bodyLarge,
                                )
                                Text(
                                    text = state.deadlineFormatted,
                                    style = typography.bodyLarge,
                                    color = colors.primary,
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(Dimens.spacingList))
                SectionLabel(text = stringResource(R.string.goal_section_icon))
                GoalFormCard {
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimens.spacingList),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.paddingSmall),
                        verticalArrangement = Arrangement.spacedBy(Dimens.paddingSmall),
                    ) {
                        viewModel.availableEmojis().forEach { emoji ->
                            val selected = state.emoji == emoji
                            Box(
                                modifier = Modifier
                                    .size(Dimens.categoryEditorIconCell)
                                    .clip(CircleShape)
                                    .background(if (selected) animatedColor.copy(alpha = 0.18f) else colors.surfaceVariant)
                                    .then(if (selected) Modifier.border(Dimens.categoryEditorIconBorder, animatedColor, CircleShape) else Modifier)
                                    .clickable { viewModel.setEmoji(emoji) },
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(emoji, style = typography.headlineSmall)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(Dimens.spacingList))
                SectionLabel(text = stringResource(R.string.goal_section_color))
                GoalFormCard {
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimens.paddingMedium),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.spacingRow, Alignment.CenterHorizontally),
                        verticalArrangement = Arrangement.spacedBy(Dimens.spacingRow),
                    ) {
                        goalColorPalette.forEach { hex ->
                            val color = colorFromHexOrDefault(hex, colors.primary)
                            val selected = state.colorHex == hex
                            Box(
                                modifier = Modifier
                                    .size(Dimens.categoryEditorSwatch)
                                    .clip(CircleShape)
                                    .background(color)
                                    .then(
                                        if (selected) Modifier.border(
                                            Dimens.categoryEditorSwatchBorder,
                                            colors.surface,
                                            CircleShape,
                                        ) else Modifier,
                                    )
                                    .clickable { viewModel.setColor(hex) },
                            )
                        }
                    }
                }
                Spacer(Modifier.height(Dimens.paddingMedium))
            }

            if (showNumPad) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .zIndex(0.5f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = dismissNumPad,
                        ),
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                IosPrimaryButton(
                    text = stringResource(R.string.goal_save_button),
                    onClick = {
                        dismissNumPad()
                        viewModel.save()
                    },
                    enabled = state.canSave,
                    loading = state.isSaving,
                    modifier = Modifier
                        .padding(horizontal = Dimens.paddingMedium),
                )
                Spacer(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .height(Dimens.spacingList),
                )
            }

            AnimatedVisibility(
                visible = showNumPad,
                enter = slideInVertically(
                    initialOffsetY = { fullHeight -> fullHeight },
                    animationSpec = androidx.compose.animation.core.spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMedium,
                    ),
                ) + fadeIn(animationSpec = tween(200)),
                exit = slideOutVertically(
                    targetOffsetY = { fullHeight -> fullHeight },
                    animationSpec = tween(220),
                ) + fadeOut(animationSpec = tween(180)),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .zIndex(1f),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.background),
                ) {
                    GoalAmountBar(
                        fieldLabel = activeAmountLabel,
                        digits = activeAmountDigits,
                        currencyCode = state.currencyCode,
                    )
                    FinanceNumPad(
                        onDigit = { d ->
                            when (activeField) {
                                TARGET_FIELD -> viewModel.setTargetDigits(state.targetDigits + d)
                                SAVED_FIELD -> viewModel.setSavedDigits(state.savedDigits + d)
                            }
                        },
                        onBackspace = {
                            when (activeField) {
                                TARGET_FIELD -> viewModel.setTargetDigits(state.targetDigits.dropLast(1))
                                SAVED_FIELD -> viewModel.setSavedDigits(state.savedDigits.dropLast(1))
                            }
                        },
                        modifier = Modifier.padding(horizontal = Dimens.formHorizontalInset),
                    )
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Outlined.KeyboardArrowDown,
                        contentDescription = null,
                        tint = colors.onSurfaceVariant,
                        modifier = Modifier
                            .padding(
                                start = Dimens.formHorizontalInset,
                                top = Dimens.paddingSmall,
                            )
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() },
                                onClick = dismissNumPad,
                            )
                            .padding(Dimens.paddingSmall)
                            .size(Dimens.iconSizeStandard),
                    )
                    Spacer(Modifier.height(Dimens.spacingList))
                }
            }
        }
    }
}

@Composable
private fun GoalAmountBar(
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
            label = "goal_amount_preview",
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
private fun GoalFormCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        shape = RoundedCornerShape(Dimens.cornerRadiusList),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(content = content)
    }
}

@Composable
private fun GoalFormTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    trailingText: String?,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.paddingMedium, vertical = Dimens.categoryEditorFieldVertical),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = typography.bodyLarge,
            modifier = Modifier.weight(0.38f),
        )
        Box(
            modifier = Modifier.weight(0.62f),
            contentAlignment = Alignment.CenterEnd,
        ) {
            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    style = typography.bodyLarge,
                    color = colors.outlineVariant,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle(
                    color = colors.onSurface,
                    fontSize = typography.bodyLarge.fontSize,
                    textAlign = TextAlign.End,
                ),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
        }
    }
    trailingText?.let {
        Text(
            text = it,
            style = typography.labelSmall,
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.End,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.paddingMedium, vertical = Dimens.paddingMicro),
        )
    }
}

@Composable
private fun GoalAmountRow(
    label: String,
    digits: String,
    currencyCode: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(if (selected) colors.primary.copy(alpha = 0.06f) else Color.Transparent)
            .padding(horizontal = Dimens.paddingMedium, vertical = Dimens.categoryEditorFieldVertical),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label, style = typography.bodyLarge)
        Text(
            text = MoneyFormat.formatEntryDisplay(digits, currencyCode),
            style = typography.bodyLarge,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) colors.primary else colors.onSurfaceVariant,
        )
    }
}
