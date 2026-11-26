package com.example.service

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.res.ResourcesCompat
import com.example.R
import com.example.data.SettingsRepository
import com.example.model.ClockFontStyle
import com.example.model.ClockSettings
import com.example.ui.clock.ColorEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

class ChromaLiveWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine = ChromaEngine()

    inner class ChromaEngine : Engine() {
        private val handler = Handler(Looper.getMainLooper())
        private var visible = false
        private var settings = ClockSettings()
        private var scope: CoroutineScope? = null
        private var customTypeface: Typeface? = null

        private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val orbPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val clockPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = Paint.Align.CENTER
        }
        private val datePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = Paint.Align.CENTER
            color = Color.WHITE
            alpha = 185
        }
        private val format24 = SimpleDateFormat("HH:mm", Locale.getDefault())
        private val format12 = SimpleDateFormat("h:mm", Locale.getDefault())
        private val dateFormat = SimpleDateFormat("EEE, MMM d", Locale.getDefault())

        private val drawRunnable = object : Runnable {
            override fun run() {
                drawFrame()
                if (visible) {
                    handler.postDelayed(this, 33L) // ~30 fps smooth low-power drift
                }
            }
        }

        override fun onCreate(surfaceHolder: SurfaceHolder?) {
            super.onCreate(surfaceHolder)
            val s = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
            scope = s
            val repo = SettingsRepository(applicationContext)
            s.launch {
                repo.settingsFlow.collectLatest { updated ->
                    settings = updated
                    val fontRes = when (updated.fontStyle) {
                        ClockFontStyle.SPACE_GROTESK -> R.font.space_grotesk
                        ClockFontStyle.JETBRAINS_MONO -> R.font.jetbrains_mono
                        ClockFontStyle.OUTFIT -> R.font.outfit
                        ClockFontStyle.SYNE -> R.font.syne
                        ClockFontStyle.PLUS_JAKARTA -> R.font.plus_jakarta_sans
                    }
                    customTypeface = runCatching {
                        ResourcesCompat.getFont(applicationContext, fontRes)
                    }.getOrNull()
                    clockPaint.typeface = customTypeface ?: Typeface.DEFAULT_BOLD
                    datePaint.typeface = customTypeface ?: Typeface.DEFAULT
                }
            }
        }

        override fun onVisibilityChanged(visible: Boolean) {
            this.visible = visible
            handler.removeCallbacks(drawRunnable)
            if (visible) {
                handler.post(drawRunnable)
            }
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder?) {
            super.onSurfaceDestroyed(holder)
            visible = false
            handler.removeCallbacks(drawRunnable)
        }

        override fun onDestroy() {
            super.onDestroy()
            visible = false
            handler.removeCallbacks(drawRunnable)
            scope?.cancel()
            scope = null
        }

        private fun drawFrame() {
            val holder = surfaceHolder
            var canvas: Canvas? = null
            try {
                canvas = holder.lockCanvas() ?: return
                val w = canvas.width.toFloat()
                val h = canvas.height.toFloat()
                if (w <= 0f || h <= 0f) return

                val now = System.currentTimeMillis()
                val palette = ColorEngine.computePalette(now, settings)
                val primaryArgb = palette.primary.toArgb()
                val secondaryArgb = palette.secondary.toArgb()
                val tintArgb = palette.subtleBgTint.toArgb()

                bgPaint.shader = LinearGradient(
                    0f, 0f, w, h,
                    intArrayOf(0xFF06080F.toInt(), tintArgb, 0xFF090D18.toInt()),
                    null, Shader.TileMode.CLAMP
                )
                canvas.drawRect(0f, 0f, w, h, bgPaint)

                val drift1X = w * (0.3f + 0.18f * sin(now / 7000.0).toFloat())
                val drift1Y = h * (0.28f + 0.12f * cos(now / 9000.0).toFloat())
                orbPaint.shader = RadialGradient(
                    drift1X, drift1Y, w * 0.7f,
                    intArrayOf(palette.primary.copy(alpha = 0.34f).toArgb(), Color.TRANSPARENT),
                    null, Shader.TileMode.CLAMP
                )
                canvas.drawCircle(drift1X, drift1Y, w * 0.7f, orbPaint)

                val drift2X = w * (0.7f + 0.16f * cos(now / 8000.0).toFloat())
                val drift2Y = h * (0.72f + 0.14f * sin(now / 11000.0).toFloat())
                orbPaint.shader = RadialGradient(
                    drift2X, drift2Y, w * 0.68f,
                    intArrayOf(palette.secondary.copy(alpha = 0.30f).toArgb(), Color.TRANSPARENT),
                    null, Shader.TileMode.CLAMP
                )
                canvas.drawCircle(drift2X, drift2Y, w * 0.68f, orbPaint)

                if (settings.showClockOnLiveWallpaper) {
                    val cal = Calendar.getInstance().apply { timeInMillis = now }
                    val timeText = if (settings.use24Hour) {
                        format24.format(cal.time)
                    } else {
                        format12.format(cal.time)
                    }
                    val size = (minOf(w, h) * 0.22f * settings.clockScale).coerceIn(56f, 220f)
                    clockPaint.textSize = size
                    clockPaint.shader = LinearGradient(
                        w * 0.25f, h * 0.42f, w * 0.75f, h * 0.56f,
                        primaryArgb, secondaryArgb, Shader.TileMode.CLAMP
                    )
                    canvas.drawText(timeText, w / 2f, h * 0.46f, clockPaint)

                    if (settings.showDate) {
                        datePaint.textSize = (size * 0.19f).coerceAtLeast(24f)
                        canvas.drawText(dateFormat.format(cal.time), w / 2f, h * 0.46f + size * 0.28f, datePaint)
                    }
                }
            } finally {
                if (canvas != null) {
                    runCatching { holder.unlockCanvasAndPost(canvas) }
                }
            }
        }
    }
}
