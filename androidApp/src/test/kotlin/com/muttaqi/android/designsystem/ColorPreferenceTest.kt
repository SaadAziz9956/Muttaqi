package com.muttaqi.android.designsystem

import com.russhwolf.settings.MapSettings
import org.junit.Assert.assertEquals
import org.junit.Test

class ColorPreferenceTest {
    @Test
    fun theChosenColorsAreKeptAcrossLaunches() {
        val settings = MapSettings()
        ColorPreference(settings).choose(ColorSource.MuttaqiGreen)
        assertEquals(ColorSource.MuttaqiGreen, ColorPreference(settings).source.value)
        ColorPreference(settings).choose(ColorSource.Wallpaper)
        assertEquals(ColorSource.Wallpaper, ColorPreference(settings).source.value)
    }

    @Test
    fun anUnknownSavedChoiceFallsBackToTheDefault() {
        val settings = MapSettings("android_color_source" to "Purple")
        val expected = if (wallpaperColorsAvailable) ColorSource.Wallpaper else ColorSource.MuttaqiGreen
        assertEquals(expected, ColorPreference(settings).source.value)
    }
}
