package com.muttaqi.android.designsystem

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

enum class ColorSource { Wallpaper, MuttaqiGreen }

val wallpaperColorsAvailable: Boolean get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MuttaqiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    colorSource: ColorSource = ColorSource.MuttaqiGreen,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colorScheme = when {
        colorSource == ColorSource.Wallpaper && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        darkTheme -> MuttaqiGreenDark
        else -> MuttaqiGreenLight
    }
    MaterialExpressiveTheme(colorScheme = colorScheme, motionScheme = MotionScheme.expressive(), content = content)
}
