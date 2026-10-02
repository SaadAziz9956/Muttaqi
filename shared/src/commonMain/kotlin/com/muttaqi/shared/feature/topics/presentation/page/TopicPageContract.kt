package com.muttaqi.shared.feature.topics.presentation.page

import com.muttaqi.shared.core.mvi.UiEffect
import com.muttaqi.shared.core.mvi.UiIntent
import com.muttaqi.shared.core.mvi.UiMutation
import com.muttaqi.shared.core.mvi.UiState
import com.muttaqi.shared.core.share.SharePassage
import com.muttaqi.shared.feature.topics.domain.model.PassageTopic

enum class TopicSection(val title: String) {
    Quran("Quran"),
    Hadith("Hadith"),
    Dua("Dua"),
}

data class TopicPassage(
    val id: String,
    val arabic: String,
    val translation: String,
    val source: String,
    val transliteration: String?,
) {
    fun toSharePassage() = SharePassage(arabic, transliteration, translation, source)
}

data class TopicPageState(
    val isLoading: Boolean = true,
    val topics: List<PassageTopic> = emptyList(),
    val selectedId: String,
    val sections: List<TopicSection> = emptyList(),
    val section: TopicSection = TopicSection.Quran,
    val passages: List<TopicPassage> = emptyList(),
    val translationCredits: List<String> = emptyList(),
) : UiState

sealed interface TopicPageIntent : UiIntent {
    data class TopicTapped(val topicId: String) : TopicPageIntent
    data class SectionTapped(val section: TopicSection) : TopicPageIntent
    data class ShareTapped(val passageId: String) : TopicPageIntent
}

sealed interface TopicPageMutation : UiMutation {
    data class Loaded(val topics: List<PassageTopic>) : TopicPageMutation
    data class TopicSelected(val topicId: String) : TopicPageMutation
    data class SectionSelected(val section: TopicSection) : TopicPageMutation
    data object LoadFailed : TopicPageMutation
}

sealed interface TopicPageEffect : UiEffect {
    data class OpenShare(val passage: SharePassage) : TopicPageEffect
}
