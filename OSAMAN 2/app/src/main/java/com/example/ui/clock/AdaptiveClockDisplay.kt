package com.example.ui.clock

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.model.ClockFaceStyle
import com.example.model.ClockSettings
import com.example.model.WallpaperCatalog
import com.example.model.WallpaperSourceType
import com.example.ui.theme.ObsidianBase
import com.example.ui.theme.clockFontFamily
import kotlinx.coroutines.isActive
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun rememberLiveClockMillis(): Long {
    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (isActive) {
            withFrameMillis {
                nowMillis = System.currentTimeMillis()
            }
        }
    }
    return nowMillis
}

@Composable
fun WallpaperBackdropLayer(
    settings: ClockSettings,
    palette: ClockPalette,
    nowMillis: Long,
    modifier: Modifier = Modifier,
    overrideWallpaperId: String? = null
) {
    val activeId = overrideWallpaperId ?: settings.selectedWallpaperId
    val preset = remember(activeId) { WallpaperCatalog.findById(activeId) }

    Box(modifier = modifier.background(ObsidianBase)) {
        when {
            activeId == "user_photo" && !settings.customPhotoPath.isNullOrBlank() -> {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(File(settings.customPhotoPath))
                        .crossfade(true)
                        .build(),
                    contentDescription = "Selected custom wallpaper",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    ObsidianBase.copy(alpha = 0.35f),
                                    palette.subtleBgTint.copy(alpha = 0.28f),
                                    ObsidianBase.copy(alpha = 0.55f)
                                )
                            )
                        )
                )
            }
            preset.type == WallpaperSourceType.BUNDLED_ART && preset.drawableRes != null -> {
                Image(
                    painter = painterResource(id = preset.drawableRes),
                    contentDescription = preset.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    ObsidianBase.copy(alpha = preset.scrimAlpha),
                                    palette.subtleBgTint.copy(alpha = 0.22f),
                                    ObsidianBase.copy(alpha = preset.scrimAlpha + 0.25f)
                                )
                            )
                        )
                )
            }
            else -> {
                ProceduralAuroraBackdrop(
                    baseColors = preset.baseColors,
                    palette = palette,
                    nowMillis = nowMillis,
                    isOledMinimal = preset.id == "live_oled_midnight",
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun ProceduralAuroraBackdrop(
    baseColors: List<Long>,
    palette: ClockPalette,
    nowMillis: Long,
    isOledMinimal: Boolean,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val c0 = if (baseColors.isNotEmpty()) Color(baseColors[0]) else ObsidianBase
        val c1 = if (baseColors.size > 1) Color(baseColors[1]) else palette.subtleBgTint
        val c2 = if (baseColors.size > 2) Color(baseColors[2]) else ObsidianBase

        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(c0, if (isOledMinimal) c0 else c1, c2),
                start = Offset.Zero,
                end = Offset(size.width, size.height)
            )
        )

        val t = nowMillis / 1000.0
        val alpha1 = if (isOledMinimal) 0.14f else 0.34f
        val alpha2 = if (isOledMinimal) 0.12f else 0.28f

        val p1 = Offset(
            x = size.width * (0.28f + 0.18f * sin(t * 0.18).toFloat()),
            y = size.height * (0.26f + 0.12f * cos(t * 0.14).toFloat())
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(palette.primary.copy(alpha = alpha1), Color.Transparent),
                center = p1,
                radius = size.minDimension * 0.78f
            ),
            radius = size.minDimension * 0.78f,
            center = p1
        )

        val p2 = Offset(
            x = size.width * (0.72f + 0.16f * cos(t * 0.15).toFloat()),
            y = size.height * (0.70f + 0.14f * sin(t * 0.19).toFloat())
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(palette.secondary.copy(alpha = alpha2), Color.Transparent),
                center = p2,
                radius = size.minDimension * 0.76f
            ),
            radius = size.minDimension * 0.76f,
            center = p2
        )
    }
}

