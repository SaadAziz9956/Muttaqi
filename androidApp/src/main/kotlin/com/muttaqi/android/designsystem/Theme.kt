package com.muttaqi.android.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MuttaqiTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalSoftColors provides if (darkTheme) DarkSoftColors else LightSoftColors) {
        MaterialExpressiveTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
            motionScheme = MotionScheme.expressive(),
            typography = MuttaqiTypography,
            content = content,
        )
    }
}

object MuttaqiTheme {
    val soft: SoftColors
        @Composable @ReadOnlyComposable get() = LocalSoftColors.current
}
