package com.muttaqi.shared.feature.share.presentation

import com.muttaqi.shared.core.mvi.Reducer

/** The passage never changes: only the verse at the foot of the page follows the reader's language */
internal object ShareReducer : Reducer<ShareState, ShareMutation> {
    override fun reduce(state: ShareState, mutation: ShareMutation): ShareState = when (mutation) {
        is ShareMutation.VerseChanged -> state.copy(verse = mutation.verse)
    }
}
