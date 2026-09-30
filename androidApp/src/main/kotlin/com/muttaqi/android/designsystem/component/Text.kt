package com.muttaqi.android.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.muttaqi.android.designsystem.MuttaqiTheme
import com.muttaqi.android.designsystem.NastaliqFont
import com.muttaqi.android.designsystem.QuranFont
import com.muttaqi.android.designsystem.ReemKufi
import com.muttaqi.shared.core.quote.DisplayedQuote
import com.muttaqi.shared.core.text.arabicMarksOutsideQuranFont
import com.muttaqi.shared.core.text.isArabicScript
import com.muttaqi.shared.core.text.kfgqpcEncoded
import com.muttaqi.shared.core.text.quoted

/**
 * Arabic in the Quran font (KFGQPC Hafs), right to left. Marks the font can't draw (Arabic punctuation and the ornate
 * brackets) are set in the system font, as on iOS
 */
@Composable
fun ArabicText(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 20.sp,
    color: Color = MuttaqiTheme.soft.textPrimary,
    textAlign: TextAlign = TextAlign.Center,
) {
    val annotated = remember(text) {
        buildAnnotatedString {
            for (char in text.kfgqpcEncoded()) {
                if (char in arabicMarksOutsideQuranFont) {
                    withStyle(SpanStyle(fontFamily = FontFamily.Default)) { append(char) }
                } else {
                    append(char)
                }
            }
        }
    }
    Text(
        annotated,
        modifier = modifier,
        color = color,
        textAlign = textAlign,
        style = TextStyle(fontFamily = QuranFont, fontSize = fontSize, lineHeight = 1.9.em, textDirection = TextDirection.Rtl),
    )
}

/** A translation in the font and direction of its script: Urdu in Nastaliq, right to left; English in Reem Kufi */
@Composable
fun TranslationText(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 14.sp,
    color: Color = MuttaqiTheme.soft.textPrimary,
    textAlign: TextAlign = TextAlign.Center,
    maxLines: Int = Int.MAX_VALUE,
) {
    val urdu = text.isArabicScript()
    Text(
        text,
        modifier = modifier,
        color = color,
        textAlign = textAlign,
        maxLines = maxLines,
        style = if (urdu) {
            TextStyle(fontFamily = NastaliqFont, fontSize = (fontSize.value + 1).sp, lineHeight = 2.1.em, textDirection = TextDirection.Rtl)
        } else {
            TextStyle(fontFamily = ReemKufi, fontSize = fontSize, lineHeight = 1.45.em)
        },
    )
}

/** A page's large title with its quote and source underneath, as at the top of each tab */
@Composable
fun PageHeader(title: String, quote: DisplayedQuote?, modifier: Modifier = Modifier) {
    val soft = MuttaqiTheme.soft
    Column(modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.headlineMedium, color = soft.appPrimary)
        if (quote != null) {
            TranslationText(quote.text.quoted(), Modifier.padding(top = 12.dp))
            Text(quote.source, style = MaterialTheme.typography.labelSmall, color = soft.textSecondary)
        }
    }
}
