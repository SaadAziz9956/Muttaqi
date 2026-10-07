package com.muttaqi.android.feature.share

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muttaqi.android.designsystem.MuttaqiTheme
import com.muttaqi.android.designsystem.component.ArabicText
import com.muttaqi.android.designsystem.component.QuranText
import com.muttaqi.android.designsystem.component.TranslationText
import com.muttaqi.android.designsystem.oneui.OneUi
import com.muttaqi.android.designsystem.oneui.OneUiArtwork
import com.muttaqi.android.designsystem.oneui.OneUiCard
import com.muttaqi.shared.core.share.SharePassage
import com.muttaqi.shared.core.text.isArabicScript

@Composable
fun ShareCard(passage: SharePassage, modifier: Modifier = Modifier) {
    val accent = OneUi.colors.accent
    val type = OneUi.typography
    OneUiCard(modifier.fillMaxWidth(), artwork = OneUiArtwork.Dawn, contentPadding = PaddingValues(horizontal = 24.dp, vertical = 32.dp)) {
        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (passage.arabic.isNotEmpty()) {
                if (passage.isQuran) {
                    QuranText(passage.arabic, Modifier.fillMaxWidth(), lineSpacing = 10.sp)
                } else {
                    ArabicText(passage.arabic, Modifier.fillMaxWidth(), style = type.body.copy(fontSize = 22.sp), textAlign = TextAlign.Center)
                }
            }
            passage.transliteration?.takeIf { it.isNotEmpty() }?.let {
                TranslationText(
                    it,
                    Modifier.fillMaxWidth(),
                    style = type.body,
                    color = accent,
                    textAlign = TextAlign.Center,
                    lineSpacing = 0.sp,
                )
            }
            if (passage.translation.isNotEmpty()) {
                TranslationText(
                    passage.translation,
                    Modifier.fillMaxWidth(),
                    style = type.body,
                    textAlign = TextAlign.Center,
                    lineSpacing = if (passage.translation.isArabicScript()) 8.sp else 4.sp,
                )
            }
            TranslationText(
                passage.reference,
                Modifier.fillMaxWidth(),
                style = type.listSummary.copy(fontWeight = FontWeight.Medium),
                color = accent,
                textAlign = TextAlign.Center,
                lineSpacing = 0.sp,
            )
            Text(
                "متقي",
                Modifier.padding(top = 8.dp).clearAndSetSemantics {},
                style = type.sectionTitle.copy(textDirection = TextDirection.Rtl),
                color = accent,
                textAlign = TextAlign.Center,
            )
        }
    }
}

private val SharePassage.isQuran: Boolean get() = reference.startsWith("Quran ")

@Composable
fun ShareCardImage(passage: SharePassage, modifier: Modifier = Modifier) {
    Box(modifier.width(390.dp).background(OneUi.colors.background)) {
        ShareCard(passage, Modifier.padding(24.dp))
    }
}

@Composable
internal fun RecordShareCardImage(passage: SharePassage, layer: GraphicsLayer) {
    Box(Modifier.size(0.dp)) {
        CompositionLocalProvider(LocalDensity provides Density(density = 3f, fontScale = 1f)) {
            MuttaqiTheme(darkTheme = false) {
                ShareCardImage(
                    passage,
                    Modifier
                        .wrapContentSize(align = Alignment.TopStart, unbounded = true)
                        .drawWithContent { layer.record { this@drawWithContent.drawContent() } },
                )
            }
        }
    }
}
