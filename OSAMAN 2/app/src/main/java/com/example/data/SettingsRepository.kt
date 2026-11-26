package com.example.data

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Rect
import android.graphics.Shader
import android.net.Uri
import android.os.Build
import androidx.compose.ui.graphics.toArgb
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.ClockFaceStyle
import com.example.model.ClockFontStyle
import com.example.model.ClockSettings
import com.example.model.ColorShiftMode
import com.example.model.WallpaperCatalog
import com.example.model.WallpaperPreset
import com.example.model.WallpaperSourceType
import com.example.model.WallpaperTarget
import com.example.model.WorldCityCatalog
import com.example.ui.clock.ColorEngine
import com.example.widget.ChromaAnalogWidgetProvider
import com.example.widget.ChromaClockWidgetProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

private val Context.chromaDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "chroma_clock_prefs"
)

class SettingsRepository(private val context: Context) {

    private object Keys {
        val CLOCK_FACE = stringPreferencesKey("clock_face")
        val COLOR_MODE = stringPreferencesKey("color_mode")
        val SHIFT_SPEED = floatPreferencesKey("shift_speed")
        val CUSTOM_HUE = floatPreferencesKey("custom_hue")
        val FONT_STYLE = stringPreferencesKey("font_style")
        val USE_24_HOUR = booleanPreferencesKey("use_24_hour")
        val SHOW_SECONDS = booleanPreferencesKey("show_seconds")
        val SHOW_DATE = booleanPreferencesKey("show_date")
        val CLOCK_SCALE = floatPreferencesKey("clock_scale")
        val SELECTED_WALLPAPER_ID = stringPreferencesKey("selected_wallpaper_id")
        val CUSTOM_PHOTO_PATH = stringPreferencesKey("custom_photo_path")
        val SCREENSAVER_BURN_IN = booleanPreferencesKey("screensaver_burn_in")
        val SCREENSAVER_DIM = floatPreferencesKey("screensaver_dim")
        val SCREENSAVER_KEEP_AWAKE = booleanPreferencesKey("screensaver_keep_awake")
        val SHOW_BATTERY_SCREENSAVER = booleanPreferencesKey("show_battery_screensaver")
        val SHOW_CLOCK_LIVE_WALLPAPER = booleanPreferencesKey("show_clock_live_wallpaper")
        val PINNED_WORLD_CITIES = stringSetPreferencesKey("pinned_world_cities")
    }

    val settingsFlow: Flow<ClockSettings> = context.chromaDataStore.data.map { prefs ->
        ClockSettings(
            clockFace = prefs[Keys.CLOCK_FACE]?.let { runCatching { ClockFaceStyle.valueOf(it) }.getOrNull() }
                ?: ClockFaceStyle.DIGITAL_PRISM,
            colorMode = prefs[Keys.COLOR_MODE]?.let { runCatching { ColorShiftMode.valueOf(it) }.getOrNull() }
                ?: ColorShiftMode.SPECTRUM_FLOW,
            shiftSpeed = prefs[Keys.SHIFT_SPEED] ?: 1.0f,
            customHue = prefs[Keys.CUSTOM_HUE] ?: 198f,
            fontStyle = prefs[Keys.FONT_STYLE]?.let { runCatching { ClockFontStyle.valueOf(it) }.getOrNull() }
                ?: ClockFontStyle.SPACE_GROTESK,
            use24Hour = prefs[Keys.USE_24_HOUR] ?: false,
            showSeconds = prefs[Keys.SHOW_SECONDS] ?: true,
            showDate = prefs[Keys.SHOW_DATE] ?: true,
            clockScale = prefs[Keys.CLOCK_SCALE] ?: 1.0f,
            selectedWallpaperId = prefs[Keys.SELECTED_WALLPAPER_ID] ?: "art_nebula",
            customPhotoPath = prefs[Keys.CUSTOM_PHOTO_PATH],
            screensaverBurnInProtect = prefs[Keys.SCREENSAVER_BURN_IN] ?: true,
            screensaverDimLevel = prefs[Keys.SCREENSAVER_DIM] ?: 0.85f,
            screensaverKeepAwake = prefs[Keys.SCREENSAVER_KEEP_AWAKE] ?: true,
            showBatteryInScreensaver = prefs[Keys.SHOW_BATTERY_SCREENSAVER] ?: true,
            showClockOnLiveWallpaper = prefs[Keys.SHOW_CLOCK_LIVE_WALLPAPER] ?: true,
            pinnedWorldCities = prefs[Keys.PINNED_WORLD_CITIES] ?: WorldCityCatalog.defaultCityIds
        )
    }

    suspend fun getCurrentSettings(): ClockSettings = settingsFlow.first()

    suspend fun updateSettings(transform: (ClockSettings) -> ClockSettings) {
        val updated = transform(getCurrentSettings())
        context.chromaDataStore.edit { prefs ->
            prefs[Keys.CLOCK_FACE] = updated.clockFace.name
            prefs[Keys.COLOR_MODE] = updated.colorMode.name
            prefs[Keys.SHIFT_SPEED] = updated.shiftSpeed
            prefs[Keys.CUSTOM_HUE] = updated.customHue
            prefs[Keys.FONT_STYLE] = updated.fontStyle.name
            prefs[Keys.USE_24_HOUR] = updated.use24Hour
            prefs[Keys.SHOW_SECONDS] = updated.showSeconds
            prefs[Keys.SHOW_DATE] = updated.showDate
            prefs[Keys.CLOCK_SCALE] = updated.clockScale
            prefs[Keys.SELECTED_WALLPAPER_ID] = updated.selectedWallpaperId
            if (updated.customPhotoPath != null) {
                prefs[Keys.CUSTOM_PHOTO_PATH] = updated.customPhotoPath
            } else {
                prefs.remove(Keys.CUSTOM_PHOTO_PATH)
            }
            prefs[Keys.SCREENSAVER_BURN_IN] = updated.screensaverBurnInProtect
            prefs[Keys.SCREENSAVER_DIM] = updated.screensaverDimLevel
            prefs[Keys.SCREENSAVER_KEEP_AWAKE] = updated.screensaverKeepAwake
            prefs[Keys.SHOW_BATTERY_SCREENSAVER] = updated.showBatteryInScreensaver
            prefs[Keys.SHOW_CLOCK_LIVE_WALLPAPER] = updated.showClockOnLiveWallpaper
            prefs[Keys.PINNED_WORLD_CITIES] = updated.pinnedWorldCities
        }
        ChromaClockWidgetProvider.updateAllWidgets(context)
        ChromaAnalogWidgetProvider.updateAllWidgets(context)
    }

