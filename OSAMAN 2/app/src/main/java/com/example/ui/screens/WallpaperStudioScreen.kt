package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.model.ClockSettings
import com.example.model.WallpaperCatalog
import com.example.model.WallpaperPreset
import com.example.model.WallpaperSourceType
import com.example.model.WallpaperTarget
import com.example.ui.ChromaViewModel
import com.example.ui.clock.AdaptiveClockFace
import com.example.ui.clock.ClockPalette
import com.example.ui.clock.WallpaperBackdropLayer
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WallpaperStudioScreen(
    nowMillis: Long,
    settings: ClockSettings,
    palette: ClockPalette,
    viewModel: ChromaViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showApplySheet by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                viewModel.importUserPhotoWallpaper(uri)
            }
        }
    )

    val activePreset = remember(settings.selectedWallpaperId) {
        if (settings.selectedWallpaperId == "user_photo") {
            WallpaperPreset(
                id = "user_photo",
                title = "Your Custom Photo",
                subtitle = "Imported from your gallery",
                type = WallpaperSourceType.USER_PHOTO
            )
        } else {
            WallpaperCatalog.findById(settings.selectedWallpaperId)
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 156.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .widthIn(max = 900.dp)
                .fillMaxSize()
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "Wallpaper Studio",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    // Interactive Live Preview Hero Card
                    Surface(
                        shape = RoundedCornerShape(26.dp),
                        border = BorderStroke(1.5.dp, palette.primary.copy(alpha = 0.45f)),
                        tonalElevation = 4.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            WallpaperBackdropLayer(
                                settings = settings,
                                palette = palette,
                                nowMillis = nowMillis,
                                modifier = Modifier.fillMaxSize()
                            )
                            AdaptiveClockFace(
                                nowMillis = nowMillis,
                                settings = settings.copy(clockScale = 0.9f),
                                palette = palette,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                            )
                            Surface(
                                color = Color.Black.copy(alpha = 0.55f),
                                shape = RoundedCornerShape(50),
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = activePreset.title,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    // Primary Actions Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { showApplySheet = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = palette.primary,
                                contentColor = Color.Black
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .minimumInteractiveComponentSize()
                                .testTag("apply_wallpaper_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Wallpaper,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.action_set_wallpaper),
                                maxLines = 1,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        OutlinedButton(
                            onClick = { viewModel.openLiveWallpaperPicker(context) },
                            border = BorderStroke(1.dp, palette.secondary.copy(alpha = 0.7f)),
                            modifier = Modifier
                                .weight(1f)
                                .minimumInteractiveComponentSize()
                                .testTag("set_live_wallpaper_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AutoAwesome,
                                contentDescription = null,
                                tint = palette.secondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Live Aura",
                                maxLines = 1,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Live Wallpaper clock toggle + Custom Photo Picker
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SettingToggleRow(
                                title = "Show Floating Clock on Live Wallpaper",
                                subtitle = "Renders the color-shifting clock directly onto the Live Aura wallpaper",
                                checked = settings.showClockOnLiveWallpaper,
                                onCheckedChange = viewModel::setShowClockOnLiveWallpaper,
                                tag = "toggle_live_wallpaper_clock"
                            )
                            OutlinedButton(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .minimumInteractiveComponentSize()
                                    .testTag("pick_photo_wallpaper_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PhotoLibrary,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Choose Photo from Device")
                            }
                        }
                    }
                }
            }

            if (!settings.customPhotoPath.isNullOrBlank()) {
                item {
                    val isSelected = settings.selectedWallpaperId == "user_photo"
                    WallpaperGridCard(
                        title = "Your Photo",
                        subtitle = "Custom gallery image",
                        selected = isSelected,
                        accentColor = palette.primary,
                        onClick = { viewModel.selectWallpaper("user_photo") },
                        tag = "wallpaper_card_user_photo"
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(File(settings.customPhotoPath))
                                .build(),
                            contentDescription = "Your custom wallpaper photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            items(WallpaperCatalog.presets, key = { it.id }) { preset ->
                val isSelected = settings.selectedWallpaperId == preset.id
                WallpaperGridCard(
                    title = preset.title,
                    subtitle = preset.subtitle,
                    selected = isSelected,
                    accentColor = palette.primary,
                    onClick = { viewModel.selectWallpaper(preset.id) },
                    tag = "wallpaper_card_${preset.id}"
                ) {
                    if (preset.type == WallpaperSourceType.BUNDLED_ART && preset.drawableRes != null) {
                        Image(
                            painter = painterResource(id = preset.drawableRes),
                            contentDescription = preset.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        val stops = if (preset.baseColors.size >= 2) {
                            preset.baseColors.map { Color(it) }
                        } else {
                            listOf(palette.subtleBgTint, palette.primary.copy(alpha = 0.4f))
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.linearGradient(stops + palette.primary.copy(alpha = 0.35f))
                                )
                        )
                    }
                }
            }
        }
    }

    if (showApplySheet) {
        ModalBottomSheet(
            onDismissRequest = { showApplySheet = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Set \"${activePreset.title}\" As Wallpaper",
                    style = MaterialTheme.typography.titleLarge
                )
                WallpaperTargetRow(
                    icon = Icons.Filled.Home,
                    title = "Home Screen",
                    accent = palette.primary,
                    tag = "target_home",
                    onClick = {
                        showApplySheet = false
                        viewModel.applyWallpaper(activePreset.id, WallpaperTarget.HOME)
                    }
                )
                WallpaperTargetRow(
                    icon = Icons.Filled.Lock,
                    title = "Lock Screen",
                    accent = palette.secondary,
                    tag = "target_lock",
                    onClick = {
                        showApplySheet = false
                        viewModel.applyWallpaper(activePreset.id, WallpaperTarget.LOCK)
                    }
                )
                WallpaperTargetRow(
                    icon = Icons.Filled.Smartphone,
                    title = "Home & Lock Screens",
                    accent = palette.primary,
                    tag = "target_both",
                    onClick = {
                        showApplySheet = false
                        viewModel.applyWallpaper(activePreset.id, WallpaperTarget.BOTH)
                    }
                )
            }
        }
    }
}

@Composable
private fun WallpaperGridCard(
    title: String,
    subtitle: String,
    selected: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    tag: String,
    preview: @Composable () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(
            width = if (selected) 2.5.dp else 1.dp,
            color = if (selected) accentColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.74f)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .testTag(tag)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            preview()
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.80f))
                        )
                    )
            )
            if (selected) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "Selected wallpaper",
                    tint = accentColor,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .size(24.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                )
            }
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    maxLines = 1
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.78f),
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
private fun WallpaperTargetRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    accent: Color,
    tag: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .minimumInteractiveComponentSize()
            .testTag(tag)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = accent)
            Text(title, style = MaterialTheme.typography.titleMedium)
        }
    }
}
