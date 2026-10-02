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
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.sp
import com.muttaqi.android.designsystem.MuttaqiTheme
import com.muttaqi.android.designsystem.component.ArabicText
import com.muttaqi.android.designsystem.component.TranslationText
import com.muttaqi.shared.core.model.Language
import com.muttaqi.shared.feature.quran.domain.model.Surah
import com.muttaqi.shared.feature.quran.domain.model.TafsirEntry
import com.muttaqi.shared.feature.quran.presentation.tafsir.TafsirState
import com.muttaqi.shared.feature.quran.presentation.tafsir.TafsirStatus

@Composable
fun TafsirContent(surah: Surah?, state: TafsirState, startAyah: Int?, onRetry: () -> Unit) {
    val soft = MuttaqiTheme.soft
    Column(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(surah?.englishName ?: "Tafseer", style = MaterialTheme.typography.titleMedium, color = soft.textPrimary)
            Text("Tafseer Ibn Kathir", style = MaterialTheme.typography.bodySmall, color = soft.textSecondary)
            if (state.showsEnglishInstead) {
                Text(
                    "Hindi tafseer isn't available yet — showing English",
                    Modifier.padding(top = 4.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = soft.textSecondary,
                )
            }
        }
        when (val status = state.status) {
            TafsirStatus.Idle, TafsirStatus.Loading -> Loading(Modifier.fillMaxSize())
            is TafsirStatus.Loaded -> if (status.entries.isEmpty()) {
                Text(
                    "No tafseer available for this surah",
                    Modifier.fillMaxWidth().padding(top = 120.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = soft.textSecondary,
                    textAlign = TextAlign.Center,
                )
            } else {
                TafsirEntries(status.entries, state.language, state.entryCovering(startAyah ?: 0))
            }
            is TafsirStatus.Failed -> LoadFailed(status.message, onRetry, title = "Failed to load tafseer")
        }
    }
}

@Composable
private fun TafsirEntries(entries: List<TafsirEntry>, language: Language, start: TafsirEntry?) {
    val soft = MuttaqiTheme.soft
    val list = rememberLazyListState()
    LaunchedEffect(start) {
        val index = entries.indexOf(start)
        if (index >= 0) list.scrollToItem(index)
    }
    LazyColumn(state = list, contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)) {
        items(entries, key = { it.ayahNumber }) { entry ->
            Column(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    if (entry.lastAyahNumber > entry.ayahNumber) "Ayah ${entry.ayahNumber}–${entry.lastAyahNumber}" else "Ayah ${entry.ayahNumber}",
                    style = MaterialTheme.typography.labelLarge,
                    color = soft.appPrimary,
                )
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    entry.paragraphs.forEach { TafsirParagraph(it, language) }
                }
                Hairline(Modifier.fillMaxWidth().padding(top = 4.dp))
            }
        }
    }
}

@Composable
private fun TafsirParagraph(paragraph: String, language: Language) {
    val soft = MuttaqiTheme.soft
    when {
        language == Language.Urdu ->
            TranslationText(paragraph, Modifier.fillMaxWidth(), fontSize = 16.sp, textAlign = TextAlign.Right, lineSpacing = 0.sp)
        startsWithArabic(paragraph) ->
            ArabicText(paragraph, Modifier.fillMaxWidth(), fontSize = 20.sp, textAlign = TextAlign.Right, lineSpacing = 0.sp)
        else -> {
            val text = remember(paragraph) { restyled(paragraph, setOf('ﷺ'), SpanStyle(fontFamily = FontFamily.Default, fontSize = 9.sp)) }
            Text(text, style = MaterialTheme.typography.bodySmall, color = soft.textPrimary)
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
