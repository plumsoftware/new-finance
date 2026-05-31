package ru.plumsoftware.finance.presentation.recurring

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.domain.model.AppSettings
import ru.plumsoftware.finance.domain.model.Category
import ru.plumsoftware.finance.domain.model.RecurringTransaction
import ru.plumsoftware.finance.domain.repository.SettingsRepository
import ru.plumsoftware.finance.presentation.common.MoneyFormat
import ru.plumsoftware.finance.ui.components.AppCard
import ru.plumsoftware.finance.ui.components.PrimaryButton
import ru.plumsoftware.finance.ui.components.SectionLabel
import ru.plumsoftware.finance.ui.theme.Dimens
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringScreen(
    navController: NavController,
    viewModel: RecurringViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val settingsRepository: SettingsRepository = koinInject()
    val settings by settingsRepository.settings.collectAsStateWithLifecycle(initialValue = AppSettings())
    val currencyCode = settings.defaultCurrencyCode
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    var showAddSheet by remember { mutableStateOf(false) }

    if (showAddSheet) {
        AddRecurringSheet(
            categories = state.categories,
            currencyCode = currencyCode,
            onDismiss = { showAddSheet = false },
            onSave = { transaction ->
                viewModel.add(transaction)
                showAddSheet = false
            },
        )
    }

    val (activeItems, pausedItems) = remember(state.items) {
        state.items.partition { it.isActive }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.recurring_transactions),
                        style = typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = navController::popBackStack) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBackIosNew,
                            contentDescription = stringResource(R.string.cd_back),
                            tint = colors.primary,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showAddSheet = true }) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = stringResource(R.string.cd_add),
                            tint = colors.primary,
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = colors.background,
                ),
            )
        },
    ) { padding ->
        if (state.items.isEmpty()) {
            RecurringEmptyState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                onAddClick = { showAddSheet = true },
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(vertical = Dimens.SpacingXs),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpacingXs),
            ) {
                if (activeItems.isNotEmpty()) {
                    item { SectionLabel(text = stringResource(R.string.recurring_active)) }
                    items(activeItems, key = { it.id }) { item ->
                        RecurringCard(
                            item = item,
                            category = state.categoryMap[item.categoryId],
                            currencyCode = currencyCode,
                            dimmed = false,
                            onToggleActive = { active ->
                                viewModel.toggleActive(item.id, active)
                            },
                            onDelete = { viewModel.delete(item.id) },
                        )
                    }
                }

                if (pausedItems.isNotEmpty()) {
                    item {
                        SectionLabel(
                            text = stringResource(R.string.recurring_paused),
                            modifier = Modifier.padding(top = Dimens.SpacingM),
                        )
                    }
                    items(pausedItems, key = { it.id }) { item ->
                        RecurringCard(
                            item = item,
                            category = state.categoryMap[item.categoryId],
                            currencyCode = currencyCode,
                            dimmed = true,
                            onToggleActive = { active ->
                                viewModel.toggleActive(item.id, active)
                            },
                            onDelete = { viewModel.delete(item.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecurringEmptyState(
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    Column(
        modifier = modifier.padding(horizontal = Dimens.SpacingL),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Rounded.Repeat,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = colors.onSurfaceVariant.copy(alpha = 0.4f),
        )
        Spacer(Modifier.height(Dimens.SpacingL))
        Text(
            text = stringResource(R.string.no_recurring),
            style = typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(Dimens.SpacingXs))
        Text(
            text = stringResource(R.string.no_recurring_desc),
            style = typography.bodyMedium,
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(Dimens.SpacingXl))
        PrimaryButton(
            text = stringResource(R.string.add_recurring),
            onClick = onAddClick,
            modifier = Modifier.padding(horizontal = Dimens.SpacingXxl),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecurringCard(
    item: RecurringTransaction,
    category: Category?,
    currencyCode: String,
    dimmed: Boolean,
    onToggleActive: (Boolean) -> Unit,
    onDelete: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val categoryColor = category?.colorArgb?.let { Color(it.toInt()) } ?: colors.onSurfaceVariant
    val amountColor = if (item.isIncome) colors.secondary else colors.error
    val amountPrefix = if (item.isIncome) "+" else "−"
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else {
                false
            }
        },
        positionalThreshold = { fullWidth -> fullWidth * 0.35f },
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        modifier = Modifier
            .padding(horizontal = Dimens.SpacingL)
            .alpha(if (dimmed) 0.5f else 1f),
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(Dimens.RadiusL))
                    .background(colors.error.copy(alpha = 0.1f))
                    .padding(end = Dimens.SpacingL),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Delete,
                    contentDescription = stringResource(R.string.cd_delete),
                    tint = colors.error,
                    modifier = Modifier.size(Dimens.IconSizeM),
                )
            }
        },
    ) {
        AppCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(Dimens.SpacingM),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(Dimens.RadiusM))
                        .background(categoryColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = category?.icon ?: "•",
                        fontSize = 22.sp,
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = Dimens.SpacingM),
                ) {
                    Text(
                        text = item.title,
                        style = typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "${stringResource(item.frequency.labelRes)} · ${
                            stringResource(
                                R.string.next_date,
                                formatRecurringDate(item.nextDateMillis),
                            )
                        }",
                        style = typography.bodySmall,
                        color = colors.onSurfaceVariant,
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$amountPrefix${MoneyFormat.format(item.amountMinor, currencyCode)}",
                        style = typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = amountColor,
                    )
                    Switch(
                        checked = item.isActive,
                        onCheckedChange = onToggleActive,
                        modifier = Modifier.scale(0.75f),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = colors.primary,
                            uncheckedTrackColor = colors.surfaceVariant,
                        ),
                    )
                }
            }
        }
    }
}

private fun formatRecurringDate(timestamp: Long): String {
    return SimpleDateFormat("d MMM", Locale("ru")).format(Date(timestamp)).lowercase()
}
