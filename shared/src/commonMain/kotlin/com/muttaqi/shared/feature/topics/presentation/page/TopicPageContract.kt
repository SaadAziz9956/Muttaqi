package com.muttaqi.shared.feature.topics.presentation.page

import com.muttaqi.shared.core.mvi.UiEffect
import com.muttaqi.shared.core.mvi.UiIntent
import com.muttaqi.shared.core.mvi.UiMutation
import com.muttaqi.shared.core.mvi.UiState
import com.muttaqi.shared.core.share.SharePassage
import com.muttaqi.shared.feature.topics.domain.model.PassageTopic

/** The kinds of text a topic page shows, in the order of its segmented control */
enum class TopicSection(val title: String) {
    Quran("Quran"),
    Hadith("Hadith"),
    Dua("Dua"),
}

/** A verse, hadith or dua as a topic page shows it */
data class TopicPassage(
    val id: String,
    /** Empty when there is none */
    val arabic: String,
    val translation: String,
    /** Where it's from, e.g. "Quran (3:134)", "Narrated by Al-Bukhāri · Authentic" or "Bukhari · Muslim" */
    val source: String,
    /** How a dua is said: on its share card, not on the page */
    val transliteration: String?,
) {
    fun toSharePassage() = SharePassage(arabic, transliteration, translation, source)
}

/**
 * One topic's verses, hadith or duas, with chips to move to its neighbours: every emotion, or the other topics in its
 * Explore group
 */
data class TopicPageState(
    val isLoading: Boolean = true,
    /** The topics in the chips */
    val topics: List<PassageTopic> = emptyList(),
    /** The topic shown: the one opened until another chip is tapped */
    val selectedId: String,
    /** The kinds of text the shown topic has, e.g. no Dua for some */
    val sections: List<TopicSection> = emptyList(),
    val section: TopicSection = TopicSection.Quran,
    /** The shown topic's texts of that kind */
    val passages: List<TopicPassage> = emptyList(),
    /** Whose translations are shown, credited as their publishers ask */
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
