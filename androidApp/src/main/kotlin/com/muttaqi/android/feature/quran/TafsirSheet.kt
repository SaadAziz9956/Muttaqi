package com.muttaqi.android.feature.quran

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.muttaqi.android.designsystem.component.FadeBetween
import com.muttaqi.android.designsystem.component.QuranText
import com.muttaqi.android.designsystem.component.TranslationText
import com.muttaqi.android.designsystem.oneui.OneUi
import com.muttaqi.android.designsystem.oneui.OneUiCard
import com.muttaqi.android.designsystem.oneui.OneUiCardSpacing
import com.muttaqi.android.designsystem.oneui.OneUiDefaults
import com.muttaqi.shared.core.model.Language
import com.muttaqi.shared.feature.quran.domain.model.Surah
import com.muttaqi.shared.feature.quran.domain.model.TafsirEntry
import com.muttaqi.shared.feature.quran.presentation.tafsir.TafsirState
import com.muttaqi.shared.feature.quran.presentation.tafsir.TafsirStatus

@Composable
fun TafsirContent(surah: Surah?, state: TafsirState, startAyah: Int?, onRetry: () -> Unit) {
    val colors = OneUi.colors
    val type = OneUi.typography
    Column(Modifier.fillMaxSize()) {
        Column(
            Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 4.dp, bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                surah?.englishName ?: "Tafseer",
                style = type.sectionTitle.copy(fontSize = 24.sp, lineHeight = 30.sp),
                color = colors.text,
                textAlign = TextAlign.Center,
            )
            Text(state.sourceTitle, style = type.listSummary, color = colors.secondaryText, textAlign = TextAlign.Center)
            if (state.showsEnglishInstead) {
                Text(
                    "Hindi tafseer isn't available yet — showing English",
                    Modifier.padding(top = 4.dp),
                    style = type.caption,
                    color = colors.secondaryText,
                    textAlign = TextAlign.Center,
                )
            }
        }
        FadeBetween(state.status, key = { it is TafsirStatus.Loaded || it is TafsirStatus.Failed }, Modifier.fillMaxSize()) { status ->
            when (status) {
                TafsirStatus.Idle, TafsirStatus.Loading -> Loading(Modifier.fillMaxSize())
                is TafsirStatus.Loaded -> if (status.entries.isEmpty()) {
                    Text(
                        "No tafseer available for this surah",
                        Modifier.fillMaxWidth().padding(top = 120.dp, start = 24.dp, end = 24.dp),
                        style = type.listSummary,
                        color = colors.secondaryText,
                        textAlign = TextAlign.Center,
                    )
                } else {
                    TafsirEntries(status.entries, state.language, state.entryCovering(startAyah ?: 0))
                }
                is TafsirStatus.Failed -> LoadFailed(status.message, onRetry, title = "Failed to load tafseer")
            }
        }
    }
}

@Composable
private fun TafsirEntries(entries: List<TafsirEntry>, language: Language, start: TafsirEntry?) {
    val list = rememberLazyListState()
    LaunchedEffect(start) {
        val index = entries.indexOf(start)
        if (index >= 0) list.scrollToItem(index)
    }
    LazyColumn(
        state = list,
        contentPadding = PaddingValues(start = OneUiDefaults.ScreenMargin, end = OneUiDefaults.ScreenMargin, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(OneUiCardSpacing),
    ) {
        items(entries, key = { it.ayahNumber }) { entry ->
            OneUiCard(Modifier.fillMaxWidth()) {
                Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        if (entry.lastAyahNumber > entry.ayahNumber) "Ayah ${entry.ayahNumber}–${entry.lastAyahNumber}" else "Ayah ${entry.ayahNumber}",
                        style = OneUi.typography.subheader,
                        color = OneUi.colors.accent,
                    )
                    entry.paragraphs.forEach { TafsirParagraph(it, language) }
                }
            }
        }
    }
}

@Composable
private fun TafsirParagraph(paragraph: String, language: Language) {
    val colors = OneUi.colors
    val type = OneUi.typography
    when {
        language == Language.Urdu ->
            TranslationText(
                paragraph,
                Modifier.fillMaxWidth(),
                style = type.body,
                color = colors.text,
                textAlign = TextAlign.Right,
                lineSpacing = 0.sp,
            )
        startsWithArabic(paragraph) ->
            QuranText(paragraph, Modifier.fillMaxWidth(), fontSize = 20.sp, color = colors.text, textAlign = TextAlign.Right, lineSpacing = 0.sp)
        else -> {
            val text = remember(paragraph) { restyled(paragraph, setOf('ﷺ'), SpanStyle(fontFamily = FontFamily.Default, fontSize = 0.75.em)) }
            Text(text, style = type.body, color = colors.text)
        }
    }
}

private fun restyled(paragraph: String, marks: Set<Char>, style: SpanStyle) = buildAnnotatedString {
    for (char in paragraph) {
        if (char in marks) withStyle(style) { append(char) } else append(char)
    }
}

private fun startsWithArabic(paragraph: String): Boolean {
    val first = paragraph.firstOrNull { it.isLetter() } ?: return false
    return when (first.code) {
        in 0x0600..0x06FF, in 0x0750..0x077F, in 0x08A0..0x08FF, in 0xFB50..0xFDFF, in 0xFE70..0xFEFF -> true
        else -> false
    }
}
