package com.muttaqi.shared.feature.names.presentation

import com.muttaqi.shared.core.mvi.UiEffect
import com.muttaqi.shared.core.mvi.UiIntent
import com.muttaqi.shared.core.mvi.UiMutation
import com.muttaqi.shared.core.mvi.UiState
import com.muttaqi.shared.core.quote.DisplayedQuote
import com.muttaqi.shared.core.share.SharePassage
import com.muttaqi.shared.feature.names.domain.model.AllahName
import com.muttaqi.shared.feature.names.domain.model.NameSearchMode

/**
 * The 99 Names page and its search, which share one state, so picking a search result turns the page to that name.
 * The name on screen changes with every swipe, so it has its own flow ([NamesViewModel.position]) rather than
 * living here.
 */
data class NamesState(
    val isLoading: Boolean = true,
    val names: List<AllahName> = emptyList(),
    /** The hadith at the foot of the page */
    val hadith: DisplayedQuote? = null,
    val query: String = "",
    val searchMode: NameSearchMode = NameSearchMode.ByNumber,
    val results: List<AllahName> = emptyList(),
    val failed: Boolean = false,
) : UiState {
    val isSearching: Boolean get() = query.isNotBlank()
}

sealed interface NamesIntent : UiIntent {
    /** The page settled on a name, by its number */
    data class NameShown(val number: Int) : NamesIntent
    /** Share the name on screen */
    data object ShareTapped : NamesIntent
    data class QueryChanged(val query: String) : NamesIntent
    /** Switching mode starts the query again, since a number and a name need different keyboards */
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
    /** Turn the page to this name, e.g. after picking it from the search */
    data class ShowName(val number: Int) : NamesEffect
}
