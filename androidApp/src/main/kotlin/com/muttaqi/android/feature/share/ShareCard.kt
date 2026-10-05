package com.muttaqi.android.feature.share

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muttaqi.android.designsystem.MuttaqiTheme
import com.muttaqi.android.designsystem.component.ArabicText
import com.muttaqi.android.designsystem.component.QuranText
import com.muttaqi.android.designsystem.component.TranslationText
import com.muttaqi.shared.core.share.SharePassage
import com.muttaqi.shared.core.text.isArabicScript

@Composable
fun ShareCard(passage: SharePassage, modifier: Modifier = Modifier) {
    Card(
        modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (passage.arabic.isNotEmpty()) {
                if (passage.isQuran) {
                    QuranText(passage.arabic, Modifier.fillMaxWidth(), lineSpacing = 10.sp)
                } else {
                    ArabicText(passage.arabic, Modifier.fillMaxWidth(), style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
                }
            }
            passage.transliteration?.takeIf { it.isNotEmpty() }?.let {
                TranslationText(
                    it,
                    Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    lineSpacing = 0.sp,
                )
            }
            if (passage.translation.isNotEmpty()) {
                TranslationText(
                    passage.translation,
                    Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    lineSpacing = if (passage.translation.isArabicScript()) 8.sp else 4.sp,
                )
            }
            TranslationText(
                passage.reference,
                Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                lineSpacing = 0.sp,
            )
            ArabicText(
                "متقي",
                Modifier.padding(top = 8.dp).clearAndSetSemantics {},
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
            )
        }
    }
}

private val SharePassage.isQuran: Boolean get() = reference.startsWith("Quran ")

@Composable
fun ShareCardImage(passage: SharePassage, modifier: Modifier = Modifier) {
    Surface(modifier.width(390.dp), color = MaterialTheme.colorScheme.surface) {
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
