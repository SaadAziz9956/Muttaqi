package com.muttaqi.shared.feature.share.presentation

import com.muttaqi.shared.core.mvi.UiEffect
import com.muttaqi.shared.core.mvi.UiIntent
import com.muttaqi.shared.core.mvi.UiMutation
import com.muttaqi.shared.core.mvi.UiState
import com.muttaqi.shared.core.quote.DisplayedQuote
import com.muttaqi.shared.core.share.SharePassage

data class ShareState(
    val passage: SharePassage,
    val verse: DisplayedQuote,
) : UiState

sealed interface ShareIntent : UiIntent {
    data object ShareTapped : ShareIntent
    data object SaveTapped : ShareIntent
}

sealed interface ShareMutation : UiMutation {
    data class VerseChanged(val verse: DisplayedQuote) : ShareMutation
}

sealed interface ShareEffect : UiEffect {
    data class ShareImage(val title: String, val fileName: String) : ShareEffect
    data class SaveImage(val fileName: String) : ShareEffect
}
