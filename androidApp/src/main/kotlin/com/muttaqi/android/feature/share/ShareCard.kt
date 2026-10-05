package com.muttaqi.android.feature.share

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muttaqi.android.designsystem.MuttaqiTheme
import com.muttaqi.android.designsystem.component.QuranText
import com.muttaqi.android.designsystem.component.SoftArtwork
import com.muttaqi.android.designsystem.component.SoftBackdrop
import com.muttaqi.android.designsystem.component.SoftCard
import com.muttaqi.android.designsystem.component.TranslationText
import com.muttaqi.shared.core.share.SharePassage
import com.muttaqi.shared.core.text.isArabicScript

@Composable
fun ShareCard(passage: SharePassage, modifier: Modifier = Modifier) {
    val soft = MuttaqiTheme.soft
    SoftCard(modifier.fillMaxWidth(), cornerRadius = 28.dp, artwork = SoftArtwork.Forest) {
        Text(
            "متقي",
            Modifier.matchParentSize().wrapContentSize(unbounded = true).clearAndSetSemantics {},
            style = MaterialTheme.typography.headlineMedium.copy(fontSize = 150.sp),
            color = Color.White.copy(alpha = 0.07f),
            softWrap = false,
        )
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            if (passage.arabic.isNotEmpty()) {
                QuranText(passage.arabic, color = Color.White, lineSpacing = 10.sp)
            }
            passage.transliteration?.takeIf { it.isNotEmpty() }?.let {
                TranslationText(it, color = soft.brandTeal, lineSpacing = 0.sp)
            }
            if (passage.translation.isNotEmpty()) {
                TranslationText(passage.translation, color = Color.White, lineSpacing = if (passage.translation.isArabicScript()) 8.sp else 4.sp)
            }
            val urduReference = passage.reference.isArabicScript()
            TranslationText(passage.reference, fontSize = if (urduReference) 11.sp else 12.sp, color = soft.brandTeal, lineSpacing = 0.sp)
        }
    }
}

@Composable
fun ShareCardImage(passage: SharePassage, modifier: Modifier = Modifier) {
    Box(modifier.width(390.dp)) {
        SoftBackdrop(Modifier.matchParentSize())
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
