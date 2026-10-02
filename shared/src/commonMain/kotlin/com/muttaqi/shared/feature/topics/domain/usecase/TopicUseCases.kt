package com.muttaqi.shared.feature.topics.domain.usecase

import com.muttaqi.shared.core.model.Language
import com.muttaqi.shared.core.quote.PublishedQuote
import com.muttaqi.shared.feature.topics.domain.model.Emotion
import com.muttaqi.shared.feature.topics.domain.model.ExploreGroup
import com.muttaqi.shared.feature.topics.domain.model.PassageTopic
import com.muttaqi.shared.feature.topics.domain.model.TopicChips
import com.muttaqi.shared.feature.topics.domain.repository.EmotionRepository
import com.muttaqi.shared.feature.topics.domain.repository.ExploreRepository

class GetEmotions(private val repository: EmotionRepository) {
    suspend operator fun invoke(language: Language): List<Emotion> = repository.emotions(language)
}

class GetEmotionsHeader(private val repository: EmotionRepository) {
    suspend operator fun invoke(): PublishedQuote = repository.header()
}

class GetExploreGroups(private val repository: ExploreRepository) {
    suspend operator fun invoke(language: Language): List<ExploreGroup> = repository.groups(language)
}

class GetExploreHeader(private val repository: ExploreRepository) {
    suspend operator fun invoke(): PublishedQuote = repository.header()
}

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
