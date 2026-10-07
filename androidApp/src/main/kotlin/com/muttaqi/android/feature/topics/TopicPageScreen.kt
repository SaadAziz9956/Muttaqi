package com.muttaqi.android.feature.topics

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.muttaqi.android.R
import com.muttaqi.android.designsystem.component.ArabicText
import com.muttaqi.android.designsystem.component.QuranText
import com.muttaqi.android.designsystem.component.TranslationText
import com.muttaqi.android.designsystem.oneui.OneUi
import com.muttaqi.android.designsystem.oneui.OneUiChip
import com.muttaqi.android.designsystem.oneui.OneUiDefaults
import com.muttaqi.android.designsystem.oneui.OneUiIconButton
import com.muttaqi.android.designsystem.oneui.OneUiScaffold
import com.muttaqi.android.designsystem.oneui.OneUiSegmented
import com.muttaqi.android.designsystem.oneui.OneUiSurface
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

@Composable
fun TopicPageScreen(title: String, state: TopicPageState, onIntent: (TopicPageIntent) -> Unit, onBack: () -> Unit) {
    val selected = state.topics.firstOrNull { it.id == state.selectedId }
    OneUiSurface {
        OneUiScaffold(
            title = selected?.title ?: if (state.isLoading) "" else title,
            onBack = onBack,
        ) { padding ->
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(top = padding.calculateTopPadding(), bottom = padding.calculateBottomPadding() + 16.dp),
            ) {
                TopicChipRow(state, onIntent)

                if (state.sections.size > 1) {
                    OneUiSegmented(
                        options = state.sections.map { it.title },
                        selected = state.sections.indexOf(state.section),
                        onSelect = { onIntent(TopicPageIntent.SectionTapped(state.sections[it])) },
                        modifier = Modifier.padding(horizontal = OneUiDefaults.ScreenMargin).padding(top = 12.dp),
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
                        TopicPassages(shown, onIntent, Modifier.padding(horizontal = OneUiDefaults.ScreenMargin).padding(top = OneUiDefaults.GroupGap))
                    }
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
        contentPadding = PaddingValues(horizontal = OneUiDefaults.ScreenMargin),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(state.topics, key = { it.id }) { topic ->
            OneUiChip(topic.title, selected = topic.id == state.selectedId, onClick = { onIntent(TopicPageIntent.TopicTapped(topic.id)) })
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

@Composable
private fun TopicPassages(state: TopicPageState, onIntent: (TopicPageIntent) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        state.passages.forEach { passage ->
            PassageCard(state.section, passage, onShare = { onIntent(TopicPageIntent.ShareTapped(passage.id)) })
        }
        if (state.translationCredits.isNotEmpty()) {
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

@Composable
private fun PassageCard(section: TopicSection, passage: TopicPassage, onShare: () -> Unit) {
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
        if (passage.arabic.isNotEmpty()) {
            if (section == TopicSection.Quran) {
                QuranText(passage.arabic, Modifier.fillMaxWidth(), color = colors.text, textAlign = TextAlign.Start)
            } else {
                ArabicText(passage.arabic, Modifier.fillMaxWidth(), color = colors.text)
            }
        }
        passage.transliteration?.let { transliteration ->
            Text(transliteration, style = type.caption.copy(fontSize = type.listSummary.fontSize), color = colors.accent)
        }
        TranslationText(passage.translation, Modifier.fillMaxWidth(), style = type.body, color = colors.text)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TranslationText(passage.source, Modifier.weight(1f), style = type.caption, color = colors.accent)
            OneUiIconButton(R.drawable.ic_share, "Share", onShare, size = 40.dp, iconSize = 20.dp, tint = colors.secondaryText)
        }
        passage.grade?.let { grade ->
            TranslationText(grade, Modifier.fillMaxWidth().padding(bottom = 8.dp), style = type.small, color = colors.secondaryText)
        }
    }
}
