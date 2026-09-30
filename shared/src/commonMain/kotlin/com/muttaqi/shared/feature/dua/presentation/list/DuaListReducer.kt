package com.muttaqi.shared.feature.dua.presentation.list

import com.muttaqi.shared.core.mvi.Reducer

internal object DuaListReducer : Reducer<DuaListState, DuaListMutation> {
    override fun reduce(state: DuaListState, mutation: DuaListMutation): DuaListState = when (mutation) {
        is DuaListMutation.Loaded -> state.copy(
            isLoading = false,
            header = mutation.header,
            categories = mutation.categories,
            results = mutation.results,
            failed = false,
        )
        is DuaListMutation.SearchUpdated -> state.copy(query = mutation.query, results = mutation.results)
        DuaListMutation.LoadFailed -> state.copy(isLoading = false, failed = true)
    }
}
