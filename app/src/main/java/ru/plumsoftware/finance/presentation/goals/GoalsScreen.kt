package ru.plumsoftware.finance.presentation.goals

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import org.koin.androidx.compose.koinViewModel
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.domain.model.Goal
import ru.plumsoftware.finance.domain.model.daysLeft
import ru.plumsoftware.finance.domain.model.isOverdue
import ru.plumsoftware.finance.domain.model.progress
import ru.plumsoftware.finance.domain.model.remainingMinor
import ru.plumsoftware.finance.navigation.popBackStackOrHome
import ru.plumsoftware.finance.presentation.common.Money
import ru.plumsoftware.finance.ui.ds.EmptyState
import ru.plumsoftware.finance.ui.ds.FCard
import ru.plumsoftware.finance.ui.ds.FProgressBar
import ru.plumsoftware.finance.ui.ds.HSpace
import ru.plumsoftware.finance.ui.ds.IconButton44
import ru.plumsoftware.finance.ui.ds.Kopi
import ru.plumsoftware.finance.ui.ds.ProgressRing
import ru.plumsoftware.finance.ui.ds.SectionHeader
import ru.plumsoftware.finance.ui.ds.SubScreenAppBar
import ru.plumsoftware.finance.ui.ds.VSpace
import ru.plumsoftware.finance.ui.theme.FinanceTheme
import ru.plumsoftware.finance.ui.theme.FinanceType

/** Список целей. Заголовок — «Цели» (§2 п.5). */
@Composable
fun GoalsScreen(
    onCreateClick: () -> Unit,
    onGoalClick: (Long) -> Unit,
    navController: NavController,
    viewModel: GoalsViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val c = FinanceTheme.colors
    Column(Modifier.fillMaxSize().background(c.bg)) {
        SubScreenAppBar(
            title = stringResource(R.string.goals_title),
            onBack = { navController.popBackStackOrHome() },
            actions = { IconButton44(R.drawable.ic_add, stringResource(R.string.goal_add_action), onCreateClick) },
        )
        if (state.goals.isEmpty()) {
            EmptyState(
                title = stringResource(R.string.empty_goals_title),
                text = stringResource(R.string.empty_goals_subtitle),
                pose = Kopi.THINKING,
                action = stringResource(R.string.goals_add_button),
                onAction = onCreateClick,
                modifier = Modifier.padding(top = 48.dp),
            )
            return@Column
        }
        LazyColumn(
            Modifier.fillMaxSize().navigationBarsPadding(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                FCard {
                    Text(stringResource(R.string.goals_total_saved), style = FinanceType.bodySmall, color = c.textSecondary)
                    Text(Money.formatRounded(state.totalSavedMinor, state.currencyCode), style = FinanceType.headlineLarge, color = c.textPrimary)
                    Text(
                        pluralStringResource(R.plurals.pl_active_goals, state.activeGoals.size, state.activeGoals.size),
                        style = FinanceType.caption,
                        color = c.textSecondary,
                    )
                    VSpace(10.dp)
                    FProgressBar(state.overallProgress, c.primary, height = 6.dp)
                }
            }
            if (state.activeGoals.isNotEmpty()) {
                item { SectionHeader(stringResource(R.string.goals_active_section)) }
                items(state.activeGoals, key = { it.id }) { GoalRowCard(it) { onGoalClick(it.id) } }
            }
            if (state.completedGoals.isNotEmpty()) {
                item { SectionHeader(stringResource(R.string.goals_completed_section)) }
                items(state.completedGoals, key = { it.id }) { GoalRowCard(it) { onGoalClick(it.id) } }
            }
        }
    }
}

@Composable
private fun GoalRowCard(goal: Goal, onClick: () -> Unit) {
    val c = FinanceTheme.colors
    FCard(onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ProgressRing(goal.progress, size = 52.dp, stroke = 5.dp, color = if (goal.isCompleted) c.success else c.primary) {
                Text(goal.emoji, fontSize = 20.sp)
            }
            HSpace(12.dp)
            Column(Modifier.weight(1f)) {
                Text(goal.name, style = FinanceType.title, color = c.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    stringResource(
                        R.string.goal_of_left,
                        Money.formatRounded(goal.targetAmountMinor, goal.currencyCode),
                        Money.formatRounded(goal.remainingMinor, goal.currencyCode),
                    ),
                    style = FinanceType.caption,
                    color = c.textSecondary,
                )
                val left = goal.daysLeft
                Text(
                    when {
                        goal.isCompleted -> stringResource(R.string.goal_completed_badge)
                        left == null -> stringResource(R.string.goal_no_deadline)
                        goal.isOverdue -> stringResource(R.string.goal_days_overdue, kotlin.math.abs(left))
                        else -> stringResource(R.string.goal_days_left, left)
                    },
                    style = FinanceType.caption,
                    color = if (goal.isOverdue) c.dangerText else c.textSecondary,
                )
            }
            Text(
                "${(goal.progress * 100).toInt()}%",
                style = FinanceType.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = if (goal.isCompleted) c.successText else c.primary,
            )
        }
    }
}
