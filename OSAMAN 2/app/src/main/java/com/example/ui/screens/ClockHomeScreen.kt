package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddToHomeScreen
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.WatchLater
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.ClockFaceStyle
import com.example.model.ClockFontStyle
import com.example.model.ClockSettings
import com.example.model.ColorShiftMode
import com.example.ui.ChromaViewModel
import com.example.ui.clock.AdaptiveClockFace
import com.example.ui.clock.ClockPalette
import com.example.ui.clock.WallpaperBackdropLayer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClockHomeScreen(
    nowMillis: Long,
    settings: ClockSettings,
    palette: ClockPalette,
    viewModel: ChromaViewModel,
    onLaunchScreensaver: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var zenMode by remember { mutableStateOf(false) }
    var showCustomizeSheet by remember { mutableStateOf(false) }
    var showWidgetSheet by remember { mutableStateOf(false) }
    var showFaceGallerySheet by remember { mutableStateOf(false) }

    val faces = ClockFaceStyle.entries
    val pagerState = rememberPagerState(
        initialPage = faces.indexOf(settings.clockFace).coerceAtLeast(0),
        pageCount = { faces.size }
    )

    LaunchedEffect(settings.clockFace) {
        val targetIdx = faces.indexOf(settings.clockFace)
        if (targetIdx >= 0 && pagerState.currentPage != targetIdx && !pagerState.isScrollInProgress) {
            pagerState.animateScrollToPage(targetIdx)
        }
    }

    LaunchedEffect(pagerState.settledPage) {
        val pagedFace = faces[pagerState.settledPage]
        if (pagedFace != settings.clockFace) {
            viewModel.setClockFace(pagedFace)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        WallpaperBackdropLayer(
            settings = settings,
            palette = palette,
            nowMillis = nowMillis,
            modifier = Modifier.fillMaxSize()
        )

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            val isWide = maxWidth > 680.dp && maxWidth > maxHeight

            if (isWide) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1.25f)
                            .fillMaxHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { zenMode = !zenMode }
                                .testTag("clock_face_pager")
                        ) { page ->
                            AdaptiveClockFace(
                                nowMillis = nowMillis,
                                settings = settings.copy(clockFace = faces[page]),
                                palette = palette,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        AnimatedVisibility(visible = !zenMode, enter = fadeIn(), exit = fadeOut()) {
                            FacePagerDots(
                                total = faces.size,
                                current = pagerState.currentPage,
                                activeColor = palette.primary,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = !zenMode,
                        enter = fadeIn(),
                        exit = fadeOut(),
                        modifier = Modifier
                            .weight(0.95f)
                            .fillMaxHeight()
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            ControlDeckCard(
                                settings = settings,
                                palette = palette,
                                onSelectFace = viewModel::setClockFace,
                                onOpenFaceGallery = { showFaceGallerySheet = true },
                                onSelectColorMode = viewModel::setColorMode,
                                onOpenWidgetSheet = { showWidgetSheet = true },
                                onLaunchScreensaver = onLaunchScreensaver,
                                onOpenCustomize = { showCustomizeSheet = true },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Top bar with clickable Style Gallery pill & Zen toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AnimatedVisibility(visible = !zenMode, enter = fadeIn(), exit = fadeOut()) {
                            Surface(
                                onClick = { showFaceGallerySheet = true },
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.68f),
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(1.dp, palette.primary.copy(alpha = 0.40f)),
                                modifier = Modifier.testTag("top_face_gallery_pill")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(
                                                Brush.linearGradient(listOf(palette.primary, palette.secondary))
                                            )
                                    )
                                    Text(
                                        text = "${settings.colorMode.label} • ${settings.clockFace.label}",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Icon(
                                        imageVector = Icons.Filled.GridView,
                                        contentDescription = "Open clock face gallery",
                                        tint = palette.primary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }

                        IconButton(
                            onClick = { zenMode = !zenMode },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("zen_mode_button")
                        ) {
                            Icon(
                                imageVector = if (zenMode) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = if (zenMode) "Show clock controls" else "Hide clock controls",
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                            )
                        }
                    }

                    // Swipeable Adaptive Clock Stage
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { zenMode = !zenMode }
                            .testTag("clock_face_pager")
                    ) { page ->
                        AdaptiveClockFace(
                            nowMillis = nowMillis,
                            settings = settings.copy(clockFace = faces[page]),
                            palette = palette,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    AnimatedVisibility(visible = !zenMode, enter = fadeIn(), exit = fadeOut()) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            FacePagerDots(
                                total = faces.size,
                                current = pagerState.currentPage,
                                activeColor = palette.primary
                            )
                            ControlDeckCard(
                                settings = settings,
                                palette = palette,
                                onSelectFace = viewModel::setClockFace,
                                onOpenFaceGallery = { showFaceGallerySheet = true },
                                onSelectColorMode = viewModel::setColorMode,
                                onOpenWidgetSheet = { showWidgetSheet = true },
                                onLaunchScreensaver = onLaunchScreensaver,
                                onOpenCustomize = { showCustomizeSheet = true },
                                modifier = Modifier
                                    .widthIn(max = 600.dp)
                                    .fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }

    if (showFaceGallerySheet) {
        ModalBottomSheet(
            onDismissRequest = { showFaceGallerySheet = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            ClockFacePickerSheet(
                nowMillis = nowMillis,
                settings = settings,
                palette = palette,
                onSelectFace = { selected ->
                    viewModel.setClockFace(selected)
                    showFaceGallerySheet = false
                }
            )
        }
    }

    if (showCustomizeSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCustomizeSheet = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            CustomizeClockSheet(
                settings = settings,
                palette = palette,
                viewModel = viewModel,
                onClose = { showCustomizeSheet = false }
            )
        }
    }

    if (showWidgetSheet) {
        ModalBottomSheet(
            onDismissRequest = { showWidgetSheet = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            AddWidgetSheet(
                palette = palette,
                onPinDigital = {
                    showWidgetSheet = false
                    viewModel.requestPinWidget(context, isAnalog = false)
                },
                onPinAnalog = {
                    showWidgetSheet = false
                    viewModel.requestPinWidget(context, isAnalog = true)
                }
            )
        }
    }
}

@Composable
private fun FacePagerDots(
    total: Int,
    current: Int,
    activeColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(total) { index ->
            val isSelected = index == current
            Box(
                modifier = Modifier
                    .height(6.dp)
                    .width(if (isSelected) 18.dp else 6.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) activeColor else Color.White.copy(alpha = 0.32f)
                    )
            )
        }
    }
}

@Composable
private fun ControlDeckCard(
    settings: ClockSettings,
    palette: ClockPalette,
    onSelectFace: (ClockFaceStyle) -> Unit,
    onOpenFaceGallery: () -> Unit,
    onSelectColorMode: (ColorShiftMode) -> Unit,
    onOpenWidgetSheet: () -> Unit,
    onLaunchScreensaver: () -> Unit,
    onOpenCustomize: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.78f),
        shape = RoundedCornerShape(26.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        tonalElevation = 6.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Clock face chips + Visual Face Gallery trigger
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = false,
                    onClick = onOpenFaceGallery,
                    label = { Text("Faces") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.GridView,
                            contentDescription = "Open clock face gallery",
                            tint = palette.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    modifier = Modifier.testTag("open_face_gallery_chip")
                )

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ClockFaceStyle.entries.forEach { face ->
                        val selected = settings.clockFace == face
                        FilterChip(
                            selected = selected,
                            onClick = { onSelectFace(face) },
                            label = { Text(face.label) },
                            leadingIcon = if (selected) {
                                {
                                    Icon(
                                        imageVector = Icons.Filled.Schedule,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = palette.primary.copy(alpha = 0.22f),
                                selectedLabelColor = MaterialTheme.colorScheme.onSurface,
                                selectedLeadingIconColor = palette.primary
                            ),
                            modifier = Modifier.testTag("face_chip_${face.name.lowercase()}")
                        )
                    }
                }
            }

            // Color mode chips
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ColorShiftMode.entries.forEach { mode ->
                    val selected = settings.colorMode == mode
                    FilterChip(
                        selected = selected,
                        onClick = { onSelectColorMode(mode) },
                        label = { Text(mode.label) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Palette,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp),
                                tint = if (selected) palette.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = palette.secondary.copy(alpha = 0.20f),
                            selectedLabelColor = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.testTag("color_chip_${mode.name.lowercase()}")
                    )
                }
            }

            // Quick Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onOpenWidgetSheet,
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .testTag("pin_widget_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = palette.primary,
                        contentColor = Color.Black
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.AddToHomeScreen,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Add to Screen",
                        maxLines = 1,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                OutlinedButton(
                    onClick = onLaunchScreensaver,
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .testTag("quick_screensaver_button"),
                    border = BorderStroke(1.dp, palette.secondary.copy(alpha = 0.65f)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Bedtime,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = palette.secondary
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Screensaver",
                        maxLines = 1,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(
                    onClick = onOpenCustomize,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .testTag("customize_clock_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Tune,
                        contentDescription = "Customize clock details",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun CustomizeClockSheet(
    settings: ClockSettings,
    palette: ClockPalette,
    viewModel: ChromaViewModel,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Customize Clock",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Font selector
        Text(
            text = "Typography",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ClockFontStyle.entries.forEach { font ->
                FilterChip(
                    selected = settings.fontStyle == font,
                    onClick = { viewModel.setFontStyle(font) },
                    label = { Text(font.label) },
                    modifier = Modifier.testTag("font_chip_${font.name.lowercase()}")
                )
            }
        }

        // Color Shift Speed
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Color Shift Speed", style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = "%.1fx".format(settings.shiftSpeed),
                    style = MaterialTheme.typography.labelLarge,
                    color = palette.primary
                )
            }
            Slider(
                value = settings.shiftSpeed,
                onValueChange = viewModel::setShiftSpeed,
                valueRange = 0.2f..3.0f,
                colors = SliderDefaults.colors(
                    thumbColor = palette.primary,
                    activeTrackColor = palette.primary
                ),
                modifier = Modifier.testTag("speed_slider")
            )
        }

        // Custom Hue picker bar
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Signature Hue (Custom Mode)", style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = "${settings.customHue.toInt()}°",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.hsl(settings.customHue, 0.88f, 0.64f)
                )
            }
            Box(contentAlignment = Alignment.Center) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(50))
                ) {
                    val spectrum = (0..360 step 30).map { h -> Color.hsl(h.toFloat(), 0.9f, 0.6f) }
                    drawRect(brush = Brush.horizontalGradient(spectrum))
                }
                Slider(
                    value = settings.customHue,
                    onValueChange = viewModel::setCustomHue,
                    valueRange = 0f..360f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color.hsl(settings.customHue, 0.9f, 0.65f),
                        activeTrackColor = Color.Transparent,
                        inactiveTrackColor = Color.Transparent
                    ),
                    modifier = Modifier.testTag("hue_slider")
                )
            }
        }

        // Clock Size Scale
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Display Fit Scale", style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = "${(settings.clockScale * 100).toInt()}%",
                    style = MaterialTheme.typography.labelLarge,
                    color = palette.secondary
                )
            }
            Slider(
                value = settings.clockScale,
                onValueChange = viewModel::setClockScale,
                valueRange = 0.75f..1.25f,
                colors = SliderDefaults.colors(
                    thumbColor = palette.secondary,
                    activeTrackColor = palette.secondary
                ),
                modifier = Modifier.testTag("scale_slider")
            )
        }

        // Toggles
        SettingToggleRow(
            title = "24-Hour Time",
            checked = settings.use24Hour,
            onCheckedChange = viewModel::setUse24Hour,
            tag = "toggle_24h"
        )
        SettingToggleRow(
            title = "Show Seconds",
            checked = settings.showSeconds,
            onCheckedChange = viewModel::setShowSeconds,
            tag = "toggle_seconds"
        )
        SettingToggleRow(
            title = "Show Date Pill",
            checked = settings.showDate,
            onCheckedChange = viewModel::setShowDate,
            tag = "toggle_date"
        )

        Button(
            onClick = onClose,
            modifier = Modifier
                .fillMaxWidth()
                .minimumInteractiveComponentSize()
                .testTag("close_customize_button")
        ) {
            Text("Done")
        }
    }
}

@Composable
private fun AddWidgetSheet(
    palette: ClockPalette,
    onPinDigital: () -> Unit,
    onPinAnalog: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.action_add_widget),
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = "Both widgets automatically resize and fit any home screen grid while syncing with your active color mode.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Surface(
            onClick = onPinDigital,
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            border = BorderStroke(1.dp, palette.primary.copy(alpha = 0.45f)),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("pin_digital_widget_card")
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Schedule,
                    contentDescription = null,
                    tint = palette.primary,
                    modifier = Modifier.size(32.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text("Digital Prism Widget", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Resizable 3x2 adaptive digital readout with color-tinted frame.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Surface(
            onClick = onPinAnalog,
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            border = BorderStroke(1.dp, palette.secondary.copy(alpha = 0.45f)),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("pin_analog_widget_card")
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.WatchLater,
                    contentDescription = null,
                    tint = palette.secondary,
                    modifier = Modifier.size(32.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text("Analog Aura Widget", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Resizable 2x2 minimalist dial that adapts cleanly to any space.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun SettingToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    tag: String,
    subtitle: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .minimumInteractiveComponentSize()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp)
            .testTag(tag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
