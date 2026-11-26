package com.example.service

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.graphics.Typeface
import android.os.BatteryManager
import android.service.dreams.DreamService
import android.view.Choreographer
import android.view.View
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.res.ResourcesCompat
import com.example.R
import com.example.data.SettingsRepository
import com.example.model.ClockFaceStyle
import com.example.model.ClockFontStyle
import com.example.model.ClockSettings
import com.example.ui.clock.ColorEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

class ChromaDreamService : DreamService() {

    private var serviceScope: CoroutineScope? = null
    private var dreamView: DreamClockView? = null

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isInteractive = false
        isFullscreen = true
        isScreenBright = false

        val view = DreamClockView(this)
        dreamView = view
        setContentView(view)

        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        serviceScope = scope
        val repo = SettingsRepository(applicationContext)
        scope.launch {
            repo.settingsFlow.collectLatest { settings ->
                view.updateSettings(settings)
            }
        }
    }

    override fun onDreamingStarted() {
        super.onDreamingStarted()
        dreamView?.startAnimating()
    }

    override fun onDreamingStopped() {
        super.onDreamingStopped()
        dreamView?.stopAnimating()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        dreamView?.stopAnimating()
        serviceScope?.cancel()
        serviceScope = null
        dreamView = null
    }
}

private class DreamClockView(context: Context) : View(context), Choreographer.FrameCallback {

