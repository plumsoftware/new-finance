package ru.plumsoftware.finance.presentation.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.DragHandle
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlin.math.roundToInt
import org.koin.androidx.compose.koinViewModel
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.domain.model.Category
import ru.plumsoftware.finance.domain.model.CategoryType
import ru.plumsoftware.finance.ui.components.AppCard
import ru.plumsoftware.finance.ui.components.PrimaryButton
import ru.plumsoftware.finance.ui.components.ios.IosEditorTopBar
import ru.plumsoftware.finance.ui.components.ios.IosSegmentedControl
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

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = {
            IosEditorTopBar(
                title = stringResource(R.string.categories_title),
                backLabel = stringResource(R.string.categories_back_settings),
                onBack = onBack,
                actionLabel = stringResource(R.string.categories_add),
                onAction = { onAdd(state.selectedType) },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(
                horizontal = Dimens.SpacingM,
                vertical = Dimens.RadiusS,
            ),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingS),
        ) {
            item {
                IosSegmentedControl(
                    labels = listOf(
                        stringResource(R.string.categories_expense_tab),
                        stringResource(R.string.categories_income_tab),
                    ),
                    selectedIndex = if (state.selectedType == CategoryType.EXPENSE) 0 else 1,
                    onSelectIndex = { viewModel.selectType(if (it == 0) CategoryType.EXPENSE else CategoryType.INCOME) },
                    modifier = Modifier.padding(vertical = Dimens.SpacingS),
                )
            }
            if (categories.isEmpty()) {
                item {
                    AppCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = Dimens.SpacingXl,
                                    vertical = Dimens.SpacingXxl,
                                ),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                stringResource(R.string.categories_empty_emoji),
                                style = typography.displayMedium,
                            )
                            Text(
                                stringResource(R.string.categories_empty_title),
                                style = typography.titleMedium,
                                modifier = Modifier.padding(top = Dimens.SpacingS),
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
                                modifier = Modifier.padding(top = Dimens.SpacingXs),
                            )
                            PrimaryButton(
                                text = stringResource(R.string.categories_add_category),
                                onClick = { onAdd(state.selectedType) },
                                modifier = Modifier.padding(top = Dimens.SpacingXl),
                            )
                        }
                    }
                }
            } else {
                item {
                    ReorderableCategoriesCard(
                        categories = categories,
                        onEdit = onEdit,
                        onDelete = { viewModel.deleteCategory(it) },
                        onReorder = { viewModel.reorderCategories(state.selectedType, it) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ReorderableCategoriesCard(
    categories: List<Category>,
    onEdit: (Long) -> Unit,
    onDelete: (Long) -> Unit,
    onReorder: (List<Long>) -> Unit,
) {
    val ordered = remember { mutableStateListOf<Category>() }
    var draggingIndex by remember { mutableIntStateOf(-1) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }
    val density = LocalDensity.current
    val rowHeightPx = with(density) {
        (Dimens.avatarSize + Dimens.categoryRowVerticalPadding * 2).toPx()
    }
    val categoryIds = categories.map { it.id }

    LaunchedEffect(categoryIds) {
        if (draggingIndex == -1) {
            ordered.clear()
            ordered.addAll(categories)
        }
    }

    AppCard(modifier = Modifier.fillMaxWidth()) {
        ordered.forEachIndexed { index, category ->
            CategoryListRow(
                category = category,
                onClick = { if (draggingIndex == -1) onEdit(category.id) },
                onDelete = { onDelete(category.id) },
                isDragging = index == draggingIndex,
                dragOffsetY = if (index == draggingIndex) dragOffsetY else 0f,
                onDragStart = {
                    draggingIndex = index
                    dragOffsetY = 0f
                },
                onDrag = { amount ->
                    dragOffsetY += amount.y
                    val targetIndex = (draggingIndex + (dragOffsetY / rowHeightPx).roundToInt())
                        .coerceIn(0, ordered.lastIndex)
                    if (targetIndex != draggingIndex) {
                        ordered.add(targetIndex, ordered.removeAt(draggingIndex))
                        draggingIndex = targetIndex
                        dragOffsetY = 0f
                    }
                },
                onDragEnd = {
                    draggingIndex = -1
                    dragOffsetY = 0f
                    onReorder(ordered.map { it.id })
                },
            )
            if (index != ordered.lastIndex) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline,
                    thickness = Dimens.dividerThickness,
                    modifier = Modifier.padding(
                        start = Dimens.SpacingM + Dimens.avatarSize + Dimens.SpacingS,
                    ),
                )
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
    isDragging: Boolean,
    dragOffsetY: Float,
    onDragStart: () -> Unit,
    onDrag: (androidx.compose.ui.geometry.Offset) -> Unit,
    onDragEnd: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val defaultCategoryColor = colors.onSurfaceVariant
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
                    .background(colors.error),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.cd_delete),
                    tint = colors.onError,
                    modifier = Modifier.padding(end = Dimens.categorySwipeDeleteEndPadding),
                )
            }
        },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .zIndex(if (isDragging) 1f else 0f)
                .offset { IntOffset(0, dragOffsetY.roundToInt()) }
                .then(
                    if (isDragging) {
                        Modifier.shadow(Dimens.SpacingXs, RoundedCornerShape(Dimens.categorySwipeCornerRadius))
                    } else {
                        Modifier
                    },
                )
                .clickable(onClick = onClick)
                .clip(RoundedCornerShape(Dimens.categorySwipeCornerRadius))
                .background(if (isDragging) colors.surfaceVariant else colors.surface)
                .padding(
                    horizontal = Dimens.SpacingM,
                    vertical = Dimens.categoryRowVerticalPadding,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(Dimens.avatarSize)
                    .background(color.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = category.icon, style = typography.titleMedium)
            }
            Text(
                text = category.name,
                style = typography.bodyLarge,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = Dimens.SpacingS),
            )
            Icon(
                imageVector = Icons.Outlined.DragHandle,
                contentDescription = stringResource(R.string.cd_drag_handle),
                tint = if (isDragging) colors.primary else colors.outlineVariant,
                modifier = Modifier
                    .size(Dimens.dragIconSize)
                    .pointerInput(category.id) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = { onDragStart() },
                            onDragEnd = { onDragEnd() },
                            onDragCancel = { onDragEnd() },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                onDrag(dragAmount)
                            },
                        )
                    },
            )
        }
    }
}
