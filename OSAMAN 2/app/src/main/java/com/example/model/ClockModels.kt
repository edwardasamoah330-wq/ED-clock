package com.example.model

import androidx.annotation.DrawableRes
import com.example.R

enum class ClockFaceStyle(val label: String, val subtitle: String) {
    DIGITAL_PRISM("Digital", "Bold gradient numerals"),
    ANALOG_AURA("Analog", "Precision luminous dial"),
    SPLIT_MINIMAL("Minimalist", "Clean stacked editorial fit"),
    FLIP_RETRO("Flip", "Segmented split-flap cards"),
    ORBIT_HYBRID("Orbit", "Concentric progress rings"),
    CHRONO_BAR("Chrono", "Kinetic time bars")
}

enum class ColorShiftMode(val label: String, val description: String) {
    SPECTRUM_FLOW("Spectrum", "Smooth full-circle chromatic drift"),
    SOLAR_CYCLE("Solar", "Shifts organically with time of day"),
    AURORA_DRIFT("Aurora", "Teal, emerald & polar violet glow"),
    EMBER_PULSE("Ember", "Warm amber, coral & twilight rose"),
    OCEAN_DEPTH("Ocean", "Deep cobalt, aqua & seafoam"),
    CUSTOM_FIXED("Custom", "Lock and fine-tune your signature hue")
}

enum class ClockFontStyle(val label: String) {
    SPACE_GROTESK("Grotesk"),
    OUTFIT("Outfit"),
    JETBRAINS_MONO("Mono"),
    SYNE("Syne"),
    PLUS_JAKARTA("Jakarta")
}

enum class WallpaperSourceType {
    LIVE_GRADIENT,
    BUNDLED_ART,
    USER_PHOTO
}

enum class WallpaperTarget(val label: String) {
    HOME("Home Screen"),
    LOCK("Lock Screen"),
    BOTH("Home & Lock")
}

data class WallpaperPreset(
    val id: String,
    val title: String,
    val subtitle: String,
    val type: WallpaperSourceType,
    @DrawableRes val drawableRes: Int? = null,
    val baseColors: List<Long> = emptyList(),
    val scrimAlpha: Float = 0.25f
)

object WallpaperCatalog {
    val presets: List<WallpaperPreset> = listOf(
        WallpaperPreset(
            id = "live_spectrum",
            title = "Live Aura Mesh",
            subtitle = "Follows clock colors in real time",
            type = WallpaperSourceType.LIVE_GRADIENT,
            baseColors = listOf(0xFF070B14, 0xFF0F2038, 0xFF1A1033),
            scrimAlpha = 0.10f
        ),
        WallpaperPreset(
            id = "art_nebula",
            title = "Cosmic Nebula",
            subtitle = "Deep violet & cyan stardust ring",
            type = WallpaperSourceType.BUNDLED_ART,
            drawableRes = R.drawable.img_wall_nebula,
            scrimAlpha = 0.28f
        ),
        WallpaperPreset(
            id = "art_aurora",
            title = "Polar Aurora",
            subtitle = "Emerald ribbons over obsidian peaks",
            type = WallpaperSourceType.BUNDLED_ART,
            drawableRes = R.drawable.img_wall_aurora,
            scrimAlpha = 0.24f
        ),
        WallpaperPreset(
            id = "art_dunes",
            title = "Twilight Dunes",
            subtitle = "Warm amber & dusk desert geometry",
            type = WallpaperSourceType.BUNDLED_ART,
            drawableRes = R.drawable.img_wall_dunes,
            scrimAlpha = 0.26f
        ),
        WallpaperPreset(
            id = "art_prism",
            title = "Liquid Prism",
            subtitle = "OLED dark glass caustic light waves",
            type = WallpaperSourceType.BUNDLED_ART,
            drawableRes = R.drawable.img_wall_prism,
            scrimAlpha = 0.22f
        ),
        WallpaperPreset(
            id = "live_oled_midnight",
            title = "OLED Midnight",
            subtitle = "Pure black center with edge halo",
            type = WallpaperSourceType.LIVE_GRADIENT,
            baseColors = listOf(0xFF000000, 0xFF050811, 0xFF0A101E),
            scrimAlpha = 0.05f
        ),
        WallpaperPreset(
            id = "live_sunset_bloom",
            title = "Solar Dusk",
            subtitle = "Warm dusk amber to deep plum",
            type = WallpaperSourceType.LIVE_GRADIENT,
            baseColors = listOf(0xFF160B18, 0xFF3B1528, 0xFF1B1F3A),
            scrimAlpha = 0.15f
        )
    )

    fun findById(id: String): WallpaperPreset =
        presets.firstOrNull { it.id == id } ?: presets.first()
}

data class WorldCity(
    val id: String,
    val city: String,
    val zoneId: String
)

object WorldCityCatalog {
    val allCities: List<WorldCity> = listOf(
        WorldCity("nyc", "New York", "America/New_York"),
        WorldCity("lon", "London", "Europe/London"),
        WorldCity("tok", "Tokyo", "Asia/Tokyo"),
        WorldCity("syd", "Sydney", "Australia/Sydney"),
        WorldCity("par", "Paris", "Europe/Paris"),
        WorldCity("dxb", "Dubai", "Asia/Dubai"),
        WorldCity("lax", "Los Angeles", "America/Los_Angeles"),
        WorldCity("sin", "Singapore", "Asia/Singapore"),
        WorldCity("acc", "Accra", "Africa/Accra"),
        WorldCity("sao", "Sao Paulo", "America/Sao_Paulo"),
        WorldCity("seo", "Seoul", "Asia/Seoul"),
        WorldCity("ber", "Berlin", "Europe/Berlin")
    )

    val defaultCityIds: Set<String> = setOf("nyc", "lon", "tok", "acc")
}

data class ClockSettings(
    val clockFace: ClockFaceStyle = ClockFaceStyle.DIGITAL_PRISM,
    val colorMode: ColorShiftMode = ColorShiftMode.SPECTRUM_FLOW,
    val shiftSpeed: Float = 1.0f,
    val customHue: Float = 198f,
    val fontStyle: ClockFontStyle = ClockFontStyle.SPACE_GROTESK,
    val use24Hour: Boolean = false,
    val showSeconds: Boolean = true,
    val showDate: Boolean = true,
    val clockScale: Float = 1.0f,
    val selectedWallpaperId: String = "art_nebula",
    val customPhotoPath: String? = null,
    val screensaverBurnInProtect: Boolean = true,
    val screensaverDimLevel: Float = 0.85f,
    val screensaverKeepAwake: Boolean = true,
    val showBatteryInScreensaver: Boolean = true,
    val showClockOnLiveWallpaper: Boolean = true,
    val pinnedWorldCities: Set<String> = WorldCityCatalog.defaultCityIds
)
