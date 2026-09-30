package com.muttaqi.shared.feature.names.presentation

import com.muttaqi.shared.core.mvi.Reducer

internal object NamesReducer : Reducer<NamesState, NamesMutation> {
    override fun reduce(state: NamesState, mutation: NamesMutation): NamesState = when (mutation) {
        is NamesMutation.Loaded -> state.copy(
            isLoading = false,
            names = mutation.names,
            hadith = mutation.hadith,
            results = mutation.results,
            failed = false,
        )
        is NamesMutation.SearchUpdated -> state.copy(query = mutation.query, results = mutation.results)
        is NamesMutation.SearchModeChanged -> state.copy(searchMode = mutation.mode, query = "", results = emptyList())
        NamesMutation.LoadFailed -> state.copy(isLoading = false, failed = true)
    }
}
