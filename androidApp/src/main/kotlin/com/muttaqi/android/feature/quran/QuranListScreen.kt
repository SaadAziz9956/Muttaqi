package com.muttaqi.android.feature.quran

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.muttaqi.android.designsystem.MuttaqiTheme
import com.muttaqi.android.designsystem.component.ArabicText
import com.muttaqi.android.designsystem.component.PageHeader
import com.muttaqi.android.designsystem.component.SoftArtwork
import com.muttaqi.android.designsystem.component.SoftBackdrop
import com.muttaqi.android.designsystem.component.SoftCard
import com.muttaqi.android.designsystem.component.SoftChip
import com.muttaqi.android.designsystem.component.SoftPillSurface
import com.muttaqi.android.designsystem.component.SoftSearchField
import com.muttaqi.shared.feature.quran.domain.model.ReadingProgress
import com.muttaqi.shared.feature.quran.domain.model.Surah
import com.muttaqi.shared.feature.quran.presentation.list.QuranListEffect
import com.muttaqi.shared.feature.quran.presentation.list.QuranListIntent
import com.muttaqi.shared.feature.quran.presentation.list.QuranListState
import com.muttaqi.shared.feature.quran.presentation.list.QuranListViewModel
import com.muttaqi.shared.feature.quran.presentation.list.RevelationFilter
import org.koin.compose.viewmodel.koinViewModel

/** The Quran tab's first screen, wired to its shared view model */
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
    // Back from a surah, the reading position may have moved
    LifecycleResumeEffect(viewModel) {
        viewModel.dispatch(QuranListIntent.Appeared)
        onPauseOrDispose {}
    }
    QuranListScreen(state, viewModel::dispatch)
}

/** The hadith under the title, where the reader left off, and the surahs to search and filter by where they were revealed */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun QuranListScreen(state: QuranListState, onIntent: (QuranListIntent) -> Unit) {
    val soft = MuttaqiTheme.soft
    Box {
        SoftBackdrop()
        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                LoadingIndicator(color = soft.appPrimary)
            }
            state.error != null -> LoadFailed(state.error.orEmpty(), onRetry = { onIntent(QuranListIntent.Retry) })
            else -> LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.statusBarsPadding(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    PageHeader("The Quran", state.header, Modifier.padding(bottom = 10.dp))
                }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    AnimatedVisibility(state.readingProgress != null, enter = fadeIn(), exit = fadeOut()) {
                        state.readingProgress?.let { ContinueReadingCard(it, onContinue = { onIntent(QuranListIntent.ContinueTapped) }) }
                    }
                }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    SoftSearchField(
                        query = state.query,
                        onQueryChange = { onIntent(QuranListIntent.QueryChanged(it)) },
                        onClear = { onIntent(QuranListIntent.ClearQuery) },
                        placeholder = "Search surah or number",
                    )
                }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        RevelationFilter.entries.forEach { filter ->
                            SoftChip(filter.label, selected = state.filter == filter, onClick = { onIntent(QuranListIntent.FilterSelected(filter)) })
                        }
                    }
                }
                if (state.visibleSurahs.isEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Text(
                            "No results for “${state.query}”",
                            Modifier.fillMaxWidth().padding(top = 32.dp),
                            style = MaterialTheme.typography.titleSmall,
                            color = soft.textSecondary,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
                items(state.visibleSurahs, key = { it.number }) { surah ->
                    SurahCard(surah, Modifier.animateItem(), onClick = { onIntent(QuranListIntent.SurahTapped(surah.number)) })
                }
            }
        }
    }
}

/** Where the reader left off, on the deep green artwork; the whole card is one button */
@Composable
private fun ContinueReadingCard(progress: ReadingProgress, onContinue: () -> Unit) {
    SoftCard(
        Modifier
            .fillMaxWidth()
            .padding(bottom = 2.dp)
            .semantics { contentDescription = "Continue reading Surah ${progress.surahEnglishName}, ayah ${progress.lastAyahNumber}" },
        rim = 3.dp,
        artwork = SoftArtwork.Forest,
        onClick = onContinue,
    ) {
        Column(Modifier.fillMaxWidth().padding(20.dp).animateContentSize()) {
            Text("Continue reading", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f))
            Row(Modifier.fillMaxWidth().padding(top = 10.dp), verticalAlignment = Alignment.Bottom) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        progress.surahEnglishName,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Medium, fontSize = 26.sp),
                        color = Color.White,
                    )
                    Text("Ayah ${progress.lastAyahNumber}", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f))
                }
                Spacer(Modifier.width(8.dp))
                ArabicText(progress.surahName, fontSize = 30.sp, color = Color.White, lineSpacing = 0.sp, maxLines = 1)
            }
            SoftPillSurface(Modifier.padding(top = 16.dp).height(38.dp), fill = Color.White, rim = false) {
                Text(
                    "Continue",
                    Modifier.padding(horizontal = 22.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = MuttaqiTheme.soft.brandGreen,
                )
            }
        }
    }
}

/** One surah on its floating card: its number, Arabic and English names, meaning, length and where it was revealed */
@Composable
fun SurahCard(surah: Surah, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val soft = MuttaqiTheme.soft
    SoftCard(modifier.fillMaxWidth(), cornerRadius = 24.dp, onClick = onClick) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Box(Modifier.size(32.dp).background(soft.tintedSurface, CircleShape), contentAlignment = Alignment.Center) {
                    Text("${surah.number}", style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp), color = soft.appPrimary)
                }
                Spacer(Modifier.weight(1f).width(6.dp))
                ArabicText(surah.name, fontSize = 18.sp, textAlign = TextAlign.Right, lineSpacing = 0.sp, maxLines = 1)
            }
            Text(
                surah.englishName,
                Modifier.padding(top = 16.dp),
                style = MaterialTheme.typography.titleSmall,
                color = soft.appPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(surah.englishNameTranslation, style = MaterialTheme.typography.labelSmall, color = soft.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                "${surah.numberOfAyahs} ayahs · ${surah.revelationType}",
                Modifier.padding(top = 10.dp),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = soft.brandTeal,
            )
        }
    }
}

/** The Quran couldn't be loaded, e.g. it couldn't be downloaded, with a way to try again */
@Composable
internal fun LoadFailed(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Failed to load",
    suggestion: String? = null,
) {
    val soft = MuttaqiTheme.soft
    Column(
        modifier.fillMaxSize().padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = soft.textPrimary)
        Text(message, style = MaterialTheme.typography.bodySmall, color = soft.textSecondary, textAlign = TextAlign.Center)
        if (suggestion != null) {
            Text(suggestion, style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp), color = soft.textSecondary, textAlign = TextAlign.Center)
        }
        SoftPillSurface(Modifier.width(120.dp).height(40.dp), fill = soft.appPrimary, rim = false, onClick = onRetry) {
            Text("Retry", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onPrimary)
        }
    }
}
