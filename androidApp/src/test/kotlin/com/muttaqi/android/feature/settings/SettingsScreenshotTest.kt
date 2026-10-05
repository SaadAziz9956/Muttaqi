package com.muttaqi.android.feature.settings

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import com.muttaqi.android.designsystem.ColorSource
import com.muttaqi.android.testing.PHONE
import com.muttaqi.android.testing.captureLightAndDark
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(application = Application::class, sdk = [35], qualifiers = "en-rPK-$PHONE")
class SettingsScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun colors() = compose.captureLightAndDark("settings") {
        SettingsScreen(ColorSource.Wallpaper, onColorSource = {}, onBack = {}, wallpaperAvailable = true)
    }

    @Test
    fun colorsBeforeAndroid12() = compose.captureLightAndDark("settings_no_wallpaper_colors") {
        SettingsScreen(ColorSource.Wallpaper, onColorSource = {}, onBack = {}, wallpaperAvailable = false)
    }
}
