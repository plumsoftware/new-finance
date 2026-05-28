package ru.plumsoftware.finance.presentation.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBackIos
import androidx.compose.material.icons.outlined.DragHandle
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.domain.model.Category
import ru.plumsoftware.finance.domain.model.CategoryType
import ru.plumsoftware.finance.ui.components.IosPrimaryButton
import ru.plumsoftware.finance.ui.components.ios.IosNavigationTextButton
import ru.plumsoftware.finance.ui.components.ios.IosSegmentedControl
import ru.plumsoftware.finance.ui.components.ios.IosTextButton
import ru.plumsoftware.finance.ui.theme.Dimens

@Composable
fun CategoriesScreen(
    onBack: () -> Unit,
    onAdd: (CategoryType) -> Unit,
    onEdit: (Long) -> Unit,
    viewModel: CategoriesViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var pendingDelete by remember { mutableStateOf<Category?>(null) }
    val categories = if (state.selectedType == CategoryType.EXPENSE) state.expenseCategories else state.incomeCategories
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val listCardShape = RoundedCornerShape(Dimens.cornerRadiusList)

    pendingDelete?.let { cat ->
        DeleteCategoryDialog(
            name = cat.name,
            onDismiss = { pendingDelete = null },
            onConfirm = {
                viewModel.deleteCategory(cat.id)
                pendingDelete = null
            },
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = colors.background,
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = Dimens.paddingMedium, vertical = Dimens.spacingRow),
            verticalArrangement = Arrangement.spacedBy(Dimens.spacingList),
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IosNavigationTextButton(
                        text = stringResource(R.string.categories_back_settings),
                        onClick = onBack,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        stringResource(R.string.categories_title),
                        style = typography.bodyLarge,
                    )
                    IosTextButton(
                        text = stringResource(R.string.categories_add),
                        onClick = { onAdd(state.selectedType) },
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.End,
                    )
                }
            }
            item {
                IosSegmentedControl(
                    labels = listOf(
                        stringResource(R.string.categories_expense_tab),
                        stringResource(R.string.categories_income_tab),
                    ),
                    selectedIndex = if (state.selectedType == CategoryType.EXPENSE) 0 else 1,
                    onSelectIndex = { viewModel.selectType(if (it == 0) CategoryType.EXPENSE else CategoryType.INCOME) },
                )
            }
            if (categories.isEmpty()) {
                item {
                    Surface(shape = listCardShape, color = colors.surface) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Dimens.paddingLarge, vertical = Dimens.paddingExtraLarge),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                stringResource(R.string.categories_empty_emoji),
                                style = typography.displayMedium,
                            )
                            Text(
                                stringResource(R.string.categories_empty_title),
                                style = typography.titleMedium,
                                modifier = Modifier.padding(top = Dimens.spacingList),
                            )
                            Text(
                                text = if (state.selectedType == CategoryType.EXPENSE) {
                                    stringResource(R.string.categories_empty_expense_subtitle)
                                } else {
                                    stringResource(R.string.categories_empty_income_subtitle)
                                },
                                color = colors.onSurfaceVariant,
                                style = typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = Dimens.paddingSmall),
                            )
                            IosPrimaryButton(
                                text = stringResource(R.string.categories_add_category),
                                onClick = { onAdd(state.selectedType) },
                                modifier = Modifier.padding(top = Dimens.paddingLarge),
                            )
                        }
                    }
                }
            } else {
                item {
                    Surface(shape = listCardShape, color = colors.surface) {
                        Column {
                            categories.forEachIndexed { index, category ->
                                CategoryListRow(
                                    category = category,
                                    onClick = { onEdit(category.id) },
                                    onDelete = { pendingDelete = category },
                                )
                                if (index != categories.lastIndex) {
                                    HorizontalDivider(
                                        color = colors.outline,
                                        thickness = Dimens.dividerThickness,
                                        modifier = Modifier.padding(start = Dimens.categoryRowInset),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryListRow(
    category: Category,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val color = Color(category.colorArgb?.toInt() ?: 0xFF8E8E93.toInt())
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = Dimens.paddingMedium, vertical = Dimens.paddingSmall),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .size(Dimens.avatarSize)
                    .background(color.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(category.icon, style = typography.titleMedium)
            }
            Text(
                category.name,
                style = typography.bodyLarge,
                color = colors.onSurface,
                modifier = Modifier.padding(start = Dimens.spacingList),
            )
        }
        Icon(
            imageVector = Icons.Outlined.Delete,
            contentDescription = stringResource(R.string.cd_delete),
            tint = colors.error,
            modifier = Modifier
                .size(Dimens.dragIconSize)
                .clickable(onClick = onDelete),
        )
        Spacer(Modifier.size(Dimens.paddingSmall))
        Icon(
            Icons.Outlined.DragHandle,
            contentDescription = null,
            tint = colors.outlineVariant,
            modifier = Modifier.size(Dimens.dragIconSize),
        )
        Icon(
            Icons.Outlined.KeyboardArrowRight,
            contentDescription = null,
            tint = colors.outlineVariant,
            modifier = Modifier.size(Dimens.dragIconSize),
        )
    }
}

@Composable
private fun DeleteCategoryDialog(
    name: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val shapes = MaterialTheme.shapes
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(shape = shapes.small, color = colors.surface) {
            Column(modifier = Modifier.padding(Dimens.paddingMedium + 2.dp)) {
                Text(
                    stringResource(R.string.categories_delete_title, name),
                    style = typography.titleMedium,
                )
                Text(
                    stringResource(R.string.categories_delete_message),
                    color = colors.onSurfaceVariant,
                    style = typography.bodyMedium,
                    modifier = Modifier.padding(top = Dimens.paddingSmall),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Dimens.spacingRow + 4.dp),
                    horizontalArrangement = Arrangement.End,
                ) {
                    IosTextButton(
                        text = stringResource(R.string.cancel),
                        onClick = onDismiss,
                    )
                    IosTextButton(
                        text = stringResource(R.string.delete),
                        onClick = onConfirm,
                        color = colors.error,
                    )
                }
            }
        }
    }
}
