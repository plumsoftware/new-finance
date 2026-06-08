package ru.plumsoftware.finance.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import ru.plumsoftware.finance.MainActivity
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.data.local.database.FinanceDatabase
import ru.plumsoftware.finance.data.local.datastore.SettingsDataStore
import ru.plumsoftware.finance.domain.model.TransactionType
import ru.plumsoftware.finance.navigation.AppDeepLinks
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Currency
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToInt

class MonthlySummaryWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        appWidgetIds.forEach { id ->
            updateWidget(context, appWidgetManager, id)
        }
    }

    companion object {
        fun updateAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, MonthlySummaryWidgetProvider::class.java)
            manager.getAppWidgetIds(componentName).forEach { id ->
                updateWidget(context, manager, id)
            }
        }

        private fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
        ) {
            val data = loadWidgetData(context)
            val views = RemoteViews(context.packageName, R.layout.widget_monthly_summary)
            views.setTextViewText(R.id.widget_monthly_title, data.monthLabel)
            views.setTextViewText(
                R.id.widget_monthly_saved_badge,
                context.getString(R.string.widget_monthly_saved_badge, data.savedPercent),
            )
            views.setTextViewText(R.id.widget_monthly_balance, data.balanceFormatted)
            views.setTextViewText(R.id.widget_monthly_income_value, data.incomeFormatted)
            views.setTextViewText(R.id.widget_monthly_expense_value, data.expenseFormatted)
            views.setTextViewText(
                R.id.widget_monthly_ops_value,
                data.operationsCount.toString(),
            )
            val openIntent = buildAnalyticsIntent(context)
            views.setOnClickPendingIntent(R.id.widget_monthly_root, openIntent)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun loadWidgetData(context: Context): MonthlyWidgetData = runBlocking(Dispatchers.IO) {
            val appContext = context.applicationContext
            val db = FinanceDatabase.create(appContext)
            val currencyCode = SettingsDataStore(appContext).settings.first().defaultCurrencyCode
            val allTransactions = db.transactionDao().getAllSync()
            val monthRange = currentMonthRange()
            val monthTransactions = allTransactions.filter { tx ->
                tx.dateMillis in monthRange.first until monthRange.second
            }
            val monthIncomeMinor = monthTransactions
                .filter { it.type == TransactionType.INCOME }
                .sumOf { it.amountMinor }
            val monthExpenseMinor = monthTransactions
                .filter { it.type == TransactionType.EXPENSE }
                .sumOf { it.amountMinor }
            val monthBalanceMinor = monthIncomeMinor - monthExpenseMinor
            val savedPercent = if (monthIncomeMinor > 0) {
                (((monthIncomeMinor - monthExpenseMinor).toDouble() / monthIncomeMinor.toDouble()) * 100.0)
                    .coerceIn(-999.0, 999.0)
                    .roundToInt()
            } else {
                0
            }
            MonthlyWidgetData(
                monthLabel = SimpleDateFormat("LLLL yyyy", Locale("ru"))
                    .format(Date())
                    .replaceFirstChar { it.uppercase(Locale("ru")) },
                savedPercent = savedPercent,
                balanceFormatted = formatAmount(
                    context = context,
                    amountMinor = monthBalanceMinor,
                    currencyCode = currencyCode,
                    signed = false,
                    fractionDigits = 0,
                ),
                incomeFormatted = formatAmount(
                    context = context,
                    amountMinor = monthIncomeMinor,
                    currencyCode = currencyCode,
                    signed = true,
                    fractionDigits = 0,
                ),
                expenseFormatted = formatAmount(
                    context = context,
                    amountMinor = -monthExpenseMinor,
                    currencyCode = currencyCode,
                    signed = true,
                    fractionDigits = 0,
                ),
                operationsCount = monthTransactions.size,
            )
        }

        private fun currentMonthRange(): Pair<Long, Long> {
            val start = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val end = (start.clone() as Calendar).apply { add(Calendar.MONTH, 1) }
            return start.timeInMillis to end.timeInMillis
        }

        private fun formatAmount(
            context: Context,
            amountMinor: Long,
            currencyCode: String,
            signed: Boolean,
            fractionDigits: Int,
        ): String {
            val locale = Locale("ru", "RU")
            val currency = Currency.getInstance(currencyCode)
            val exp = currency.defaultFractionDigits.coerceAtLeast(0)
            val major = amountMinor / 10.0.pow(exp.toDouble())
            val formatter = NumberFormat.getNumberInstance(locale).apply {
                maximumFractionDigits = fractionDigits
                minimumFractionDigits = fractionDigits
            }
            val value = "${formatter.format(abs(major))} ${currency.getSymbol(locale)}"
            if (!signed || amountMinor == 0L) return value
            return context.getString(
                if (amountMinor > 0) R.string.amount_signed_positive else R.string.amount_signed_negative,
                value,
            )
        }

        private fun buildAnalyticsIntent(context: Context): PendingIntent {
            val intent = Intent(
                Intent.ACTION_VIEW,
                AppDeepLinks.analytics(),
                context,
                MainActivity::class.java,
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            return PendingIntent.getActivity(
                context,
                1201,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        }
    }
}

private data class MonthlyWidgetData(
    val monthLabel: String,
    val savedPercent: Int,
    val balanceFormatted: String,
    val incomeFormatted: String,
    val expenseFormatted: String,
    val operationsCount: Int,
)
