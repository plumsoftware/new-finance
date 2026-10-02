package ru.plumsoftware.finance.presentation.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import ru.plumsoftware.finance.AppConfig
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.domain.model.Category
import ru.plumsoftware.finance.domain.model.CategoryType
import ru.plumsoftware.finance.presentation.common.CategoryColors
import ru.plumsoftware.finance.ui.ads.AdBannerBottomBar
import ru.plumsoftware.finance.ui.ds.CardDivider
import ru.plumsoftware.finance.ui.ds.EmojiBadge
import ru.plumsoftware.finance.ui.ds.EmptyState
import ru.plumsoftware.finance.ui.ds.HSpace
import ru.plumsoftware.finance.ui.ds.IconButton44
import ru.plumsoftware.finance.ui.ds.Kopi
import ru.plumsoftware.finance.ui.ds.SegmentedLight
import ru.plumsoftware.finance.ui.ds.SubScreenAppBar
import ru.plumsoftware.finance.ui.ds.SwipeHint
import ru.plumsoftware.finance.ui.ds.SwipeHintButton
import ru.plumsoftware.finance.ui.ds.rememberSwipeHint
import ru.plumsoftware.finance.ui.ds.VSpace
import ru.plumsoftware.finance.ui.theme.FinanceTheme
import ru.plumsoftware.finance.ui.theme.FinanceType
import kotlin.math.roundToInt

private val RowHeight = 64.dp

/**
 * Категории: «Расходы / Доходы», порядок перетаскиванием за ≡ (долгое нажатие),
 * удаление пользовательских категорий свайпом с подтверждением.
 */
