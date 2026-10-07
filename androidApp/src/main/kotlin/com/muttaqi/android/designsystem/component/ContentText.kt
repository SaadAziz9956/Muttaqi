package com.muttaqi.android.designsystem.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.unit.sp
import com.muttaqi.android.designsystem.NastaliqFont
import com.muttaqi.android.designsystem.QuranFont
import com.muttaqi.shared.core.text.arabicMarksOutsideQuranFont
import com.muttaqi.shared.core.text.isArabicScript

@Composable
fun QuranText(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 22.sp,
    color: Color = Color.Unspecified,
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
fun ArabicText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.headlineSmall,
    color: Color = Color.Unspecified,
    textAlign: TextAlign = TextAlign.Start,
) {
    Text(
        text,
        modifier = modifier,
        color = color,
        textAlign = textAlign,
        style = style.merge(
            TextStyle(
                fontFamily = FontFamily.Default,
                lineHeight = (style.fontSize.value * ARABIC_LINE_HEIGHT).sp,
                lineBreak = LineBreak.Paragraph,
                textDirection = TextDirection.Rtl,
            ),
        ),
    )
}

@Composable
fun TranslationText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    fontSize: TextUnit = TextUnit.Unspecified,
    color: Color = Color.Unspecified,
    textAlign: TextAlign = TextAlign.Start,
    maxLines: Int = Int.MAX_VALUE,
    lineSpacing: TextUnit = TextUnit.Unspecified,
) {
    val size = if (fontSize.isSpecified) fontSize else style.fontSize
    val urdu = text.isArabicScript()
    Text(
        text,
        modifier = modifier,
        color = color,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        style = if (urdu) {
            val urduSize = (size.value + 1).sp
            style.merge(
                TextStyle(
                    fontFamily = NastaliqFont,
                    fontSize = urduSize,
                    lineHeight = lineHeight(urduSize, NASTALIQ_LINE_HEIGHT, if (lineSpacing.isSpecified) lineSpacing else 8.sp),
                    lineHeightStyle = SpacingBelowLines,
                    lineBreak = LineBreak.Paragraph,
                    textDirection = TextDirection.Rtl,
                ),
            )
        } else {
            style.merge(
                TextStyle(
                    fontSize = size,
                    lineHeight = if (lineSpacing.isSpecified) (size.value * 1.4f + lineSpacing.value).sp else style.lineHeight,
                    lineBreak = LineBreak.Paragraph,
                ),
            )
        },
    )
}

private fun lineHeight(fontSize: TextUnit, fontLineHeight: Float, lineSpacing: TextUnit): TextUnit =
    (fontSize.value * fontLineHeight + lineSpacing.value).sp

private val SpacingBelowLines = LineHeightStyle(LineHeightStyle.Alignment.Top, LineHeightStyle.Trim.Both)

internal const val QURAN_FONT_LINE_HEIGHT = 1.758f
private const val NASTALIQ_LINE_HEIGHT = 2.5f
private const val ARABIC_LINE_HEIGHT = 1.7f

internal val OrnateBrackets = setOf('﴾', '﴿')
