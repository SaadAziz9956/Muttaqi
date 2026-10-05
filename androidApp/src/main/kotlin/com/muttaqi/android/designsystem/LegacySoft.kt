package com.muttaqi.android.designsystem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.muttaqi.android.designsystem.component.TranslationText
import com.muttaqi.shared.core.quote.DisplayedQuote
import com.muttaqi.shared.core.text.quoted

@Immutable
data class SoftColors(
    val canvas: Color,
    val surface: Color,
    val rim: Color,
    val shadow: Color,
    val brandGreen: Color,
    val tintedSurface: Color,
    val appPrimary: Color,
    val brandTeal: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val dark: Boolean,
)

object MuttaqiTheme {
    val soft: SoftColors
        @Composable @ReadOnlyComposable get() {
            val scheme = MaterialTheme.colorScheme
            return SoftColors(
                canvas = scheme.surface,
                surface = scheme.surfaceContainerLow,
                rim = scheme.outlineVariant,
                shadow = Color.Transparent,
                brandGreen = scheme.primary,
                tintedSurface = scheme.secondaryContainer,
                appPrimary = scheme.primary,
                brandTeal = scheme.tertiary,
                textPrimary = scheme.onSurface,
                textSecondary = scheme.onSurfaceVariant,
                dark = scheme.surface.luminance() < 0.5f,
            )
        }
}

internal val ReemKufi = FontFamily.Default

@Composable
fun PageHeader(title: String, quote: DisplayedQuote?, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
        if (quote != null) {
            TranslationText(quote.text.quoted(), Modifier.padding(top = 12.dp))
            Text(quote.source, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