@Composable
fun AdaptiveClockFace(
    nowMillis: Long,
    settings: ClockSettings,
    palette: ClockPalette,
    modifier: Modifier = Modifier,
    isScreensaver: Boolean = false,
    dimAlpha: Float = 1f
) {
    val primary = palette.primary.copy(alpha = dimAlpha)
    val secondary = palette.secondary.copy(alpha = dimAlpha)
    val glow = palette.glow.copy(alpha = palette.glow.alpha * dimAlpha)
    val fontFamily = remember(settings.fontStyle) { clockFontFamily(settings.fontStyle) }

    val calendar = remember(nowMillis / 250L) {
        Calendar.getInstance().apply { timeInMillis = nowMillis }
    }
    val hourNum = if (settings.use24Hour) {
        calendar.get(Calendar.HOUR_OF_DAY)
    } else {
        val h = calendar.get(Calendar.HOUR)
        if (h == 0) 12 else h
    }
    val minuteNum = calendar.get(Calendar.MINUTE)
    val secondNum = calendar.get(Calendar.SECOND)
    val amPmText = remember(calendar.get(Calendar.AM_PM)) {
        SimpleDateFormat("a", Locale.getDefault()).format(calendar.time)
    }
    val dateText = remember(calendar.get(Calendar.DAY_OF_YEAR), calendar.get(Calendar.YEAR)) {
        SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(calendar.time)
    }

    val hourStr = if (settings.use24Hour) "%02d".format(hourNum) else hourNum.toString()
    val minStr = "%02d".format(minuteNum)
    val secStr = "%02d".format(secondNum)

    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        val availW = maxWidth
        val availH = maxHeight
        val minDim = minOf(availW, availH)

        // Subtle Lissajous pixel-shift in screensaver mode for OLED protection
        val driftOffsetX = if (isScreensaver && settings.screensaverBurnInProtect) {
            (sin(nowMillis / 19000.0) * minDim.value * 0.035).roundToInt()
        } else 0
        val driftOffsetY = if (isScreensaver && settings.screensaverBurnInProtect) {
            (cos(nowMillis / 23000.0) * minDim.value * 0.035).roundToInt()
        } else 0

        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(driftOffsetX, driftOffsetY) },
            contentAlignment = Alignment.Center
        ) {
            when (settings.clockFace) {
                ClockFaceStyle.DIGITAL_PRISM -> {
                    DigitalPrismFace(
                        availW = availW,
                        availH = availH,
                        hourStr = hourStr,
                        minStr = minStr,
                        secStr = secStr,
                        amPmText = amPmText,
                        dateText = dateText,
                        settings = settings,
                        primary = primary,
                        secondary = secondary,
                        glow = glow,
                        fontFamily = fontFamily,
                        dimAlpha = dimAlpha
                    )
                }

                ClockFaceStyle.ANALOG_AURA -> {
                    AnalogAuraFace(
                        dialSize = (minDim * 0.80f * settings.clockScale).coerceIn(110.dp, 460.dp),
                        nowMillis = nowMillis,
                        showSeconds = settings.showSeconds,
                        showDate = settings.showDate,
                        dateText = dateText,
                        primary = primary,
                        secondary = secondary,
                        glow = glow,
                        dimAlpha = dimAlpha
                    )
                }

                ClockFaceStyle.SPLIT_MINIMAL -> {
                    SplitMinimalFace(
                        availW = availW,
                        availH = availH,
                        hourStr = "%02d".format(hourNum),
                        minStr = minStr,
                        secStr = secStr,
                        amPmText = amPmText,
                        dateText = dateText,
                        settings = settings,
                        primary = primary,
                        secondary = secondary,
                        fontFamily = fontFamily,
                        dimAlpha = dimAlpha
                    )
                }

                ClockFaceStyle.ORBIT_HYBRID -> {
                    OrbitHybridFace(
                        dialSize = (minDim * 0.82f * settings.clockScale).coerceIn(120.dp, 440.dp),
                        nowMillis = nowMillis,
                        hourStr = hourStr,
                        minStr = minStr,
                        secStr = secStr,
                        amPmText = amPmText,
                        dateText = dateText,
                        settings = settings,
                        primary = primary,
                        secondary = secondary,
                        fontFamily = fontFamily,
                        dimAlpha = dimAlpha
                    )
                }

                ClockFaceStyle.FLIP_RETRO -> {
                    FlipRetroFace(
                        availW = availW,
                        availH = availH,
                        hourStr = "%02d".format(hourNum),
                        minStr = minStr,
                        secStr = secStr,
                        amPmText = amPmText,
                        dateText = dateText,
                        settings = settings,
                        primary = primary,
                        secondary = secondary,
                        fontFamily = fontFamily,
                        dimAlpha = dimAlpha
                    )
                }

                ClockFaceStyle.CHRONO_BAR -> {
                    ChronoBarFace(
                        availW = availW,
                        availH = availH,
                        nowMillis = nowMillis,
                        hourStr = hourStr,
                        minStr = minStr,
                        secStr = secStr,
                        amPmText = amPmText,
                        dateText = dateText,
                        settings = settings,
                        primary = primary,
                        secondary = secondary,
                        fontFamily = fontFamily,
                        dimAlpha = dimAlpha
                    )
                }
            }
        }
    }
}

