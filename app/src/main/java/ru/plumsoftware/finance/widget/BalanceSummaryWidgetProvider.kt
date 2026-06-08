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

class BalanceSummaryWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        appWidgetIds.forEach { appWidgetId ->
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: android.os.Bundle,
    ) {
        updateWidget(context, appWidgetManager, appWidgetId)
    }

    companion object {
        fun updateAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, BalanceSummaryWidgetProvider::class.java)
            val ids = manager.getAppWidgetIds(componentName)
            ids.forEach { id -> updateWidget(context, manager, id) }
        }

        private fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
        ) {
            val widgetData = loadWidgetData(context)
            val views = RemoteViews(context.packageName, R.layout.widget_balance_summary)
            views.setTextViewText(
                R.id.widget_balance_title,
                context.getString(R.string.widget_balance_title_with_month, widgetData.monthLabel),
            )
            views.setTextViewText(R.id.widget_balance_amount, widgetData.totalBalanceFormatted)
            views.setTextViewText(R.id.widget_income_value, widgetData.monthIncomeFormatted)
            views.setTextViewText(R.id.widget_expense_value, widgetData.monthExpenseFormatted)
            val rootIntent = buildHomeIntent(context)
            views.setOnClickPendingIntent(R.id.widget_balance_root, rootIntent)
            views.setOnClickPendingIntent(R.id.widget_income_card, rootIntent)
            views.setOnClickPendingIntent(R.id.widget_expense_card, rootIntent)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun loadWidgetData(context: Context): BalanceWidgetData = runBlocking(Dispatchers.IO) {
            val appContext = context.applicationContext
            val database = FinanceDatabase.create(appContext)
            val transactions = database.transactionDao().getAllSync()
            val currencyCode = SettingsDataStore(appContext).settings.first().defaultCurrencyCode
            val totalBalanceMinor = transactions.sumOf { tx ->
                when (tx.type) {
                    TransactionType.INCOME -> tx.amountMinor
                    TransactionType.EXPENSE -> -tx.amountMinor
                    TransactionType.SAVINGS -> 0L
                }
            }
            val monthRange = currentMonthRange()
            val monthTransactions = transactions.filter { tx ->
                tx.dateMillis in monthRange.first until monthRange.second
            }
            val monthIncomeMinor = monthTransactions
                .filter { it.type == TransactionType.INCOME }
                .sumOf { it.amountMinor }
            val monthExpenseMinor = monthTransactions
                .filter { it.type == TransactionType.EXPENSE }
                .sumOf { it.amountMinor }
            BalanceWidgetData(
                monthLabel = SimpleDateFormat("LLLL", Locale("ru"))
                    .format(Date())
                    .replaceFirstChar { it.uppercase(Locale("ru")) },
                totalBalanceFormatted = formatAmount(
                    context = context,
                    amountMinor = totalBalanceMinor,
                    currencyCode = currencyCode,
                    signed = false,
                    fractionDigits = 2,
                ),
                monthIncomeFormatted = formatAmount(
                    context = context,
                    amountMinor = monthIncomeMinor,
                    currencyCode = currencyCode,
                    signed = true,
                    fractionDigits = 0,
                ),
                monthExpenseFormatted = formatAmount(
                    context = context,
                    amountMinor = -monthExpenseMinor,
                    currencyCode = currencyCode,
                    signed = true,
                    fractionDigits = 0,
                ),
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
            val end = (start.clone() as Calendar).apply {
                add(Calendar.MONTH, 1)
            }
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

        private fun buildHomeIntent(context: Context): PendingIntent {
            val intent = Intent(
                Intent.ACTION_VIEW,
                AppDeepLinks.home(),
                context,
                MainActivity::class.java,
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            return PendingIntent.getActivity(
                context,
                1001,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        }
    }
}

private data class BalanceWidgetData(
    val monthLabel: String,
    val totalBalanceFormatted: String,
    val monthIncomeFormatted: String,
    val monthExpenseFormatted: String,
)
