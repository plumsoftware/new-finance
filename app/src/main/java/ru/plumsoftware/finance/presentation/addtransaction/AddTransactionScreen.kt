package ru.plumsoftware.finance.presentation.addtransaction

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlin.math.roundToInt
import org.koin.androidx.compose.koinViewModel
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.domain.model.TransactionType
import ru.plumsoftware.finance.presentation.common.MoneyFormat
import ru.plumsoftware.finance.ui.components.ios.IosAlertDialog
import ru.plumsoftware.finance.ui.components.ios.IosTextField
import ru.plumsoftware.finance.ui.components.PrimaryButton
import ru.plumsoftware.finance.ui.theme.CategoryUiDefaults
import ru.plumsoftware.finance.ui.theme.Dimens
import ru.plumsoftware.finance.ui.theme.Inter28Family

private val numPadKeys = listOf(
    listOf("1", "2", "3"),
    listOf("4", "5", "6"),
    listOf("7", "8", "9"),
    listOf(".", "0", "⌫"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    onBack: () -> Unit,
    viewModel: AddTransactionViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val canSave = MoneyFormat.majorDigitsToMinor(state.amountMajorDigits, state.currencyCode) > 0L && !state.isSaving
    var showQuickCategorySheet by remember { mutableStateOf(false) }
    var quickName by remember { mutableStateOf("") }
    var quickIcon by remember { mutableStateOf("🛒") }
    var quickColor by remember { mutableLongStateOf(CategoryUiDefaults.DEFAULT_COLOR_ARGB) }

    LaunchedEffect(state.saved) {
        if (state.saved) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onBack()
        }
    }

    state.errorMessage?.let { message ->
        IosAlertDialog(
            message = message,
            onDismiss = viewModel::clearError,
        )
    }

    if (showQuickCategorySheet) {
        ModalBottomSheet(
            onDismissRequest = { showQuickCategorySheet = false },
            containerColor = colors.surface,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.SpacingM, vertical = Dimens.SpacingXs),
            ) {
                Text(
                    stringResource(R.string.new_category_title),
                    style = typography.titleLarge,
                )
                Spacer(Modifier.height(Dimens.SpacingS))
                IosTextField(
                    value = quickName,
                    onValueChange = { quickName = it.take(30) },
                    placeholder = stringResource(R.string.category_name),
                )
                Spacer(Modifier.height(Dimens.RadiusS))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingXs),
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                ) {
                    val emojis = if (state.type == TransactionType.INCOME) {
                        listOf("💼", "💻", "📈", "🎓", "💰", "🏆", "🚀", "🎤")
                    } else {
                        listOf("🛒", "☕", "🚌", "💊", "🍕", "🏠", "🎮", "🎁")
                    }
                    emojis.forEach { emoji ->
                        Surface(
                            shape = RoundedCornerShape(Dimens.RadiusS),
                            color = if (quickIcon == emoji) {
                                Color(quickColor.toInt()).copy(alpha = 0.2f)
                            } else {
                                colors.surfaceVariant
                            },
                            modifier = Modifier
                                .size(Dimens.emojiPickerSize)
                                .clickable { quickIcon = emoji },
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(emoji, style = typography.titleMedium)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(Dimens.RadiusS))
                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.RadiusS)) {
                    listOf(
                        CategoryUiDefaults.DEFAULT_COLOR_ARGB,
                        0xFFFF3B30,
                        0xFFFF9500,
                        0xFF34C759,
                        0xFF007AFF,
                        0xFF5856D6,
                    ).forEach { color ->
                        Surface(
                            shape = CircleShape,
                            color = Color(color.toInt()),
                            modifier = Modifier
                                .size(Dimens.colorSwatchSize)
                                .clickable { quickColor = color },
                        ) {
                            if (quickColor == color) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        stringResource(R.string.checkmark),
                                        color = colors.surface,
                                        style = typography.labelLarge,
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(Dimens.RadiusS + Dimens.SpacingXxs))
                AddTransactionPrimaryButton(
                    text = stringResource(R.string.create),
                    onClick = {
                        viewModel.createQuickCategory(
                            name = quickName,
                            icon = quickIcon,
                            colorArgb = quickColor,
                            onCreated = {
                                quickName = ""
                                showQuickCategorySheet = false
                            },
                        )
                    },
                    enabled = quickName.isNotBlank(),
                    loading = state.quickCategorySaving,
                )
                Spacer(Modifier.height(Dimens.SpacingXl))
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = {
            AddTransactionTopBar(
                title = stringResource(R.string.add_transaction),
                onBack = onBack,
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.SpacingL)
                    .navigationBarsPadding()
                    .padding(bottom = Dimens.SpacingM),
            ) {
                AddTransactionPrimaryButton(
                    text = stringResource(R.string.save),
                    onClick = viewModel::save,
                    loading = state.isSaving,
                    enabled = canSave,
                )
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = Dimens.SpacingL),
        ) {
            val segmentType = when (state.type) {
                TransactionType.INCOME -> TransactionType.INCOME
                else -> TransactionType.EXPENSE
            }
            AddTransactionTypeToggle(
                labels = listOf(
                    stringResource(R.string.type_expense),
                    stringResource(R.string.type_income),
                ),
                selectedIndex = if (segmentType == TransactionType.INCOME) 1 else 0,
                onSelectIndex = { index ->
                    viewModel.setType(
                        if (index == 1) TransactionType.INCOME else TransactionType.EXPENSE,
                    )
                },
            )
            Spacer(modifier = Modifier.height(Dimens.SpacingS))
            AddTransactionAmountDisplay(
                digits = state.amountMajorDigits,
                currencyCode = state.currencyCode,
            )
            Spacer(modifier = Modifier.height(Dimens.SpacingS))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingXs),
            ) {
                state.categories.forEach { cat ->
                    AddTransactionCategoryChip(
                        text = "${cat.icon} ${cat.name}",
                        selected = state.selectedCategoryId == cat.id,
                        onClick = { viewModel.selectCategory(cat.id) },
                    )
                }
                AddTransactionCategoryChip(
                    text = stringResource(R.string.plus_sign) + stringResource(R.string.new_category_chip),
                    selected = false,
                    onClick = { showQuickCategorySheet = true },
                )
            }
            Spacer(modifier = Modifier.height(Dimens.SpacingS))
            AddTransactionNoteField(
                value = state.note,
                onValueChange = viewModel::setNote,
                placeholder = stringResource(R.string.note_placeholder),
            )
            Spacer(modifier = Modifier.height(Dimens.SpacingS))
            AddTransactionNumPad(
                onDigit = viewModel::appendDigit,
                onBackspace = viewModel::backspace,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddTransactionTopBar(
    title: String,
    onBack: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title,
                style = typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp,
                ),
                color = colors.onSurface,
            )
        },
        navigationIcon = {
            IconButton(
                onClick = onBack,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                ),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowLeft,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(Dimens.iconSizeNav),
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = colors.background,
        ),
    )
}

