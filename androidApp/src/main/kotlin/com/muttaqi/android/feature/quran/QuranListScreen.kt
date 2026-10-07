package com.muttaqi.android.feature.quran

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.muttaqi.android.designsystem.component.ArabicText
import com.muttaqi.android.designsystem.component.DelayedLoadingIndicator
import com.muttaqi.android.designsystem.component.FadeBetween
import com.muttaqi.android.designsystem.component.TranslationText
import com.muttaqi.android.designsystem.oneui.LocalTabBarInset
import com.muttaqi.android.designsystem.oneui.OneUiHeaderQuote
import com.muttaqi.android.designsystem.oneui.OneUi
import com.muttaqi.android.designsystem.oneui.OneUiButton
import com.muttaqi.android.designsystem.oneui.OneUiCard
import com.muttaqi.android.designsystem.oneui.OneUiDefaults
import com.muttaqi.android.designsystem.oneui.OneUiScaffold
import com.muttaqi.android.designsystem.oneui.OneUiSearchField
import com.muttaqi.android.designsystem.oneui.OneUiSegmented
import com.muttaqi.android.designsystem.oneui.OneUiSubheader
import com.muttaqi.android.designsystem.oneui.OneUiSurface
import com.muttaqi.shared.core.quote.DisplayedQuote
import com.muttaqi.shared.core.text.quoted
import com.muttaqi.shared.feature.quran.domain.model.ReadingProgress
import com.muttaqi.shared.feature.quran.domain.model.Surah
import com.muttaqi.shared.feature.quran.presentation.list.QuranListEffect
import com.muttaqi.shared.feature.quran.presentation.list.QuranListIntent
import com.muttaqi.shared.feature.quran.presentation.list.QuranListState
import com.muttaqi.shared.feature.quran.presentation.list.QuranListViewModel
import com.muttaqi.shared.feature.quran.presentation.list.RevelationFilter
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun QuranListRoute(onOpenSurah: (surahNumber: Int, startAyah: Int) -> Unit) {
    val viewModel = koinViewModel<QuranListViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is QuranListEffect.OpenSurah -> onOpenSurah(effect.surahNumber, 0)
                is QuranListEffect.ContinueReading -> onOpenSurah(effect.surahNumber, effect.ayahNumber)
            }
        }
    }
    LifecycleResumeEffect(viewModel) {
        viewModel.dispatch(QuranListIntent.Appeared)
        onPauseOrDispose {}
    }
    QuranListScreen(state, viewModel::dispatch)
}

@Composable
fun QuranListScreen(state: QuranListState, onIntent: (QuranListIntent) -> Unit) {
    OneUiSurface {
        OneUiScaffold(
            title = "The Quran",
            subtitle = state.header?.let { header -> { OneUiHeaderQuote(header.text, header.source) } },
        ) { padding ->
            FadeBetween(state, key = { it.isLoading to (it.error != null) }) { state ->
                when {
                    state.isLoading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                        DelayedLoadingIndicator()
                    }
                    state.error != null -> LoadFailed(
                        state.error.orEmpty(),
                        onRetry = { onIntent(QuranListIntent.Retry) },
                        modifier = Modifier.padding(padding),
                    )
                    else -> SurahList(state, onIntent, padding)
                }
            }
        }
    }
}

