package com.muttaqi.shared.feature.topics.domain.repository

import com.muttaqi.shared.core.model.Language
import com.muttaqi.shared.core.quote.PublishedQuote
import com.muttaqi.shared.feature.topics.domain.model.Emotion
import com.muttaqi.shared.feature.topics.domain.model.ExploreGroup

/** The emotions, and the verse under their list's title */
interface EmotionRepository {
    suspend fun header(): PublishedQuote

    /** Every emotion in order, in [language]: English where there's no published translation in it */
    suspend fun emotions(language: Language): List<Emotion>
}

/** Explore's topics in their groups, and the verse under its title */
interface ExploreRepository {
    suspend fun header(): PublishedQuote

    /** Every group and its topics in order, in [language]: English where there's no published translation in it */
    suspend fun groups(language: Language): List<ExploreGroup>
}
