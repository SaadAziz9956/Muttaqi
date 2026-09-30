package com.muttaqi.shared.feature.quran.presentation.list

import com.muttaqi.shared.core.mvi.Reducer

internal object QuranListReducer : Reducer<QuranListState, QuranListMutation> {
    override fun reduce(state: QuranListState, mutation: QuranListMutation): QuranListState = when (mutation) {
        QuranListMutation.Loading -> state.copy(isLoading = true, error = null)
        is QuranListMutation.HeaderChanged -> state.copy(header = mutation.header)
        is QuranListMutation.Loaded -> state.copy(
            isLoading = false,
            error = null,
            surahs = mutation.surahs,
            readingProgress = mutation.readingProgress,
            visibleSurahs = mutation.visibleSurahs,
        )
        is QuranListMutation.LoadFailed -> state.copy(isLoading = false, error = mutation.message)
        is QuranListMutation.ProgressChanged -> state.copy(readingProgress = mutation.readingProgress)
        is QuranListMutation.Filtered -> state.copy(
            query = mutation.query,
            filter = mutation.filter,
            visibleSurahs = mutation.visibleSurahs,
        )
    }
}