@Composable
private fun AddTransactionTypeToggle(
    labels: List<String>,
    selectedIndex: Int,
    onSelectIndex: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (labels.isEmpty()) return
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val safeIndex = selectedIndex.coerceIn(0, labels.lastIndex)
    val animatedIndex by animateFloatAsState(
        targetValue = safeIndex.toFloat(),
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "add_tx_segment_offset",
    )
    val density = LocalDensity.current
    val segmentHeight = Dimens.categoryEditorMiniSegmentHeight
    val segmentInnerPadding = Dimens.categoryEditorMiniSegmentInset
    val trackShape = RoundedCornerShape(segmentHeight / 2)
    val thumbShape = RoundedCornerShape(Dimens.RadiusS)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(segmentHeight)
            .clip(trackShape)
            .background(colors.outline),
    ) {
        val innerWidth = maxWidth - segmentInnerPadding * 2
        val segmentWidth = innerWidth / labels.size
        val indicatorOffsetPx = with(density) { (segmentWidth * animatedIndex).toPx() }

        Box(
            modifier = Modifier
                .padding(segmentInnerPadding)
                .offset { IntOffset(indicatorOffsetPx.roundToInt(), 0) }
                .width(segmentWidth)
                .height(segmentHeight - segmentInnerPadding * 2)
                .shadow(
                    elevation = Dimens.borderThin,
                    shape = thumbShape,
                    clip = false,
                    ambientColor = Color.Black.copy(alpha = 0.12f),
                    spotColor = Color.Black.copy(alpha = 0.12f),
                )
                .clip(thumbShape)
                .background(colors.surface),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(segmentInnerPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            labels.forEachIndexed { index, label ->
                val isSelected = index == safeIndex
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(segmentHeight - segmentInnerPadding * 2)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) { onSelectIndex(index) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = label,
                        style = typography.bodyMedium.copy(fontSize = 15.sp),
                        color = if (isSelected) colors.onSurface else colors.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun AddTransactionAmountDisplay(
    digits: String,
    currencyCode: String,
) {
    val colors = MaterialTheme.colorScheme
    val symbol = MoneyFormat.symbol(currencyCode)
    val display = MoneyFormat.formatEntryDisplay(digits, currencyCode)
    val amountText = if (display.endsWith(symbol)) {
        display.dropLast(symbol.length).trimEnd()
    } else {
        display
    }
    val amountSize = 56.sp

    AnimatedContent(
        targetState = amountText,
        transitionSpec = {
            (scaleIn(
                initialScale = 0.95f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium,
                ),
            ) + fadeIn(tween(120))).togetherWith(
                scaleOut(targetScale = 0.95f, animationSpec = tween(100)) + fadeOut(tween(80)),
            )
        },
        label = "add_tx_amount",
        modifier = Modifier.fillMaxWidth(),
    ) { amount ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = amount,
                style = TextStyle(
                    fontFamily = Inter28Family,
                    fontWeight = FontWeight.Bold,
                    fontSize = amountSize,
                    letterSpacing = (-1).sp,
                ),
                color = colors.onSurface,
                textAlign = TextAlign.Center,
            )
            Text(
                text = " $symbol",
                style = TextStyle(
                    fontFamily = Inter28Family,
                    fontWeight = FontWeight.Light,
                    fontSize = amountSize,
                    letterSpacing = (-1).sp,
                ),
                color = colors.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun AddTransactionCategoryChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val chipShape = RoundedCornerShape(Dimens.RadiusL)
    Box(
        modifier = Modifier
            .height(Dimens.segmentedHeight)
            .clip(chipShape)
            .background(if (selected) colors.primary else colors.outline)
            .clickable(onClick = onClick)
            .padding(horizontal = Dimens.SpacingM),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = typography.bodyMedium.copy(fontSize = 15.sp),
            color = if (selected) colors.onPrimary else colors.onSurface,
        )
    }
}

@Composable
private fun AddTransactionNoteField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val shapes = MaterialTheme.shapes
    val textStyle = typography.bodyMedium.copy(
        fontSize = 14.sp,
        color = colors.onSurface,
    )

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = textStyle,
        singleLine = true,
        cursorBrush = SolidColor(colors.primary),
        modifier = Modifier
            .fillMaxWidth()
            .clip(shapes.small)
            .background(colors.surface)
            .padding(
                horizontal = Dimens.RadiusM,
                vertical = Dimens.RadiusM,
            ),
        decorationBox = { inner ->
            Box(contentAlignment = Alignment.CenterStart) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = textStyle,
                        color = colors.outlineVariant,
                    )
                }
                inner()
            }
        },
    )
}

