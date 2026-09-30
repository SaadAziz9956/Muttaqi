package com.muttaqi.shared.feature.topics.presentation.explore

import com.muttaqi.shared.core.mvi.Reducer

internal object ExploreReducer : Reducer<ExploreState, ExploreMutation> {
    override fun reduce(state: ExploreState, mutation: ExploreMutation): ExploreState = when (mutation) {
        is ExploreMutation.Loaded -> state.copy(
            isLoading = false,
            header = mutation.header,
            groups = mutation.groups,
            results = mutation.results,
            failed = false,
        )
        is ExploreMutation.SearchUpdated -> state.copy(query = mutation.query, results = mutation.results)
        ExploreMutation.LoadFailed -> state.copy(isLoading = false, failed = true)
    }
}
