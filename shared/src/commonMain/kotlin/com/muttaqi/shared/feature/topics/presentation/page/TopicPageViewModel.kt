package com.muttaqi.shared.feature.topics.presentation.page

import androidx.lifecycle.viewModelScope
import com.muttaqi.shared.core.mvi.MviViewModel
import com.muttaqi.shared.core.preferences.SelectedLanguage
import com.muttaqi.shared.feature.topics.domain.model.TopicChips
import com.muttaqi.shared.feature.topics.domain.usecase.GetTopicPageTopics
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class TopicPageViewModel(
    chips: TopicChips,
    topicId: String,
    getTopics: GetTopicPageTopics,
    selectedLanguage: SelectedLanguage,
) : MviViewModel<TopicPageState, TopicPageIntent, TopicPageMutation, TopicPageEffect>(
    TopicPageState(selectedId = topicId),
    TopicPageReducer,
) {
    init {
        viewModelScope.launch {
            selectedLanguage.changes.collect { language ->
                try {
                    mutate(TopicPageMutation.Loaded(getTopics(chips, topicId, language)))
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    mutate(TopicPageMutation.LoadFailed)
                }
            }
        }
    }

    override fun handle(intent: TopicPageIntent) {
        when (intent) {
            is TopicPageIntent.TopicTapped -> mutate(TopicPageMutation.TopicSelected(intent.topicId))
            is TopicPageIntent.SectionTapped -> mutate(TopicPageMutation.SectionSelected(intent.section))
            is TopicPageIntent.ShareTapped -> state.value.passages.firstOrNull { it.id == intent.passageId }
                ?.let { emit(TopicPageEffect.OpenShare(it.toSharePassage())) }
        }
    }
}
