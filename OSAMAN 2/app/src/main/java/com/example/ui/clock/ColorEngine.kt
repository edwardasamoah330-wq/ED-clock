package com.example.ui.clock

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.example.model.ClockSettings
import com.example.model.ColorShiftMode
import java.util.Calendar
import kotlin.math.sin

data class ClockPalette(
    val primary: Color,
    val secondary: Color,
    val glow: Color,
    val subtleBgTint: Color
)

object ColorEngine {

    fun computePalette(
        nowMillis: Long,
        settings: ClockSettings
    ): ClockPalette {
        val speed = settings.shiftSpeed.coerceIn(0.2f, 3.0f)
        val tSeconds = (nowMillis / 1000.0) * speed

        val (h1, h2, sat, light) = when (settings.colorMode) {
            ColorShiftMode.SPECTRUM_FLOW -> {
                val baseHue = ((tSeconds * 16.0) % 360.0).toFloat()
                val secondHue = (baseHue + 52f) % 360f
                Quad(baseHue, secondHue, 0.86f, 0.66f)
            }

            ColorShiftMode.SOLAR_CYCLE -> {
                val cal = Calendar.getInstance().apply { timeInMillis = nowMillis }
                val minutesOfDay = cal.get(Calendar.HOUR_OF_DAY) * 60f + cal.get(Calendar.MINUTE)
                // 00:00 -> Indigo (245), 06:00 -> Rose/Coral (345), 12:00 -> Cyan/Sky (195), 18:00 -> Amber/Gold (32)
                val baseHue = when {
                    minutesOfDay < 360f -> lerpHue(245f, 345f, minutesOfDay / 360f)
                    minutesOfDay < 720f -> lerpHue(345f, 195f, (minutesOfDay - 360f) / 360f)
                    minutesOfDay < 1080f -> lerpHue(195f, 32f, (minutesOfDay - 720f) / 360f)
                    else -> lerpHue(32f, 245f, (minutesOfDay - 1080f) / 360f)
                }
                val breathe = (sin(tSeconds * 0.7) * 12.0).toFloat()
                Quad((baseHue + breathe + 360f) % 360f, (baseHue + 38f) % 360f, 0.84f, 0.68f)
            }

            ColorShiftMode.AURORA_DRIFT -> {
                val wave = ((sin(tSeconds * 0.45) + 1.0) * 0.5).toFloat()
                // Sweep between emerald (155), cyan (188), and polar violet (278)
                val baseHue = if (wave < 0.5f) {
                    155f + (188f - 155f) * (wave * 2f)
                } else {
                    188f + (278f - 188f) * ((wave - 0.5f) * 2f)
                }
                val secondHue = (baseHue + 46f) % 360f
                Quad(baseHue, secondHue, 0.85f, 0.65f)
            }

            ColorShiftMode.EMBER_PULSE -> {
                val wave = ((sin(tSeconds * 0.65) + 1.0) * 0.5).toFloat()
                // Sweep crimson (348) -> coral (14) -> warm amber (42)
                val hue = (348f + wave * 54f) % 360f
                val secondHue = (hue + 28f) % 360f
                Quad(hue, secondHue, 0.90f, 0.65f)
            }

            ColorShiftMode.OCEAN_DEPTH -> {
                val wave = ((sin(tSeconds * 0.5) + 1.0) * 0.5).toFloat()
                val hue = 172f + wave * 58f // 172 aqua -> 230 cobalt
                val secondHue = (hue - 24f + 360f) % 360f
                Quad(hue, secondHue, 0.84f, 0.64f)
            }

            ColorShiftMode.CUSTOM_FIXED -> {
                val h = settings.customHue.coerceIn(0f, 360f)
                val subtlePulse = (sin(tSeconds * 0.8) * 0.05).toFloat()
                Quad(h, (h + 36f) % 360f, (0.85f + subtlePulse).coerceIn(0.6f, 1f), 0.66f)
            }
        }

        val primary = Color.hsl(h1, sat, light)
        val secondary = Color.hsl(h2, sat, (light + 0.06f).coerceAtMost(0.82f))
        val glow = Color.hsl(h1, 0.95f, 0.60f, alpha = 0.45f)
        val bgTint = Color.hsl(h1, 0.65f, 0.14f)

        return ClockPalette(
            primary = primary,
            secondary = secondary,
            glow = glow,
            subtleBgTint = bgTint
        )
    }

    fun primaryArgb(nowMillis: Long, settings: ClockSettings): Int =
        computePalette(nowMillis, settings).primary.toArgb()

    fun secondaryArgb(nowMillis: Long, settings: ClockSettings): Int =
        computePalette(nowMillis, settings).secondary.toArgb()

    private fun lerpHue(start: Float, end: Float, fraction: Float): Float {
        val f = fraction.coerceIn(0f, 1f)
        var diff = end - start
        if (diff > 180f) diff -= 360f
        if (diff < -180f) diff += 360f
        return (start + diff * f + 360f) % 360f
    }

    private data class Quad(val h1: Float, val h2: Float, val s: Float, val l: Float)
}