@Composable
private fun AddTransactionNumPad(
    onDigit: (String) -> Unit,
    onBackspace: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val shapes = MaterialTheme.shapes
    val keyStyle = TextStyle(
        fontFamily = Inter28Family,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
    )
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpacingXs),
    ) {
        numPadKeys.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingXs),
            ) {
                row.forEach { key ->
                    val cellModifier = Modifier
                        .weight(1f)
                        .height(Dimens.numPadKeyHeight)
                    when (key) {
                        "⌫" -> AddTransactionNumPadKey(
                            modifier = cellModifier,
                            onClick = onBackspace,
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Backspace,
                                contentDescription = null,
                                tint = colors.onSurface,
                                modifier = Modifier.size(Dimens.iconSizeNav),
                            )
                        }
                        else -> AddTransactionNumPadKey(
                            modifier = cellModifier,
                            onClick = { onDigit(key) },
                        ) {
                            Text(text = key, style = keyStyle, color = colors.onSurface)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AddTransactionNumPadKey(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val shapes = MaterialTheme.shapes
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val backgroundColor by animateFloatAsState(
        targetValue = if (isPressed) 1f else 0f,
        animationSpec = tween(durationMillis = 80),
        label = "numpad_press",
    )
    val pressedBg = colors.outline
    val normalBg = colors.surface

    Box(
        modifier = modifier
            .clip(shapes.small)
            .background(lerp(normalBg, pressedBg, backgroundColor))
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
private fun AddTransactionPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    if (loading) {
        val colors = MaterialTheme.colorScheme
        Button(
            onClick = onClick,
            enabled = false,
            modifier = modifier
                .fillMaxWidth()
                .height(Dimens.ButtonHeight),
            shape = RoundedCornerShape(Dimens.RadiusL),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.primary,
                contentColor = Color.White,
            ),
            elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp),
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(Dimens.IconSizeM),
                strokeWidth = Dimens.borderThin + 1.5.dp,
                color = Color.White,
            )
        }
    } else {
        PrimaryButton(
            text = text,
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
        )
    }
}
