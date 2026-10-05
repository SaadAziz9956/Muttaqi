package com.muttaqi.android.feature.quran

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muttaqi.android.R
import com.muttaqi.android.designsystem.MuttaqiTheme
import com.muttaqi.android.designsystem.component.ArabicText
import com.muttaqi.shared.core.model.Language
import com.muttaqi.shared.feature.quran.domain.model.ReadingMode
import com.muttaqi.shared.feature.quran.presentation.MushafSamples
import com.muttaqi.shared.feature.quran.presentation.settings.ReadingSettingsIntent
import com.muttaqi.shared.feature.quran.presentation.settings.ReadingSettingsState

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ReadingSettingsContent(state: ReadingSettingsState, onIntent: (ReadingSettingsIntent) -> Unit, onChooseLanguage: () -> Unit) {
    val soft = MuttaqiTheme.soft
    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 48.dp)) {
        Text("Reading mode", Modifier.padding(top = 8.dp), style = MaterialTheme.typography.titleSmall, color = soft.textPrimary)
        Row(Modifier.padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ReadingModeCard(ReadingMode.WithTranslation, "With Translation", state.mode == ReadingMode.WithTranslation, onIntent, Modifier.weight(1f))
            ReadingModeCard(ReadingMode.ArabicOnly, "Arabic Only", state.mode == ReadingMode.ArabicOnly, onIntent, Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth().padding(top = 28.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Font Size", style = MaterialTheme.typography.bodyMedium, color = soft.textPrimary)
            Spacer(Modifier.weight(1f))
            IconButton(onClick = { onIntent(ReadingSettingsIntent.FontSizeDecreased) }, Modifier.size(28.dp)) {
                Icon(painterResource(R.drawable.ic_minus_circle_bold), "Smaller", Modifier.size(28.dp), tint = soft.appPrimary)
            }
            Text(
                "${state.fontSize.percentage}%",
                Modifier.width(66.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = soft.textPrimary,
                textAlign = TextAlign.Center,
            )
            IconButton(onClick = { onIntent(ReadingSettingsIntent.FontSizeIncreased) }, Modifier.size(28.dp)) {
                Icon(painterResource(R.drawable.ic_add_circle_bold), "Larger", Modifier.size(28.dp), tint = soft.appPrimary)
            }
        }
        Row(
            Modifier.fillMaxWidth().padding(top = 28.dp).clickable(onClick = onChooseLanguage),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Translation", style = MaterialTheme.typography.bodyMedium, color = soft.textPrimary)
            Spacer(Modifier.weight(1f))
            Text(state.language.displayName, style = MaterialTheme.typography.bodyMedium, color = soft.textPrimary)
            Icon(painterResource(R.drawable.ic_arrow_down_02_linear), null, Modifier.padding(start = 4.dp).size(14.dp), tint = soft.textSecondary)
        }
        if (state.isDownloadingLanguage) {
            Row(Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                LoadingIndicator(Modifier.size(24.dp), color = soft.appPrimary)
                Text("Downloading translation...", style = MaterialTheme.typography.bodySmall, color = soft.textSecondary)
            }
        }
    }
}

@Composable
private fun ReadingModeCard(
    mode: ReadingMode,
    label: String,
    isSelected: Boolean,
    onIntent: (ReadingSettingsIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val soft = MuttaqiTheme.soft
    val shape = RoundedCornerShape(8.dp)
    Column(modifier.clickable { onIntent(ReadingSettingsIntent.ModeSelected(mode)) }, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(if (isSelected) soft.appPrimary.copy(alpha = 0.05f) else systemGray6(), shape)
                .border(if (isSelected) 2.dp else 1.dp, if (isSelected) soft.appPrimary else systemGray4(), shape)
                .padding(8.dp),
            contentAlignment = Alignment.Center,
        ) {
            when (mode) {
                ReadingMode.WithTranslation -> Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    ArabicText(MushafSamples.BISMILLAH_OPENING_WORDS, fontSize = 12.sp, lineSpacing = 0.sp)
                    Text("Bismillaahir", fontSize = 8.sp, fontFamily = FontFamily.Default, color = soft.appPrimary)
                    Text("In the name of Allah", fontSize = 7.sp, fontFamily = FontFamily.Default, color = soft.textSecondary)
                    Box(Modifier.padding(horizontal = 12.dp).fillMaxWidth().height(0.5.dp).background(systemGray4()))
                    ArabicText(MushafSamples.ALHAMDU_OPENING_WORDS, fontSize = 12.sp, lineSpacing = 0.sp)
                    Text("Alhamdu lillaahi", fontSize = 8.sp, fontFamily = FontFamily.Default, color = soft.appPrimary)
                }
                ReadingMode.ArabicOnly -> ArabicText(
                    MushafSamples.FATIHA_OPENING,
                    Modifier.padding(horizontal = 8.dp),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Right,
                    lineSpacing = 0.sp,
                )
            }
        }
        Text(
            label,
            Modifier.padding(top = 8.dp),
            style = MaterialTheme.typography.labelMedium,
            color = if (isSelected) soft.appPrimary else soft.textSecondary,
        )
    }
}

@Composable
fun LanguagePickerContent(state: ReadingSettingsState, onSelect: (ReadingSettingsIntent) -> Unit) {
    val soft = MuttaqiTheme.soft
    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 48.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Choose Language", Modifier.padding(top = 8.dp), style = MaterialTheme.typography.titleMedium, color = soft.textPrimary)
        Column(Modifier.padding(top = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            state.languages.forEach { language ->
                LanguageRow(language, selected = state.language == language) { onSelect(ReadingSettingsIntent.LanguageSelected(language)) }
            }
        }
    }
}

@Composable
private fun LanguageRow(language: Language, selected: Boolean, onClick: () -> Unit) {
    val soft = MuttaqiTheme.soft
    val shape = RoundedCornerShape(8.dp)
    Text(
        language.displayName,
        Modifier
            .fillMaxWidth()
            .border(if (selected) 2.dp else 1.dp, if (selected) soft.appPrimary else systemGray4(), shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        style = MaterialTheme.typography.bodyLarge,
        color = soft.textPrimary,
    )
}

@Composable
private fun systemGray4(): Color = if (MuttaqiTheme.soft.dark) Color(0xFF3A3A3C) else Color(0xFFD1D1D6)

@Composable
private fun systemGray6(): Color = if (MuttaqiTheme.soft.dark) Color(0xFF1C1C1E) else Color(0xFFF2F2F7)
