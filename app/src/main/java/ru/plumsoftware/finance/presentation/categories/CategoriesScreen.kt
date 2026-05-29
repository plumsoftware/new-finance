package ru.plumsoftware.finance.presentation.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.DragHandle
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
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

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    onBack: () -> Unit,
    onAdd: (CategoryType) -> Unit,
    onEdit: (Long) -> Unit,
    viewModel: CategoriesViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val categories = if (state.selectedType == CategoryType.EXPENSE) state.expenseCategories else state.incomeCategories
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val listCardShape = RoundedCornerShape(Dimens.cornerRadiusList)

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
                    modifier = Modifier.padding(vertical = Dimens.spacingList),
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
                                    onDelete = { viewModel.deleteCategory(category.id) },
                                )
                                if (index != categories.lastIndex) {
                                    HorizontalDivider(
                                        color = colors.outline,
                                        thickness = Dimens.dividerThickness,
                                        modifier = Modifier.padding(start = Dimens.paddingMedium + Dimens.avatarSize + Dimens.spacingList),
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

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun CategoryListRow(
    category: Category,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    val defaultCategoryColor = MaterialTheme.colorScheme.onSurfaceVariant
    val color = category.colorArgb?.let { Color(it.toInt()) } ?: defaultCategoryColor
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else {
                false
            }
        },
        positionalThreshold = { it * 0.35f },
    )
    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Dimens.categorySwipeCornerRadius))
                    .background(MaterialTheme.colorScheme.error),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.cd_delete),
                    tint = MaterialTheme.colorScheme.onError,
                    modifier = Modifier.padding(end = Dimens.categorySwipeDeleteEndPadding),
                )
            }
        },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .clip(RoundedCornerShape(Dimens.categorySwipeCornerRadius))
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = Dimens.paddingMedium, vertical = Dimens.categoryRowVerticalPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(Dimens.avatarSize)
                    .background(color.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = category.icon, style = MaterialTheme.typography.titleMedium)
            }
            Text(
                text = category.name,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = Dimens.spacingList),
            )
            Icon(
                imageVector = Icons.Outlined.DragHandle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.size(Dimens.dragIconSize),
            )
        }
    }
}

