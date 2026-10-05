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
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.unit.sp
import com.muttaqi.android.designsystem.MuttaqiTheme
import com.muttaqi.android.designsystem.NastaliqFont
import com.muttaqi.android.designsystem.QuranFont
import com.muttaqi.android.designsystem.ReemKufi
import com.muttaqi.shared.core.quote.DisplayedQuote
import com.muttaqi.shared.core.text.arabicMarksOutsideQuranFont
import com.muttaqi.shared.core.text.isArabicScript
import com.muttaqi.shared.core.text.quoted

@Composable
fun ArabicText(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 20.sp,
    color: Color = MuttaqiTheme.soft.textPrimary,
    textAlign: TextAlign = TextAlign.Center,
    lineSpacing: TextUnit = 10.sp,
    maxLines: Int = Int.MAX_VALUE,
) {
    val annotated = remember(text) {
        buildAnnotatedString {
            for (char in text) {
                if (char in arabicMarksOutsideQuranFont) {
                    withStyle(SpanStyle(fontFamily = if (char in OrnateBrackets) NastaliqFont else FontFamily.Default, fontSize = 0.9.em)) {
                        append(char)
                    }
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
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        style = TextStyle(
            fontFamily = QuranFont,
            fontSize = fontSize,
            lineHeight = lineHeight(fontSize, QURAN_FONT_LINE_HEIGHT, lineSpacing),
            lineHeightStyle = SpacingBelowLines,
            lineBreak = LineBreak.Paragraph,
            textDirection = TextDirection.Rtl,
        ),
    )
}

@Composable
fun TranslationText(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 14.sp,
    color: Color = MuttaqiTheme.soft.textPrimary,
    textAlign: TextAlign = TextAlign.Center,
    maxLines: Int = Int.MAX_VALUE,
    lineSpacing: TextUnit = TextUnit.Unspecified,
) {
    val urdu = text.isArabicScript()
    Text(
        text,
        modifier = modifier,
        color = color,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        style = if (urdu) {
            val size = (fontSize.value + 1).sp
            TextStyle(
                fontFamily = NastaliqFont,
                fontSize = size,
                lineHeight = lineHeight(size, NASTALIQ_LINE_HEIGHT, if (lineSpacing.isSpecified) lineSpacing else 8.sp),
                lineHeightStyle = SpacingBelowLines,
                lineBreak = LineBreak.Paragraph,
                textDirection = TextDirection.Rtl,
            )
        } else {
            TextStyle(
                fontFamily = ReemKufi,
                fontSize = fontSize,
                lineHeight = lineHeight(fontSize, REEM_KUFI_LINE_HEIGHT, if (lineSpacing.isSpecified) lineSpacing else 4.sp),
                lineHeightStyle = SpacingBelowLines,
                lineBreak = LineBreak.Paragraph,
            )
        },
    )
}

private const val QURAN_FONT_LINE_HEIGHT = 1.758f
private const val NASTALIQ_LINE_HEIGHT = 2.5f
private const val REEM_KUFI_LINE_HEIGHT = 1.5f

internal val OrnateBrackets = setOf('﴾', '﴿')

private fun lineHeight(fontSize: TextUnit, fontLineHeight: Float, lineSpacing: TextUnit): TextUnit =
    (fontSize.value * fontLineHeight + lineSpacing.value).sp

private val SpacingBelowLines = LineHeightStyle(LineHeightStyle.Alignment.Top, LineHeightStyle.Trim.Both)

@Composable
fun PageHeader(title: String, quote: DisplayedQuote?, modifier: Modifier = Modifier) {
    val soft = MuttaqiTheme.soft
    Column(modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.headlineMedium, color = soft.appPrimary)
        if (quote != null) {
            TranslationText(quote.text.quoted(), Modifier.padding(top = 12.dp), lineSpacing = 0.sp)
            Text(quote.source, style = MaterialTheme.typography.labelSmall, color = soft.textSecondary)
        }
    }
}
