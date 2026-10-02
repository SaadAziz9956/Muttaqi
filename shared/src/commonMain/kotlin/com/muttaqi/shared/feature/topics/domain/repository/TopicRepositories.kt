package com.muttaqi.shared.feature.topics.domain.repository

import com.muttaqi.shared.core.model.Language
import com.muttaqi.shared.core.quote.PublishedQuote
import com.muttaqi.shared.feature.topics.domain.model.Emotion
import com.muttaqi.shared.feature.topics.domain.model.ExploreGroup

interface EmotionRepository {
    suspend fun header(): PublishedQuote

    suspend fun emotions(language: Language): List<Emotion>
}

interface ExploreRepository {
    suspend fun header(): PublishedQuote

    suspend fun groups(language: Language): List<ExploreGroup>
}
