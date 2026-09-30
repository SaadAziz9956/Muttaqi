package com.muttaqi.android.designsystem

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.muttaqi.android.R

internal val ReemKufi = FontFamily(
    Font(R.font.reem_kufi_regular, FontWeight.Normal),
    Font(R.font.reem_kufi_medium, FontWeight.Medium),
    Font(R.font.reem_kufi_semi_bold, FontWeight.SemiBold),
    Font(R.font.reem_kufi_bold, FontWeight.Bold),
)

// The iOS type scale (Typography.swift), in Reem Kufi
internal val MuttaqiTypography = Typography(
    displayLarge = TextStyle(fontFamily = ReemKufi, fontWeight = FontWeight.Bold, fontSize = 34.sp),
    displayMedium = TextStyle(fontFamily = ReemKufi, fontWeight = FontWeight.Bold, fontSize = 28.sp),
    headlineMedium = TextStyle(fontFamily = ReemKufi, fontWeight = FontWeight.Normal, fontSize = 28.sp),
    titleLarge = TextStyle(fontFamily = ReemKufi, fontWeight = FontWeight.SemiBold, fontSize = 22.sp),
    titleMedium = TextStyle(fontFamily = ReemKufi, fontWeight = FontWeight.SemiBold, fontSize = 18.sp),
    titleSmall = TextStyle(fontFamily = ReemKufi, fontWeight = FontWeight.Medium, fontSize = 16.sp),
    bodyLarge = TextStyle(fontFamily = ReemKufi, fontWeight = FontWeight.Normal, fontSize = 17.sp),
    bodyMedium = TextStyle(fontFamily = ReemKufi, fontWeight = FontWeight.Normal, fontSize = 15.sp),
    bodySmall = TextStyle(fontFamily = ReemKufi, fontWeight = FontWeight.Normal, fontSize = 13.sp),
    labelLarge = TextStyle(fontFamily = ReemKufi, fontWeight = FontWeight.Medium, fontSize = 14.sp),
    labelMedium = TextStyle(fontFamily = ReemKufi, fontWeight = FontWeight.Medium, fontSize = 12.sp),
    labelSmall = TextStyle(fontFamily = ReemKufi, fontWeight = FontWeight.Normal, fontSize = 12.sp),
)
