package ru.plumsoftware.finance.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import ru.plumsoftware.finance.MainActivity
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.navigation.AppDeepLinks

class QuickAddWidgetProvider : AppWidgetProvider() {
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
            val componentName = ComponentName(context, QuickAddWidgetProvider::class.java)
            val ids = manager.getAppWidgetIds(componentName)
            ids.forEach { id -> updateWidget(context, manager, id) }
        }

        private fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_quick_add)
            val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
            val minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 110)
            val minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 110)

            val showSecondary = minWidth >= 140
            val showTransport = minWidth >= 170 && minHeight >= 110
            views.setViewVisibility(
                R.id.widget_chip_cafe,
                if (showSecondary) View.VISIBLE else View.GONE,
            )
            views.setViewVisibility(
                R.id.widget_chip_transport,
                if (showTransport) View.VISIBLE else View.GONE,
            )

            views.setOnClickPendingIntent(
                R.id.widget_root,
                buildAddIntent(context, null),
            )
            views.setOnClickPendingIntent(
                R.id.widget_add_button,
                buildAddIntent(context, null),
            )
            views.setOnClickPendingIntent(
                R.id.widget_chip_products,
                buildAddIntent(context, context.getString(R.string.widget_category_products)),
            )
            views.setOnClickPendingIntent(
                R.id.widget_chip_cafe,
                buildAddIntent(context, context.getString(R.string.widget_category_cafe)),
            )
            views.setOnClickPendingIntent(
                R.id.widget_chip_transport,
                buildAddIntent(context, context.getString(R.string.widget_category_transport)),
            )
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun buildAddIntent(context: Context, quickCategory: String?): PendingIntent {
            val intent = Intent(
                Intent.ACTION_VIEW,
                AppDeepLinks.addTransaction(quickCategory),
                context,
                MainActivity::class.java,
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            val requestCode = quickCategory?.hashCode() ?: 0
            return PendingIntent.getActivity(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        }
    }
}
