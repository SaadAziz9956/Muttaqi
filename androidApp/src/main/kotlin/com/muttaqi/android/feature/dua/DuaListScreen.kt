package com.muttaqi.android.feature.dua

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.muttaqi.android.R
import com.muttaqi.android.designsystem.component.PageQuote
import com.muttaqi.android.designsystem.component.PageSearchBar
import com.muttaqi.shared.feature.dua.presentation.list.DuaListEffect
import com.muttaqi.shared.feature.dua.presentation.list.DuaListIntent
import com.muttaqi.shared.feature.dua.presentation.list.DuaListState
import com.muttaqi.shared.feature.dua.presentation.list.DuaListViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DuaListRoute(onOpenCategory: (String) -> Unit, onOpenChapter: (String) -> Unit) {
    val viewModel = koinViewModel<DuaListViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is DuaListEffect.OpenCategory -> onOpenCategory(effect.categoryId)
                is DuaListEffect.OpenChapter -> onOpenChapter(effect.chapterId)
            }
        }
    }
    DuaListScreen(state, viewModel::dispatch)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DuaListScreen(state: DuaListState, onIntent: (DuaListIntent) -> Unit) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = { LargeFlexibleTopAppBar(title = { Text("Dua") }, scrollBehavior = scrollBehavior) },
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
            state.header?.let { header -> item(span = { GridItemSpan(maxLineSpan) }) { PageQuote(header) } }
            item(span = { GridItemSpan(maxLineSpan) }) {
                PageSearchBar(
                    query = state.query,
                    onQueryChange = { onIntent(DuaListIntent.QueryChanged(it)) },
                    onClear = { onIntent(DuaListIntent.ClearQuery) },
                    placeholder = "Search duas",
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            }
            if (state.isSearching) {
                if (state.results.isEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Text(
                            "No results for “${state.query}”",
                            Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                items(state.results, key = { it.chapter.id }, span = { GridItemSpan(maxLineSpan) }) { result ->
                    Card(onClick = { onIntent(DuaListIntent.SearchResultTapped(result.chapter.id)) }) {
                        ListItem(
                            headlineContent = { Text(result.chapter.title) },
                            supportingContent = { Text(result.category.title) },
                            trailingContent = { Icon(painterResource(R.drawable.ic_chevron_right), contentDescription = null) },
                        )
                    }
                }
            } else {
                items(state.categories, key = { it.id }) { category ->
                    Card(onClick = { onIntent(DuaListIntent.CategoryTapped(category.id)) }) {
                        Column(
                            Modifier.fillMaxWidth().heightIn(min = 112.dp).padding(16.dp),
                            verticalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(category.title, style = MaterialTheme.typography.titleMedium)
                            Text(
                                if (category.entryCount == 1) "1 dua" else "${category.entryCount} duas",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}
