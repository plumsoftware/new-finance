package ru.plumsoftware.finance.presentation.goals

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.domain.model.progress
import ru.plumsoftware.finance.domain.model.remainingMinor
import ru.plumsoftware.finance.presentation.common.DateFmt
import ru.plumsoftware.finance.presentation.common.Money
import ru.plumsoftware.finance.ui.ds.ButtonTonal
import ru.plumsoftware.finance.ui.ds.FCard
import ru.plumsoftware.finance.ui.ds.FSwitch
import ru.plumsoftware.finance.ui.ds.InkCard
import ru.plumsoftware.finance.ui.ds.Kopi
import ru.plumsoftware.finance.ui.ds.LocalMascotSnackbar
import ru.plumsoftware.finance.ui.ds.ProgressRing
import ru.plumsoftware.finance.ui.ds.SubScreenAppBar
import ru.plumsoftware.finance.ui.ds.TextAction
import ru.plumsoftware.finance.ui.ds.VSpace
import ru.plumsoftware.finance.ui.theme.FinanceTheme
import ru.plumsoftware.finance.ui.theme.FinanceType
import java.time.YearMonth
import kotlin.math.roundToLong

@Composable
fun GoalDetailScreen(
    goalId: Long,
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
    onDeleted: () -> Unit,
    viewModel: GoalDetailViewModel = koinViewModel { parametersOf(goalId) },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val celebrate by viewModel.celebrate.collectAsStateWithLifecycle()
    val c = FinanceTheme.colors
    val snackbar = LocalMascotSnackbar.current
    var confirmDelete by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(state.deleted) { if (state.deleted) onDeleted() }
    val doneText = stringResource(R.string.goal_reached_toast)
    LaunchedEffect(celebrate) {
        if (celebrate) {
            snackbar.show(doneText, Kopi.TROPHY)
            viewModel.consumeCelebration()
        }
    }

    val goal = state.goal
    Column(Modifier.fillMaxSize().background(c.bg)) {
        SubScreenAppBar(
            title = goal?.name.orEmpty(),
            onBack = onBack,
            actions = { if (goal != null) TextAction(stringResource(R.string.goal_edit), { onEdit(goal.id) }) },
        )
        if (goal == null) return@Column
        val cur = state.currencyCode
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // 2. Кольцо и суммы. «Осталось» = цель − накоплено (§2 п.1).
            FCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    ProgressRing(goal.progress, size = 136.dp, stroke = 11.dp) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(goal.emoji, fontSize = 34.sp)
                            Text("${(goal.progress * 100).toInt()}%", style = FinanceType.titleLarge.copy(fontWeight = FontWeight.ExtraBold), color = c.primary)
                        }
                    }
                    VSpace(12.dp)
                    Text(Money.formatRounded(goal.savedAmountMinor, cur), style = FinanceType.headline.copy(fontSize = 30.sp), color = c.textPrimary)
                    Text(
                        stringResource(
                            R.string.goal_of_left,
                            Money.formatRounded(goal.targetAmountMinor, cur),
                            Money.formatRounded(goal.remainingMinor, cur),
                        ),
                        style = FinanceType.bodySmall,
                        color = c.textSecondary,
                    )
                }
            }

            // 3. Темп.
            val plan = state.plan
            if (plan != null && goal.remainingMinor > 0) {
                InkCard(radius = 24.dp) {
                    Text(stringResource(R.string.goal_if_save_monthly), style = FinanceType.bodySmall, color = c.onInkSecondary)
                    Text(Money.formatRounded(state.paceMinor, cur), style = FinanceType.headline, color = Color.White)
                    Slider(
                        value = state.paceMinor.toFloat(),
                        onValueChange = { v ->
                            val step = GoalPlanner.PACE_STEP
                            viewModel.setPace((((v - GoalPlanner.PACE_MIN) / step).roundToLong() * step + GoalPlanner.PACE_MIN).coerceIn(GoalPlanner.PACE_MIN, GoalPlanner.PACE_MAX))
                        },
                        valueRange = GoalPlanner.PACE_MIN.toFloat()..GoalPlanner.PACE_MAX.toFloat(),
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = c.onInkBar,
                            inactiveTrackColor = Color.White.copy(alpha = 0.14f),
                        ),
                    )
                    plan.reachMonth?.let {
                        Text(
                            stringResource(R.string.goal_reach_on, DateFmt.monthYear(it.atDay(1))),
                            style = FinanceType.title,
                            color = Color.White,
                        )
                    }
                    plan.slackMonths?.let { slack ->
                        Text(
                            if (slack >= 0) stringResource(R.string.goal_on_time, pluralStringResource(R.plurals.pl_months_short, slack, slack))
                            else stringResource(R.string.goal_late, pluralStringResource(R.plurals.pl_months_short, -slack, -slack)),
                            style = FinanceType.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = if (slack >= 0) c.onInkSuccess else c.onInkWarning,
                        )
                    }
                    if (plan.deadlineMonth != null && plan.neededPerMonthMinor != null) {
                        VSpace(6.dp)
                        Text(
                            stringResource(
                                R.string.goal_needed_by,
                                monthGenitiveYear(plan.deadlineMonth),
                                Money.formatRounded(plan.neededPerMonthMinor, cur),
                            ),
                            style = FinanceType.caption,
                            color = c.onInkSecondary,
                        )
                    }
                }

                // 4. Быстрые пополнения.
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(500_000L, 1_000_000L, 5_000_000L).forEach { amount ->
                        ButtonTonal(
                            "+${Money.number(amount, cur)}",
                            onClick = { viewModel.quickDeposit(amount) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            // 5. Показывать на главной.
            FCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.goal_show_on_home), style = FinanceType.body, color = c.textPrimary, modifier = Modifier.weight(1f))
                    FSwitch(goal.showOnHome, viewModel::setShowOnHome)
                }
            }

            // 6. Удалить.
            TextAction(
                stringResource(R.string.goal_delete),
                { confirmDelete = true },
                color = c.dangerText,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
            VSpace(16.dp)
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            containerColor = c.surface,
            title = { Text(stringResource(R.string.goal_delete_confirm_title), style = FinanceType.titleLarge, color = c.textPrimary) },
            text = { Text(stringResource(R.string.goal_delete_confirm_text), style = FinanceType.bodySmall, color = c.textSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    viewModel.delete()
                }) { Text(stringResource(R.string.delete), color = c.dangerText) }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text(stringResource(R.string.cancel), color = c.textSecondary) } },
        )
    }
}

/** «сентября 2030» */
private fun monthGenitiveYear(ym: YearMonth): String = "${DateFmt.dayMonth(ym.atDay(1)).substringAfter(' ')} ${ym.year}"
