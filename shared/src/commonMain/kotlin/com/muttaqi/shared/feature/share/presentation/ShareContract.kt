package com.muttaqi.shared.feature.share.presentation

import com.muttaqi.shared.core.mvi.UiEffect
import com.muttaqi.shared.core.mvi.UiIntent
import com.muttaqi.shared.core.mvi.UiMutation
import com.muttaqi.shared.core.mvi.UiState
import com.muttaqi.shared.core.quote.DisplayedQuote
import com.muttaqi.shared.core.share.SharePassage

/**
 * The Share page: a passage on the brand-green card, shared as an image, and the verse at the foot of the page in
 * the reader's language. The card shows the passage word for word as it was handed over; each app draws it and turns
 * it into the image
 */
data class ShareState(
    val passage: SharePassage,
    /** Quran 16:125, at the foot of the page */
    val verse: DisplayedQuote,
) : UiState

sealed interface ShareIntent : UiIntent {
    /** The share button. On iOS, SwiftUI's ShareLink opens the share sheet itself, so only Android sends this */
    data object ShareTapped : ShareIntent
    /** Save the card to the photo library. iOS's share sheet has its own Save Image, so only Android has this button */
    data object SaveTapped : ShareIntent
}

sealed interface ShareMutation : UiMutation {
    data class VerseChanged(val verse: DisplayedQuote) : ShareMutation
}

sealed interface ShareEffect : UiEffect {
    /** The platform draws the card as an image and opens its share sheet with it, titled with where it's from */
    data class ShareImage(val title: String, val fileName: String) : ShareEffect
    /** The platform draws the card as an image and saves it to the photo library */
    data class SaveImage(val fileName: String) : ShareEffect
}
