package ru.plumsoftware.finance.presentation.categories

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.domain.model.CategoryType
import ru.plumsoftware.finance.ui.components.IosPrimaryButton
import ru.plumsoftware.finance.ui.components.ios.IosNavigationTextButton
import ru.plumsoftware.finance.ui.components.ios.IosTextButton
import ru.plumsoftware.finance.ui.theme.Dimens

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryEditorScreen(
    onBack: () -> Unit,
    viewModel: CategoryEditorViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val selectedColor = Color(state.colorArgb.toInt())
    val previewColor by animateColorAsState(
        targetValue = selectedColor,
        animationSpec = tween(300),
        label = "category_preview_color",
    )

    LaunchedEffect(state.saved) {
        if (state.saved) onBack()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = colors.background,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.paddingMedium),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IosNavigationTextButton(
                    text = stringResource(R.string.categories_title),
                    onClick = onBack,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = if (state.isEdit) {
                        stringResource(R.string.category_editor_title_edit)
                    } else {
                        stringResource(R.string.category_editor_title_new)
                    },
                    style = typography.bodyLarge,
                )
                IosTextButton(
                    text = stringResource(R.string.done),
                    onClick = viewModel::save,
                    enabled = state.canSave,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.End,
                    color = if (state.canSave) colors.secondary else colors.outlineVariant,
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Dimens.paddingMedium),
            ) {
                HeroPreview(
                    name = state.name,
                    icon = state.icon,
                    previewColor = previewColor,
                )
                SectionTitle(stringResource(R.string.category_editor_section_name))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(Dimens.cornerRadiusList))
                        .background(colors.surface)
                        .padding(
                            horizontal = Dimens.paddingMedium,
                            vertical = Dimens.categoryEditorFieldVertical
                        ),
                ) {
                    if (state.name.isEmpty()) {
                        Text(
                            text = stringResource(R.string.category_editor_name_placeholder),
                            color = colors.outlineVariant,
                            style = typography.bodyLarge,
                        )
                    }
                    BasicTextField(
                        value = state.name,
                        onValueChange = viewModel::setName,
                        textStyle = TextStyle(
                            fontSize = typography.bodyLarge.fontSize,
                            color = colors.onSurface,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                }
                Text(
                    text = "${state.name.length}/30",
                    style = typography.labelSmall,
                    color = colors.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Dimens.categoryEditorCounterTop, end = Dimens.paddingMicro),
                    textAlign = TextAlign.End,
                )

                Spacer(Modifier.height(Dimens.categoryEditorSectionGap))
                SectionTitle(stringResource(R.string.category_editor_section_type))
                Surface(
                    shape = RoundedCornerShape(Dimens.cornerRadiusList),
                    color = colors.surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = Dimens.paddingMedium,
                                vertical = Dimens.spacingList
                            ),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.category_editor_type_label),
                            style = typography.bodyLarge,
                            color = colors.onSurface,
                        )
                        Box(
                            modifier = Modifier
                                .height(Dimens.categoryEditorMiniSegmentHeight)
                                .clip(RoundedCornerShape(Dimens.cornerRadiusSegmentInner))
                                .background(colors.surfaceVariant)
                                .padding(Dimens.categoryEditorMiniSegmentInset),
                        ) {
                            Row {
                                listOf(CategoryType.EXPENSE, CategoryType.INCOME).forEach { type ->
                                    val selected = state.type == type
                                    Box(
                                        modifier = Modifier
                                            .height(Dimens.categoryEditorMiniSegmentItemHeight)
                                            .clip(RoundedCornerShape(Dimens.paddingSmall - Dimens.paddingMicro))
                                            .background(if (selected) colors.surface else Color.Transparent)
                                            .clickable { viewModel.setType(type) }
                                            .padding(horizontal = Dimens.spacingRow),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            text = if (type == CategoryType.EXPENSE) {
                                                stringResource(R.string.type_expense)
                                            } else {
                                                stringResource(R.string.type_income)
                                            },
                                            style = typography.labelLarge,
                                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                            color = if (selected) colors.onSurface else colors.onSurfaceVariant,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(Dimens.categoryEditorSectionGap))
                SectionTitle(stringResource(R.string.category_editor_section_icon))
                Surface(
                    shape = RoundedCornerShape(Dimens.cornerRadiusList),
                    color = colors.surface
                ) {
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimens.spacingList),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.paddingSmall),
                        verticalArrangement = Arrangement.spacedBy(Dimens.paddingSmall),
                    ) {
                        state.availableEmojis.forEach { emoji ->
                            val isSelected = emoji == state.icon
                            Box(
                                modifier = Modifier
                                    .size(Dimens.categoryEditorIconCell)
                                    .clip(CircleShape)
                                    .background(if (isSelected) selectedColor.copy(alpha = 0.18f) else colors.surfaceVariant)
                                    .then(
                                        if (isSelected) Modifier.border(
                                            Dimens.categoryEditorIconBorder,
                                            selectedColor,
                                            CircleShape
                                        ) else Modifier,
                                    )
                                    .clickable { viewModel.setIcon(emoji) },
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = emoji,
                                    style = typography.headlineSmall,
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(Dimens.categoryEditorSectionGap))
                SectionTitle(stringResource(R.string.category_editor_section_color))
                Surface(
                    shape = RoundedCornerShape(Dimens.cornerRadiusList),
                    color = colors.surface
                ) {
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimens.paddingMedium),
                        horizontalArrangement = Arrangement.spacedBy(
                            Dimens.spacingRow,
                            Alignment.CenterHorizontally
                        ),
                        verticalArrangement = Arrangement.spacedBy(Dimens.spacingRow),
                    ) {
                        categoryColors.forEach { colorValue ->
                            val color = Color(colorValue.toInt())
                            val isSelected = colorValue == state.colorArgb
                            Box(
                                modifier = Modifier
                                    .size(Dimens.categoryEditorSwatch)
                                    .aspectRatio(1f)
                                    .clip(CircleShape)
                                    .background(color)
                                    .then(
                                        if (isSelected) Modifier.border(
                                            Dimens.categoryEditorSwatchBorder,
                                            colors.surface,
                                            CircleShape
                                        ) else Modifier,
                                    )
                                    .clickable { viewModel.setColor(colorValue) },
                                contentAlignment = Alignment.Center,
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = colors.surface,
                                        modifier = Modifier.size(Dimens.categoryEditorSwatchCheck),
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(Dimens.paddingMedium))
            }

            IosPrimaryButton(
                text = stringResource(R.string.save),
                onClick = viewModel::save,
                enabled = state.canSave,
                loading = state.isSaving,
                modifier = Modifier.padding(
                    start = Dimens.paddingMedium,
                    end = Dimens.paddingMedium,
                    bottom = Dimens.bottomSheetBottomPadding,
                ),
            )
        }
    }
}

@Composable
private fun HeroPreview(
    name: String,
    icon: String,
    previewColor: Color,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = Dimens.spacingSection, bottom = Dimens.spacingSection),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(Dimens.avatarSizeLarge)
                .clip(CircleShape)
                .background(previewColor.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.displayMedium,
            )
        }
        Text(
            text = name.ifBlank { stringResource(R.string.category_editor_preview) },
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (name.isNotBlank()) FontWeight.SemiBold else FontWeight.Normal,
            color = if (name.isNotBlank()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outlineVariant,
            modifier = Modifier.padding(top = Dimens.spacingRow),
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.labelSmall,
        modifier = Modifier.padding(start = Dimens.paddingMicro, bottom = Dimens.paddingSmall),
    )
}
