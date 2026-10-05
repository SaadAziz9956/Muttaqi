package com.muttaqi.android.feature.topics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.muttaqi.android.R
import com.muttaqi.android.designsystem.component.PageQuote
import com.muttaqi.android.designsystem.component.PageSearchBar
import com.muttaqi.android.designsystem.topicSymbol
import com.muttaqi.shared.feature.topics.domain.model.ExploreGroup
import com.muttaqi.shared.feature.topics.domain.model.ExploreTopic
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ExploreScreen(state: ExploreState, onIntent: (ExploreIntent) -> Unit) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    var selectedGroupId by rememberSaveable { mutableStateOf<String?>(null) }
    val selectedGroup = state.groups.firstOrNull { it.id == selectedGroupId } ?: state.groups.firstOrNull()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = { LargeFlexibleTopAppBar(title = { Text("Explore") }, scrollBehavior = scrollBehavior) },
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = padding.calculateTopPadding(),
                bottom = padding.calculateBottomPadding() + 16.dp,
            ),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            state.header?.let { header -> item(key = "header", span = { GridItemSpan(maxLineSpan) }) { PageQuote(header) } }
            item(key = "search", span = { GridItemSpan(maxLineSpan) }) {
                PageSearchBar(
                    query = state.query,
                    onQueryChange = { onIntent(ExploreIntent.QueryChanged(it)) },
                    onClear = { onIntent(ExploreIntent.ClearQuery) },
                    placeholder = "Search",
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            }
            if (state.isSearching) {
                item(key = "results", span = { GridItemSpan(maxLineSpan) }) {
                    if (state.results.isEmpty()) {
                        NoResults(state.query)
                    } else {
                        SearchResults(state.results, onOpen = { onIntent(ExploreIntent.TopicTapped(it)) })
                    }
                }
            } else if (selectedGroup != null) {
                item(key = "groups", span = { GridItemSpan(maxLineSpan) }) {
                    GroupChips(state.groups, selectedGroup.id, onSelect = { selectedGroupId = it })
                }
                items(selectedGroup.topics, key = { it.id }) { topic ->
                    TopicCard(topic, onClick = { onIntent(ExploreIntent.TopicTapped(topic.id)) })
                }
            }
        }
    }
}

@Composable
private fun GroupChips(groups: List<ExploreGroup>, selectedId: String, onSelect: (String) -> Unit) {
    FlowRow(
        Modifier.fillMaxWidth().selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        groups.forEach { group ->
            val selected = group.id == selectedId
            FilterChip(
                selected = selected,
                onClick = { onSelect(group.id) },
                label = { Text(group.title) },
                leadingIcon = if (selected) {
                    { Icon(painterResource(R.drawable.ic_check), contentDescription = null, Modifier.size(FilterChipDefaults.IconSize)) }
                } else {
                    null
                },
            )
        }
    }
}

@Composable
private fun TopicCard(topic: ExploreTopic, onClick: () -> Unit) {
    Card(onClick = onClick) {
        Column(
            Modifier.fillMaxWidth().heightIn(min = 124.dp).padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Icon(painterResource(topicSymbol(topic.icon)), contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(topic.title, Modifier.padding(top = 12.dp), style = MaterialTheme.typography.titleMedium)
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SearchResults(results: List<ExploreSearchResult>, onOpen: (String) -> Unit) {
    val colors = ListItemDefaults.segmentedColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
        results.forEachIndexed { index, result ->
            SegmentedListItem(
                onClick = { onOpen(result.topic.id) },
                shapes = ListItemDefaults.segmentedShapes(index = index, count = results.size),
                colors = colors,
                leadingContent = {
                    Icon(painterResource(topicSymbol(result.topic.icon)), contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                },
                supportingContent = { Text(result.group.title) },
                trailingContent = { Icon(painterResource(R.drawable.ic_chevron_right), contentDescription = null) },
            ) {
                Text(result.topic.title)
            }
        }
    }
}

@Composable
private fun NoResults(query: String) {
    Column(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            painterResource(R.drawable.ic_search),
            contentDescription = null,
            Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text("No Results for “$query”", style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        Text(
            "Check the spelling or try a new search.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}
