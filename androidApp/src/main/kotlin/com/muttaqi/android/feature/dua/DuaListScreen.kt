package com.muttaqi.android.feature.dua

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.muttaqi.android.R
import com.muttaqi.android.designsystem.MuttaqiTheme
import com.muttaqi.android.designsystem.component.PageHeader
import com.muttaqi.android.designsystem.component.SoftBackdrop
import com.muttaqi.android.designsystem.component.SoftCard
import com.muttaqi.android.designsystem.component.SoftSearchField
import com.muttaqi.shared.feature.dua.presentation.list.DuaListEffect
import com.muttaqi.shared.feature.dua.presentation.list.DuaListIntent
import com.muttaqi.shared.feature.dua.presentation.list.DuaListState
import com.muttaqi.shared.feature.dua.presentation.list.DuaListViewModel
import org.koin.compose.viewmodel.koinViewModel

/** The Dua tab's first screen, wired to its shared view model */
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

/** The categories as tiles under the title and search; search results replace them while there's a query */
@Composable
fun DuaListScreen(state: DuaListState, onIntent: (DuaListIntent) -> Unit) {
    val soft = MuttaqiTheme.soft
    Box {
        SoftBackdrop()
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.statusBarsPadding(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) { PageHeader("Dua", state.header) }
            item(span = { GridItemSpan(maxLineSpan) }) {
                SoftSearchField(
                    query = state.query,
                    onQueryChange = { onIntent(DuaListIntent.QueryChanged(it)) },
                    onClear = { onIntent(DuaListIntent.ClearQuery) },
                    modifier = Modifier.padding(top = 22.dp, bottom = 6.dp),
                )
            }
            if (state.isSearching) {
                if (state.results.isEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Text("No results for “${state.query}”", Modifier.padding(top = 32.dp), style = MaterialTheme.typography.titleSmall, color = soft.textSecondary)
                    }
                }
                items(state.results, key = { it.chapter.id }, span = { GridItemSpan(maxLineSpan) }) { result ->
                    SoftCard(cornerRadius = 22.dp, onClick = { onIntent(DuaListIntent.SearchResultTapped(result.chapter.id)) }) {
                        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text(result.chapter.title, style = MaterialTheme.typography.bodyMedium, color = soft.textPrimary)
                                Text(result.category.title, style = MaterialTheme.typography.labelSmall, color = soft.brandTeal)
                            }
                            Icon(painterResource(R.drawable.ic_arrow_right_02_linear), null, Modifier.size(16.dp), tint = soft.textSecondary)
                        }
                    }
                }
            } else {
                items(state.categories, key = { it.id }) { category ->
                    SoftCard(cornerRadius = 24.dp, onClick = { onIntent(DuaListIntent.CategoryTapped(category.id)) }) {
                        Column(
                            Modifier.fillMaxWidth().heightIn(min = 96.dp).padding(16.dp),
                            verticalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(category.title, style = MaterialTheme.typography.titleSmall, color = soft.appPrimary, maxLines = 2)
                            Text(
                                if (category.entryCount == 1) "1 dua" else "${category.entryCount} duas",
                                style = MaterialTheme.typography.labelSmall,
                                color = soft.brandTeal,
                            )
                        }
                    }
                }
            }
        }
    }
}
