package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.os.Bundle
import android.util.TypedValue
import android.widget.RemoteViews
import androidx.compose.ui.graphics.toArgb
import com.example.MainActivity
import com.example.R
import com.example.data.SettingsRepository
import com.example.model.ClockSettings
import com.example.ui.clock.ColorEngine
import kotlinx.coroutines.runBlocking

class ChromaClockWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val settings = runBlocking { SettingsRepository(context).getCurrentSettings() }
        appWidgetIds.forEach { widgetId ->
            val options = appWidgetManager.getAppWidgetOptions(widgetId)
            updateWidget(context, appWidgetManager, widgetId, options, settings)
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
            val component = ComponentName(context, ChromaClockWidgetProvider::class.java)
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
            val views = RemoteViews(context.packageName, R.layout.widget_chroma_digital)
            val minWidthDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 180)
            val minHeightDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 90)

            val palette = ColorEngine.computePalette(System.currentTimeMillis(), settings)
            val bgBitmap = createWidgetBackdropBitmap(
                widthPx = (minWidthDp * 2).coerceIn(220, 900),
                heightPx = (minHeightDp * 2).coerceIn(140, 700),
                primary = palette.primary.toArgb(),
                secondary = palette.secondary.toArgb(),
                tint = palette.subtleBgTint.toArgb()
            )
            views.setImageViewBitmap(R.id.widget_bg, bgBitmap)
            views.setTextColor(R.id.widget_clock, palette.primary.toArgb())
            views.setTextColor(R.id.widget_date, palette.secondary.toArgb())

            // Auto-fit text size to widget container bounds
            val clockTextSp = (minOf(minWidthDp * 0.24f, minHeightDp * 0.45f) * settings.clockScale)
                .coerceIn(24f, 76f)
            val dateTextSp = (clockTextSp * 0.28f).coerceIn(11f, 18f)
            views.setTextViewTextSize(R.id.widget_clock, TypedValue.COMPLEX_UNIT_SP, clockTextSp)
            views.setTextViewTextSize(R.id.widget_date, TypedValue.COMPLEX_UNIT_SP, dateTextSp)

            if (settings.showSeconds) {
                views.setCharSequence(R.id.widget_clock, "setFormat12Hour", "h:mm:ss")
                views.setCharSequence(R.id.widget_clock, "setFormat24Hour", "HH:mm:ss")
            } else {
                views.setCharSequence(R.id.widget_clock, "setFormat12Hour", "h:mm")
                views.setCharSequence(R.id.widget_clock, "setFormat24Hour", "HH:mm")
            }

            val launchIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                widgetId,
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)
            manager.updateAppWidget(widgetId, views)
        }

        fun createWidgetBackdropBitmap(
            widthPx: Int,
            heightPx: Int,
            primary: Int,
            secondary: Int,
            tint: Int
        ): Bitmap {
            val bmp = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)
            val rect = RectF(0f, 0f, widthPx.toFloat(), heightPx.toFloat())
            val radius = minOf(widthPx, heightPx) * 0.16f

            val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = LinearGradient(
                    0f, 0f, widthPx.toFloat(), heightPx.toFloat(),
                    intArrayOf(0xE80A0D16.toInt(), tint, 0xE8101626.toInt()),
                    null,
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawRoundRect(rect, radius, radius, fillPaint)

            val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 3f
                shader = LinearGradient(
                    0f, 0f, widthPx.toFloat(), heightPx.toFloat(),
                    intArrayOf(primary, secondary),
                    null,
                    Shader.TileMode.CLAMP
                )
            }
            val inset = RectF(2f, 2f, widthPx - 2f, heightPx - 2f)
            canvas.drawRoundRect(inset, radius, radius, strokePaint)
            return bmp
        }
    }
}
