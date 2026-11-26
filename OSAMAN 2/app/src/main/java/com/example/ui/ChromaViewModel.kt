package com.example.ui

import android.app.Application
import android.app.PendingIntent
import android.app.WallpaperManager
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.SettingsRepository
import com.example.model.ClockFaceStyle
import com.example.model.ClockFontStyle
import com.example.model.ClockSettings
import com.example.model.ColorShiftMode
import com.example.model.WallpaperCatalog
import com.example.model.WallpaperPreset
import com.example.model.WallpaperSourceType
import com.example.model.WallpaperTarget
import com.example.service.ChromaLiveWallpaperService
import com.example.widget.ChromaAnalogWidgetProvider
import com.example.widget.ChromaClockWidgetProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

sealed interface ActionFeedback {
    data object Idle : ActionFeedback
    data object Working : ActionFeedback
    data class Success(val message: String) : ActionFeedback
    data class Error(val message: String) : ActionFeedback
}

data class FocusTimerState(
    val presetMinutes: Int = 25,
    val totalSeconds: Int = 25 * 60,
    val remainingSeconds: Int = 25 * 60,
    val isRunning: Boolean = false,
    val stopwatchMillis: Long = 0L,
    val stopwatchRunning: Boolean = false,
    val laps: List<Long> = emptyList()
)

class ChromaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SettingsRepository(application.applicationContext)

    val settings: StateFlow<ClockSettings> = repository.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ClockSettings()
    )

    private val _feedback = MutableStateFlow<ActionFeedback>(ActionFeedback.Idle)
    val feedback: StateFlow<ActionFeedback> = _feedback.asStateFlow()

    private val _timerState = MutableStateFlow(FocusTimerState())
    val timerState: StateFlow<FocusTimerState> = _timerState.asStateFlow()

    private var timerJob: Job? = null
    private var stopwatchJob: Job? = null

    fun clearFeedback() {
        _feedback.value = ActionFeedback.Idle
    }

    fun setClockFace(face: ClockFaceStyle) = update { it.copy(clockFace = face) }
    fun setColorMode(mode: ColorShiftMode) = update { it.copy(colorMode = mode) }
    fun setShiftSpeed(speed: Float) = update { it.copy(shiftSpeed = speed.coerceIn(0.2f, 3.0f)) }
    fun setCustomHue(hue: Float) = update { it.copy(customHue = hue.coerceIn(0f, 360f), colorMode = ColorShiftMode.CUSTOM_FIXED) }
    fun setFontStyle(font: ClockFontStyle) = update { it.copy(fontStyle = font) }
    fun setUse24Hour(use24: Boolean) = update { it.copy(use24Hour = use24) }
    fun setShowSeconds(show: Boolean) = update { it.copy(showSeconds = show) }
    fun setShowDate(show: Boolean) = update { it.copy(showDate = show) }
    fun setClockScale(scale: Float) = update { it.copy(clockScale = scale.coerceIn(0.75f, 1.25f)) }
    fun selectWallpaper(id: String) = update { it.copy(selectedWallpaperId = id) }
    fun setBurnInProtect(enabled: Boolean) = update { it.copy(screensaverBurnInProtect = enabled) }
    fun setScreensaverDim(level: Float) = update { it.copy(screensaverDimLevel = level.coerceIn(0.25f, 1f)) }
    fun setScreensaverKeepAwake(keepAwake: Boolean) = update { it.copy(screensaverKeepAwake = keepAwake) }
    fun setShowBatteryInScreensaver(show: Boolean) = update { it.copy(showBatteryInScreensaver = show) }
    fun setShowClockOnLiveWallpaper(show: Boolean) = update { it.copy(showClockOnLiveWallpaper = show) }

    fun toggleWorldCity(cityId: String) = update { current ->
        val next = current.pinnedWorldCities.toMutableSet()
        if (next.contains(cityId)) {
            if (next.size > 1) next.remove(cityId)
        } else {
            next.add(cityId)
        }
        current.copy(pinnedWorldCities = next)
    }

    fun importUserPhotoWallpaper(uri: Uri) {
        viewModelScope.launch {
            _feedback.value = ActionFeedback.Working
            val result = repository.importCustomWallpaperPhoto(uri)
            result.fold(
                onSuccess = { path ->
                    repository.updateSettings {
                        it.copy(
                            selectedWallpaperId = "user_photo",
                            customPhotoPath = path
                        )
                    }
                    _feedback.value = ActionFeedback.Success("Custom photo loaded! Tap Apply Wallpaper to set it.")
                },
                onFailure = { err ->
                    _feedback.value = ActionFeedback.Error(err.localizedMessage ?: "Failed to load image.")
                }
            )
        }
    }

    fun applyWallpaper(presetId: String, target: WallpaperTarget) {
        viewModelScope.launch {
            _feedback.value = ActionFeedback.Working
            val current = settings.value
            val preset = if (presetId == "user_photo") {
                WallpaperPreset(
                    id = "user_photo",
                    title = "Your Photo",
                    subtitle = "Imported from device",
                    type = WallpaperSourceType.USER_PHOTO
                )
            } else {
                WallpaperCatalog.findById(presetId)
            }
            repository.updateSettings { it.copy(selectedWallpaperId = presetId) }
            val result = repository.applySystemWallpaper(
                preset = preset,
                customPhotoPath = current.customPhotoPath,
                target = target,
                settings = current
            )
            _feedback.value = result.fold(
                onSuccess = { ActionFeedback.Success("Applied ${preset.title} to ${target.label}") },
                onFailure = { err -> ActionFeedback.Error(err.localizedMessage ?: "Unable to set wallpaper") }
            )
        }
    }

    fun requestPinWidget(context: Context, isAnalog: Boolean) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            _feedback.value = ActionFeedback.Success("Long-press your home screen and select Widgets -> ChromaClock.")
            return
        }
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val provider = ComponentName(
            context,
            if (isAnalog) ChromaAnalogWidgetProvider::class.java else ChromaClockWidgetProvider::class.java
        )
        if (appWidgetManager.isRequestPinAppWidgetSupported) {
            val callback = PendingIntent.getActivity(
                context,
                if (isAnalog) 202 else 201,
                Intent(context, com.example.MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val launched = appWidgetManager.requestPinAppWidget(provider, null, callback)
            if (!launched) {
                _feedback.value = ActionFeedback.Success("Long-press your home screen and choose Widgets -> ChromaClock.")
            }
        } else {
            _feedback.value = ActionFeedback.Success("Your launcher uses the widget drawer: long-press home screen -> Widgets -> ChromaClock.")
        }
    }

    fun openSystemScreensaverSettings(context: Context) {
        val dreamIntent = Intent(Settings.ACTION_DREAM_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching {
            context.startActivity(dreamIntent)
        }.onFailure {
            runCatching {
                context.startActivity(Intent(Settings.ACTION_DISPLAY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }.onFailure {
                _feedback.value = ActionFeedback.Error("Open Android Settings -> Display -> Screen saver to choose ChromaClock.")
            }
        }
    }

    fun openLiveWallpaperPicker(context: Context) {
        val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
            putExtra(
                WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                ComponentName(context, ChromaLiveWallpaperService::class.java)
            )
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching {
            context.startActivity(intent)
        }.onFailure {
            runCatching {
                context.startActivity(
                    Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }.onFailure {
                _feedback.value = ActionFeedback.Error("Live Wallpaper chooser is unavailable on this device.")
            }
        }
    }

    // Focus Timer & Stopwatch controls
    fun selectTimerPreset(minutes: Int) {
        timerJob?.cancel()
        val secs = minutes * 60
        _timerState.update {
            it.copy(
                presetMinutes = minutes,
                totalSeconds = secs,
                remainingSeconds = secs,
                isRunning = false
            )
        }
    }

    fun toggleTimerRunning() {
        val current = _timerState.value
        if (current.isRunning) {
            timerJob?.cancel()
            _timerState.update { it.copy(isRunning = false) }
        } else {
            val startRem = if (current.remainingSeconds <= 0) current.totalSeconds else current.remainingSeconds
            _timerState.update { it.copy(remainingSeconds = startRem, isRunning = true) }
            timerJob?.cancel()
            timerJob = viewModelScope.launch {
                while (isActive && _timerState.value.remainingSeconds > 0) {
                    delay(1000L)
                    _timerState.update { state ->
                        val next = (state.remainingSeconds - 1).coerceAtLeast(0)
                        state.copy(
                            remainingSeconds = next,
                            isRunning = next > 0
                        )
                    }
                }
            }
        }
    }

    fun resetTimer() {
        timerJob?.cancel()
        _timerState.update {
            it.copy(remainingSeconds = it.totalSeconds, isRunning = false)
        }
    }

    fun toggleStopwatch() {
        val current = _timerState.value
        if (current.stopwatchRunning) {
            stopwatchJob?.cancel()
            _timerState.update { it.copy(stopwatchRunning = false) }
        } else {
            val baseElapsed = current.stopwatchMillis
            val startMark = System.currentTimeMillis()
            _timerState.update { it.copy(stopwatchRunning = true) }
            stopwatchJob?.cancel()
            stopwatchJob = viewModelScope.launch {
                while (isActive) {
                    delay(45L)
                    val elapsed = baseElapsed + (System.currentTimeMillis() - startMark)
                    _timerState.update { it.copy(stopwatchMillis = elapsed) }
                }
            }
        }
    }

    fun lapStopwatch() {
        val current = _timerState.value
        if (current.stopwatchMillis > 0L) {
            _timerState.update { it.copy(laps = (listOf(current.stopwatchMillis) + it.laps).take(12)) }
        }
    }

    fun resetStopwatch() {
        stopwatchJob?.cancel()
        _timerState.update { it.copy(stopwatchMillis = 0L, stopwatchRunning = false, laps = emptyList()) }
    }

    private fun update(transform: (ClockSettings) -> ClockSettings) {
        viewModelScope.launch {
            repository.updateSettings(transform)
        }
    }
}
