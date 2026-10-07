package com.muttaqi.android.feature.quran

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muttaqi.android.R
import com.muttaqi.android.designsystem.component.QuranText
import com.muttaqi.android.designsystem.oneui.OneUi
import com.muttaqi.android.designsystem.oneui.OneUiDefaults
import com.muttaqi.android.designsystem.oneui.OneUiGroup
import com.muttaqi.android.designsystem.oneui.OneUiIconButton
import com.muttaqi.android.designsystem.oneui.OneUiListRow
import com.muttaqi.android.designsystem.oneui.OneUiProgress
import com.muttaqi.android.designsystem.oneui.OneUiSegmented
import com.muttaqi.android.designsystem.oneui.OneUiSubheader
import com.muttaqi.shared.feature.quran.domain.model.FontSize
import com.muttaqi.shared.feature.quran.domain.model.ReadingMode
import com.muttaqi.shared.feature.quran.presentation.MushafSamples
import com.muttaqi.shared.feature.quran.presentation.settings.ReadingSettingsIntent
import com.muttaqi.shared.feature.quran.presentation.settings.ReadingSettingsState

@Composable
fun ReadingSettingsContent(state: ReadingSettingsState, onIntent: (ReadingSettingsIntent) -> Unit) {
    val colors = OneUi.colors
    val type = OneUi.typography
    val modes = listOf(ReadingMode.WithTranslation to "With translation", ReadingMode.ArabicOnly to "Arabic only")
    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(bottom = 24.dp)) {
        Text(
            "Reading settings",
            Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 4.dp, bottom = 20.dp),
            style = type.sectionTitle,
            color = colors.text,
            textAlign = TextAlign.Center,
        )
        OneUiSubheader("Reading mode")
        OneUiGroup {
            OneUiSegmented(
                options = modes.map { it.second },
                selected = modes.indexOfFirst { it.first == state.mode },
                onSelect = { onIntent(ReadingSettingsIntent.ModeSelected(modes[it].first)) },
                modifier = Modifier.padding(16.dp),
            )
            ReadingPreview(state.mode, state.fontSize, Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp))
            OneUiListRow(
                title = "Font size",
                trailing = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OneUiIconButton(
                            R.drawable.ic_remove,
                            "Smaller",
                            onClick = { onIntent(ReadingSettingsIntent.FontSizeDecreased) },
                            background = colors.component,
                            size = 40.dp,
                            iconSize = 20.dp,
                        )
                        Text(
                            "${state.fontSize.percentage}%",
                            Modifier.widthIn(min = 64.dp),
                            style = type.listTitle,
                            color = colors.text,
                            textAlign = TextAlign.Center,
                        )
                        OneUiIconButton(
                            R.drawable.ic_add,
                            "Larger",
                            onClick = { onIntent(ReadingSettingsIntent.FontSizeIncreased) },
                            background = colors.component,
                            size = 40.dp,
                            iconSize = 20.dp,
                        )
                    }
                },
            )
        }
        Spacer(Modifier.height(OneUiDefaults.GroupGap))
        OneUiSubheader("Translation")
        OneUiGroup(Modifier.selectableGroup()) {
            state.languages.forEachIndexed { index, language ->
                LanguageRow(
                    name = language.displayName,
                    selected = state.language == language,
                    divider = index < state.languages.lastIndex || state.isDownloadingLanguage,
                    onClick = { onIntent(ReadingSettingsIntent.LanguageSelected(language)) },
                )
            }
            if (state.isDownloadingLanguage) {
                Row(
                    Modifier.fillMaxWidth().heightIn(min = 60.dp).padding(horizontal = RADIO_START, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(RADIO_GAP),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OneUiProgress(size = RADIO_SIZE)
                    Text("Downloading translation...", style = type.listSummary, color = colors.secondaryText)
                }
            }
        }
    }
}

@Composable
private fun LanguageRow(name: String, selected: Boolean, divider: Boolean, onClick: () -> Unit) {
    val colors = OneUi.colors
    Column(Modifier.fillMaxWidth().selectable(selected = selected, role = Role.RadioButton, onClick = onClick)) {
        Row(
            Modifier.fillMaxWidth().heightIn(min = 60.dp).padding(horizontal = RADIO_START, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RadioButton(
                selected = selected,
                onClick = null,
                colors = RadioButtonDefaults.colors(selectedColor = colors.accent, unselectedColor = colors.secondaryText),
            )
            Spacer(Modifier.width(RADIO_GAP))
            Text(name, style = OneUi.typography.listTitle, color = colors.text)
        }
        if (divider) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(start = RADIO_START + RADIO_SIZE + RADIO_GAP, end = OneUiDefaults.ItemPadding)
                    .height(1.dp)
                    .background(colors.divider),
            )
        }
    }
}

@Composable
private fun ReadingPreview(mode: ReadingMode, fontSize: FontSize, modifier: Modifier = Modifier) {
    val colors = OneUi.colors
    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(colors.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        when (mode) {
            ReadingMode.WithTranslation -> {
                QuranText(
                    MushafSamples.BISMILLAH_OPENING_WORDS,
                    Modifier.fillMaxWidth(),
                    fontSize = fontSize.arabicSize.sp,
                    color = colors.text,
                    textAlign = TextAlign.Right,
                    lineSpacing = 0.sp,
                )
                Text("Bismillaahir", style = previewStyle(fontSize.transliterationSize), color = colors.accent)
                Text("In the name of Allah", style = previewStyle(fontSize.translationSize), color = colors.text)
                Box(Modifier.fillMaxWidth().padding(vertical = 4.dp).height(1.dp).background(colors.divider))
                QuranText(
                    MushafSamples.ALHAMDU_OPENING_WORDS,
                    Modifier.fillMaxWidth(),
                    fontSize = fontSize.arabicSize.sp,
                    color = colors.text,
                    textAlign = TextAlign.Right,
                    lineSpacing = 0.sp,
                )
                Text("Alhamdu lillaahi", style = previewStyle(fontSize.transliterationSize), color = colors.accent)
            }
            ReadingMode.ArabicOnly -> QuranText(
                MushafSamples.FATIHA_OPENING,
                Modifier.fillMaxWidth(),
                fontSize = fontSize.arabicSize.sp,
                color = colors.text,
                textAlign = TextAlign.Right,
                lineSpacing = 0.sp,
            )
        }
    }
}

@Composable
private fun previewStyle(size: Double): TextStyle = OneUi.typography.body.copy(fontSize = size.sp, lineHeight = (size * 1.4).sp)

private val RADIO_START: Dp = OneUiDefaults.ItemPadding - 2.dp
private val RADIO_SIZE: Dp = 24.dp
private val RADIO_GAP: Dp = 16.dp