    suspend fun importCustomWallpaperPhoto(uri: Uri): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val destFile = File(context.filesDir, "custom_wallpaper.jpg")
            context.contentResolver.openInputStream(uri)?.use { input ->
                val raw = BitmapFactory.decodeStream(input)
                    ?: error("Could not decode selected photo.")
                val scaled = scaleBitmapMaxEdge(raw, 2400)
                FileOutputStream(destFile).use { out ->
                    scaled.compress(Bitmap.CompressFormat.JPEG, 92, out)
                }
                if (scaled !== raw) raw.recycle()
                scaled.recycle()
            } ?: error("Unable to read selected image.")
            destFile.absolutePath
        }
    }

    suspend fun applySystemWallpaper(
        preset: WallpaperPreset,
        customPhotoPath: String?,
        target: WallpaperTarget,
        settings: ClockSettings
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val wm = WallpaperManager.getInstance(context)
            val dm = context.resources.displayMetrics
            val targetWidth = maxOf(dm.widthPixels, 1080)
            val targetHeight = maxOf(dm.heightPixels, 1920)

            val bitmap = when {
                preset.id == "user_photo" && !customPhotoPath.isNullOrBlank() -> {
                    val file = File(customPhotoPath)
                    val decoded = BitmapFactory.decodeFile(file.absolutePath)
                        ?: error("Custom photo not found.")
                    centerCropBitmap(decoded, targetWidth, targetHeight)
                }
                preset.type == WallpaperSourceType.BUNDLED_ART && preset.drawableRes != null -> {
                    val decoded = BitmapFactory.decodeResource(context.resources, preset.drawableRes)
                        ?: error("Wallpaper asset could not be decoded.")
                    centerCropBitmap(decoded, targetWidth, targetHeight)
                }
                else -> {
                    renderProceduralWallpaperBitmap(
                        width = targetWidth,
                        height = targetHeight,
                        preset = preset,
                        settings = settings
                    )
                }
            }

            val flags = when (target) {
                WallpaperTarget.HOME -> WallpaperManager.FLAG_SYSTEM
                WallpaperTarget.LOCK -> WallpaperManager.FLAG_LOCK
                WallpaperTarget.BOTH -> WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                wm.setBitmap(bitmap, null, true, flags)
            } else {
                wm.setBitmap(bitmap)
            }
            bitmap.recycle()
        }
    }

    private fun scaleBitmapMaxEdge(src: Bitmap, maxEdge: Int): Bitmap {
        val w = src.width
        val h = src.height
        val longest = maxOf(w, h)
        if (longest <= maxEdge) return src
        val ratio = maxEdge.toFloat() / longest.toFloat()
        return Bitmap.createScaledBitmap(src, (w * ratio).toInt(), (h * ratio).toInt(), true)
    }

    private fun centerCropBitmap(src: Bitmap, reqW: Int, reqH: Int): Bitmap {
        val srcRatio = src.width.toFloat() / src.height.toFloat()
        val dstRatio = reqW.toFloat() / reqH.toFloat()
        val cropRect = if (srcRatio > dstRatio) {
            val newW = (src.height * dstRatio).toInt()
            val left = (src.width - newW) / 2
            Rect(left, 0, left + newW, src.height)
        } else {
            val newH = (src.width / dstRatio).toInt()
            val top = (src.height - newH) / 2
            Rect(0, top, src.width, top + newH)
        }
        val out = Bitmap.createBitmap(reqW, reqH, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        canvas.drawBitmap(src, cropRect, Rect(0, 0, reqW, reqH), paint)
        src.recycle()
        return out
    }

    private fun renderProceduralWallpaperBitmap(
        width: Int,
        height: Int,
        preset: WallpaperPreset,
        settings: ClockSettings
    ): Bitmap {
        val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        val palette = ColorEngine.computePalette(System.currentTimeMillis(), settings)
        val colors = if (preset.baseColors.size >= 3) {
            intArrayOf(
                preset.baseColors[0].toInt(),
                preset.baseColors[1].toInt(),
                preset.baseColors[2].toInt()
            )
        } else {
            intArrayOf(
                0xFF07090F.toInt(),
                palette.subtleBgTint.toArgb(),
                0xFF0B1120.toInt()
            )
        }
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, width.toFloat(), height.toFloat(),
                colors, null, Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        val orb1 = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                width * 0.25f, height * 0.28f, width * 0.72f,
                intArrayOf(palette.primary.copy(alpha = 0.42f).toArgb(), 0x00000000),
                null, Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(width * 0.25f, height * 0.28f, width * 0.72f, orb1)

        val orb2 = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                width * 0.78f, height * 0.72f, width * 0.75f,
                intArrayOf(palette.secondary.copy(alpha = 0.36f).toArgb(), 0x00000000),
                null, Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(width * 0.78f, height * 0.72f, width * 0.75f, orb2)
        return out
    }
}
