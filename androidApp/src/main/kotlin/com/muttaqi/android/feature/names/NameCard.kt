package com.muttaqi.android.feature.names

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muttaqi.android.designsystem.component.ArabicText
import com.muttaqi.android.designsystem.component.TranslationText
import com.muttaqi.android.designsystem.oneui.OneUi
import com.muttaqi.android.designsystem.oneui.OneUiArtwork
import com.muttaqi.android.designsystem.oneui.OneUiCard
import com.muttaqi.shared.core.text.isArabicScript
import com.muttaqi.shared.feature.names.domain.model.AllahName

@Composable
fun NameCard(name: AllahName, modifier: Modifier = Modifier, minHeight: Dp = 300.dp) {
    val type = OneUi.typography
    OneUiCard(
        modifier.clearAndSetSemantics { contentDescription = "${name.number}, ${name.arabic}, ${name.transliteration}, ${name.meaning}" },
        artwork = OneUiArtwork.Dawn,
        contentPadding = PaddingValues(0.dp),
    ) {
        val content = LocalContentColor.current
        Box(Modifier.fillMaxWidth().heightIn(min = minHeight)) {
            NameNumber(
                name.number,
                Modifier.align(Alignment.TopStart).padding(16.dp),
                container = content.copy(alpha = 0.1f),
                textColor = content,
            )
            Column(
                Modifier.align(Alignment.Center).fillMaxWidth().padding(horizontal = 24.dp, vertical = 72.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ArabicText(name.arabic, style = type.largeTitle.copy(fontSize = 46.sp), color = content, textAlign = TextAlign.Center)
                Text(
                    name.transliteration,
                    Modifier.padding(top = 4.dp),
                    style = type.sectionTitle.copy(fontSize = 24.sp, lineHeight = 30.sp),
                    color = OneUi.colors.accent,
                    textAlign = TextAlign.Center,
                )
                TranslationText(
                    name.meaning,
                    style = type.body.copy(fontSize = 17.sp),
                    color = content,
                    textAlign = TextAlign.Center,
                    lineSpacing = if (name.meaning.isArabicScript()) 6.sp else 2.sp,
                )
            }
        }
    }
}

@Composable
internal fun NameNumber(
    number: Int,
    modifier: Modifier = Modifier,
    container: Color = OneUi.colors.accent.copy(alpha = if (OneUi.colors.isDark) 0.16f else 0.1f),
    textColor: Color = OneUi.colors.accent,
) {
    Box(modifier.size(40.dp).clip(CircleShape).background(container), contentAlignment = Alignment.Center) {
        Text("$number", style = OneUi.typography.listSummary.copy(fontWeight = FontWeight.SemiBold), color = textColor)
    }
}
