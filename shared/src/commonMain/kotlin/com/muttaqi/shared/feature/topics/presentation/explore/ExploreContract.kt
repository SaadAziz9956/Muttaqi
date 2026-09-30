package com.muttaqi.shared.feature.topics.presentation.explore

import com.muttaqi.shared.core.mvi.UiEffect
import com.muttaqi.shared.core.mvi.UiIntent
import com.muttaqi.shared.core.mvi.UiMutation
import com.muttaqi.shared.core.mvi.UiState
import com.muttaqi.shared.core.quote.DisplayedQuote
import com.muttaqi.shared.feature.topics.domain.model.ExploreGroup
import com.muttaqi.shared.feature.topics.domain.usecase.ExploreSearchResult

/** The Explore tab: the verse under the title, the topics in their groups, and search across every topic */
data class ExploreState(
    val isLoading: Boolean = true,
    val header: DisplayedQuote? = null,
    val groups: List<ExploreGroup> = emptyList(),
    val query: String = "",
    val results: List<ExploreSearchResult> = emptyList(),
    val failed: Boolean = false,
) : UiState {
    val isSearching: Boolean get() = query.isNotBlank()
}

sealed interface ExploreIntent : UiIntent {
    data class QueryChanged(val query: String) : ExploreIntent
    data object ClearQuery : ExploreIntent

    /** A topic's tile, or a search result */
    data class TopicTapped(val topicId: String) : ExploreIntent
}

sealed interface ExploreMutation : UiMutation {
    data class Loaded(
        val header: DisplayedQuote,
        val groups: List<ExploreGroup>,
        val results: List<ExploreSearchResult>,
    ) : ExploreMutation
    data class SearchUpdated(val query: String, val results: List<ExploreSearchResult>) : ExploreMutation
    data object LoadFailed : ExploreMutation
}

sealed interface ExploreEffect : UiEffect {
    /** The topic page, with a chip for each topic in its group */
    data class OpenTopic(val topicId: String) : ExploreEffect
}