@Composable
fun CategoriesScreen(
    onBack: () -> Unit,
    onAdd: (CategoryType) -> Unit,
    onEdit: (Long) -> Unit,
    viewModel: CategoriesViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val c = FinanceTheme.colors
    val isExpense = state.selectedType == CategoryType.EXPENSE
    val categories = if (isExpense) state.expenseCategories else state.incomeCategories
    var toDelete by remember { mutableStateOf<Category?>(null) }
    val showSwipeHint = rememberSwipeHint(SwipeHint.CATEGORIES, hasItems = categories.isNotEmpty())

    toDelete?.let { cat ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            containerColor = c.surface,
            title = { Text(stringResource(R.string.categories_delete_title, cat.name), style = FinanceType.titleLarge, color = c.textPrimary) },
            text = { Text(stringResource(R.string.categories_delete_text), style = FinanceType.bodySmall, color = c.textSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteCategory(cat.id)
                    toDelete = null
                }) { Text(stringResource(R.string.delete), color = c.dangerText) }
            },
            dismissButton = { TextButton(onClick = { toDelete = null }) { Text(stringResource(R.string.cancel), color = c.textSecondary) } },
        )
    }

    Scaffold(
        containerColor = c.bg,
        topBar = {
            SubScreenAppBar(
                title = stringResource(R.string.categories_title),
                onBack = onBack,
                actions = {
                    SwipeHintButton(showSwipeHint)
                    IconButton44(R.drawable.ic_add, stringResource(R.string.categories_add_category), { onAdd(state.selectedType) })
                },
            )
        },
        bottomBar = { AdBannerBottomBar(adUnitId = AppConfig.bannerCategories) },
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
        ) {
            item {
                SegmentedLight(
                    options = listOf(stringResource(R.string.categories_expense_tab), stringResource(R.string.categories_income_tab)),
                    selectedIndex = if (isExpense) 0 else 1,
                    onSelect = { viewModel.selectType(if (it == 0) CategoryType.EXPENSE else CategoryType.INCOME) },
                    onBackground = true,
                )
                VSpace(12.dp)
            }
            if (categories.isEmpty()) {
                item {
                    EmptyState(
                        title = stringResource(R.string.categories_empty_title),
                        text = stringResource(if (isExpense) R.string.categories_empty_expense_subtitle else R.string.categories_empty_income_subtitle),
                        pose = Kopi.THINKING,
                        action = stringResource(R.string.categories_add_category),
                        onAction = { onAdd(state.selectedType) },
                    )
                }
            } else {
                item(key = "list_${state.selectedType}") {
                    ReorderableCard(
                        categories = categories,
                        onEdit = onEdit,
                        onDeleteRequest = { toDelete = it },
                        onReorder = { viewModel.reorderCategories(state.selectedType, it) },
                    )
                }
                item {
                    VSpace(10.dp)
                    Text(
                        stringResource(R.string.categories_hint),
                        style = FinanceType.caption,
                        color = c.textSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
private fun ReorderableCard(
    categories: List<Category>,
    onEdit: (Long) -> Unit,
    onDeleteRequest: (Category) -> Unit,
    onReorder: (List<Long>) -> Unit,
) {
    val c = FinanceTheme.colors
    val ordered = remember { mutableStateListOf<Category>() }
    var draggingIndex by remember { mutableIntStateOf(-1) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }
    val rowHeightPx = with(LocalDensity.current) { RowHeight.toPx() }

    LaunchedEffect(categories) {
        if (draggingIndex == -1) {
            ordered.clear()
            ordered.addAll(categories)
        }
    }

    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(c.surface),
    ) {
        ordered.forEachIndexed { index, category ->
            if (index > 0) CardDivider(Modifier.padding(start = 70.dp))
            CategoryRow(
                category = category,
                isDragging = index == draggingIndex,
                dragOffsetY = if (index == draggingIndex) dragOffsetY else 0f,
                onClick = { if (draggingIndex == -1) onEdit(category.id) },
                onDeleteRequest = { onDeleteRequest(category) },
                onDragStart = {
                    draggingIndex = index
                    dragOffsetY = 0f
                },
                onDrag = { dy ->
                    dragOffsetY += dy
                    val target = (draggingIndex + (dragOffsetY / rowHeightPx).roundToInt()).coerceIn(0, ordered.lastIndex)
                    if (target != draggingIndex) {
                        ordered.add(target, ordered.removeAt(draggingIndex))
                        dragOffsetY -= (target - draggingIndex) * rowHeightPx
                        draggingIndex = target
                    }
                },
                onDragEnd = {
                    draggingIndex = -1
                    dragOffsetY = 0f
                    onReorder(ordered.map { it.id })
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryRow(
    category: Category,
    isDragging: Boolean,
    dragOffsetY: Float,
    onClick: () -> Unit,
    onDeleteRequest: () -> Unit,
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
) {
    val c = FinanceTheme.colors
    val swipe = rememberSwipeToDismissBoxState()
    LaunchedEffect(swipe.currentValue) {
        if (swipe.currentValue == SwipeToDismissBoxValue.EndToStart) {
            onDeleteRequest()
            swipe.reset()
        }
    }
    SwipeToDismissBox(
        state = swipe,
        enableDismissFromStartToEnd = false,
        // Стандартные категории репозиторий не удаляет — свайп для них отключён.
        enableDismissFromEndToStart = !category.isSystem && !isDragging,
        modifier = Modifier
            .zIndex(if (isDragging) 1f else 0f)
            .offset { IntOffset(0, dragOffsetY.roundToInt()) },
        backgroundContent = {
            Box(Modifier.fillMaxSize().background(c.danger).padding(horizontal = 20.dp), contentAlignment = Alignment.CenterEnd) {
                Text(stringResource(R.string.delete), style = FinanceType.title, color = Color.White)
            }
        },
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .heightIn(min = RowHeight)
                .then(if (isDragging) Modifier.shadow(8.dp, RoundedCornerShape(12.dp)) else Modifier)
                .background(if (isDragging) c.bg else c.surface)
                .clickable(role = Role.Button, onClick = onClick)
                .padding(start = 16.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            EmojiBadge(category.icon, CategoryColors.of(category), size = 42.dp)
            HSpace(12.dp)
            Column(Modifier.weight(1f)) {
                Text(category.name, style = FinanceType.bodyMedium, color = c.textPrimary, maxLines = 2)
                category.monthlyLimitMinor?.takeIf { it > 0 }?.let { limit ->
                    Text(
                        stringResource(R.string.categories_limit_caption, ru.plumsoftware.finance.presentation.common.Money.formatRounded(limit)),
                        style = FinanceType.caption,
                        color = c.textSecondary,
                    )
                }
            }
            Box(
                Modifier
                    .size(44.dp)
                    .pointerInput(category.id) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = { onDragStart() },
                            onDragEnd = { onDragEnd() },
                            onDragCancel = { onDragEnd() },
                            onDrag = { change, amount ->
                                change.consume()
                                onDrag(amount.y)
                            },
                        )
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "≡",
                    style = FinanceType.titleLarge,
                    color = if (isDragging) c.primary else c.textDisabled,
                )
            }
        }
    }
}