    private var settings = ClockSettings()
    private var running = false
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
    }
    private val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        alpha = 200
    }
    private val dialPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }
    private val handPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeCap = Paint.Cap.ROUND
    }
    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var customTypeface: Typeface? = null

    private val time24Format = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val time12Format = SimpleDateFormat("h:mm", Locale.getDefault())
    private val secFormat = SimpleDateFormat(":ss", Locale.getDefault())
    private val amPmFormat = SimpleDateFormat("a", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("EEEE, MMM d", Locale.getDefault())

    fun updateSettings(newSettings: ClockSettings) {
        settings = newSettings
        val fontRes = when (newSettings.fontStyle) {
            ClockFontStyle.SPACE_GROTESK -> R.font.space_grotesk
            ClockFontStyle.JETBRAINS_MONO -> R.font.jetbrains_mono
            ClockFontStyle.OUTFIT -> R.font.outfit
            ClockFontStyle.SYNE -> R.font.syne
            ClockFontStyle.PLUS_JAKARTA -> R.font.plus_jakarta_sans
        }
        customTypeface = runCatching { ResourcesCompat.getFont(context, fontRes) }.getOrNull()
        textPaint.typeface = customTypeface ?: Typeface.DEFAULT_BOLD
        subPaint.typeface = customTypeface ?: Typeface.DEFAULT
        invalidate()
    }

    fun startAnimating() {
        if (!running) {
            running = true
            Choreographer.getInstance().postFrameCallback(this)
        }
    }

    fun stopAnimating() {
        running = false
        Choreographer.getInstance().removeFrameCallback(this)
    }

    override fun doFrame(frameTimeNanos: Long) {
        if (!running) return
        invalidate()
        Choreographer.getInstance().postFrameCallback(this)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) return

        canvas.drawColor(Color.BLACK)

        val now = System.currentTimeMillis()
        val palette = ColorEngine.computePalette(now, settings)
        val dimAlpha = (settings.screensaverDimLevel.coerceIn(0.25f, 1f) * 255).toInt()

        // Gentle OLED burn-in drift over a 2-minute Lissajous curve
        val driftX = if (settings.screensaverBurnInProtect) {
            (sin(now / 19000.0) * minOf(w, h) * 0.035).toFloat()
        } else 0f
        val driftY = if (settings.screensaverBurnInProtect) {
            (cos(now / 23000.0) * minOf(w, h) * 0.035).toFloat()
        } else 0f

        val cx = w / 2f + driftX
        val cy = h / 2f + driftY

        // Subtle ambient halo behind the clock
        val haloRadius = minOf(w, h) * 0.45f
        glowPaint.shader = RadialGradient(
            cx, cy, haloRadius,
            intArrayOf(
                palette.primary.copy(alpha = 0.20f * settings.screensaverDimLevel).toArgb(),
                Color.TRANSPARENT
            ),
            null, Shader.TileMode.CLAMP
        )
        canvas.drawCircle(cx, cy, haloRadius, glowPaint)

        val primaryArgb = palette.primary.toArgb()
        val secondaryArgb = palette.secondary.toArgb()

        when (settings.clockFace) {
            ClockFaceStyle.ANALOG_AURA -> {
                drawAnalogDream(canvas, cx, cy, minOf(w, h) * 0.34f * settings.clockScale, now, primaryArgb, secondaryArgb, dimAlpha)
            }
            ClockFaceStyle.SPLIT_MINIMAL -> {
                drawSplitDream(canvas, cx, cy, w, h, now, primaryArgb, secondaryArgb, dimAlpha)
            }
            else -> {
                drawDigitalDream(canvas, cx, cy, w, h, now, primaryArgb, secondaryArgb, dimAlpha)
            }
        }
    }

    private fun drawSplitDream(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        w: Float,
        h: Float,
        now: Long,
        primaryArgb: Int,
        secondaryArgb: Int,
        dimAlpha: Int
    ) {
        val cal = Calendar.getInstance().apply { timeInMillis = now }
        val hourVal = if (settings.use24Hour) cal.get(Calendar.HOUR_OF_DAY) else cal.get(Calendar.HOUR).let { if (it == 0) 12 else it }
        val hourStr = "%02d".format(hourVal)
        val minStr = "%02d".format(cal.get(Calendar.MINUTE))

        val digitSize = (minOf(w * 0.36f, h * 0.30f) * settings.clockScale).coerceIn(54f, 240f)
        textPaint.textSize = digitSize
        textPaint.shader = null
        textPaint.color = primaryArgb
        textPaint.alpha = dimAlpha
        canvas.drawText(hourStr, cx, cy - digitSize * 0.08f, textPaint)

        textPaint.color = secondaryArgb
        textPaint.alpha = dimAlpha
        canvas.drawText(minStr, cx, cy + digitSize * 0.80f, textPaint)

        if (settings.showDate) {
            subPaint.textSize = (digitSize * 0.16f).coerceAtLeast(22f)
            subPaint.color = Color.WHITE
            subPaint.alpha = (dimAlpha * 0.70f).toInt()
            canvas.drawText(dateFormat.format(cal.time), cx, cy + digitSize * 1.12f, subPaint)
        }
    }

    private fun drawDigitalDream(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        w: Float,
        h: Float,
        now: Long,
        primaryArgb: Int,
        secondaryArgb: Int,
        dimAlpha: Int
    ) {
        val cal = Calendar.getInstance().apply { timeInMillis = now }
        val mainStr = if (settings.use24Hour) {
            time24Format.format(cal.time)
        } else {
            time12Format.format(cal.time)
        }
        val extraStr = buildString {
            if (settings.showSeconds) append(secFormat.format(cal.time))
            if (!settings.use24Hour) {
                if (isNotEmpty()) append(" ")
                append(amPmFormat.format(cal.time))
            }
        }

        val textSize = (minOf(w * 0.25f, h * 0.32f) * settings.clockScale).coerceIn(48f, 280f)
        textPaint.textSize = textSize
        textPaint.alpha = dimAlpha
        textPaint.shader = LinearGradient(
            cx - textSize * 1.4f, cy - textSize * 0.5f,
            cx + textSize * 1.4f, cy + textSize * 0.5f,
            primaryArgb, secondaryArgb, Shader.TileMode.CLAMP
        )
        canvas.drawText(mainStr, cx, cy + textSize * 0.22f, textPaint)

        var yCursor = cy + textSize * 0.48f
        if (extraStr.isNotEmpty()) {
            subPaint.textSize = textSize * 0.26f
            subPaint.color = secondaryArgb
            subPaint.alpha = (dimAlpha * 0.85f).toInt()
            canvas.drawText(extraStr, cx, yCursor, subPaint)
            yCursor += textSize * 0.26f
        }

        if (settings.showDate) {
            subPaint.textSize = (textSize * 0.17f).coerceAtLeast(22f)
            subPaint.color = Color.WHITE
            subPaint.alpha = (dimAlpha * 0.68f).toInt()
            canvas.drawText(dateFormat.format(cal.time), cx, yCursor + 12f, subPaint)
            yCursor += subPaint.textSize + 20f
        }

        if (settings.showBatteryInScreensaver) {
            val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
            val pct = bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: -1
            if (pct in 0..100) {
                subPaint.textSize = (textSize * 0.14f).coerceAtLeast(18f)
                subPaint.color = primaryArgb
                subPaint.alpha = (dimAlpha * 0.62f).toInt()
                canvas.drawText("Battery $pct%", cx, yCursor + 8f, subPaint)
            }
        }
    }

    private fun drawAnalogDream(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        radius: Float,
        now: Long,
        primaryArgb: Int,
        secondaryArgb: Int,
        dimAlpha: Int
    ) {
        dialPaint.strokeWidth = 6f
        dialPaint.shader = LinearGradient(
            cx - radius, cy - radius, cx + radius, cy + radius,
            primaryArgb, secondaryArgb, Shader.TileMode.CLAMP
        )
        dialPaint.alpha = dimAlpha
        canvas.drawCircle(cx, cy, radius, dialPaint)

        for (i in 0 until 12) {
            val angle = Math.toRadians((i * 30.0) - 90.0)
            val inner = radius * 0.84f
            val outer = radius * 0.94f
            handPaint.color = if (i % 3 == 0) primaryArgb else secondaryArgb
            handPaint.alpha = dimAlpha
            handPaint.strokeWidth = if (i % 3 == 0) 6f else 3f
            canvas.drawLine(
                cx + (cos(angle) * inner).toFloat(),
                cy + (sin(angle) * inner).toFloat(),
                cx + (cos(angle) * outer).toFloat(),
                cy + (sin(angle) * outer).toFloat(),
                handPaint
            )
        }

        val cal = Calendar.getInstance().apply { timeInMillis = now }
        val seconds = cal.get(Calendar.SECOND) + cal.get(Calendar.MILLISECOND) / 1000f
        val minutes = cal.get(Calendar.MINUTE) + seconds / 60f
        val hours = (cal.get(Calendar.HOUR) % 12) + minutes / 60f

        val hourAngle = Math.toRadians((hours * 30.0) - 90.0)
        handPaint.color = Color.WHITE
        handPaint.alpha = dimAlpha
        handPaint.strokeWidth = 10f
        canvas.drawLine(
            cx, cy,
            cx + (cos(hourAngle) * radius * 0.50f).toFloat(),
            cy + (sin(hourAngle) * radius * 0.50f).toFloat(),
            handPaint
        )

        val minAngle = Math.toRadians((minutes * 6.0) - 90.0)
        handPaint.color = primaryArgb
        handPaint.alpha = dimAlpha
        handPaint.strokeWidth = 7f
        canvas.drawLine(
            cx, cy,
            cx + (cos(minAngle) * radius * 0.72f).toFloat(),
            cy + (sin(minAngle) * radius * 0.72f).toFloat(),
            handPaint
        )

        if (settings.showSeconds) {
            val secAngle = Math.toRadians((seconds * 6.0) - 90.0)
            handPaint.color = secondaryArgb
            handPaint.alpha = dimAlpha
            handPaint.strokeWidth = 3.5f
            canvas.drawLine(
                cx, cy,
                cx + (cos(secAngle) * radius * 0.82f).toFloat(),
                cy + (sin(secAngle) * radius * 0.82f).toFloat(),
                handPaint
            )
        }
    }
}
