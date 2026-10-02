package com.muttaqi.android.testing

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import com.muttaqi.android.designsystem.MuttaqiTheme

fun ComposeContentTestRule.captureLightAndDark(name: String, content: @Composable () -> Unit) {
    var dark by mutableStateOf(false)
    setContent { MuttaqiTheme(darkTheme = dark) { content() } }
    onRoot().captureRoboImage("screenshots/$name.png")
    dark = true
    waitForIdle()
    onRoot().captureRoboImage("screenshots/${name}_dark.png")
}

const val PHONE = "w402dp-h874dp-xxhdpi"
