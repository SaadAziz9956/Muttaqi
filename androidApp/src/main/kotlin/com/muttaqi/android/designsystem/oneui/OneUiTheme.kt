package com.muttaqi.android.designsystem.oneui

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.DeviceFontFamilyName
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muttaqi.android.R
import com.muttaqi.android.designsystem.MuttaqiGreenDark
import com.muttaqi.android.designsystem.MuttaqiGreenLight

@Immutable
data class OneUiColors(
    val isDark: Boolean,
    val background: Color,
    val container: Color,
    val text: Color,
    val secondaryText: Color,
    val divider: Color,
    val component: Color,
    val accent: Color,
    val onAccent: Color,
    val pressOverlay: Color,
    val tabBar: Color,
    val tabSelected: Color,
    val destructive: Color,
    val switchOff: Color,
    val toast: Color,
    val onToast: Color,
)

internal val OneUiLight = OneUiColors(
    isDark = false,
    background = Color(0xFFF4F4F6),
    container = Color(0xFFFFFFFF),
    text = Color(0xFF1F1F1F),
    secondaryText = Color(0xFF7C7C80),
    divider = Color(0xFFE6E6E8),
    component = Color(0xFFEBEBED),
    accent = MuttaqiGreenLight.primary,
    onAccent = MuttaqiGreenLight.onPrimary,
    pressOverlay = Color.Black.copy(alpha = 0.06f),
    tabBar = Color.White.copy(alpha = 0.97f),
    tabSelected = Color(0xFFECECEE),
    destructive = Color(0xFFE5243F),
    switchOff = Color(0xFFC4C4C8),
    toast = Color(0xFF2B2B2E),
    onToast = Color(0xFFF5F5F5),
)

internal val OneUiDark = OneUiColors(
    isDark = true,
    background = Color(0xFF000000),
    container = Color(0xFF17171A),
    text = Color(0xFFF5F5F5),
    secondaryText = Color(0xFF9A9A9E),
    divider = Color(0xFF2A2A2D),
    component = Color(0xFF2B2B2E),
    accent = MuttaqiGreenDark.primary,
    onAccent = MuttaqiGreenDark.onPrimary,
    pressOverlay = Color.White.copy(alpha = 0.08f),
    tabBar = Color(0xFF242427).copy(alpha = 0.97f),
    tabSelected = Color(0xFF38383C),
    destructive = Color(0xFFFF5A6E),
    switchOff = Color(0xFF55555A),
    toast = Color(0xFFE8E8EA),
    onToast = Color(0xFF1F1F1F),
)

internal val ReemKufi = FontFamily(
    Font(R.font.reem_kufi_regular, FontWeight.Normal),
    Font(R.font.reem_kufi_medium, FontWeight.Medium),
    Font(R.font.reem_kufi_semi_bold, FontWeight.SemiBold),
    Font(R.font.reem_kufi_bold, FontWeight.Bold),
)

internal val SystemSans = FontFamily(
    Font(DeviceFontFamilyName("sec"), FontWeight.Normal),
    Font(DeviceFontFamilyName("sec"), FontWeight.Medium),
    Font(DeviceFontFamilyName("sec"), FontWeight.SemiBold),
    Font(DeviceFontFamilyName("sec"), FontWeight.Bold),
)

@Immutable
data class OneUiTypography(
    val largeTitle: TextStyle = TextStyle(fontFamily = ReemKufi, fontWeight = FontWeight.Normal, fontSize = 34.sp, lineHeight = 42.sp),
    val barTitle: TextStyle = TextStyle(fontFamily = ReemKufi, fontWeight = FontWeight.Medium, fontSize = 20.sp, lineHeight = 26.sp),
    val listTitle: TextStyle = TextStyle(fontFamily = SystemSans, fontSize = 17.sp, lineHeight = 23.sp),
    val listSummary: TextStyle = TextStyle(fontFamily = SystemSans, fontSize = 14.sp, lineHeight = 19.sp),
    val subheader: TextStyle = TextStyle(fontFamily = SystemSans, fontWeight = FontWeight.Medium, fontSize = 13.sp, lineHeight = 18.sp),
    val body: TextStyle = TextStyle(fontFamily = SystemSans, fontSize = 16.sp, lineHeight = 24.sp),
    val caption: TextStyle = TextStyle(fontFamily = SystemSans, fontSize = 13.sp, lineHeight = 18.sp),
    val small: TextStyle = TextStyle(fontFamily = SystemSans, fontSize = 12.sp, lineHeight = 17.sp),
    val tabLabel: TextStyle = TextStyle(fontFamily = SystemSans, fontSize = 11.sp, lineHeight = 14.sp),
    val field: TextStyle = TextStyle(fontFamily = SystemSans, fontSize = 16.sp, lineHeight = 22.sp),
    val button: TextStyle = TextStyle(fontFamily = SystemSans, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 20.sp),
    val dialogText: TextStyle = TextStyle(fontFamily = SystemSans, fontSize = 15.sp, lineHeight = 21.sp),
    val dialogTitle: TextStyle = TextStyle(fontFamily = SystemSans, fontWeight = FontWeight.SemiBold, fontSize = 19.sp, lineHeight = 25.sp),
    val number: TextStyle = TextStyle(fontFamily = SystemSans, fontWeight = FontWeight.SemiBold, fontSize = 26.sp, lineHeight = 32.sp),
    val sectionTitle: TextStyle = TextStyle(fontFamily = ReemKufi, fontWeight = FontWeight.Medium, fontSize = 20.sp, lineHeight = 26.sp),
)

object OneUiDefaults {
    val ContainerRadius = 26.dp
    val ScreenMargin = 12.dp
    val ItemPadding = 20.dp
    val GroupGap = 20.dp
    val CollapsedBarHeight = 56.dp
    val BadgeSize = 40.dp
    val Easing = CubicBezierEasing(0.22f, 0.25f, 0f, 1f)
    const val EXPANDED_PROPORTION = 0.3967f
    const val EXPANDED_PROPORTION_WIDE = 0.25f
}

internal val LocalOneUiColors = staticCompositionLocalOf { OneUiLight }
internal val LocalOneUiTypography = staticCompositionLocalOf { OneUiTypography() }

object OneUi {
    val colors: OneUiColors
        @Composable @ReadOnlyComposable get() = LocalOneUiColors.current

    val typography: OneUiTypography
        @Composable @ReadOnlyComposable get() = LocalOneUiTypography.current
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OneUiTheme(darkTheme: Boolean = MaterialTheme.colorScheme.background.luminance() < 0.5f, content: @Composable () -> Unit) {
    val colors = if (darkTheme) OneUiDark else OneUiLight
    val typography = OneUiTypography()
    CompositionLocalProvider(
        LocalOneUiColors provides colors,
        LocalOneUiTypography provides typography,
        LocalIndication provides RecoilIndication(colors.pressOverlay),
        LocalRippleConfiguration provides null,
        LocalContentColor provides colors.text,
        LocalTextStyle provides typography.body,
        content = content,
    )
}

@Composable
fun OneUiSurface(content: @Composable () -> Unit) {
    OneUiTheme {
        Box(Modifier.fillMaxSize().background(OneUi.colors.background)) { content() }
    }
}
