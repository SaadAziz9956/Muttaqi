package com.muttaqi.android.feature.topics

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.muttaqi.android.R
import com.muttaqi.android.designsystem.component.ArabicText
import com.muttaqi.android.designsystem.component.BackButton
import com.muttaqi.android.designsystem.component.QuranText
import com.muttaqi.android.designsystem.component.TranslationText
import com.muttaqi.shared.core.share.SharePassage
import com.muttaqi.shared.feature.topics.domain.model.TopicChips
import com.muttaqi.shared.feature.topics.presentation.page.TopicPageEffect
import com.muttaqi.shared.feature.topics.presentation.page.TopicPageIntent
import com.muttaqi.shared.feature.topics.presentation.page.TopicPageState
import com.muttaqi.shared.feature.topics.presentation.page.TopicPageViewModel
import com.muttaqi.shared.feature.topics.presentation.page.TopicPassage
import com.muttaqi.shared.feature.topics.presentation.page.TopicSection
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun TopicPageRoute(chips: TopicChips, topicId: String, onShare: (SharePassage) -> Unit, onBack: () -> Unit) {
    val viewModel = koinViewModel<TopicPageViewModel> { parametersOf(chips, topicId) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is TopicPageEffect.OpenShare -> onShare(effect.passage)
            }
        }
    }
    TopicPageScreen(chips.title, state, viewModel::dispatch, onBack)
}

internal val TopicChips.title: String
    get() = when (this) {
        TopicChips.Emotions -> "Emotions"
        TopicChips.ExploreGroup -> "Explore"
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicPageScreen(title: String, state: TopicPageState, onIntent: (TopicPageIntent) -> Unit, onBack: () -> Unit) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = { BackButton(onBack) },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = padding.calculateTopPadding(), bottom = padding.calculateBottomPadding() + 16.dp),
        ) {
            TopicChipRow(state, onIntent)

            if (state.sections.size > 1) {
                TopicSectionPicker(
                    sections = state.sections,
                    selected = state.section,
                    onSelect = { onIntent(TopicPageIntent.SectionTapped(it)) },
                    modifier = Modifier.padding(horizontal = 16.dp).padding(top = 4.dp, bottom = 8.dp),
                )
            }

            if (state.topics.isNotEmpty()) {
                AnimatedContent(
                    targetState = state,
                    contentKey = { it.selectedId to it.section },
                    transitionSpec = {
                        if (initialState.selectedId != targetState.selectedId) {
                            fadeIn() togetherWith fadeOut()
                        } else {
                            EnterTransition.None togetherWith ExitTransition.None
                        }
                    },
                    label = "topic",
                ) { shown ->
                    TopicPassages(shown, onIntent, Modifier.padding(horizontal = 16.dp).padding(top = 8.dp))
                }
            }
        }
    }
}

@Composable
private fun TopicChipRow(state: TopicPageState, onIntent: (TopicPageIntent) -> Unit) {
    val row = rememberLazyListState()
    val selectedIndex = state.topics.indexOfFirst { it.id == state.selectedId }
    val loaded = state.topics.isNotEmpty()
    LaunchedEffect(loaded) { if (loaded && selectedIndex >= 0) row.scrollToCenter(selectedIndex, animated = false) }
    LaunchedEffect(state.selectedId) { if (loaded && selectedIndex >= 0) row.scrollToCenter(selectedIndex, animated = true) }

    LazyRow(
        state = row,
        modifier = Modifier.selectableGroup(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(state.topics, key = { it.id }) { topic ->
            val selected = topic.id == state.selectedId
            FilterChip(
                selected = selected,
                onClick = { onIntent(TopicPageIntent.TopicTapped(topic.id)) },
                label = { Text(topic.title) },
                leadingIcon = if (selected) {
                    { Icon(painterResource(R.drawable.ic_check), contentDescription = null, Modifier.size(FilterChipDefaults.IconSize)) }
                } else {
                    null
                },
            )
        }
    }
}

private suspend fun LazyListState.scrollToCenter(index: Int, animated: Boolean) {
    if (layoutInfo.visibleItemsInfo.none { it.index == index }) scrollToItem(index)
    val item = layoutInfo.visibleItemsInfo.firstOrNull { it.index == index } ?: return
    val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
    val distance = (item.offset + item.size / 2 - viewportCenter).toFloat()
    if (animated) animateScrollBy(distance) else scrollBy(distance)
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun TopicSectionPicker(
    sections: List<TopicSection>,
    selected: TopicSection,
    onSelect: (TopicSection) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier.fillMaxWidth().selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
    ) {
        sections.forEachIndexed { index, section ->
            ToggleButton(
                checked = section == selected,
                onCheckedChange = { onSelect(section) },
                modifier = Modifier.weight(1f).semantics { role = Role.RadioButton },
                shapes = when (index) {
                    0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                    sections.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                },
            ) {
                Text(section.title)
            }
        }
    }
}

@Composable
private fun TopicPassages(state: TopicPageState, onIntent: (TopicPageIntent) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        state.passages.forEach { passage ->
            PassageCard(state.section, passage, onShare = { onIntent(TopicPageIntent.ShareTapped(passage.id)) })
        }
        if (state.translationCredits.isNotEmpty()) {
            Text(
                "Translation: " + state.translationCredits.joinToString(", "),
                Modifier.fillMaxWidth().padding(top = 4.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun PassageCard(section: TopicSection, passage: TopicPassage, onShare: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (passage.arabic.isNotEmpty()) {
                if (section == TopicSection.Quran) {
                    QuranText(passage.arabic, Modifier.fillMaxWidth(), textAlign = TextAlign.Start)
                } else {
                    ArabicText(passage.arabic, Modifier.fillMaxWidth())
                }
            }
            passage.transliteration?.let { transliteration ->
                Text(transliteration, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            }
            TranslationText(passage.translation, Modifier.fillMaxWidth())
            Row(verticalAlignment = Alignment.CenterVertically) {
                TranslationText(
                    passage.source,
                    Modifier.weight(1f),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                IconButton(onClick = onShare) { Icon(painterResource(R.drawable.ic_share), contentDescription = "Share") }
            }
            passage.grade?.let { grade ->
                TranslationText(
                    grade,
                    Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