@Composable
private fun DigitalPrismFace(
    availW: Dp,
    availH: Dp,
    hourStr: String,
    minStr: String,
    secStr: String,
    amPmText: String,
    dateText: String,
    settings: ClockSettings,
    primary: Color,
    secondary: Color,
    glow: Color,
    fontFamily: androidx.compose.ui.text.font.FontFamily,
    dimAlpha: Float
) {
    // Fit width cleanly across 5 characters ("23:59") and height constraints
    val baseSp = minOf(availW.value * 0.255f, availH.value * 0.44f) * settings.clockScale
    val mainSp = baseSp.coerceIn(30f, 152f)
    val secSp = (mainSp * 0.24f).coerceIn(12f, 34f)

    Box(contentAlignment = Alignment.Center) {
        // Soft ambient glow behind numerals
        Canvas(modifier = Modifier.size((mainSp * 2.4f).dp, (mainSp * 1.4f).dp)) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(glow, Color.Transparent),
                    center = center,
                    radius = size.maxDimension * 0.52f
                )
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "$hourStr:$minStr",
                    style = TextStyle(
                        fontFamily = fontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = mainSp.sp,
                        lineHeight = (mainSp * 1.02f).sp,
                        letterSpacing = (-2).sp,
                        fontFeatureSettings = "tnum",
                        brush = Brush.linearGradient(listOf(primary, secondary))
                    ),
                    maxLines = 1,
                    softWrap = false
                )

                if (settings.showSeconds || !settings.use24Hour) {
                    Column(
                        modifier = Modifier.padding(start = (mainSp * 0.08f).dp, bottom = (mainSp * 0.14f).dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        if (settings.showSeconds) {
                            Text(
                                text = secStr,
                                style = TextStyle(
                                    fontFamily = fontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = secSp.sp,
                                    fontFeatureSettings = "tnum",
                                    color = secondary
                                ),
                                maxLines = 1
                            )
                        }
                        if (!settings.use24Hour) {
                            Text(
                                text = amPmText,
                                style = TextStyle(
                                    fontFamily = fontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = (secSp * 0.65f).coerceAtLeast(10f).sp,
                                    color = Color.White.copy(alpha = 0.72f * dimAlpha)
                                ),
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            if (settings.showDate) {
                Spacer(modifier = Modifier.height((mainSp * 0.12f).coerceIn(4f, 16f).dp))
                Surface(
                    color = Color.White.copy(alpha = 0.08f * dimAlpha),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = dateText,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        style = TextStyle(
                            fontFamily = fontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = (mainSp * 0.17f).coerceIn(11f, 20f).sp,
                            color = Color.White.copy(alpha = 0.88f * dimAlpha)
                        ),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun AnalogAuraFace(
    dialSize: Dp,
    nowMillis: Long,
    showSeconds: Boolean,
    showDate: Boolean,
    dateText: String,
    primary: Color,
    secondary: Color,
    glow: Color,
    dimAlpha: Float
) {
    val cal = remember(nowMillis / 33L) {
        Calendar.getInstance().apply { timeInMillis = nowMillis }
    }
    val seconds = cal.get(Calendar.SECOND) + cal.get(Calendar.MILLISECOND) / 1000f
    val minutes = cal.get(Calendar.MINUTE) + seconds / 60f
    val hours = (cal.get(Calendar.HOUR) % 12) + minutes / 60f

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Canvas(modifier = Modifier.size(dialSize)) {
            val radius = size.minDimension * 0.46f
            val ringWidth = (size.minDimension * 0.018f).coerceAtLeast(3f)

            // Ambient radial halo
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(glow.copy(alpha = 0.25f * dimAlpha), Color.Transparent),
                    center = center,
                    radius = radius * 1.15f
                ),
                radius = radius * 1.15f
            )

            // Outer chromatic ring
            drawCircle(
                brush = Brush.sweepGradient(listOf(primary, secondary, primary)),
                radius = radius,
                style = Stroke(width = ringWidth)
            )

            // 60 tick marks
            for (i in 0 until 60) {
                val angle = Math.toRadians((i * 6.0) - 90.0)
                val isMajor = i % 5 == 0
                val inner = radius * if (isMajor) 0.84f else 0.90f
                val outer = radius * 0.94f
                drawLine(
                    color = if (isMajor) primary else Color.White.copy(alpha = 0.35f * dimAlpha),
                    start = Offset(
                        center.x + (cos(angle) * inner).toFloat(),
                        center.y + (sin(angle) * inner).toFloat()
                    ),
                    end = Offset(
                        center.x + (cos(angle) * outer).toFloat(),
                        center.y + (sin(angle) * outer).toFloat()
                    ),
                    strokeWidth = if (isMajor) ringWidth * 1.1f else ringWidth * 0.45f,
                    cap = StrokeCap.Round
                )
            }

            // Hour hand
            val hourAngle = Math.toRadians((hours * 30.0) - 90.0)
            drawLine(
                color = Color.White.copy(alpha = 0.95f * dimAlpha),
                start = center,
                end = Offset(
                    center.x + (cos(hourAngle) * radius * 0.50f).toFloat(),
                    center.y + (sin(hourAngle) * radius * 0.50f).toFloat()
                ),
                strokeWidth = (radius * 0.065f).coerceAtLeast(5f),
                cap = StrokeCap.Round
            )

            // Minute hand
            val minAngle = Math.toRadians((minutes * 6.0) - 90.0)
            drawLine(
                color = primary,
                start = center,
                end = Offset(
                    center.x + (cos(minAngle) * radius * 0.72f).toFloat(),
                    center.y + (sin(minAngle) * radius * 0.72f).toFloat()
                ),
                strokeWidth = (radius * 0.042f).coerceAtLeast(3.5f),
                cap = StrokeCap.Round
            )

            // Sweeping second hand
            if (showSeconds) {
                val secAngle = Math.toRadians((seconds * 6.0) - 90.0)
                drawLine(
                    color = secondary,
                    start = Offset(
                        center.x - (cos(secAngle) * radius * 0.16f).toFloat(),
                        center.y - (sin(secAngle) * radius * 0.16f).toFloat()
                    ),
                    end = Offset(
                        center.x + (cos(secAngle) * radius * 0.82f).toFloat(),
                        center.y + (sin(secAngle) * radius * 0.82f).toFloat()
                    ),
                    strokeWidth = (radius * 0.020f).coerceAtLeast(2f),
                    cap = StrokeCap.Round
                )
            }

            drawCircle(
                color = Color.White.copy(alpha = dimAlpha),
                radius = (radius * 0.055f).coerceAtLeast(4f)
            )
            drawCircle(
                color = primary,
                radius = (radius * 0.030f).coerceAtLeast(2.5f)
            )
        }

        if (showDate) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = dateText,
                style = TextStyle(
                    fontSize = (dialSize.value * 0.07f).coerceIn(11f, 18f).sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.82f * dimAlpha)
                ),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun SplitMinimalFace(
    availW: Dp,
    availH: Dp,
    hourStr: String,
    minStr: String,
    secStr: String,
    amPmText: String,
    dateText: String,
    settings: ClockSettings,
    primary: Color,
    secondary: Color,
    fontFamily: androidx.compose.ui.text.font.FontFamily,
    dimAlpha: Float
) {
    val digitSp = (minOf(availW.value * 0.36f, availH.value * 0.30f) * settings.clockScale)
        .coerceIn(32f, 150f)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = hourStr,
            style = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = digitSp.sp,
                lineHeight = (digitSp * 0.90f).sp,
                fontFeatureSettings = "tnum",
                color = primary
            ),
            textAlign = TextAlign.Center,
            maxLines = 1
        )
        Text(
            text = minStr,
            style = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = digitSp.sp,
                lineHeight = (digitSp * 0.90f).sp,
                fontFeatureSettings = "tnum",
                brush = Brush.linearGradient(listOf(secondary, Color.White.copy(alpha = 0.92f * dimAlpha)))
            ),
            textAlign = TextAlign.Center,
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (settings.showSeconds) {
                Text(
                    text = ": $secStr",
                    style = TextStyle(
                        fontFamily = fontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = (digitSp * 0.22f).coerceIn(12f, 24f).sp,
                        fontFeatureSettings = "tnum",
                        color = secondary
                    )
                )
            }
            if (!settings.use24Hour) {
                Text(
                    text = amPmText,
                    style = TextStyle(
                        fontFamily = fontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = (digitSp * 0.18f).coerceIn(11f, 20f).sp,
                        color = Color.White.copy(alpha = 0.72f * dimAlpha)
                    )
                )
            }
            if (settings.showDate) {
                Text(
                    text = dateText,
                    style = TextStyle(
                        fontFamily = fontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = (digitSp * 0.16f).coerceIn(11f, 18f).sp,
                        color = Color.White.copy(alpha = 0.78f * dimAlpha)
                    )
                )
            }
        }
    }
}

@Composable
private fun OrbitHybridFace(
    dialSize: Dp,
    nowMillis: Long,
    hourStr: String,
    minStr: String,
    secStr: String,
    amPmText: String,
    dateText: String,
    settings: ClockSettings,
    primary: Color,
    secondary: Color,
    fontFamily: androidx.compose.ui.text.font.FontFamily,
    dimAlpha: Float
) {
    val cal = remember(nowMillis / 33L) {
        Calendar.getInstance().apply { timeInMillis = nowMillis }
    }
    val secFrac = (cal.get(Calendar.SECOND) + cal.get(Calendar.MILLISECOND) / 1000f) / 60f
    val minFrac = (cal.get(Calendar.MINUTE) + secFrac) / 60f
    val hourFrac = ((cal.get(Calendar.HOUR) % 12) + minFrac) / 12f

    Box(
        modifier = Modifier.size(dialSize),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeOuter = (size.minDimension * 0.032f).coerceAtLeast(5f)
            val strokeInner = (size.minDimension * 0.024f).coerceAtLeast(4f)

            // Outer seconds ring
            val pad1 = strokeOuter
            drawArc(
                color = Color.White.copy(alpha = 0.10f * dimAlpha),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(pad1, pad1),
                size = Size(size.width - pad1 * 2, size.height - pad1 * 2),
                style = Stroke(width = strokeOuter)
            )
            drawArc(
                brush = Brush.sweepGradient(listOf(primary, secondary, primary)),
                startAngle = -90f,
                sweepAngle = secFrac * 360f,
                useCenter = false,
                topLeft = Offset(pad1, pad1),
                size = Size(size.width - pad1 * 2, size.height - pad1 * 2),
                style = Stroke(width = strokeOuter, cap = StrokeCap.Round)
            )

            // Inner minutes ring
            val pad2 = strokeOuter * 3.1f
            drawArc(
                color = Color.White.copy(alpha = 0.08f * dimAlpha),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(pad2, pad2),
                size = Size(size.width - pad2 * 2, size.height - pad2 * 2),
                style = Stroke(width = strokeInner)
            )
            drawArc(
                color = secondary,
                startAngle = -90f,
                sweepAngle = minFrac * 360f,
                useCenter = false,
                topLeft = Offset(pad2, pad2),
                size = Size(size.width - pad2 * 2, size.height - pad2 * 2),
                style = Stroke(width = strokeInner, cap = StrokeCap.Round)
            )

            // Innermost hour ring
            val pad3 = strokeOuter * 5.0f
            drawArc(
                color = primary.copy(alpha = 0.72f),
                startAngle = -90f,
                sweepAngle = hourFrac * 360f,
                useCenter = false,
                topLeft = Offset(pad3, pad3),
                size = Size(size.width - pad3 * 2, size.height - pad3 * 2),
                style = Stroke(width = strokeInner * 0.75f, cap = StrokeCap.Round)
            )
        }

        val centerTextSp = (dialSize.value * 0.19f).coerceIn(20f, 72f)
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$hourStr:$minStr",
                style = TextStyle(
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = centerTextSp.sp,
                    fontFeatureSettings = "tnum",
                    brush = Brush.linearGradient(listOf(primary, secondary))
                ),
                maxLines = 1
            )
            if (settings.showSeconds) {
                Text(
                    text = if (settings.use24Hour) secStr else "$secStr $amPmText",
                    style = TextStyle(
                        fontFamily = fontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = (centerTextSp * 0.34f).coerceIn(10f, 20f).sp,
                        fontFeatureSettings = "tnum",
                        color = secondary
                    ),
                    maxLines = 1
                )
            }
            if (settings.showDate) {
                Text(
                    text = dateText,
                    modifier = Modifier.padding(top = 2.dp),
                    style = TextStyle(
                        fontFamily = fontFamily,
                        fontSize = (centerTextSp * 0.25f).coerceIn(10f, 15f).sp,
                        color = Color.White.copy(alpha = 0.75f * dimAlpha)
                    ),
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun FlipRetroFace(
    availW: Dp,
    availH: Dp,
    hourStr: String,
    minStr: String,
    secStr: String,
    amPmText: String,
    dateText: String,
    settings: ClockSettings,
    primary: Color,
    secondary: Color,
    fontFamily: androidx.compose.ui.text.font.FontFamily,
    dimAlpha: Float
) {
    val cardW = (minOf(availW.value * 0.40f, availH.value * 0.48f) * settings.clockScale)
        .coerceIn(58f, 210f)
    val cardH = cardW * 1.08f
    val digitSp = (cardW * 0.56f).coerceIn(24f, 118f)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy((cardW * 0.10f).coerceIn(6f, 18f).dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FlipTile(
                text = hourStr,
                width = cardW.dp,
                height = cardH.dp,
                digitSp = digitSp,
                accent = primary,
                fontFamily = fontFamily,
                dimAlpha = dimAlpha,
                badge = if (!settings.use24Hour) amPmText else null
            )
            FlipTile(
                text = minStr,
                width = cardW.dp,
                height = cardH.dp,
                digitSp = digitSp,
                accent = secondary,
                fontFamily = fontFamily,
                dimAlpha = dimAlpha,
                badge = if (settings.showSeconds) secStr else null
            )
        }

        if (settings.showDate) {
            Spacer(modifier = Modifier.height((cardW * 0.12f).coerceIn(6f, 16f).dp))
            Text(
                text = dateText,
                style = TextStyle(
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = (cardW * 0.12f).coerceIn(11f, 18f).sp,
                    color = Color.White.copy(alpha = 0.80f * dimAlpha)
                ),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun FlipTile(
    text: String,
    width: Dp,
    height: Dp,
    digitSp: Float,
    accent: Color,
    fontFamily: androidx.compose.ui.text.font.FontFamily,
    dimAlpha: Float,
    badge: String?
) {
    Surface(
        shape = RoundedCornerShape((width.value * 0.16f).dp),
        color = Color(0xFF131824).copy(alpha = 0.88f * dimAlpha),
        border = androidx.compose.foundation.BorderStroke(1.2.dp, accent.copy(alpha = 0.45f)),
        modifier = Modifier.size(width, height)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                style = TextStyle(
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = digitSp.sp,
                    fontFeatureSettings = "tnum",
                    color = accent
                ),
                maxLines = 1
            )
            // Classic split-flap center hinge line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.5.dp)
                    .background(Color.Black.copy(alpha = 0.65f))
            )
            if (badge != null) {
                Text(
                    text = badge,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding((width.value * 0.08f).dp),
                    style = TextStyle(
                        fontFamily = fontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = (digitSp * 0.22f).coerceIn(9f, 18f).sp,
                        fontFeatureSettings = "tnum",
                        color = Color.White.copy(alpha = 0.78f * dimAlpha)
                    )
                )
            }
        }
    }
}

@Composable
private fun ChronoBarFace(
    availW: Dp,
    availH: Dp,
    nowMillis: Long,
    hourStr: String,
    minStr: String,
    secStr: String,
    amPmText: String,
    dateText: String,
    settings: ClockSettings,
    primary: Color,
    secondary: Color,
    fontFamily: androidx.compose.ui.text.font.FontFamily,
    dimAlpha: Float
) {
    val cal = remember(nowMillis / 40L) {
        Calendar.getInstance().apply { timeInMillis = nowMillis }
    }
    val secFrac = (cal.get(Calendar.SECOND) + cal.get(Calendar.MILLISECOND) / 1000f) / 60f
    val minFrac = (cal.get(Calendar.MINUTE) + secFrac) / 60f
    val hourFrac = (cal.get(Calendar.HOUR_OF_DAY) + minFrac) / 24f

    val blockW = (availW.value * 0.84f).coerceIn(140f, 520f).dp
    val digitSp = (minOf(availW.value * 0.19f, availH.value * 0.28f) * settings.clockScale)
        .coerceIn(26f, 96f)

    Column(
        modifier = Modifier.size(width = blockW, height = Dp.Unspecified),
        verticalArrangement = Arrangement.spacedBy((digitSp * 0.14f).coerceIn(6f, 14f).dp),
        horizontalAlignment = Alignment.Start
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = "$hourStr:$minStr" + if (settings.showSeconds) ":$secStr" else "",
                style = TextStyle(
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = digitSp.sp,
                    fontFeatureSettings = "tnum",
                    brush = Brush.linearGradient(listOf(primary, secondary))
                ),
                maxLines = 1
            )
            if (!settings.use24Hour) {
                Text(
                    text = amPmText,
                    style = TextStyle(
                        fontFamily = fontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = (digitSp * 0.30f).coerceIn(11f, 22f).sp,
                        color = Color.White.copy(alpha = 0.72f * dimAlpha)
                    )
                )
            }
        }

        ChronoProgressRow(
            label = "HR",
            fraction = hourFrac,
            color = primary,
            barHeight = (digitSp * 0.14f).coerceIn(5f, 10f).dp,
            fontFamily = fontFamily,
            dimAlpha = dimAlpha
        )
        ChronoProgressRow(
            label = "MN",
            fraction = minFrac,
            color = secondary,
            barHeight = (digitSp * 0.14f).coerceIn(5f, 10f).dp,
            fontFamily = fontFamily,
            dimAlpha = dimAlpha
        )
        if (settings.showSeconds) {
            ChronoProgressRow(
                label = "SC",
                fraction = secFrac,
                color = Color.White.copy(alpha = 0.88f * dimAlpha),
                barHeight = (digitSp * 0.11f).coerceIn(4f, 8f).dp,
                fontFamily = fontFamily,
                dimAlpha = dimAlpha
            )
        }

        if (settings.showDate) {
            Text(
                text = dateText,
                style = TextStyle(
                    fontFamily = fontFamily,
                    fontSize = (digitSp * 0.24f).coerceIn(11f, 17f).sp,
                    color = Color.White.copy(alpha = 0.75f * dimAlpha)
                ),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun ChronoProgressRow(
    label: String,
    fraction: Float,
    color: Color,
    barHeight: Dp,
    fontFamily: androidx.compose.ui.text.font.FontFamily,
    dimAlpha: Float
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = label,
            style = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.65f * dimAlpha)
            )
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(barHeight)
                .background(Color.White.copy(alpha = 0.12f * dimAlpha), RoundedCornerShape(50))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction.coerceIn(0.02f, 1f))
                    .height(barHeight)
                    .background(color, RoundedCornerShape(50))
            )
        }
    }
}

