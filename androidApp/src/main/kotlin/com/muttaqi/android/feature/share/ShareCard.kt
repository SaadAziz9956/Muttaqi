package com.muttaqi.android.feature.share

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muttaqi.android.designsystem.MuttaqiTheme
import com.muttaqi.android.designsystem.component.ArabicText
import com.muttaqi.android.designsystem.component.TranslationText
import com.muttaqi.shared.core.share.SharePassage
import com.muttaqi.shared.core.text.isArabicScript

/**
 * The card on the Share page, and the image that's shared: the passage word for word on the brand green, with the
 * app's name faint behind it (ShareCard.swift)
 */
@Composable
fun ShareCard(passage: SharePassage, modifier: Modifier = Modifier) {
    val soft = MuttaqiTheme.soft
    val shape = RoundedCornerShape(15.dp)
    Box(
        modifier
            .fillMaxWidth()
            .dropShadow(shape, Shadow(radius = 5.dp, color = Color.Black.copy(alpha = 0.25f), offset = DpOffset(0.dp, 1.dp)))
            .clip(shape)
            .background(soft.brandGreen),
    ) {
        // As large as it draws, whatever the card's size, and cut off at the card's edge
        Text(
            "متقي",
            Modifier.matchParentSize().wrapContentSize(unbounded = true).clearAndSetSemantics {},
            style = MaterialTheme.typography.headlineMedium.copy(fontSize = 150.sp),
            color = Color.White.copy(alpha = 0.06f),
            softWrap = false,
        )
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            if (passage.arabic.isNotEmpty()) {
                ArabicText(passage.arabic, color = Color.White, lineSpacing = 10.sp)
            }
            passage.transliteration?.takeIf { it.isNotEmpty() }?.let {
                TranslationText(it, color = soft.brandTeal, lineSpacing = 0.sp)
            }
            if (passage.translation.isNotEmpty()) {
                TranslationText(passage.translation, color = Color.White, lineSpacing = if (passage.translation.isArabicScript()) 8.sp else 4.sp)
            }
            // A reference in Urdu is set in Nastaliq a point smaller, which TranslationText enlarges by one
            val urduReference = passage.reference.isArabicScript()
            TranslationText(passage.reference, fontSize = if (urduReference) 11.sp else 12.sp, color = soft.brandTeal, lineSpacing = 0.sp)
        }
    }
}

/** The image that's shared: the card on a margin of the same green, so it reads as one picture, 390 points wide */
@Composable
fun ShareCardImage(passage: SharePassage, modifier: Modifier = Modifier) {
    Box(modifier.width(390.dp).background(MuttaqiTheme.soft.brandGreen).padding(24.dp)) {
        ShareCard(passage)
    }
}

/**
 * Draws [ShareCardImage] into [layer] without showing it, for sharing and saving. It's drawn as iOS renders it: 3
 * pixels to a point whatever the phone, so the image is the same everywhere, and in the light palette, as iOS's
 * ImageRenderer has no dark mode
 */
@Composable
internal fun RecordShareCardImage(passage: SharePassage, layer: GraphicsLayer) {
    // Takes no room on the page
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
