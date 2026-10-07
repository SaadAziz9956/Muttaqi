package com.muttaqi.android.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import com.muttaqi.android.designsystem.oneui.OneUiDark
import com.muttaqi.android.designsystem.oneui.OneUiLight
import com.muttaqi.android.designsystem.oneui.OneUiTheme
import com.muttaqi.android.designsystem.oneui.ReemKufi
import com.muttaqi.android.designsystem.oneui.SystemSans

@Composable
fun MuttaqiTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val oneUi = if (darkTheme) OneUiDark else OneUiLight
    val base = if (darkTheme) MuttaqiGreenDark else MuttaqiGreenLight
    val scheme = base.copy(
        background = oneUi.background,
        onBackground = oneUi.text,
        surface = oneUi.background,
        onSurface = oneUi.text,
        onSurfaceVariant = oneUi.secondaryText,
        surfaceBright = oneUi.container,
        surfaceDim = oneUi.background,
        surfaceContainerLowest = oneUi.container,
        surfaceContainerLow = oneUi.container,
        surfaceContainer = oneUi.container,
        surfaceContainerHigh = oneUi.container,
        surfaceContainerHighest = oneUi.component,
        outlineVariant = oneUi.divider,
        scrim = Color.Black,
    )
    MaterialTheme(colorScheme = scheme, typography = MuttaqiTypography) {
        OneUiTheme(darkTheme = darkTheme, content = content)
    }
}

private fun TextStyle.sans() = copy(fontFamily = SystemSans)

private fun TextStyle.kufi() = copy(fontFamily = ReemKufi)

private val MuttaqiTypography = Typography().run {
    copy(
        displayLarge = displayLarge.kufi(),
        displayMedium = displayMedium.kufi(),
        displaySmall = displaySmall.kufi(),
        headlineLarge = headlineLarge.kufi(),
        headlineMedium = headlineMedium.kufi(),
        headlineSmall = headlineSmall.sans(),
        titleLarge = titleLarge.kufi(),
        titleMedium = titleMedium.sans(),
        titleSmall = titleSmall.sans(),
        bodyLarge = bodyLarge.sans(),
        bodyMedium = bodyMedium.sans(),
        bodySmall = bodySmall.sans(),
        labelLarge = labelLarge.sans(),
        labelMedium = labelMedium.sans(),
        labelSmall = labelSmall.sans(),
    )
}
