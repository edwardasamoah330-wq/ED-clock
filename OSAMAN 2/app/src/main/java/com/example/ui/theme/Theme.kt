package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val ChromaDarkColorScheme = darkColorScheme(
    primary = ChromaCyan,
    onPrimary = ObsidianBase,
    primaryContainer = Color(0xFF0E334B),
    onPrimaryContainer = TextPrimary,
    secondary = ChromaViolet,
    onSecondary = ObsidianBase,
    secondaryContainer = Color(0xFF281F4A),
    onSecondaryContainer = TextPrimary,
    tertiary = ChromaRose,
    onTertiary = ObsidianBase,
    background = ObsidianBase,
    onBackground = TextPrimary,
    surface = ObsidianSurface,
    onSurface = TextPrimary,
    surfaceVariant = ObsidianSurfaceHigh,
    onSurfaceVariant = TextSecondary,
    outline = ObsidianOutline
)

private val ChromaLightColorScheme = lightColorScheme(
    primary = Color(0xFF0284C7),
    onPrimary = Color.White,
    secondary = Color(0xFF7C3AED),
    onSecondary = Color.White,
    tertiary = Color(0xFFE11D48),
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceHigh,
    onSurfaceVariant = LightTextSecondary
)

val ChromaShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp)
)

@Composable
fun ChromaClockTheme(
    darkTheme: Boolean = true,
    accentPrimary: Color? = null,
    accentSecondary: Color? = null,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val baseScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> ChromaDarkColorScheme
        else -> ChromaLightColorScheme
    }

    val finalScheme = if (accentPrimary != null && accentSecondary != null) {
        baseScheme.copy(
            primary = accentPrimary,
            secondary = accentSecondary
        )
    } else {
        baseScheme
    }

    MaterialTheme(
        colorScheme = finalScheme,
        typography = AppTypography,
        shapes = ChromaShapes,
        content = content
    )
}
