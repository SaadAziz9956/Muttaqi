package com.muttaqi.android.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.runtime.Composable

/** Material 3 Expressive in the brand palette, with its springy expressive motion */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MuttaqiTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialExpressiveTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        motionScheme = MotionScheme.expressive(),
        typography = MuttaqiTypography,
        content = content,
    )
}
