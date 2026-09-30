package com.muttaqi.android.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

/** Material 3 Expressive in the brand palette, with its springy expressive motion and the soft style's colours */
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
    /** The soft style's colours for the current light or dark mode */
    val soft: SoftColors
        @Composable @ReadOnlyComposable get() = LocalSoftColors.current
}
