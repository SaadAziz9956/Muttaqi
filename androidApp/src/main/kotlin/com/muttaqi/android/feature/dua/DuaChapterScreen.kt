package com.muttaqi.android.feature.dua

import android.content.ClipData
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.muttaqi.android.R
import com.muttaqi.android.designsystem.component.ArabicText
import com.muttaqi.android.designsystem.component.QuranText
import com.muttaqi.android.designsystem.component.TranslationText
import com.muttaqi.android.designsystem.oneui.OneUi
import com.muttaqi.android.designsystem.oneui.OneUiCountPill
import com.muttaqi.android.designsystem.oneui.OneUiDefaults
import com.muttaqi.android.designsystem.oneui.OneUiScaffold
import com.muttaqi.android.designsystem.oneui.OneUiSurface
import com.muttaqi.shared.core.share.SharePassage
import com.muttaqi.shared.feature.dua.domain.model.DuaEntry
import com.muttaqi.shared.feature.dua.presentation.chapter.DuaChapterEffect
import com.muttaqi.shared.feature.dua.presentation.chapter.DuaChapterIntent
import com.muttaqi.shared.feature.dua.presentation.chapter.DuaChapterState
import com.muttaqi.shared.feature.dua.presentation.chapter.DuaChapterViewModel
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun DuaChapterRoute(chapterId: String, onShare: (SharePassage) -> Unit, onBack: () -> Unit) {
    val viewModel = koinViewModel<DuaChapterViewModel> { parametersOf(chapterId) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is DuaChapterEffect.OpenShare -> onShare(effect.passage)
                is DuaChapterEffect.Copy -> scope.launch { clipboard.setClipEntry(ClipEntry(ClipData.newPlainText("Dua", effect.text))) }
            }
        }
    }
    DuaChapterScreen(state, viewModel::dispatch, onBack)
}

@Composable
fun DuaChapterScreen(state: DuaChapterState, onIntent: (DuaChapterIntent) -> Unit, onBack: () -> Unit) {
    val chapter = state.chapter
    OneUiSurface {
        OneUiScaffold(
            title = chapter?.title.orEmpty(),
            onBack = onBack,
            subtitle = chapter?.titleArabic?.takeIf { it.isNotBlank() }?.let { arabic ->
                {
                    ArabicText(
                        arabic,
                        Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.titleMedium,
                        color = OneUi.colors.secondaryText,
                        textAlign = TextAlign.Center,
                    )
                }
            },
        ) { padding ->
            LazyColumn(
                contentPadding = PaddingValues(
                    start = OneUiDefaults.ScreenMargin,
                    end = OneUiDefaults.ScreenMargin,
                    top = padding.calculateTopPadding(),
                    bottom = padding.calculateBottomPadding() + 16.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(chapter?.entries.orEmpty(), key = { it.id }) { entry ->
                    DuaEntryCard(
                        entry,
                        onShare = { onIntent(DuaChapterIntent.ShareTapped(entry.id)) },
                        onCopy = { onIntent(DuaChapterIntent.CopyTapped(entry.id)) },
                    )
                }
                if (state.translationCredits.isNotEmpty()) {
                    item(key = "credits") {
                        Text(
                            "Translation: " + state.translationCredits.joinToString(", "),
                            Modifier.fillMaxWidth().padding(top = 4.dp, start = 12.dp, end = 12.dp),
                            style = OneUi.typography.small,
                            color = OneUi.colors.secondaryText,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DuaEntryCard(entry: DuaEntry, onShare: () -> Unit, onCopy: () -> Unit) {
    val colors = OneUi.colors
    val type = OneUi.typography
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(OneUiDefaults.ContainerRadius))
            .background(colors.container)
            .padding(start = OneUiDefaults.ItemPadding, end = OneUiDefaults.ItemPadding, top = 22.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (entry.isQuran) {
            QuranText(entry.arabic, Modifier.fillMaxWidth(), color = colors.text, textAlign = TextAlign.Start)
        } else {
            ArabicText(entry.arabic, Modifier.fillMaxWidth(), color = colors.text)
        }
        if (entry.transliteration.isNotBlank()) {
            Text(entry.transliteration, style = type.caption.copy(fontSize = type.listSummary.fontSize), color = colors.accent)
        }
        TranslationText(entry.translation, Modifier.fillMaxWidth(), style = type.body, color = colors.text)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(entry.source, Modifier.weight(1f), style = type.caption, color = colors.accent)
            if (entry.repeatCount > 1) OneUiCountPill("${entry.repeatCount}×")
            CardAction(R.drawable.ic_content_copy, "Copy", onCopy)
            CardAction(R.drawable.ic_share, "Share", onShare)
        }
        entry.grade?.let { grade ->
            TranslationText(grade, Modifier.fillMaxWidth(), style = type.small, color = colors.secondaryText)
        }
        if (entry.reference.isNotBlank()) {
            ArabicText(entry.reference, Modifier.fillMaxWidth().padding(bottom = 8.dp), style = MaterialTheme.typography.bodySmall, color = colors.secondaryText)
        }
    }
}

@Composable
private fun CardAction(icon: Int, description: String, onClick: () -> Unit) {
    Box(Modifier.size(40.dp).clip(CircleShape).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Icon(painterResource(icon), contentDescription = description, Modifier.size(20.dp), tint = OneUi.colors.secondaryText)
    }
}
