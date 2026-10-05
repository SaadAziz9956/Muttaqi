package com.muttaqi.android.designsystem

import com.russhwolf.settings.ObservableSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ColorPreference(private val settings: ObservableSettings) {
    private val current = MutableStateFlow(read())

    val source: StateFlow<ColorSource> = current.asStateFlow()

    fun choose(source: ColorSource) {
        settings.putString(KEY, source.name)
        current.value = source
    }

    private fun read(): ColorSource =
        settings.getStringOrNull(KEY)?.let { saved -> ColorSource.entries.firstOrNull { it.name == saved } }
            ?: if (wallpaperColorsAvailable) ColorSource.Wallpaper else ColorSource.MuttaqiGreen

    private companion object {
        const val KEY = "android_color_source"
    }
}
