package com.muttaqi.shared.feature.topics.domain.usecase

import com.muttaqi.shared.core.model.Language
import com.muttaqi.shared.core.quote.PublishedQuote
import com.muttaqi.shared.feature.topics.domain.model.Emotion
import com.muttaqi.shared.feature.topics.domain.model.ExploreGroup
import com.muttaqi.shared.feature.topics.domain.model.PassageTopic
import com.muttaqi.shared.feature.topics.domain.model.TopicChips
import com.muttaqi.shared.feature.topics.domain.repository.EmotionRepository
import com.muttaqi.shared.feature.topics.domain.repository.ExploreRepository

/** Every emotion in the reader's language */
class GetEmotions(private val repository: EmotionRepository) {
    suspend operator fun invoke(language: Language): List<Emotion> = repository.emotions(language)
}

/** The verse under the Emotions title (Quran 65:3) */
class GetEmotionsHeader(private val repository: EmotionRepository) {
    suspend operator fun invoke(): PublishedQuote = repository.header()
}

/** Every Explore group and its topics in the reader's language */
class GetExploreGroups(private val repository: ExploreRepository) {
    suspend operator fun invoke(language: Language): List<ExploreGroup> = repository.groups(language)
}

/** The verse under the Explore title (Quran 29:69) */
class GetExploreHeader(private val repository: ExploreRepository) {
    suspend operator fun invoke(): PublishedQuote = repository.header()
}

/**
 * The topics a topic page's chips show when [topicId] is opened: every emotion, or the topics in its Explore group.
 * Empty if there's no such topic
 */
class GetTopicPageTopics(
    private val emotions: EmotionRepository,
    private val explore: ExploreRepository,
) {
    suspend operator fun invoke(chips: TopicChips, topicId: String, language: Language): List<PassageTopic> = when (chips) {
        TopicChips.Emotions -> emotions.emotions(language)
        TopicChips.ExploreGroup ->
            explore.groups(language).firstOrNull { group -> group.topics.any { it.id == topicId } }?.topics.orEmpty()
    }
}
