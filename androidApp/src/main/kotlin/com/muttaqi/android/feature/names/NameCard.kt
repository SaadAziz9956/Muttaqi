package com.muttaqi.android.feature.names

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muttaqi.android.designsystem.MuttaqiTheme
import com.muttaqi.android.designsystem.ReemKufi
import com.muttaqi.android.designsystem.component.ArabicText
import com.muttaqi.android.designsystem.component.SoftArtwork
import com.muttaqi.android.designsystem.component.SoftCard
import com.muttaqi.android.designsystem.component.TranslationText
import com.muttaqi.shared.core.text.isArabicScript
import com.muttaqi.shared.core.text.sentenceCased
import com.muttaqi.shared.feature.names.domain.model.AllahName

@Composable
fun NameCard(name: AllahName, modifier: Modifier = Modifier, minHeight: Dp = 300.dp, onClick: (() -> Unit)? = null) {
    val soft = MuttaqiTheme.soft
    SoftCard(
        modifier.clearAndSetSemantics { contentDescription = "${name.number}, ${name.arabic}, ${name.transliteration}, ${name.meaning}" },
        cornerRadius = 30.dp,
        artwork = SoftArtwork.Dawn,
        onClick = onClick,
    ) {
        Box(Modifier.fillMaxWidth().heightIn(min = minHeight), contentAlignment = Alignment.Center) {
            Text(
                name.arabic,
                Modifier.padding(horizontal = 12.dp).offset(y = 24.dp),
                color = soft.brandTeal.copy(alpha = if (soft.dark) 0.08f else 0.11f),
                maxLines = 1,
                softWrap = false,
                autoSize = TextAutoSize.StepBased(minFontSize = 36.sp, maxFontSize = 90.sp),
                style = TextStyle(fontFamily = ReemKufi, fontSize = 90.sp),
            )
            Column(Modifier.padding(horizontal = 20.dp, vertical = 48.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                ArabicText(name.arabic, fontSize = 40.sp, lineSpacing = 0.sp)
                Text(
                    name.transliteration,
                    Modifier.padding(top = 12.dp),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                    color = soft.appPrimary,
                )
                TranslationText(
                    name.meaning.sentenceCased(),
                    Modifier.padding(top = 10.dp),
                    lineSpacing = if (name.meaning.isArabicScript()) 6.sp else 2.sp,
                )
            }
            Box(
                Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 16.dp, top = 16.dp)
                    .size(36.dp)
                    .background(soft.surface, CircleShape)
                    .border(1.5.dp, soft.rim, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text("${name.number}", style = MaterialTheme.typography.labelLarge, color = soft.appPrimary)
            }
        }
    }
}
