package com.muttaqi.android.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.muttaqi.android.R
import com.muttaqi.android.designsystem.component.ArabicText
import com.muttaqi.android.designsystem.component.QuranText
import com.muttaqi.android.designsystem.component.TranslationText
import com.muttaqi.shared.core.text.isArabicScript
import com.muttaqi.shared.feature.dua.domain.model.QuranicDua
import com.muttaqi.shared.feature.home.presentation.DailyCard
import com.muttaqi.shared.feature.home.presentation.HomeIntent
import com.muttaqi.shared.feature.quran.domain.model.DailyAyah
import com.muttaqi.shared.feature.topics.domain.model.HadithPassage

@Composable
internal fun SurahShortcut(surahNumber: Int, title: String, subtitle: String, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier.size(48.dp).background(MaterialTheme.colorScheme.primary, MaterialShapes.Cookie9Sided.toShape()),
                contentAlignment = Alignment.Center,
            ) {
                Text("$surahNumber", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimary)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(painterResource(R.drawable.ic_chevron_right), contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
internal fun AyahOfTheDayCard(dailyAyah: DailyAyah, onIntent: (HomeIntent) -> Unit) {
    DailyPassageCard(
        title = "Ayah of the Day",
        card = DailyCard.Ayah,
        onIntent = onIntent,
        onClick = { onIntent(HomeIntent.AyahOfTheDayTapped) },
        source = { Source("Quran (${dailyAyah.reference})") },
    ) {
        QuranText(dailyAyah.ayah.arabicText, Modifier.fillMaxWidth(), textAlign = TextAlign.Start)
        dailyAyah.ayah.translation?.let { TranslationText(it, Modifier.fillMaxWidth()) }
    }
}

@Composable
internal fun HadithOfTheDayCard(hadith: HadithPassage, onIntent: (HomeIntent) -> Unit) {
    DailyPassageCard(
        title = "Hadith of the Day",
        card = DailyCard.Hadith,
        onIntent = onIntent,
        source = { Source(hadith.source) },
    ) {
        if (hadith.arabic.isNotEmpty()) {
            ArabicText(hadith.arabic, Modifier.fillMaxWidth(), style = MaterialTheme.typography.titleLarge)
        }
        TranslationText(hadith.translation, Modifier.fillMaxWidth())
    }
}

@Composable
internal fun DuaOfTheDayCard(dua: QuranicDua, onIntent: (HomeIntent) -> Unit) {
    DailyPassageCard(
        title = "Dua of the Day",
        card = DailyCard.Dua,
        onIntent = onIntent,
        source = { Source("Quran (${dua.reference})") },
    ) {
        QuranText(dua.arabic, Modifier.fillMaxWidth(), textAlign = TextAlign.Start)
        if (dua.transliteration.isNotBlank()) {
            Text(dua.transliteration, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
        }
        TranslationText(dua.translation, Modifier.fillMaxWidth())
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
    val body: @Composable ColumnScope.() -> Unit = {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            content()
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.weight(1f)) { source() }
                IconButton(onClick = { onIntent(HomeIntent.CopyTapped(card)) }) {
                    Icon(painterResource(R.drawable.ic_content_copy), contentDescription = "Copy")
                }
                IconButton(onClick = { onIntent(HomeIntent.ShareTapped(card)) }) {
                    Icon(painterResource(R.drawable.ic_share), contentDescription = "Share")
                }
            }
        }
    }
    if (onClick != null) {
        Card(onClick = onClick, modifier = Modifier.fillMaxWidth(), content = body)
    } else {
        Card(Modifier.fillMaxWidth(), content = body)
    }
}

@Composable
private fun Source(text: String) {
    if (text.isArabicScript()) {
        TranslationText(
            text,
            Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
    } else {
        Text(text, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
    }
}
