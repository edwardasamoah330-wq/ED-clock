package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.ClockSettings
import com.example.model.ColorShiftMode
import com.example.model.WallpaperCatalog
import com.example.ui.clock.ColorEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read app name from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("ChromaClock", appName)
  }

  @Test
  fun `color engine shifts hues over time`() {
    val settings = ClockSettings(colorMode = ColorShiftMode.SPECTRUM_FLOW, shiftSpeed = 1.0f)
    val p1 = ColorEngine.computePalette(0L, settings)
    val p2 = ColorEngine.computePalette(5_000L, settings)
    assertNotEquals(p1.primary, p2.primary)
    assertNotEquals(p1.primary, p1.secondary)
  }

  @Test
  fun `wallpaper catalog contains bundled art and live presets`() {
    assertTrue(WallpaperCatalog.presets.size >= 5)
    assertEquals("art_nebula", WallpaperCatalog.findById("art_nebula").id)
  }
}
