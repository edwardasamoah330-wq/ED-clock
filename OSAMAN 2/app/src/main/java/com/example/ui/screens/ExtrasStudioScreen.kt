package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddToHomeScreen
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.WatchLater
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ClockSettings
import com.example.model.WorldCityCatalog
import com.example.ui.ChromaViewModel
import com.example.ui.FocusTimerState
import com.example.ui.clock.ClockPalette
import com.example.ui.theme.clockFontFamily
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ExtrasStudioScreen(
    nowMillis: Long,
    settings: ClockSettings,
    timerState: FocusTimerState,
    palette: ClockPalette,
    viewModel: ChromaViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var timerTab by remember { mutableIntStateOf(0) } // 0 = Focus Timer, 1 = Stopwatch
    var showCitySheet by remember { mutableStateOf(false) }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 760.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Time Tools & Widgets",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            // Focus Timer & Stopwatch Card
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        SegmentedButton(
                            selected = timerTab == 0,
                            onClick = { timerTab = 0 },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                            modifier = Modifier.testTag("tab_focus_timer")
                        ) {
                            Text("Focus Timer")
                        }
                        SegmentedButton(
                            selected = timerTab == 1,
                            onClick = { timerTab = 1 },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                            modifier = Modifier.testTag("tab_stopwatch")
                        ) {
                            Text("Stopwatch")
                        }
                    }

                    if (timerTab == 0) {
                        FocusTimerSection(
                            timerState = timerState,
                            settings = settings,
                            palette = palette,
                            onSelectPreset = viewModel::selectTimerPreset,
                            onToggleRun = viewModel::toggleTimerRunning,
                            onReset = viewModel::resetTimer
                        )
                    } else {
                        StopwatchSection(
                            timerState = timerState,
                            settings = settings,
                            palette = palette,
                            onToggle = viewModel::toggleStopwatch,
                            onLap = viewModel::lapStopwatch,
                            onReset = viewModel::resetStopwatch
                        )
                    }
                }
            }

            // World Clocks Card
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Public,
                                contentDescription = null,
                                tint = palette.primary
                            )
                            Text("World Clocks", style = MaterialTheme.typography.titleLarge)
                        }
                        TextButton(
                            onClick = { showCitySheet = true },
                            modifier = Modifier.testTag("edit_world_cities_button")
                        ) {
                            Text("Edit Cities", color = palette.secondary)
                        }
                    }

                    val pinnedCities = remember(settings.pinnedWorldCities) {
                        WorldCityCatalog.allCities.filter { settings.pinnedWorldCities.contains(it.id) }
                    }

                    pinnedCities.forEach { city ->
                        val tz = remember(city.zoneId) { TimeZone.getTimeZone(city.zoneId) }
                        val timeFormatter = remember(settings.use24Hour, city.zoneId) {
                            SimpleDateFormat(if (settings.use24Hour) "HH:mm" else "h:mm a", Locale.getDefault()).apply {
                                timeZone = tz
                            }
                        }
                        val dayFormatter = remember(city.zoneId) {
                            SimpleDateFormat("EEE", Locale.getDefault()).apply {
                                timeZone = tz
                            }
                        }
                        val nowDate = Date(nowMillis)
                        val offsetHrs = tz.getOffset(nowMillis) / 3600000.0
                        val offsetLabel = if (offsetHrs >= 0) "+%.0fh".format(offsetHrs) else "%.0fh".format(offsetHrs)

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(city.city, style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        text = "${dayFormatter.format(nowDate)} • UTC$offsetLabel",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = timeFormatter.format(nowDate),
                                    style = TextStyle(
                                        fontFamily = clockFontFamily(settings.fontStyle),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 22.sp,
                                        fontFeatureSettings = "tnum",
                                        color = palette.primary
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Home Screen Widget Studio Card
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AddToHomeScreen,
                            contentDescription = null,
                            tint = palette.secondary
                        )
                        Text("Home Screen Widgets", style = MaterialTheme.typography.titleLarge)
                    }
                    Text(
                        text = "Pin resizable widgets to your home screen. Drag widget corners anytime to make them fit cleanly.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { viewModel.requestPinWidget(context, isAnalog = false) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = palette.primary,
                                contentColor = Color.Black
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .minimumInteractiveComponentSize()
                                .testTag("extras_pin_digital")
                        ) {
                            Icon(Icons.Filled.Schedule, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Pin Digital", maxLines = 1, fontWeight = FontWeight.SemiBold)
                        }
                        OutlinedButton(
                            onClick = { viewModel.requestPinWidget(context, isAnalog = true) },
                            border = BorderStroke(1.dp, palette.secondary.copy(alpha = 0.7f)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .minimumInteractiveComponentSize()
                                .testTag("extras_pin_analog")
                        ) {
                            Icon(
                                Icons.Outlined.WatchLater,
                                contentDescription = null,
                                tint = palette.secondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Pin Analog", maxLines = 1, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        }
    }

    if (showCitySheet) {
        ModalBottomSheet(
            onDismissRequest = { showCitySheet = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Select World Cities", style = MaterialTheme.typography.titleLarge)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    WorldCityCatalog.allCities.forEach { city ->
                        FilterChip(
                            selected = settings.pinnedWorldCities.contains(city.id),
                            onClick = { viewModel.toggleWorldCity(city.id) },
                            label = { Text(city.city) },
                            modifier = Modifier.testTag("city_chip_${city.id}")
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FocusTimerSection(
    timerState: FocusTimerState,
    settings: ClockSettings,
    palette: ClockPalette,
    onSelectPreset: (Int) -> Unit,
    onToggleRun: () -> Unit,
    onReset: () -> Unit
) {
    val progress = if (timerState.totalSeconds > 0) {
        timerState.remainingSeconds.toFloat() / timerState.totalSeconds.toFloat()
    } else 0f
    val mins = timerState.remainingSeconds / 60
    val secs = timerState.remainingSeconds % 60

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        listOf(15, 25, 45, 60).forEach { preset ->
            FilterChip(
                selected = timerState.presetMinutes == preset,
                onClick = { onSelectPreset(preset) },
                label = { Text("${preset}m") },
                modifier = Modifier.testTag("timer_preset_$preset")
            )
        }
    }

    Box(
        modifier = Modifier.size(170.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawArc(
                color = Color.White.copy(alpha = 0.1f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = 12f)
            )
            drawArc(
                brush = Brush.sweepGradient(listOf(palette.primary, palette.secondary, palette.primary)),
                startAngle = -90f,
                sweepAngle = progress * 360f,
                useCenter = false,
                style = Stroke(width = 12f, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "%02d:%02d".format(mins, secs),
                style = TextStyle(
                    fontFamily = clockFontFamily(settings.fontStyle),
                    fontWeight = FontWeight.Bold,
                    fontSize = 36.sp,
                    fontFeatureSettings = "tnum",
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            Text(
                text = if (timerState.isRunning) "Focusing" else "Ready",
                style = MaterialTheme.typography.labelSmall,
                color = palette.primary
            )
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Button(
            onClick = onToggleRun,
            colors = ButtonDefaults.buttonColors(
                containerColor = palette.primary,
                contentColor = Color.Black
            ),
            modifier = Modifier
                .weight(1f)
                .minimumInteractiveComponentSize()
                .testTag("timer_toggle_button")
        ) {
            Icon(
                imageVector = if (timerState.isRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = null
            )
            Spacer(Modifier.width(6.dp))
            Text(if (timerState.isRunning) "Pause" else "Start", fontWeight = FontWeight.SemiBold)
        }
        OutlinedButton(
            onClick = onReset,
            modifier = Modifier
                .minimumInteractiveComponentSize()
                .testTag("timer_reset_button")
        ) {
            Icon(Icons.Filled.Refresh, contentDescription = "Reset timer")
        }
    }
}

@Composable
private fun StopwatchSection(
    timerState: FocusTimerState,
    settings: ClockSettings,
    palette: ClockPalette,
    onToggle: () -> Unit,
    onLap: () -> Unit,
    onReset: () -> Unit
) {
    val totalMs = timerState.stopwatchMillis
    val mins = (totalMs / 60000L)
    val secs = (totalMs / 1000L) % 60L
    val centis = (totalMs / 10L) % 100L

    Spacer(Modifier.height(6.dp))
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(Icons.Filled.Timer, contentDescription = null, tint = palette.secondary)
        Text(
            text = "%02d:%02d.%02d".format(mins, secs, centis),
            style = TextStyle(
                fontFamily = clockFontFamily(settings.fontStyle),
                fontWeight = FontWeight.Bold,
                fontSize = 42.sp,
                fontFeatureSettings = "tnum",
                brush = Brush.linearGradient(listOf(palette.primary, palette.secondary))
            )
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Button(
            onClick = onToggle,
            colors = ButtonDefaults.buttonColors(
                containerColor = palette.primary,
                contentColor = Color.Black
            ),
            modifier = Modifier
                .weight(1f)
                .minimumInteractiveComponentSize()
                .testTag("stopwatch_toggle_button")
        ) {
            Icon(
                imageVector = if (timerState.stopwatchRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = null
            )
            Spacer(Modifier.width(6.dp))
            Text(if (timerState.stopwatchRunning) "Pause" else "Start", fontWeight = FontWeight.SemiBold)
        }
        OutlinedButton(
            onClick = onLap,
            enabled = timerState.stopwatchMillis > 0L,
            modifier = Modifier
                .minimumInteractiveComponentSize()
                .testTag("stopwatch_lap_button")
        ) {
            Icon(Icons.Filled.Flag, contentDescription = "Record lap")
        }
        OutlinedButton(
            onClick = onReset,
            modifier = Modifier
                .minimumInteractiveComponentSize()
                .testTag("stopwatch_reset_button")
        ) {
            Icon(Icons.Filled.Refresh, contentDescription = "Reset stopwatch")
        }
    }

    if (timerState.laps.isNotEmpty()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            timerState.laps.forEachIndexed { index, lapMs ->
                val lMin = lapMs / 60000L
                val lSec = (lapMs / 1000L) % 60L
                val lCen = (lapMs / 10L) % 100L
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Lap ${timerState.laps.size - index}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "%02d:%02d.%02d".format(lMin, lSec, lCen),
                        style = MaterialTheme.typography.labelLarge,
                        color = palette.secondary
                    )
                }
            }
        }
    }
}
