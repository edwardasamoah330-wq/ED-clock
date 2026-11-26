package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.os.BatteryManager
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.R
import com.example.model.ClockSettings
import com.example.ui.ChromaViewModel
import com.example.ui.clock.AdaptiveClockFace
import com.example.ui.clock.ClockPalette

@Composable
fun ScreensaverHubScreen(
    nowMillis: Long,
    settings: ClockSettings,
    palette: ClockPalette,
    viewModel: ChromaViewModel,
    onLaunchScreensaver: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 720.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Screensaver Studio",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            // OLED Standby Live Preview
            Surface(
                shape = RoundedCornerShape(26.dp),
                color = Color.Black,
                border = BorderStroke(1.5.dp, palette.primary.copy(alpha = 0.45f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AdaptiveClockFace(
                        nowMillis = nowMillis,
                        settings = settings.copy(clockScale = 0.85f),
                        palette = palette,
                        isScreensaver = true,
                        dimAlpha = settings.screensaverDimLevel,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Bedtime,
                                contentDescription = null,
                                tint = palette.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "OLED Nightstand Preview",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }

            // Launch Buttons
            Button(
                onClick = onLaunchScreensaver,
                colors = ButtonDefaults.buttonColors(
                    containerColor = palette.primary,
                    contentColor = Color.Black
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .minimumInteractiveComponentSize()
                    .testTag("launch_standby_screensaver_button")
            ) {
                Icon(imageVector = Icons.Filled.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.action_launch_screensaver),
                    fontWeight = FontWeight.SemiBold
                )
            }

            OutlinedButton(
                onClick = { viewModel.openSystemScreensaverSettings(context) },
                border = BorderStroke(1.dp, palette.secondary.copy(alpha = 0.65f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .minimumInteractiveComponentSize()
                    .testTag("system_screensaver_settings_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = null,
                    tint = palette.secondary
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.action_system_screensaver),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Settings Card
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Standby & DreamService Options", style = MaterialTheme.typography.titleMedium)

                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Nightstand Brightness", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                text = "${(settings.screensaverDimLevel * 100).toInt()}%",
                                style = MaterialTheme.typography.labelLarge,
                                color = palette.primary
                            )
                        }
                        Slider(
                            value = settings.screensaverDimLevel,
                            onValueChange = viewModel::setScreensaverDim,
                            valueRange = 0.25f..1.0f,
                            colors = SliderDefaults.colors(
                                thumbColor = palette.primary,
                                activeTrackColor = palette.primary
                            ),
                            modifier = Modifier.testTag("screensaver_dim_slider")
                        )
                    }

                    SettingToggleRow(
                        title = "OLED Burn-in Pixel Drift",
                        subtitle = "Gently shifts the clock position over time to protect OLED displays",
                        checked = settings.screensaverBurnInProtect,
                        onCheckedChange = viewModel::setBurnInProtect,
                        tag = "toggle_burn_in_protect"
                    )

                    SettingToggleRow(
                        title = "Keep Screen Awake in Standby",
                        subtitle = "Prevents screen timeout while in-app screensaver is active",
                        checked = settings.screensaverKeepAwake,
                        onCheckedChange = viewModel::setScreensaverKeepAwake,
                        tag = "toggle_keep_awake"
                    )

                    SettingToggleRow(
                        title = "Show Battery Status",
                        subtitle = "Displays live battery percentage below the standby clock",
                        checked = settings.showBatteryInScreensaver,
                        onCheckedChange = viewModel::setShowBatteryInScreensaver,
                        tag = "toggle_battery_screensaver"
                    )
                }
            }
        }
    }
}

@Composable
fun ImmersiveScreensaverOverlay(
    nowMillis: Long,
    settings: ClockSettings,
    palette: ClockPalette,
    onExit: () -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current
    var showOverlayHud by remember { mutableStateOf(true) }

    BackHandler {
        onExit()
    }

    DisposableEffect(settings.screensaverKeepAwake) {
        val window = (context as? Activity)?.window
        if (window != null) {
            if (settings.screensaverKeepAwake) {
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
            val controller = WindowCompat.getInsetsController(window, view)
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller.hide(WindowInsetsCompat.Type.systemBars())
        }
        onDispose {
            if (window != null) {
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                val controller = WindowCompat.getInsetsController(window, view)
                controller.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    val batteryPct = remember(nowMillis / 15000L) {
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: -1
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { showOverlayHud = !showOverlayHud }
            .testTag("immersive_screensaver_root")
    ) {
        AdaptiveClockFace(
            nowMillis = nowMillis,
            settings = settings,
            palette = palette,
            isScreensaver = true,
            dimAlpha = settings.screensaverDimLevel,
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        )

        if (settings.showBatteryInScreensaver && batteryPct in 0..100) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.BatteryChargingFull,
                    contentDescription = "Battery percentage",
                    tint = palette.primary.copy(alpha = settings.screensaverDimLevel * 0.8f),
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "$batteryPct%",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = settings.screensaverDimLevel * 0.72f)
                )
            }
        }

        AnimatedVisibility(
            visible = showOverlayHud,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(24.dp)
        ) {
            Button(
                onClick = onExit,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("exit_screensaver_button")
            ) {
                Icon(imageVector = Icons.Filled.Close, contentDescription = "Exit screensaver")
                Spacer(Modifier.width(6.dp))
                Text("Exit Standby")
            }
        }
    }
}
