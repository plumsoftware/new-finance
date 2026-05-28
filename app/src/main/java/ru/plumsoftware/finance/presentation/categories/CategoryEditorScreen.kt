package ru.plumsoftware.finance.presentation.categories

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBackIos
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.domain.model.CategoryType
import ru.plumsoftware.finance.ui.components.IosPrimaryButton
import ru.plumsoftware.finance.ui.components.ios.IosNavigationTextButton
import ru.plumsoftware.finance.ui.components.ios.IosTextButton
import ru.plumsoftware.finance.ui.theme.Dimens

@Composable
fun CategoryEditorScreen(
    onBack: () -> Unit,
    viewModel: CategoryEditorViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val listCardShape = RoundedCornerShape(Dimens.cornerRadiusList)

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
                    .padding(horizontal = Dimens.spacingList, vertical = Dimens.spacingRow),
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
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterEnd,
                ) {
                    IosTextButton(
                        text = stringResource(R.string.done),
                        onClick = viewModel::save,
                        enabled = state.canSave,
                        color = if (state.canSave) colors.secondary else colors.outlineVariant,
                        contentPadding = PaddingValues(end = Dimens.paddingMicro),
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = Dimens.paddingMedium),
            ) {
                HeroPreview(state)
                SectionTitle(stringResource(R.string.category_editor_section_name))
                Surface(shape = listCardShape, color = colors.surface) {
                    BasicTextField(
                        value = state.name,
                        onValueChange = viewModel::setName,
                        textStyle = typography.bodyLarge.copy(color = colors.onSurface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimens.paddingMedium),
                        decorationBox = { inner ->
                            if (state.name.isEmpty()) {
                                Text(
                                    stringResource(R.string.category_editor_name_placeholder),
                                    color = colors.outlineVariant,
                                    style = typography.bodyLarge,
                                )
                            }
                            inner()
                        },
                    )
                }
                Text(
                    text = "${state.name.length}/30",
                    color = colors.onSurfaceVariant,
                    style = typography.labelSmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Dimens.paddingMicro),
                )

                if (!state.isEdit) {
                    Spacer(Modifier.size(Dimens.paddingMedium))
                    SectionTitle(stringResource(R.string.category_editor_section_type))
                    Surface(shape = listCardShape, color = colors.surface) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Dimens.paddingMedium),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                stringResource(R.string.category_editor_type_label),
                                style = typography.bodyLarge,
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.spacingList)) {
                                Text(
                                    text = stringResource(R.string.type_expense),
                                    color = if (state.type == CategoryType.EXPENSE) colors.secondary else colors.onSurfaceVariant,
                                    style = typography.bodyLarge,
                                    modifier = Modifier.clickable { viewModel.setType(CategoryType.EXPENSE) },
                                )
                                Text(
                                    text = stringResource(R.string.type_income),
                                    color = if (state.type == CategoryType.INCOME) colors.secondary else colors.onSurfaceVariant,
                                    style = typography.bodyLarge,
                                    modifier = Modifier.clickable { viewModel.setType(CategoryType.INCOME) },
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.size(Dimens.paddingMedium))
                SectionTitle(stringResource(R.string.category_editor_section_icon))
                Surface(shape = listCardShape, color = colors.surface) {
                    LazyHorizontalGrid(
                        rows = GridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimens.spacingList),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.paddingSmall),
                        verticalArrangement = Arrangement.spacedBy(Dimens.paddingSmall),
                    ) {
                        items(state.availableEmojis) { emoji ->
                            val isSelected = emoji == state.icon
                            Box(
                                modifier = Modifier
                                    .size(Dimens.emojiPickerSize + 4.dp)
                                    .background(
                                        color = if (isSelected) Color(state.colorArgb.toInt()).copy(alpha = 0.2f) else colors.surfaceVariant,
                                        shape = RoundedCornerShape(Dimens.cornerRadiusChip),
                                    )
                                    .clickable { viewModel.setIcon(emoji) },
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(emoji, style = typography.headlineMedium)
                            }
                        }
                    }
                }

                Spacer(Modifier.size(Dimens.paddingMedium))
                SectionTitle(stringResource(R.string.category_editor_section_color))
                Surface(shape = listCardShape, color = colors.surface) {
                    Column(
                        modifier = Modifier.padding(Dimens.paddingMedium),
                        verticalArrangement = Arrangement.spacedBy(Dimens.spacingList),
                    ) {
                        categoryColors.chunked(5).forEach { row ->
                            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.spacingList)) {
                                row.forEach { colorValue ->
                                    val isSelected = colorValue == state.colorArgb
                                    Box(
                                        modifier = Modifier
                                            .size(Dimens.colorSwatchSize + 6.dp)
                                            .background(Color(colorValue.toInt()), CircleShape)
                                            .clickable { viewModel.setColor(colorValue) },
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        if (isSelected) {
                                            Text(
                                                stringResource(R.string.checkmark),
                                                color = colors.surface,
                                                style = typography.titleMedium,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.weight(1f))
                IosPrimaryButton(
                    text = stringResource(R.string.save),
                    onClick = viewModel::save,
                    enabled = state.canSave,
                    loading = state.isSaving,
                    modifier = Modifier.padding(bottom = Dimens.bottomSheetBottomPadding),
                )
            }
        }
    }
}

@Composable
private fun HeroPreview(state: CategoryEditorUiState) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val color = Color(state.colorArgb.toInt())
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = Dimens.paddingLarge, bottom = Dimens.paddingLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(Dimens.avatarSizeLarge)
                .background(color.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = state.icon, style = typography.displayMedium)
        }
        Text(
            text = state.name.ifBlank { stringResource(R.string.category_editor_preview) },
            color = if (state.name.isBlank()) colors.outlineVariant else colors.onSurface,
            style = typography.titleMedium,
            modifier = Modifier.padding(top = Dimens.spacingRow),
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    val colors = MaterialTheme.colorScheme
    Text(
        text = text,
        color = colors.onSurfaceVariant,
        style = MaterialTheme.typography.labelSmall,
        modifier = Modifier.padding(start = Dimens.paddingMedium, bottom = Dimens.paddingSmall),
    )
}
