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
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import ru.plumsoftware.finance.domain.model.Category
import ru.plumsoftware.finance.domain.model.CategoryType
import ru.plumsoftware.finance.ui.components.IosPrimaryButton
import ru.plumsoftware.finance.ui.components.ios.IosSegmentedControl
import ru.plumsoftware.finance.ui.theme.IosBlue
import ru.plumsoftware.finance.ui.theme.IosRed

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
        containerColor = Color(0xFFF2F2F7),
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clickable(onClick = onBack),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBackIos, contentDescription = null, tint = IosBlue, modifier = Modifier.size(16.dp))
                        Text("Настройки", color = IosBlue, fontSize = 17.sp)
                    }
                    Text("Категории", fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                    Text(
                        text = "Добавить",
                        color = IosBlue,
                        fontSize = 17.sp,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onAdd(state.selectedType) },
                        textAlign = TextAlign.End,
                    )
                }
            }
            item {
                IosSegmentedControl(
                    labels = listOf("Расходы", "Доходы"),
                    selectedIndex = if (state.selectedType == CategoryType.EXPENSE) 0 else 1,
                    onSelectIndex = { viewModel.selectType(if (it == 0) CategoryType.EXPENSE else CategoryType.INCOME) },
                )
            }
            if (categories.isEmpty()) {
                item {
                    Surface(shape = RoundedCornerShape(16.dp), color = Color.White) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text("🏷️", fontSize = 48.sp)
                            Text("Нет категорий", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 12.dp))
                            Text(
                                text = if (state.selectedType == CategoryType.EXPENSE) {
                                    "Добавьте первую категорию расходов"
                                } else {
                                    "Добавьте первую категорию доходов"
                                },
                                color = Color(0xFF8E8E93),
                                fontSize = 15.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 8.dp),
                            )
                            IosPrimaryButton(
                                text = "Добавить категорию",
                                onClick = { onAdd(state.selectedType) },
                                modifier = Modifier.padding(top = 20.dp),
                            )
                        }
                    }
                }
            } else {
                item {
                    Surface(shape = RoundedCornerShape(16.dp), color = Color.White) {
                        Column {
                            categories.forEachIndexed { index, category ->
                                CategoryListRow(
                                    category = category,
                                    onClick = { onEdit(category.id) },
                                    onDelete = { pendingDelete = category },
                                )
                                if (index != categories.lastIndex) {
                                    HorizontalDivider(
                                        color = Color(0xFFE5E5EA),
                                        thickness = 0.5.dp,
                                        modifier = Modifier.padding(start = 60.dp),
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
    val color = Color(category.colorArgb?.toInt() ?: 0xFF8E8E93.toInt())
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(category.icon, fontSize = 20.sp)
            }
            Text(
                category.name,
                fontSize = 17.sp,
                color = Color.Black,
                modifier = Modifier.padding(start = 12.dp),
            )
        }
        Icon(
            imageVector = Icons.Outlined.Delete,
            contentDescription = "Удалить",
            tint = IosRed,
            modifier = Modifier
                .size(20.dp)
                .clickable(onClick = onDelete),
        )
        Spacer(Modifier.size(8.dp))
        Icon(Icons.Outlined.DragHandle, contentDescription = null, tint = Color(0xFFC7C7CC), modifier = Modifier.size(20.dp))
        Icon(Icons.Outlined.KeyboardArrowRight, contentDescription = null, tint = Color(0xFFC7C7CC), modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun DeleteCategoryDialog(
    name: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(14.dp), color = Color.White) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text("Удалить «$name»?", fontWeight = FontWeight.SemiBold)
                Text(
                    "Эта категория будет удалена. Операции сохранятся.",
                    color = Color(0xFF8E8E93),
                    modifier = Modifier.padding(top = 8.dp),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp),
                    horizontalArrangement = Arrangement.End,
                ) {
                    Text("Отмена", color = IosBlue, modifier = Modifier.clickable(onClick = onDismiss).padding(8.dp))
                    Text("Удалить", color = IosRed, fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable(onClick = onConfirm).padding(8.dp))
                }
            }
        }
    }
}

