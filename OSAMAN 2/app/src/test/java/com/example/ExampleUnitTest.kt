package com.example

import com.example.model.ClockFaceStyle
import com.example.model.ClockSettings
import com.example.model.ColorShiftMode
import com.example.model.WallpaperCatalog
import com.example.ui.clock.ColorEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun colorEngineShiftsHuesOverTime() {
    val settings = ClockSettings(colorMode = ColorShiftMode.SPECTRUM_FLOW, shiftSpeed = 1.0f)
    val p1 = ColorEngine.computePalette(0L, settings)
    val p2 = ColorEngine.computePalette(5_000L, settings)
    assertNotEquals(p1.primary, p2.primary)
    assertNotEquals(p1.primary, p1.secondary)
  }

  @Test
  fun wallpaperCatalogContainsBundledArtAndLivePresets() {
    assertTrue(WallpaperCatalog.presets.size >= 5)
    assertEquals("art_nebula", WallpaperCatalog.findById("art_nebula").id)
  }

  @Test
  fun clockFaceStylesIncludeDigitalAnalogMinimalistAndVarieties() {
    val labels = ClockFaceStyle.entries.map { it.label }
    assertTrue(labels.containsAll(listOf("Digital", "Analog", "Minimalist", "Flip", "Orbit", "Chrono")))
    assertEquals(6, ClockFaceStyle.entries.size)
  }
}
