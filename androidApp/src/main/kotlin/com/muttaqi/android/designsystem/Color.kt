package com.muttaqi.android.designsystem

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// The brand palette, the same values as the iOS asset catalogue (and SoftStyle.swift for the soft tokens)
internal object BrandColors {
    val green = Color(0xFF114538)
    val greenLight = Color(0xFF7CC4A8)
    val teal = Color(0xFF81CACF)
    val tealDark = Color(0xFF8ECDD2)
    val tintedSurface = Color(0xFFF2FBFF)
    val tintedSurfaceDark = Color(0xFF132A2E)
    val canvas = Color(0xFFF3F7F5)
    val canvasDark = Color(0xFF0A1210)
    val text = Color(0xFF393939)
    val textDark = Color(0xFFEBEBEB)
    val textSecondary = Color(0xFFA5A5A5)
    val textSecondaryDark = Color(0xFF98989D)
}

/** The soft floating style's colours, beyond Material's scheme */
@Immutable
data class SoftColors(
    /** The page behind floating cards */
    val canvas: Color,
    /** A floating card's fill */
    val surface: Color,
    /** The bright edge around a floating card */
    val rim: Color,
    val shadow: Color,
    /** The brand green in both modes: share cards, selected chips, main actions */
    val brandGreen: Color,
    val tintedSurface: Color,
    val appPrimary: Color,
    val brandTeal: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val dark: Boolean,
)

internal val LightSoftColors = SoftColors(
    canvas = BrandColors.canvas,
    surface = Color.White.copy(alpha = 0.82f),
    rim = Color.White.copy(alpha = 0.95f),
    shadow = BrandColors.green.copy(alpha = 0.16f),
    brandGreen = BrandColors.green,
    tintedSurface = BrandColors.tintedSurface,
    appPrimary = BrandColors.green,
    brandTeal = BrandColors.teal,
    textPrimary = BrandColors.text,
    textSecondary = BrandColors.textSecondary,
    dark = false,
)

internal val DarkSoftColors = SoftColors(
    canvas = BrandColors.canvasDark,
    surface = Color(0xEB14201C),
    rim = Color.White.copy(alpha = 0.07f),
    shadow = Color.Black.copy(alpha = 0.55f),
    brandGreen = BrandColors.green,
    tintedSurface = BrandColors.tintedSurfaceDark,
    appPrimary = BrandColors.greenLight,
    brandTeal = BrandColors.tealDark,
    textPrimary = BrandColors.textDark,
    textSecondary = BrandColors.textSecondaryDark,
    dark = true,
)

internal val LocalSoftColors = staticCompositionLocalOf { LightSoftColors }

// Brand colours rather than the wallpaper's dynamic colours, so the app looks the same on every phone and on iOS
internal val LightColors = lightColorScheme(
    primary = BrandColors.green,
    onPrimary = Color.White,
    primaryContainer = BrandColors.tintedSurface,
    onPrimaryContainer = BrandColors.green,
    secondary = BrandColors.teal,
    onSecondary = BrandColors.green,
    secondaryContainer = BrandColors.tintedSurface,
    onSecondaryContainer = BrandColors.green,
    background = BrandColors.canvas,
    onBackground = BrandColors.text,
    surface = Color.White,
    onSurface = BrandColors.text,
    surfaceContainer = BrandColors.tintedSurface,
    onSurfaceVariant = BrandColors.textSecondary,
)

internal val DarkColors = darkColorScheme(
    primary = BrandColors.greenLight,
    onPrimary = Color(0xFF0B2219),
    primaryContainer = BrandColors.tintedSurfaceDark,
    onPrimaryContainer = BrandColors.greenLight,
    secondary = BrandColors.tealDark,
    onSecondary = Color(0xFF0B2219),
    secondaryContainer = BrandColors.tintedSurfaceDark,
    onSecondaryContainer = BrandColors.greenLight,
    background = BrandColors.canvasDark,
    onBackground = BrandColors.textDark,
    surface = Color(0xFF14201C),
    onSurface = BrandColors.textDark,
    surfaceContainer = BrandColors.tintedSurfaceDark,
    onSurfaceVariant = BrandColors.textSecondaryDark,
)
