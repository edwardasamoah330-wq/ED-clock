package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.RemoteViews
import androidx.compose.ui.graphics.toArgb
import com.example.MainActivity
import com.example.R
import com.example.data.SettingsRepository
import com.example.model.ClockSettings
import com.example.ui.clock.ColorEngine
import kotlinx.coroutines.runBlocking

class ChromaAnalogWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val settings = runBlocking { SettingsRepository(context).getCurrentSettings() }
        appWidgetIds.forEach { id ->
            val options = appWidgetManager.getAppWidgetOptions(id)
            updateWidget(context, appWidgetManager, id, options, settings)
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        val settings = runBlocking { SettingsRepository(context).getCurrentSettings() }
        updateWidget(context, appWidgetManager, appWidgetId, newOptions, settings)
    }

    companion object {
        fun updateAllWidgets(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val component = ComponentName(context, ChromaAnalogWidgetProvider::class.java)
            val ids = manager.getAppWidgetIds(component)
            if (ids.isEmpty()) return
            val settings = runBlocking { SettingsRepository(context).getCurrentSettings() }
            ids.forEach { id ->
                updateWidget(context, manager, id, manager.getAppWidgetOptions(id), settings)
            }
        }

        private fun updateWidget(
            context: Context,
            manager: AppWidgetManager,
            widgetId: Int,
            options: Bundle,
            settings: ClockSettings
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_chroma_analog)
            val minWidthDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 140)
            val minHeightDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 140)

            val palette = ColorEngine.computePalette(System.currentTimeMillis(), settings)
            val bgBitmap = ChromaClockWidgetProvider.createWidgetBackdropBitmap(
                widthPx = (minWidthDp * 2).coerceIn(200, 700),
                heightPx = (minHeightDp * 2).coerceIn(200, 700),
                primary = palette.primary.toArgb(),
                secondary = palette.secondary.toArgb(),
                tint = palette.subtleBgTint.toArgb()
            )
            views.setImageViewBitmap(R.id.widget_analog_bg, bgBitmap)

            val launchIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                widgetId + 1000,
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_analog_root, pendingIntent)
            manager.updateAppWidget(widgetId, views)
        }
    }
}
