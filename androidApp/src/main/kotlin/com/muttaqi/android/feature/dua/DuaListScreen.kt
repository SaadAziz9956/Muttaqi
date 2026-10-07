package com.muttaqi.android.feature.dua

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.muttaqi.android.designsystem.component.TranslationText
import com.muttaqi.android.designsystem.oneui.LocalTabBarInset
import com.muttaqi.android.designsystem.oneui.OneUiHeaderQuote
import com.muttaqi.android.designsystem.oneui.OneUi
import com.muttaqi.android.designsystem.oneui.OneUiDefaults
import com.muttaqi.android.designsystem.oneui.OneUiGroup
import com.muttaqi.android.designsystem.oneui.OneUiListRow
import com.muttaqi.android.designsystem.oneui.OneUiScaffold
import com.muttaqi.android.designsystem.oneui.OneUiSearchField
import com.muttaqi.android.designsystem.oneui.OneUiSubheader
import com.muttaqi.android.designsystem.oneui.OneUiSurface
import com.muttaqi.shared.core.quote.DisplayedQuote
import com.muttaqi.shared.core.text.quoted
import com.muttaqi.shared.feature.dua.domain.model.DuaCategory
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

@Composable
fun DuaListScreen(state: DuaListState, onIntent: (DuaListIntent) -> Unit) {
    OneUiSurface {
        OneUiScaffold(
            title = "Dua",
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
                        onQueryChange = { onIntent(DuaListIntent.QueryChanged(it)) },
                        onClear = { onIntent(DuaListIntent.ClearQuery) },
                        placeholder = "Search duas",
                    )
                }
                if (state.isSearching) {
                    item(key = "results") {
                        if (state.results.isEmpty()) {
                            Text(
                                "No results for “${state.query}”",
                                Modifier.fillMaxWidth().padding(24.dp),
                                style = OneUi.typography.listSummary,
                                color = OneUi.colors.secondaryText,
                                textAlign = TextAlign.Center,
                            )
                        } else {
                            OneUiGroup {
                                state.results.forEachIndexed { index, result ->
                                    OneUiListRow(
                                        title = result.chapter.title,
                                        summary = result.category.title,
                                        divider = index < state.results.lastIndex,
                                        onClick = { onIntent(DuaListIntent.SearchResultTapped(result.chapter.id)) },
                                    )
                                }
                            }
                        }
                    }
                } else {
                    val (quran, hisn) = state.categories.partition { it.id == RABBANA_ID }
                    if (quran.isNotEmpty()) item(key = "quran") { CategoryGroup("From the Quran", quran, onIntent) }
                    if (hisn.isNotEmpty()) item(key = "hisn") { CategoryGroup("Hisn al-Muslim", hisn, onIntent) }
                }
            }
        }
    }
}

@Composable
private fun CategoryGroup(title: String, categories: List<DuaCategory>, onIntent: (DuaListIntent) -> Unit) {
    Column {
        OneUiSubheader(title)
        OneUiGroup {
            categories.forEachIndexed { index, category ->
                OneUiListRow(
                    title = category.title,
                    summary = duaCount(category.entryCount),
                    divider = index < categories.lastIndex,
                    onClick = { onIntent(DuaListIntent.CategoryTapped(category.id)) },
                )
            }
        }
    }
}

internal fun duaCount(count: Int) = if (count == 1) "1 dua" else "$count duas"

private const val RABBANA_ID = "rabbana"
