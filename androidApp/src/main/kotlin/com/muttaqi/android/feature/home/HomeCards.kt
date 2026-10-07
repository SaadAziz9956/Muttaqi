package com.muttaqi.android.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.muttaqi.android.R
import com.muttaqi.android.designsystem.component.ArabicText
import com.muttaqi.android.designsystem.component.QuranText
import com.muttaqi.android.designsystem.component.TranslationText
import com.muttaqi.android.designsystem.oneui.OneUi
import com.muttaqi.android.designsystem.oneui.OneUiCard
import com.muttaqi.android.designsystem.oneui.OneUiDefaults
import com.muttaqi.android.designsystem.oneui.OneUiIconButton
import com.muttaqi.shared.core.text.isArabicScript
import com.muttaqi.shared.feature.dua.domain.model.QuranicDua
import com.muttaqi.shared.feature.home.presentation.DailyCard
import com.muttaqi.shared.feature.home.presentation.HomeIntent
import com.muttaqi.shared.feature.quran.domain.model.DailyAyah
import com.muttaqi.shared.feature.topics.domain.model.HadithPassage

internal class SurahShortcut(val surahNumber: Int, val title: String, val subtitle: String, val onClick: () -> Unit)

@Composable
internal fun SurahShortcuts(shortcuts: List<SurahShortcut>) {
    OneUiCard(Modifier.fillMaxWidth(), contentPadding = PaddingValues(0.dp)) {
        shortcuts.forEachIndexed { index, shortcut ->
            SurahShortcutRow(shortcut, divider = index < shortcuts.lastIndex)
        }
    }
}

@Composable
private fun SurahShortcutRow(shortcut: SurahShortcut, divider: Boolean) {
    val colors = OneUi.colors
    val type = OneUi.typography
    Column(Modifier.fillMaxWidth().clickable(onClick = shortcut.onClick)) {
        Row(
            Modifier.fillMaxWidth().heightIn(min = 72.dp).padding(start = 16.dp, end = 14.dp, top = 12.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(44.dp).background(colors.accent.copy(alpha = 0.12f), CircleShape), contentAlignment = Alignment.Center) {
                Text("${shortcut.surahNumber}", style = type.listSummary.copy(fontWeight = FontWeight.SemiBold), color = colors.accent)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(shortcut.title, style = type.listTitle, color = colors.text)
                Text(shortcut.subtitle, style = type.listSummary, color = colors.secondaryText)
            }
            Icon(painterResource(R.drawable.ic_chevron_right), contentDescription = null, Modifier.size(22.dp), tint = colors.secondaryText)
        }
        if (divider) {
            Box(Modifier.fillMaxWidth().padding(start = 74.dp, end = OneUiDefaults.ItemPadding).height(1.dp).background(colors.divider))
        }
    }
}

@Composable
internal fun AyahOfTheDayCard(dailyAyah: DailyAyah, onIntent: (HomeIntent) -> Unit) {
    val colors = OneUi.colors
    DailyPassageCard(
        title = "Ayah of the Day",
        card = DailyCard.Ayah,
        onIntent = onIntent,
        onClick = { onIntent(HomeIntent.AyahOfTheDayTapped) },
        source = { Source("Quran (${dailyAyah.reference})") },
    ) {
        QuranText(dailyAyah.ayah.arabicText, Modifier.fillMaxWidth(), color = colors.text, textAlign = TextAlign.Start)
        dailyAyah.ayah.translation?.let { TranslationText(it, Modifier.fillMaxWidth(), style = OneUi.typography.body, color = colors.text) }
    }
}

@Composable
internal fun HadithOfTheDayCard(hadith: HadithPassage, onIntent: (HomeIntent) -> Unit) {
    val colors = OneUi.colors
    DailyPassageCard(
        title = "Hadith of the Day",
        card = DailyCard.Hadith,
        onIntent = onIntent,
        source = { Source(hadith.source) },
    ) {
        if (hadith.arabic.isNotEmpty()) {
            ArabicText(hadith.arabic, Modifier.fillMaxWidth(), style = MaterialTheme.typography.titleLarge, color = colors.text)
        }
        TranslationText(hadith.translation, Modifier.fillMaxWidth(), style = OneUi.typography.body, color = colors.text)
    }
}

@Composable
internal fun DuaOfTheDayCard(dua: QuranicDua, onIntent: (HomeIntent) -> Unit) {
    val colors = OneUi.colors
    val type = OneUi.typography
    DailyPassageCard(
        title = "Dua of the Day",
        card = DailyCard.Dua,
        onIntent = onIntent,
        source = { Source("Quran (${dua.reference})") },
    ) {
        QuranText(dua.arabic, Modifier.fillMaxWidth(), color = colors.text, textAlign = TextAlign.Start)
        if (dua.transliteration.isNotBlank()) {
            Text(dua.transliteration, style = type.caption.copy(fontSize = type.listSummary.fontSize), color = colors.accent)
        }
        TranslationText(dua.translation, Modifier.fillMaxWidth(), style = type.body, color = colors.text)
    }
}

@Composable
private fun DailyPassageCard(
    title: String,
    card: DailyCard,
    onIntent: (HomeIntent) -> Unit,
    source: @Composable () -> Unit,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = OneUi.colors
    OneUiCard(
        Modifier.fillMaxWidth(),
        onClick = onClick,
        contentPadding = PaddingValues(start = OneUiDefaults.ItemPadding, end = 8.dp, top = 20.dp, bottom = 8.dp),
    ) {
        Column(Modifier.padding(end = 12.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(title, style = OneUi.typography.sectionTitle, color = colors.text)
            content()
        }
        Row(Modifier.padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.weight(1f)) { source() }
            OneUiIconButton(
                R.drawable.ic_content_copy,
                "Copy",
                onClick = { onIntent(HomeIntent.CopyTapped(card)) },
                tint = colors.secondaryText,
                iconSize = 20.dp,
            )
            OneUiIconButton(
                R.drawable.ic_share,
                "Share",
                onClick = { onIntent(HomeIntent.ShareTapped(card)) },
                tint = colors.secondaryText,
                iconSize = 20.dp,
            )
        }
    }
}

@Composable
private fun Source(text: String) {
    val style = OneUi.typography.caption.copy(fontWeight = FontWeight.Medium)
    if (text.isArabicScript()) {
        TranslationText(text, Modifier.fillMaxWidth(), style = style, color = OneUi.colors.accent)
    } else {
        Text(text, style = style, color = OneUi.colors.accent)
    }
}