@Composable
private fun SurahList(state: QuranListState, onIntent: (QuranListIntent) -> Unit, padding: PaddingValues) {
    val surahs = state.visibleSurahs
    LazyColumn(
        contentPadding = PaddingValues(
            top = padding.calculateTopPadding(),
            bottom = padding.calculateBottomPadding() + LocalTabBarInset.current + 16.dp,
        ),
    ) {
        state.readingProgress?.let { progress ->
            item(key = "continue") {
                ContinueReadingCard(
                    progress,
                    Modifier.animateItem().padding(bottom = OneUiDefaults.GroupGap),
                    onContinue = { onIntent(QuranListIntent.ContinueTapped) },
                )
            }
        }
        item(key = "search") {
            OneUiSearchField(
                query = state.query,
                onQueryChange = { onIntent(QuranListIntent.QueryChanged(it)) },
                onClear = { onIntent(QuranListIntent.ClearQuery) },
                placeholder = "Search surah or number",
                modifier = Modifier.padding(bottom = 12.dp),
            )
        }
        item(key = "filter") {
            OneUiSegmented(
                options = RevelationFilter.entries.map { it.label },
                selected = RevelationFilter.entries.indexOf(state.filter),
                onSelect = { onIntent(QuranListIntent.FilterSelected(RevelationFilter.entries[it])) },
                modifier = Modifier.padding(start = OneUiDefaults.ScreenMargin, end = OneUiDefaults.ScreenMargin, bottom = OneUiDefaults.GroupGap),
            )
        }
        if (surahs.isEmpty()) {
            item(key = "empty") {
                Text(
                    "No results for “${state.query}”",
                    Modifier.fillMaxWidth().padding(24.dp),
                    style = OneUi.typography.listSummary,
                    color = OneUi.colors.secondaryText,
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            item(key = "count") { OneUiSubheader(surahCount(surahs.size), Modifier.animateItem()) }
        }
        itemsIndexed(surahs, key = { _, surah -> surah.number }) { index, surah ->
            SurahRow(
                surah,
                first = index == 0,
                last = index == surahs.lastIndex,
                modifier = Modifier.animateItem(),
                onClick = { onIntent(QuranListIntent.SurahTapped(surah.number)) },
            )
        }
    }
}

@Composable
private fun ContinueReadingCard(progress: ReadingProgress, modifier: Modifier = Modifier, onContinue: () -> Unit) {
    val colors = OneUi.colors
    val type = OneUi.typography
    OneUiCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = OneUiDefaults.ScreenMargin)
            .semantics { contentDescription = "Continue reading Surah ${progress.surahEnglishName}, ayah ${progress.lastAyahNumber}" }
            .animateContentSize(),
        onClick = onContinue,
    ) {
        Text("Continue reading", style = type.subheader, color = colors.accent)
        Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(progress.surahEnglishName, style = type.sectionTitle.copy(fontSize = 24.sp, lineHeight = 30.sp), color = colors.text)
                Text("Ayah ${progress.lastAyahNumber}", style = type.listSummary, color = colors.secondaryText)
            }
            ArabicText(progress.surahName, Modifier.padding(start = 12.dp), style = type.sectionTitle.copy(fontSize = 22.sp), color = colors.accent)
        }
        OneUiButton("Continue", onContinue, Modifier.padding(top = 16.dp))
    }
}

@Composable
private fun SurahRow(surah: Surah, first: Boolean, last: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val colors = OneUi.colors
    val type = OneUi.typography
    val top = if (first) OneUiDefaults.ContainerRadius else 0.dp
    val bottom = if (last) OneUiDefaults.ContainerRadius else 0.dp
    Column(
        modifier
            .fillMaxWidth()
            .padding(horizontal = OneUiDefaults.ScreenMargin)
            .clip(RoundedCornerShape(topStart = top, topEnd = top, bottomStart = bottom, bottomEnd = bottom))
            .background(colors.container)
            .clickable(onClick = onClick),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .heightIn(min = 72.dp)
                .padding(horizontal = OneUiDefaults.ItemPadding, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NumberBadge(surah.number)
            Spacer(Modifier.width(BADGE_GAP))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(surah.englishName, style = type.listTitle, color = colors.text)
                Text(surah.englishNameTranslation, style = type.listSummary, color = colors.secondaryText)
                Text("${surah.numberOfAyahs} ayahs · ${surah.revelationType}", style = type.small, color = colors.secondaryText)
            }
            Spacer(Modifier.width(12.dp))
            ArabicText(surah.name, style = type.listTitle.copy(fontSize = 20.sp), color = colors.accent)
        }
        if (!last) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(start = OneUiDefaults.ItemPadding + BADGE_SIZE + BADGE_GAP, end = OneUiDefaults.ItemPadding)
                    .height(1.dp)
                    .background(colors.divider),
            )
        }
    }
}

@Composable
private fun NumberBadge(number: Int) {
    Box(Modifier.size(BADGE_SIZE).clip(CircleShape).background(OneUi.colors.component), contentAlignment = Alignment.Center) {
        Text("$number", style = OneUi.typography.caption.copy(fontWeight = FontWeight.SemiBold), color = OneUi.colors.text)
    }
}

private fun surahCount(count: Int) = if (count == 1) "1 surah" else "$count surahs"

private val BADGE_SIZE = 36.dp
private val BADGE_GAP = 16.dp

@Composable
internal fun LoadFailed(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Failed to load",
    suggestion: String? = null,
) {
    val colors = OneUi.colors
    val type = OneUi.typography
    Column(
        modifier.fillMaxSize().padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(title, style = type.dialogTitle, color = colors.text, textAlign = TextAlign.Center)
        Text(message, style = type.listSummary, color = colors.secondaryText, textAlign = TextAlign.Center)
        if (suggestion != null) {
            Text(suggestion, style = type.caption, color = colors.secondaryText, textAlign = TextAlign.Center)
        }
        OneUiButton("Retry", onRetry, Modifier.padding(top = 8.dp))
    }
}
