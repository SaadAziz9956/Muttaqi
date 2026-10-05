package com.muttaqi.android.feature.quran

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muttaqi.android.R
import com.muttaqi.android.designsystem.component.QuranText
import com.muttaqi.shared.feature.quran.domain.model.FontSize
import com.muttaqi.shared.feature.quran.domain.model.ReadingMode
import com.muttaqi.shared.feature.quran.presentation.MushafSamples
import com.muttaqi.shared.feature.quran.presentation.settings.ReadingSettingsIntent
import com.muttaqi.shared.feature.quran.presentation.settings.ReadingSettingsState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ReadingSettingsContent(state: ReadingSettingsState, onIntent: (ReadingSettingsIntent) -> Unit) {
    val itemColors = ListItemDefaults.colors(containerColor = BottomSheetDefaults.ContainerColor)
    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(bottom = 24.dp)) {
        SectionTitle("Reading mode")
        ConnectedChoice(
            options = listOf(ReadingMode.WithTranslation to "With Translation", ReadingMode.ArabicOnly to "Arabic Only"),
            selected = state.mode,
            onSelect = { onIntent(ReadingSettingsIntent.ModeSelected(it)) },
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        ReadingPreview(state.mode, state.fontSize, Modifier.padding(16.dp))
        ListItem(
            headlineContent = { Text("Font Size") },
            trailingContent = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilledTonalIconButton(onClick = { onIntent(ReadingSettingsIntent.FontSizeDecreased) }) {
                        Icon(painterResource(R.drawable.ic_remove), contentDescription = "Smaller")
                    }
                    Text(
                        "${state.fontSize.percentage}%",
                        Modifier.widthIn(min = 64.dp),
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                    )
                    FilledTonalIconButton(onClick = { onIntent(ReadingSettingsIntent.FontSizeIncreased) }) {
                        Icon(painterResource(R.drawable.ic_add), contentDescription = "Larger")
                    }
                }
            },
            colors = itemColors,
        )
        SectionTitle("Translation", Modifier.padding(top = 8.dp))
        Column(Modifier.selectableGroup()) {
            state.languages.forEach { language ->
                val selected = state.language == language
                ListItem(
                    headlineContent = { Text(language.displayName) },
                    leadingContent = { RadioButton(selected = selected, onClick = null) },
                    modifier = Modifier.selectable(
                        selected = selected,
                        role = Role.RadioButton,
                        onClick = { onIntent(ReadingSettingsIntent.LanguageSelected(language)) },
                    ),
                    colors = itemColors,
                )
            }
        }
        if (state.isDownloadingLanguage) {
            Row(
                Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LoadingIndicator(Modifier.size(32.dp))
                Text(
                    "Downloading translation...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String, modifier: Modifier = Modifier) {
    Text(
        title,
        modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
    )
}

@Composable
private fun ReadingPreview(mode: ReadingMode, fontSize: FontSize, modifier: Modifier = Modifier) {
    OutlinedCard(modifier.fillMaxWidth()) {
        when (mode) {
            ReadingMode.WithTranslation -> Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                QuranText(
                    MushafSamples.BISMILLAH_OPENING_WORDS,
                    Modifier.fillMaxWidth(),
                    fontSize = fontSize.arabicSize.sp,
                    textAlign = TextAlign.Right,
                    lineSpacing = 0.sp,
                )
                Text(
                    "Bismillaahir",
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = fontSize.transliterationSize.sp,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text("In the name of Allah", style = MaterialTheme.typography.bodyLarge, fontSize = fontSize.translationSize.sp)
                HorizontalDivider(Modifier.padding(vertical = 4.dp))
                QuranText(
                    MushafSamples.ALHAMDU_OPENING_WORDS,
                    Modifier.fillMaxWidth(),
                    fontSize = fontSize.arabicSize.sp,
                    textAlign = TextAlign.Right,
                    lineSpacing = 0.sp,
                )
                Text(
                    "Alhamdu lillaahi",
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = fontSize.transliterationSize.sp,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            ReadingMode.ArabicOnly -> QuranText(
                MushafSamples.FATIHA_OPENING,
                Modifier.fillMaxWidth().padding(16.dp),
                fontSize = fontSize.arabicSize.sp,
                textAlign = TextAlign.Right,
                lineSpacing = 0.sp,
            )
        }
    }
}
