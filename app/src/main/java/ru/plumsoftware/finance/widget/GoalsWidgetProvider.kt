package ru.plumsoftware.finance.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import ru.plumsoftware.finance.MainActivity
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.data.local.database.FinanceDatabase
import ru.plumsoftware.finance.data.local.datastore.SettingsDataStore
import ru.plumsoftware.finance.navigation.AppDeepLinks
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale
import kotlin.math.roundToInt

class GoalsWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        appWidgetIds.forEach { appWidgetId ->
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, GoalsWidgetProvider::class.java)
            val ids = manager.getAppWidgetIds(componentName)
            ids.forEach { id -> updateWidget(context, manager, id) }
        }

        private fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
        ) {
            val data = loadWidgetData(context)
            val views = RemoteViews(context.packageName, R.layout.widget_goals)
            views.setTextViewText(
                R.id.widget_goals_total,
                context.getString(R.string.widget_goals_saved_template, data.totalSavedFormatted),
            )
            bindGoalRow(
                views = views,
                rowIndex = 0,
                row = data.rows.getOrNull(0),
            )
            bindGoalRow(
                views = views,
                rowIndex = 1,
                row = data.rows.getOrNull(1),
            )
            bindGoalRow(
                views = views,
                rowIndex = 2,
                row = data.rows.getOrNull(2),
            )
            views.setViewVisibility(
                R.id.widget_goal_divider_1,
                if (data.rows.getOrNull(1) != null) View.VISIBLE else View.GONE,
            )
            views.setViewVisibility(
                R.id.widget_goal_divider_2,
                if (data.rows.getOrNull(2) != null) View.VISIBLE else View.GONE,
            )
            val openGoalsIntent = buildGoalsIntent(context)
            views.setOnClickPendingIntent(R.id.widget_goals_root, openGoalsIntent)
            views.setOnClickPendingIntent(R.id.widget_goal_row_1, openGoalsIntent)
            views.setOnClickPendingIntent(R.id.widget_goal_row_2, openGoalsIntent)
            views.setOnClickPendingIntent(R.id.widget_goal_row_3, openGoalsIntent)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun bindGoalRow(
            views: RemoteViews,
            rowIndex: Int,
            row: GoalWidgetRow?,
        ) {
            val rowContainer = when (rowIndex) {
                0 -> R.id.widget_goal_row_1
                1 -> R.id.widget_goal_row_2
                else -> R.id.widget_goal_row_3
            }
            val iconView = when (rowIndex) {
                0 -> R.id.widget_goal_emoji_1
                1 -> R.id.widget_goal_emoji_2
                else -> R.id.widget_goal_emoji_3
            }
            val titleView = when (rowIndex) {
                0 -> R.id.widget_goal_title_1
                1 -> R.id.widget_goal_title_2
                else -> R.id.widget_goal_title_3
            }
            val percentView = when (rowIndex) {
                0 -> R.id.widget_goal_percent_1
                1 -> R.id.widget_goal_percent_2
                else -> R.id.widget_goal_percent_3
            }
            val progressView = when (rowIndex) {
                0 -> R.id.widget_goal_progress_1
                1 -> R.id.widget_goal_progress_2
                else -> R.id.widget_goal_progress_3
            }
            val dividerView = when (rowIndex) {
                0 -> R.id.widget_goal_divider_1
                1 -> R.id.widget_goal_divider_2
                else -> null
            }

            if (row == null) {
                views.setViewVisibility(rowContainer, View.GONE)
                dividerView?.let { views.setViewVisibility(it, View.GONE) }
                return
            }

            views.setViewVisibility(rowContainer, View.VISIBLE)
            dividerView?.let { views.setViewVisibility(it, View.VISIBLE) }
            views.setTextViewText(iconView, row.emoji)
            views.setTextViewText(titleView, row.name)
            views.setTextViewText(percentView, row.percentLabel)
            views.setProgressBar(progressView, 100, row.percent, false)
        }

        private fun loadWidgetData(context: Context): GoalsWidgetData = runBlocking(Dispatchers.IO) {
            val appContext = context.applicationContext
            val database = FinanceDatabase.create(appContext)
            val currencyCode = SettingsDataStore(appContext).settings.first().defaultCurrencyCode
            val goals = database.goalDao().getAllGoalsSync()
                .sortedWith(compareBy({ it.isCompleted }, { -it.createdAtMillis }))
            val activeGoals = goals.filter { !it.isCompleted }
            val shownGoals = (if (activeGoals.isNotEmpty()) activeGoals else goals).take(3)
            val rows = shownGoals.map { goal ->
                val percent = if (goal.targetAmountMinor <= 0L) {
                    0
                } else {
                    ((goal.savedAmountMinor.toDouble() / goal.targetAmountMinor.toDouble()) * 100.0)
                        .coerceIn(0.0, 100.0)
                        .roundToInt()
                }
                GoalWidgetRow(
                    emoji = goal.emoji,
                    name = goal.name,
                    percent = percent,
                    percentLabel = "$percent%",
                )
            }
            val totalSavedMinor = goals.sumOf { it.savedAmountMinor }
            GoalsWidgetData(
                totalSavedFormatted = formatMoney(totalSavedMinor, currencyCode),
                rows = rows,
            )
        }

        private fun formatMoney(amountMinor: Long, currencyCode: String): String {
            val locale = Locale("ru", "RU")
            val currency = Currency.getInstance(currencyCode)
            val formatter = NumberFormat.getNumberInstance(locale).apply {
                maximumFractionDigits = 0
                minimumFractionDigits = 0
            }
            return "${formatter.format(amountMinor / 100.0)} ${currency.getSymbol(locale)}"
        }

        private fun buildGoalsIntent(context: Context): PendingIntent {
            val intent = Intent(
                Intent.ACTION_VIEW,
                AppDeepLinks.goals(),
                context,
                MainActivity::class.java,
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            return PendingIntent.getActivity(
                context,
                1101,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        }
    }
}

private data class GoalsWidgetData(
    val totalSavedFormatted: String,
    val rows: List<GoalWidgetRow>,
)

private data class GoalWidgetRow(
    val emoji: String,
    val name: String,
    val percent: Int,
    val percentLabel: String,
)
