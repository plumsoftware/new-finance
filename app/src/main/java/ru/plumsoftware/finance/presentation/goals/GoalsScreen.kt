package ru.plumsoftware.finance.presentation.goals

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.domain.model.Goal
import ru.plumsoftware.finance.domain.model.daysLeft
import ru.plumsoftware.finance.domain.model.isOverdue
import ru.plumsoftware.finance.domain.model.progress
import ru.plumsoftware.finance.presentation.common.MoneyFormat
import ru.plumsoftware.finance.ui.components.IosCard
import ru.plumsoftware.finance.ui.components.IosPrimaryButton
import ru.plumsoftware.finance.ui.components.MascotEmptyState
import ru.plumsoftware.finance.ui.components.ios.IosTextButton
import ru.plumsoftware.finance.ui.theme.Dimens
import ru.plumsoftware.finance.ui.theme.IosBlue
import ru.plumsoftware.finance.ui.theme.IosGreen
import ru.plumsoftware.finance.ui.theme.MascotAssets

@Composable
fun GoalsScreen(
    onCreateClick: () -> Unit,
    onGoalClick: (Long) -> Unit,
    viewModel: GoalsViewModel = koinViewModel(),
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    Scaffold(
        containerColor = colors.background,
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(
                start = Dimens.paddingMedium,
                end = Dimens.paddingMedium,
                top = Dimens.statusBarInset,
                bottom = Dimens.spacingRow,
            ),
            verticalArrangement = Arrangement.spacedBy(Dimens.spacingList),
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = androidx.compose.ui.res.stringResource(R.string.goals_title),
                        style = typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface,
                    )
                    IosTextButton(
                        text = androidx.compose.ui.res.stringResource(R.string.goal_add_action),
                        onClick = onCreateClick,
                        color = IosBlue,
                        style = typography.bodyLarge,
                        textAlign = TextAlign.End,
                    )
                }
            }
            item {
                IosCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(
                                text = androidx.compose.ui.res.stringResource(R.string.goals_total_saved),
                                style = typography.bodySmall,
                                color = colors.onSurfaceVariant,
                            )
                            Text(
                                text = MoneyFormat.format(state.totalSavedMinor, state.currencyCode),
                                style = typography.headlineMedium,
                                color = IosGreen,
                            )
                        }
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                progress = { state.overallProgress },
                                modifier = Modifier.size(Dimens.iconPickerCellSize),
                                color = IosBlue,
                                trackColor = colors.outline,
                                strokeWidth = Dimens.progressHeightThin,
                            )
                            Text(
                                text = androidx.compose.ui.res.stringResource(
                                    R.string.goal_progress_percent,
                                    (state.overallProgress * 100).toInt(),
                                ),
                                style = typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            }
            if (state.goals.isEmpty()) {
                item {
                    EmptyGoalsState(onCreateClick = onCreateClick)
                }
            } else {
                if (state.activeGoals.isNotEmpty()) {
                    item {
                        Text(
                            text = androidx.compose.ui.res.stringResource(R.string.goals_active_section),
                            style = typography.labelSmall,
                            color = colors.onSurfaceVariant,
                        )
                    }
                    items(state.activeGoals, key = { it.id }) { goal ->
                        GoalCard(
                            goal = goal,
                            currencyCode = state.currencyCode,
                            onClick = { onGoalClick(goal.id) },
                        )
                    }
                }
                if (state.completedGoals.isNotEmpty()) {
                    item {
                        Text(
                            text = androidx.compose.ui.res.stringResource(R.string.goals_completed_section),
                            style = typography.labelSmall,
                            color = colors.onSurfaceVariant,
                            modifier = Modifier.padding(top = Dimens.spacingList),
                        )
                    }
                    items(state.completedGoals, key = { it.id }) { goal ->
                        GoalCard(
                            goal = goal,
                            currencyCode = state.currencyCode,
                            onClick = { onGoalClick(goal.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GoalCard(
    goal: Goal,
    currencyCode: String,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val goalColor = colorFromHexOrDefault(goal.colorHex, colors.primary)
    IosCard(
        modifier = Modifier
            .fillMaxWidth(),
        onClick = onClick,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(Dimens.avatarSize + Dimens.paddingSmall)
                    .background(goalColor.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(goal.emoji, style = typography.headlineMedium)
            }

            Spacer(Modifier.width(Dimens.spacingList))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimens.paddingSmall),
                ) {
                    Text(
                        text = goal.name,
                        style = typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    if (goal.isCompleted) {
                        Box(
                            modifier = Modifier
                                .background(IosGreen.copy(alpha = 0.12f), RoundedCornerShape(Dimens.paddingSmall))
                                .padding(horizontal = Dimens.paddingSmall - Dimens.paddingMicro, vertical = Dimens.paddingMicro / 2),
                        ) {
                            Text(
                                text = androidx.compose.ui.res.stringResource(R.string.goal_completed_badge),
                                style = typography.labelSmall,
                                color = IosGreen,
                            )
                        }
                    }
                }

                Spacer(Modifier.height(Dimens.paddingSmall - Dimens.paddingMicro / 2))

                LinearProgressIndicator(
                    progress = { goal.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimens.progressHeightThin),
                    color = goalColor,
                    trackColor = colors.outline,
                )

                Spacer(Modifier.height(Dimens.paddingMicro + Dimens.paddingMicro / 4))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = androidx.compose.ui.res.stringResource(
                            R.string.goal_saved_of,
                            MoneyFormat.format(goal.savedAmountMinor, currencyCode),
                            MoneyFormat.format(goal.targetAmountMinor, currencyCode),
                        ),
                        style = typography.labelSmall,
                        color = colors.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    val left = goal.daysLeft
                    Text(
                        text = when {
                            left == null -> androidx.compose.ui.res.stringResource(R.string.goal_no_deadline)
                            goal.isOverdue -> androidx.compose.ui.res.stringResource(R.string.goal_days_overdue, kotlin.math.abs(left))
                            else -> androidx.compose.ui.res.stringResource(R.string.goal_days_left, left)
                        },
                        style = typography.labelSmall,
                        color = goalDeadlineColor(goal.isOverdue, left, colors.onSurfaceVariant),
                        modifier = Modifier.padding(start = Dimens.paddingSmall),
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Spacer(Modifier.width(Dimens.paddingSmall))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = colors.outlineVariant,
                modifier = Modifier.size(Dimens.dragIconSize),
            )
        }
    }
}

@Composable
private fun EmptyGoalsState(onCreateClick: () -> Unit) {
    MascotEmptyState(
        mascotRes = MascotAssets.happy,
        title = androidx.compose.ui.res.stringResource(R.string.empty_goals_title),
        subtitle = androidx.compose.ui.res.stringResource(R.string.empty_goals_subtitle),
        mascotPhrase = androidx.compose.ui.res.stringResource(R.string.mascot_phrase_no_goals),
        action = {
            IosPrimaryButton(
                text = androidx.compose.ui.res.stringResource(R.string.goals_add_button),
                onClick = onCreateClick,
            )
        },
    )
}
