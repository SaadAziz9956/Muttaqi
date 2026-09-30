package com.muttaqi.shared.feature.dua.presentation.list

import com.muttaqi.shared.core.mvi.UiEffect
import com.muttaqi.shared.core.mvi.UiIntent
import com.muttaqi.shared.core.mvi.UiMutation
import com.muttaqi.shared.core.mvi.UiState
import com.muttaqi.shared.core.quote.DisplayedQuote
import com.muttaqi.shared.feature.dua.domain.model.DuaCategory
import com.muttaqi.shared.feature.dua.domain.usecase.DuaSearchResult

/** The Dua tab: the verse under the title, the category tiles, and search across every chapter */
data class DuaListState(
    val isLoading: Boolean = true,
    val header: DisplayedQuote? = null,
    val categories: List<DuaCategory> = emptyList(),
    val query: String = "",
    val results: List<DuaSearchResult> = emptyList(),
    val failed: Boolean = false,
) : UiState {
    val isSearching: Boolean get() = query.isNotBlank()
}

sealed interface DuaListIntent : UiIntent {
    data class QueryChanged(val query: String) : DuaListIntent
    data object ClearQuery : DuaListIntent
    data class CategoryTapped(val categoryId: String) : DuaListIntent
    data class SearchResultTapped(val chapterId: String) : DuaListIntent
}

sealed interface DuaListMutation : UiMutation {
    data class Loaded(
        val header: DisplayedQuote,
        val categories: List<DuaCategory>,
        val results: List<DuaSearchResult>,
    ) : DuaListMutation
    data class SearchUpdated(val query: String, val results: List<DuaSearchResult>) : DuaListMutation
    data object LoadFailed : DuaListMutation
}

sealed interface DuaListEffect : UiEffect {
    data class OpenCategory(val categoryId: String) : DuaListEffect
    /** A category with a single chapter opens straight to its duas */
    data class OpenChapter(val chapterId: String) : DuaListEffect
}
