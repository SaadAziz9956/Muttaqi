package com.muttaqi.shared.feature.journal.presentation.list

import com.muttaqi.shared.core.mvi.Reducer

internal object JournalListReducer : Reducer<JournalListState, JournalListMutation> {
    override fun reduce(state: JournalListState, mutation: JournalListMutation): JournalListState = when (mutation) {
        is JournalListMutation.HeaderLoaded -> state.copy(header = mutation.header)
        is JournalListMutation.EntriesLoaded -> state.copy(isLoading = false, entries = mutation.entries, results = mutation.results)
        is JournalListMutation.SearchUpdated -> state.copy(query = mutation.query, results = mutation.results)
        is JournalListMutation.EntryRemoved -> state.copy(
            entries = state.entries.filterNot { it.id == mutation.entryId },
            results = state.results.filterNot { it.id == mutation.entryId },
        )
    }
}
