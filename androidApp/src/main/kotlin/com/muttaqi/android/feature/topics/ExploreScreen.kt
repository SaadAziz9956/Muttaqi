package com.muttaqi.android.feature.topics

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.muttaqi.android.R
import com.muttaqi.android.designsystem.component.TranslationText
import com.muttaqi.android.designsystem.oneui.LocalTabBarInset
import com.muttaqi.android.designsystem.oneui.OneUiHeaderQuote
import com.muttaqi.android.designsystem.oneui.OneUi
import com.muttaqi.android.designsystem.oneui.OneUiChip
import com.muttaqi.android.designsystem.oneui.OneUiDefaults
import com.muttaqi.android.designsystem.oneui.OneUiGroup
import com.muttaqi.android.designsystem.oneui.OneUiScaffold
import com.muttaqi.android.designsystem.oneui.OneUiSearchField
import com.muttaqi.android.designsystem.oneui.OneUiSubheader
import com.muttaqi.android.designsystem.oneui.OneUiSurface
import com.muttaqi.android.designsystem.topicSymbol
import com.muttaqi.shared.core.quote.DisplayedQuote
import com.muttaqi.shared.core.text.quoted
import com.muttaqi.shared.feature.topics.domain.model.ExploreGroup
import com.muttaqi.shared.feature.topics.domain.model.PassageTopic
import com.muttaqi.shared.feature.topics.domain.usecase.ExploreSearchResult
import com.muttaqi.shared.feature.topics.presentation.explore.ExploreEffect
import com.muttaqi.shared.feature.topics.presentation.explore.ExploreIntent
import com.muttaqi.shared.feature.topics.presentation.explore.ExploreState
import com.muttaqi.shared.feature.topics.presentation.explore.ExploreViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ExploreRoute(onOpenTopic: (String) -> Unit) {
    val viewModel = koinViewModel<ExploreViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is ExploreEffect.OpenTopic -> onOpenTopic(effect.topicId)
            }
        }
    }
    ExploreScreen(state, viewModel::dispatch)
}

@Composable
fun ExploreScreen(state: ExploreState, onIntent: (ExploreIntent) -> Unit) {
    var selectedGroupId by rememberSaveable { mutableStateOf<String?>(null) }
    val selectedGroup = state.groups.firstOrNull { it.id == selectedGroupId } ?: state.groups.firstOrNull()
    OneUiSurface {
        OneUiScaffold(
            title = "Explore",
            subtitle = state.header?.let { header -> { OneUiHeaderQuote(header.text, header.source) } },
        ) { padding ->
            LazyColumn(
                contentPadding = PaddingValues(
                    top = padding.calculateTopPadding(),
                    bottom = padding.calculateBottomPadding() + LocalTabBarInset.current + 16.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(OneUiDefaults.GroupGap),
            ) {
                item(key = "search") {
                    OneUiSearchField(
                        query = state.query,
                        onQueryChange = { onIntent(ExploreIntent.QueryChanged(it)) },
                        onClear = { onIntent(ExploreIntent.ClearQuery) },
                        placeholder = "Search",
                    )
                }
                if (state.isSearching) {
                    item(key = "results") {
                        if (state.results.isEmpty()) {
                            NoResults(state.query)
                        } else {
                            SearchResults(state.results, onOpen = { onIntent(ExploreIntent.TopicTapped(it)) })
                        }
                    }
                } else if (selectedGroup != null) {
                    item(key = "groups") {
                        GroupChips(state.groups, selectedGroup.id, onSelect = { selectedGroupId = it })
                    }
                    item(key = "topics") {
                        Column {
                            OneUiSubheader(selectedGroup.title)
                            OneUiGroup {
                                selectedGroup.topics.forEachIndexed { index, topic ->
                                    TopicRow(
                                        icon = topicSymbol(topic.icon),
                                        title = topic.title,
                                        summary = passageSummary(topic),
                                        divider = index < selectedGroup.topics.lastIndex,
                                        onClick = { onIntent(ExploreIntent.TopicTapped(topic.id)) },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

internal fun passageSummary(topic: PassageTopic): String = listOfNotNull(
    topic.verses.size.takeIf { it > 0 }?.let { if (it == 1) "1 verse" else "$it verses" },
    topic.hadith.size.takeIf { it > 0 }?.let { "$it hadith" },
    topic.duas.size.takeIf { it > 0 }?.let { if (it == 1) "1 dua" else "$it duas" },
).joinToString(" · ")

@Composable
private fun GroupChips(groups: List<ExploreGroup>, selectedId: String, onSelect: (String) -> Unit) {
    LazyRow(
        Modifier.fillMaxWidth().selectableGroup(),
        contentPadding = PaddingValues(horizontal = OneUiDefaults.ScreenMargin),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(groups, key = { it.id }) { group ->
            OneUiChip(group.title, selected = group.id == selectedId, onClick = { onSelect(group.id) })
        }
    }
}

@Composable
private fun SearchResults(results: List<ExploreSearchResult>, onOpen: (String) -> Unit) {
    OneUiGroup {
        results.forEachIndexed { index, result ->
            TopicRow(
                icon = topicSymbol(result.topic.icon),
                title = result.topic.title,
                summary = result.group.title,
                divider = index < results.lastIndex,
                onClick = { onOpen(result.topic.id) },
            )
        }
    }
}

@Composable
private fun TopicRow(@DrawableRes icon: Int, title: String, summary: String, divider: Boolean, onClick: () -> Unit) {
    val colors = OneUi.colors
    Column(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            Modifier.fillMaxWidth().heightIn(min = 64.dp).padding(horizontal = OneUiDefaults.ItemPadding, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier.size(LeadingSize).clip(CircleShape).background(colors.accent.copy(alpha = if (colors.isDark) 0.16f else 0.1f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(painterResource(icon), contentDescription = null, Modifier.size(22.dp), tint = colors.accent)
            }
            Spacer(Modifier.width(LeadingGap))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, style = OneUi.typography.listTitle, color = colors.text)
                if (summary.isNotEmpty()) Text(summary, style = OneUi.typography.listSummary, color = colors.secondaryText)
            }
        }
        if (divider) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(start = OneUiDefaults.ItemPadding + LeadingSize + LeadingGap, end = OneUiDefaults.ItemPadding)
                    .height(1.dp)
                    .background(colors.divider),
            )
        }
    }
}

@Composable
private fun NoResults(query: String) {
    Column(
        Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(painterResource(R.drawable.ic_search), contentDescription = null, Modifier.size(48.dp), tint = OneUi.colors.secondaryText)
        Text("No results for “$query”", style = OneUi.typography.listTitle, color = OneUi.colors.text, textAlign = TextAlign.Center)
        Text(
            "Check the spelling or try a new search.",
            style = OneUi.typography.listSummary,
            color = OneUi.colors.secondaryText,
            textAlign = TextAlign.Center,
        )
    }
}

private val LeadingSize = 40.dp
private val LeadingGap = 16.dp
