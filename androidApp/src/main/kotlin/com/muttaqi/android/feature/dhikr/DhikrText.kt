package com.muttaqi.android.feature.dhikr

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.muttaqi.android.designsystem.MuttaqiTheme
import com.muttaqi.android.designsystem.NastaliqFont
import com.muttaqi.android.designsystem.QuranFont
import com.muttaqi.android.designsystem.ReemKufi
import com.muttaqi.shared.core.text.arabicMarksOutsideQuranFont
import com.muttaqi.shared.core.text.isArabicScript
import com.muttaqi.shared.core.text.kfgqpcEncoded

// The Dikr rows show their Arabic and caption on one line each, cut off with an ellipsis as on iOS, which the design
// system's ArabicText and TranslationText don't do; these set the text the same way otherwise

/** Arabic in the Quran font on one line, right-aligned, with the marks the font can't draw in the system font */
@Composable
internal fun SingleLineArabic(text: String, fontSize: TextUnit, modifier: Modifier = Modifier, color: Color = MuttaqiTheme.soft.textPrimary) {
    val annotated = remember(text) {
        buildAnnotatedString {
            for (char in text.kfgqpcEncoded()) {
                if (char in arabicMarksOutsideQuranFont) withStyle(SpanStyle(fontFamily = FontFamily.Default)) { append(char) } else append(char)
            }
        }
    }
    Text(
        annotated,
        modifier = modifier,
        color = color,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = TextStyle(fontFamily = QuranFont, fontSize = fontSize, textAlign = TextAlign.Right, textDirection = TextDirection.Rtl),
    )
}

/** A caption on one line in the font and direction of its script: Urdu in Nastaliq, English in Reem Kufi */
@Composable
internal fun SingleLineTranslation(text: String, fontSize: TextUnit, color: Color, modifier: Modifier = Modifier) {
    val urdu = text.isArabicScript()
    Text(
        text,
        modifier = modifier,
        color = color,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = if (urdu) {
            TextStyle(fontFamily = NastaliqFont, fontSize = (fontSize.value + 1).sp, textAlign = TextAlign.Right, textDirection = TextDirection.Rtl)
        } else {
            TextStyle(fontFamily = ReemKufi, fontSize = fontSize, textAlign = TextAlign.Left)
        },
    )
}
