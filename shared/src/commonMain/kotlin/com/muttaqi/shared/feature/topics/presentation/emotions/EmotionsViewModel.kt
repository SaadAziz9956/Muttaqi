package com.muttaqi.shared.feature.topics.presentation.emotions

import androidx.lifecycle.viewModelScope
import com.muttaqi.shared.core.mvi.MviViewModel
import com.muttaqi.shared.core.mvi.Reducer
import com.muttaqi.shared.core.preferences.SelectedLanguage
import com.muttaqi.shared.core.quote.displayed
import com.muttaqi.shared.feature.topics.domain.usecase.GetEmotions
import com.muttaqi.shared.feature.topics.domain.usecase.GetEmotionsHeader
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

internal object EmotionsReducer : Reducer<EmotionsState, EmotionsMutation> {
    override fun reduce(state: EmotionsState, mutation: EmotionsMutation): EmotionsState = when (mutation) {
        is EmotionsMutation.Loaded -> state.copy(isLoading = false, header = mutation.header, emotions = mutation.emotions, failed = false)
        EmotionsMutation.LoadFailed -> state.copy(isLoading = false, failed = true)
    }
}

class EmotionsViewModel(
    getHeader: GetEmotionsHeader,
    getEmotions: GetEmotions,
    selectedLanguage: SelectedLanguage,
) : MviViewModel<EmotionsState, EmotionsIntent, EmotionsMutation, EmotionsEffect>(EmotionsState(), EmotionsReducer) {

    init {
        viewModelScope.launch {
            selectedLanguage.changes.collect { language ->
                try {
                    mutate(EmotionsMutation.Loaded(getHeader().displayed(language), getEmotions(language)))
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    mutate(EmotionsMutation.LoadFailed)
                }
            }
        }
    }

    override fun handle(intent: EmotionsIntent) {
        when (intent) {
            is EmotionsIntent.EmotionTapped -> emit(EmotionsEffect.OpenEmotion(intent.emotionId))
        }
    }
}
