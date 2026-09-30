package com.muttaqi.android.testing

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import com.muttaqi.android.designsystem.MuttaqiTheme

/**
 * Renders [content] in light and dark and saves both as PNGs under androidApp/screenshots/, so a screen can be checked
 * without an emulator. Run `./gradlew :androidApp:recordRoborazziDebug` and open the images.
 * Test classes use `@Config(application = android.app.Application::class, sdk = [35], qualifiers = PHONE)`, so the
 * app's own Application (which starts Koin) isn't created for every test.
 */
fun ComposeContentTestRule.captureLightAndDark(name: String, content: @Composable () -> Unit) {
    var dark by mutableStateOf(false)
    setContent { MuttaqiTheme(darkTheme = dark) { content() } }
    onRoot().captureRoboImage("screenshots/$name.png")
    dark = true
    waitForIdle()
    onRoot().captureRoboImage("screenshots/${name}_dark.png")
}

/** An iPhone 17 Pro-sized screen, to compare with the iOS screenshots */
const val PHONE = "w402dp-h874dp-xxhdpi"
