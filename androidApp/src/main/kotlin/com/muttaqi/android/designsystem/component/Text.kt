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
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.unit.dp
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
 * brackets) are set in the system font, as on iOS. `lineSpacing` is the space between lines, as iOS's `lineSpacing`;
 * text cut off at `maxLines` ends in an ellipsis
 */
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
            for (char in text.kfgqpcEncoded().withRightToLeftGuillemets()) {
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

/**
 * A translation in the font and direction of its script: Urdu in Nastaliq, right to left; English in Reem Kufi.
 * `lineSpacing` is the space between lines, as iOS's `lineSpacing`; unset, it's the app's usual 8 for Urdu and 4 for
 * English. Text cut off at `maxLines` ends in an ellipsis
 */
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
        if (urdu) text.withRightToLeftGuillemets() else text,
        modifier = modifier,
        color = color,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        style = if (urdu) {
            // One point larger, as on iOS, since Nastaliq reads small for its size
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

/**
 * « and » facing outwards in right-to-left text, as iOS draws them. Android mirrors brackets there but not these, so
 * the pair is swapped for display; the words are untouched
 */
private fun String.withRightToLeftGuillemets(): String = buildString(length) {
    for (char in this@withRightToLeftGuillemets) {
        append(
            when (char) {
                '«' -> '»'
                '»' -> '«'
                else -> char
            },
        )
    }
}

// Each font's own line height, in ems (its ascender plus descender), which iOS lays lines out by
private const val QURAN_FONT_LINE_HEIGHT = 1.758f
private const val NASTALIQ_LINE_HEIGHT = 2.5f
private const val REEM_KUFI_LINE_HEIGHT = 1.5f

/** A line as tall as iOS makes it: the font's own line height, then the spacing */
private fun lineHeight(fontSize: TextUnit, fontLineHeight: Float, lineSpacing: TextUnit): TextUnit =
    (fontSize.value * fontLineHeight + lineSpacing.value).sp

// Lines are broken for the paragraph as a whole, as iOS does, which keeps a lone word off the last line

/** The spacing goes under each line but the last, as on iOS, so the text starts and ends where its glyphs do */
private val SpacingBelowLines = LineHeightStyle(LineHeightStyle.Alignment.Top, LineHeightStyle.Trim.Both)

/** A page's large title with its quote and source underneath, as at the top of each tab */
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
