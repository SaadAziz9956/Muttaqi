package com.muttaqi.shared.feature.names.presentation

import com.muttaqi.shared.core.mvi.UiEffect
import com.muttaqi.shared.core.mvi.UiIntent
import com.muttaqi.shared.core.mvi.UiMutation
import com.muttaqi.shared.core.mvi.UiState
import com.muttaqi.shared.core.quote.DisplayedQuote
import com.muttaqi.shared.core.share.SharePassage
import com.muttaqi.shared.feature.names.domain.model.AllahName
import com.muttaqi.shared.feature.names.domain.model.NameSearchMode

data class NamesState(
    val isLoading: Boolean = true,
    val names: List<AllahName> = emptyList(),
    val hadith: DisplayedQuote? = null,
    val query: String = "",
    val searchMode: NameSearchMode = NameSearchMode.ByNumber,
    val results: List<AllahName> = emptyList(),
    val failed: Boolean = false,
) : UiState {
    val isSearching: Boolean get() = query.isNotBlank()

    val listSource: String get() = NAMES_LIST_SOURCE
}

const val NAMES_LIST_SOURCE = "List of names: Jami at-Tirmidhi 3507 · graded weak (ḍaʿīf) by al-Albani and Darussalam"

sealed interface NamesIntent : UiIntent {
    data class NameShown(val number: Int) : NamesIntent
    data object ShareTapped : NamesIntent
    data class QueryChanged(val query: String) : NamesIntent
    data class SearchModeChanged(val mode: NameSearchMode) : NamesIntent
    data object ClearQuery : NamesIntent
    data class ResultTapped(val number: Int) : NamesIntent
}

sealed interface NamesMutation : UiMutation {
    data class Loaded(val names: List<AllahName>, val hadith: DisplayedQuote, val results: List<AllahName>) : NamesMutation
    data class SearchUpdated(val query: String, val results: List<AllahName>) : NamesMutation
    data class SearchModeChanged(val mode: NameSearchMode) : NamesMutation
    data object LoadFailed : NamesMutation
}

sealed interface NamesEffect : UiEffect {
    data class OpenShare(val passage: SharePassage) : NamesEffect
    data class ShowName(val number: Int) : NamesEffect
}
