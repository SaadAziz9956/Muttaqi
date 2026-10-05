package com.muttaqi.android.feature.quran

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.muttaqi.android.designsystem.component.ArabicText
import com.muttaqi.android.designsystem.component.DelayedLoadingIndicator
import com.muttaqi.android.designsystem.component.FadeBetween
import com.muttaqi.android.designsystem.component.PageQuote
import com.muttaqi.android.designsystem.component.PageSearchBar
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun QuranListScreen(state: QuranListState, onIntent: (QuranListIntent) -> Unit) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = { LargeFlexibleTopAppBar(title = { Text("The Quran") }, scrollBehavior = scrollBehavior) },
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SurahList(state: QuranListState, onIntent: (QuranListIntent) -> Unit, padding: PaddingValues) {
    LazyColumn(
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = padding.calculateTopPadding(),
            bottom = padding.calculateBottomPadding() + 16.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
    ) {
        state.header?.let { header ->
            item(key = "header") { PageQuote(header, Modifier.padding(bottom = 8.dp)) }
        }
        item(key = "continue") {
            AnimatedVisibility(state.readingProgress != null, enter = fadeIn(), exit = fadeOut()) {
                state.readingProgress?.let { progress ->
                    ContinueReadingCard(progress, Modifier.padding(top = 8.dp), onContinue = { onIntent(QuranListIntent.ContinueTapped) })
                }
            }
        }
        item(key = "search") {
            PageSearchBar(
                query = state.query,
                onQueryChange = { onIntent(QuranListIntent.QueryChanged(it)) },
                onClear = { onIntent(QuranListIntent.ClearQuery) },
                placeholder = "Search surah or number",
                modifier = Modifier.padding(vertical = 8.dp),
            )
        }
        item(key = "filter") {
            ConnectedChoice(
                options = RevelationFilter.entries.map { it to it.label },
                selected = state.filter,
                onSelect = { onIntent(QuranListIntent.FilterSelected(it)) },
                modifier = Modifier.padding(bottom = 16.dp),
            )
        }
        if (state.visibleSurahs.isEmpty()) {
            item(key = "empty") {
                Text(
                    "No results for “${state.query}”",
                    Modifier.fillMaxWidth().padding(top = 32.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
        itemsIndexed(state.visibleSurahs, key = { _, surah -> surah.number }) { index, surah ->
            SurahListItem(
                surah,
                index = index,
                count = state.visibleSurahs.size,
                modifier = Modifier.animateItem(),
                onClick = { onIntent(QuranListIntent.SurahTapped(surah.number)) },
            )
        }
    }
}

@Composable
private fun ContinueReadingCard(progress: ReadingProgress, modifier: Modifier = Modifier, onContinue: () -> Unit) {
    Card(
        onClick = onContinue,
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = "Continue reading Surah ${progress.surahEnglishName}, ayah ${progress.lastAyahNumber}" },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp).animateContentSize()) {
            Text("Continue reading", style = MaterialTheme.typography.labelLarge)
            Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(progress.surahEnglishName, style = MaterialTheme.typography.headlineSmall)
                    Text("Ayah ${progress.lastAyahNumber}", style = MaterialTheme.typography.bodyMedium)
                }
                ArabicText(progress.surahName, Modifier.padding(start = 8.dp), style = MaterialTheme.typography.headlineSmall)
            }
            Button(onClick = onContinue, Modifier.padding(top = 16.dp)) { Text("Continue") }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SurahListItem(surah: Surah, index: Int, count: Int, modifier: Modifier = Modifier, onClick: () -> Unit) {
    SegmentedListItem(
        onClick = onClick,
        shapes = ListItemDefaults.segmentedShapes(index = index, count = count),
        modifier = modifier,
        colors = ListItemDefaults.segmentedColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        leadingContent = { NumberBadge(surah.number) },
        trailingContent = {
            ArabicText(surah.name, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
        },
        supportingContent = {
            Column {
                Text(surah.englishNameTranslation)
                Text("${surah.numberOfAyahs} ayahs · ${surah.revelationType}")
            }
        },
        content = { Text(surah.englishName) },
    )
}

@Composable
private fun NumberBadge(number: Int) {
    Surface(Modifier.size(40.dp), shape = MaterialTheme.shapes.extraLarge, color = MaterialTheme.colorScheme.secondaryContainer) {
        Box(contentAlignment = Alignment.Center) {
            Text("$number", style = MaterialTheme.typography.labelLarge)
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun <T> ConnectedChoice(
    options: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)) {
        options.forEachIndexed { index, (option, label) ->
            ToggleButton(
                checked = option == selected,
                onCheckedChange = { onSelect(option) },
                modifier = Modifier.weight(1f).semantics { role = Role.RadioButton },
                shapes = when (index) {
                    0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                    options.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                },
            ) {
                Text(label)
            }
        }
    }
}

@Composable
internal fun LoadFailed(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Failed to load",
    suggestion: String? = null,
) {
    Column(
        modifier.fillMaxSize().padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        if (suggestion != null) {
            Text(suggestion, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
        Button(onClick = onRetry) { Text("Retry") }
    }
}
