package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.ClockFaceStyle
import com.example.model.ClockSettings
import com.example.ui.clock.AdaptiveClockFace
import com.example.ui.clock.ClockPalette
import com.example.ui.clock.WallpaperBackdropLayer

@Composable
fun ClockFacePickerSheet(
    nowMillis: Long,
    settings: ClockSettings,
    palette: ClockPalette,
    onSelectFace: (ClockFaceStyle) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Choose Clock Face",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Tap any design below to switch instantly, or swipe left and right directly on the clock.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(440.dp)
        ) {
            items(ClockFaceStyle.entries, key = { it.name }) { face ->
                val isSelected = settings.clockFace == face
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    border = BorderStroke(
                        width = if (isSelected) 2.5.dp else 1.dp,
                        color = if (isSelected) palette.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    ),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(0.95f)
                        .clip(RoundedCornerShape(22.dp))
                        .clickable { onSelectFace(face) }
                        .testTag("gallery_face_${face.name.lowercase()}")
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        WallpaperBackdropLayer(
                            settings = settings,
                            palette = palette,
                            nowMillis = nowMillis,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Miniature live preview of this clock face
                        AdaptiveClockFace(
                            nowMillis = nowMillis,
                            settings = settings.copy(
                                clockFace = face,
                                clockScale = 0.92f,
                                showDate = false
                            ),
                            palette = palette,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 10.dp, vertical = 22.dp)
                        )

                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = "Selected clock face",
                                tint = palette.primary,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(10.dp)
                                    .size(22.dp)
                                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                            )
                        }

                        Surface(
                            color = Color.Black.copy(alpha = 0.68f),
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = face.label,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isSelected) palette.primary else Color.White,
                                        maxLines = 1
                                    )
                                }
                                Spacer(modifier = Modifier.height(1.dp))
                                Text(
                                    text = face.subtitle,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.72f),
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
