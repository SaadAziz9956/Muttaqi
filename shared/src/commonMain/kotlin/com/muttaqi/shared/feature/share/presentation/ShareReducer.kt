package com.muttaqi.shared.feature.share.presentation

import com.muttaqi.shared.core.mvi.Reducer

internal object ShareReducer : Reducer<ShareState, ShareMutation> {
    override fun reduce(state: ShareState, mutation: ShareMutation): ShareState = when (mutation) {
        is ShareMutation.VerseChanged -> state.copy(verse = mutation.verse)
    }
}
