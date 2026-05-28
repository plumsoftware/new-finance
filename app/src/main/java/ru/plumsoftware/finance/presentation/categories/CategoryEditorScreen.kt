package ru.plumsoftware.finance.presentation.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBackIos
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import ru.plumsoftware.finance.domain.model.CategoryType
import ru.plumsoftware.finance.ui.components.IosPrimaryButton
import ru.plumsoftware.finance.ui.theme.IosBlue

@Composable
fun CategoryEditorScreen(
    onBack: () -> Unit,
    viewModel: CategoryEditorViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.saved) {
        if (state.saved) onBack()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFFF2F2F7),
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onBack),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    androidx.compose.material3.Icon(
                        Icons.AutoMirrored.Outlined.ArrowBackIos,
                        contentDescription = null,
                        tint = IosBlue,
                        modifier = Modifier.size(16.dp),
                    )
                    Text("Категории", color = IosBlue, fontSize = 17.sp)
                }
                Text(
                    text = if (state.isEdit) "Изменить" else "Новая категория",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "Готово",
                    color = if (state.canSave) IosBlue else Color(0xFFC7C7CC),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .weight(1f)
                        .clickable(enabled = state.canSave, onClick = viewModel::save)
                        .padding(end = 4.dp),
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
            ) {
                HeroPreview(state)
                SectionTitle("НАЗВАНИЕ")
                Surface(shape = RoundedCornerShape(16.dp), color = Color.White) {
                    BasicTextField(
                        value = state.name,
                        onValueChange = viewModel::setName,
                        textStyle = TextStyle(color = Color.Black, fontSize = 17.sp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        decorationBox = { inner ->
                            if (state.name.isEmpty()) Text("Напр. Продукты", color = Color(0xFFC7C7CC), fontSize = 17.sp)
                            inner()
                        },
                    )
                }
                Text(
                    text = "${state.name.length}/30",
                    color = Color(0xFF8E8E93),
                    fontSize = 12.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                )

                if (!state.isEdit) {
                    Spacer(Modifier.size(16.dp))
                    SectionTitle("ТИП")
                    Surface(shape = RoundedCornerShape(16.dp), color = Color.White) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text("Тип операции", fontSize = 17.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(
                                    text = "Расход",
                                    color = if (state.type == CategoryType.EXPENSE) IosBlue else Color(0xFF8E8E93),
                                    modifier = Modifier.clickable { viewModel.setType(CategoryType.EXPENSE) },
                                )
                                Text(
                                    text = "Доход",
                                    color = if (state.type == CategoryType.INCOME) IosBlue else Color(0xFF8E8E93),
                                    modifier = Modifier.clickable { viewModel.setType(CategoryType.INCOME) },
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.size(16.dp))
                SectionTitle("ИКОНКА")
                Surface(shape = RoundedCornerShape(16.dp), color = Color.White) {
                    LazyHorizontalGrid(
                        rows = GridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(state.availableEmojis) { emoji ->
                            val isSelected = emoji == state.icon
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(
                                        color = if (isSelected) Color(state.colorArgb.toInt()).copy(alpha = 0.2f) else Color(0xFFF2F2F7),
                                        shape = RoundedCornerShape(12.dp),
                                    )
                                    .clickable { viewModel.setIcon(emoji) },
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(emoji, fontSize = 22.sp)
                            }
                        }
                    }
                }

                Spacer(Modifier.size(16.dp))
                SectionTitle("ЦВЕТ")
                Surface(shape = RoundedCornerShape(16.dp), color = Color.White) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        categoryColors.chunked(5).forEach { row ->
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                row.forEach { colorValue ->
                                    val isSelected = colorValue == state.colorArgb
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(Color(colorValue.toInt()), CircleShape)
                                            .clickable { viewModel.setColor(colorValue) },
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        if (isSelected) Text("✓", color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.weight(1f))
                IosPrimaryButton(
                    text = "Сохранить",
                    onClick = viewModel::save,
                    enabled = state.canSave,
                    loading = state.isSaving,
                    modifier = Modifier.padding(bottom = 34.dp),
                )
            }
        }
    }
}

@Composable
private fun HeroPreview(state: CategoryEditorUiState) {
    val color = Color(state.colorArgb.toInt())
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp, bottom = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(color.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = state.icon, fontSize = 40.sp)
        }
        Text(
            text = state.name.ifBlank { "Название категории" },
            color = if (state.name.isBlank()) Color(0xFFC7C7CC) else Color.Black,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 10.dp),
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        color = Color(0xFF8E8E93),
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(start = 16.dp, bottom = 8.dp),
    )
}

