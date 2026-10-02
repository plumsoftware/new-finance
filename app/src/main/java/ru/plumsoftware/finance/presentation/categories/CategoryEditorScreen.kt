package ru.plumsoftware.finance.presentation.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.domain.model.CategoryType
import ru.plumsoftware.finance.ui.ds.BottomActionBar
import ru.plumsoftware.finance.ui.ds.EmojiPicker
import ru.plumsoftware.finance.ui.ds.FCard
import ru.plumsoftware.finance.ui.ds.FormTextField
import ru.plumsoftware.finance.ui.ds.Kopi
import ru.plumsoftware.finance.ui.ds.LocalMascotSnackbar
import ru.plumsoftware.finance.ui.ds.SectionHeader
import ru.plumsoftware.finance.ui.ds.SegmentedLight
import ru.plumsoftware.finance.ui.ds.SubScreenAppBar
import ru.plumsoftware.finance.ui.ds.VSpace
import ru.plumsoftware.finance.ui.theme.FinanceTheme
import ru.plumsoftware.finance.ui.theme.FinanceType

/**
 * Новая категория / изменение категории. Тип выбирается только при создании;
 * у стандартных категорий меняются иконка и цвет, название зафиксировано.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryEditorScreen(
    onBack: () -> Unit,
    viewModel: CategoryEditorViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val c = FinanceTheme.colors
    val snackbar = LocalMascotSnackbar.current
    val accent = Color(state.colorArgb.toInt())

    LaunchedEffect(state.saved) { if (state.saved) onBack() }
    LaunchedEffect(state.error) {
        state.error?.let {
            snackbar.show(it, Kopi.THINKING)
            viewModel.clearError()
        }
    }

    Column(Modifier.fillMaxSize().background(c.bg).imePadding()) {
        SubScreenAppBar(
            title = stringResource(if (state.isEdit) R.string.category_editor_title_edit else R.string.category_editor_title_new),
            onBack = onBack,
        )
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Превью как в списках: эмодзи на подложке цвета 13%.
            Column(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier.size(84.dp).background(accent.copy(alpha = 0x22 / 255f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) { Text(state.icon, fontSize = 40.sp) }
                VSpace(8.dp)
                Text(
                    state.name.ifBlank { stringResource(R.string.category_editor_name_placeholder) },
                    style = FinanceType.title,
                    color = if (state.name.isBlank()) c.textSecondary else c.textPrimary,
                )
            }

            SectionHeader(stringResource(R.string.category_editor_section_name))
            FCard {
                if (state.isSystem) {
                    Box(Modifier.fillMaxWidth().heightIn(min = 40.dp), contentAlignment = Alignment.CenterStart) {
                        Text(state.name, style = FinanceType.body, color = c.textSecondary)
                    }
                    Text(stringResource(R.string.category_system_name_locked), style = FinanceType.caption, color = c.textSecondary)
                } else {
                    FormTextField(state.name, viewModel::setName, stringResource(R.string.category_editor_name_placeholder))
                }
            }

            if (!state.isEdit) {
                SectionHeader(stringResource(R.string.category_editor_section_type))
                SegmentedLight(
                    options = listOf(stringResource(R.string.type_expense), stringResource(R.string.type_income)),
                    selectedIndex = if (state.type == CategoryType.EXPENSE) 0 else 1,
                    onSelect = { viewModel.setType(if (it == 0) CategoryType.EXPENSE else CategoryType.INCOME) },
                    onBackground = true,
                )
            }

            SectionHeader(stringResource(R.string.category_editor_section_icon))
            FCard {
                EmojiPicker((listOf(state.icon) + state.availableEmojis).distinct().take(18), state.icon, viewModel::setIcon)
            }

            SectionHeader(stringResource(R.string.category_editor_section_color))
            FCard {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    categoryColors.forEach { argb ->
                        val color = Color(argb.toInt())
                        val selected = argb == state.colorArgb
                        Box(
                            Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(color)
                                .then(if (selected) Modifier.border(3.dp, c.surface, CircleShape) else Modifier)
                                .clickable(role = Role.RadioButton) { viewModel.setColor(argb) }
                                .semantics {
                                    this.selected = selected
                                    contentDescription = "#%06X".format(argb and 0xFFFFFF)
                                },
                        )
                    }
                }
            }
            VSpace(16.dp)
        }
        BottomActionBar(
            text = stringResource(R.string.save),
            onClick = viewModel::save,
            enabled = state.canSave && !state.isSaving,
        )
    }
}
