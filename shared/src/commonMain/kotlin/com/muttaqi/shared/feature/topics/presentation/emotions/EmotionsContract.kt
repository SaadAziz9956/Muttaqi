package com.muttaqi.shared.feature.topics.presentation.emotions

import com.muttaqi.shared.core.mvi.UiEffect
import com.muttaqi.shared.core.mvi.UiIntent
import com.muttaqi.shared.core.mvi.UiMutation
import com.muttaqi.shared.core.mvi.UiState
import com.muttaqi.shared.core.quote.DisplayedQuote
import com.muttaqi.shared.feature.topics.domain.model.Emotion

data class EmotionsState(
    val isLoading: Boolean = true,
    val header: DisplayedQuote? = null,
    val emotions: List<Emotion> = emptyList(),
    val failed: Boolean = false,
) : UiState

sealed interface EmotionsIntent : UiIntent {
    data class EmotionTapped(val emotionId: String) : EmotionsIntent
}

sealed interface EmotionsMutation : UiMutation {
    data class Loaded(val header: DisplayedQuote, val emotions: List<Emotion>) : EmotionsMutation
    data object LoadFailed : EmotionsMutation
}

sealed interface EmotionsEffect : UiEffect {
    data class OpenEmotion(val emotionId: String) : EmotionsEffect
}
