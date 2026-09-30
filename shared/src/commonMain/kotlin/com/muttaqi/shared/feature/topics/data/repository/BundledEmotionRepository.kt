package com.muttaqi.shared.feature.topics.data.repository

import com.muttaqi.shared.core.content.BundledContentSource
import com.muttaqi.shared.core.content.ContentJson
import com.muttaqi.shared.core.domain.DispatcherProvider
import com.muttaqi.shared.core.model.Language
import com.muttaqi.shared.core.quote.PublishedQuote
import com.muttaqi.shared.feature.dua.domain.usecase.GetDuaEntriesById
import com.muttaqi.shared.feature.topics.data.dto.EmotionsBookDto
import com.muttaqi.shared.feature.topics.domain.model.Emotion
import com.muttaqi.shared.feature.topics.domain.repository.EmotionRepository

/**
 * Emotions.json: the Quran verses (Uthmani Arabic, Saheeh International, Jalandhry), HadeethEnc's authentic hadith
 * and the Hisn al-Muslim duas chosen for each emotion. The duas are listed by number and joined to the Dua feature's
 * entries, so they show as they do there. The file is decoded once and kept.
 */
class BundledEmotionRepository(
    content: BundledContentSource,
    dispatchers: DispatcherProvider,
    private val getDuaEntries: GetDuaEntriesById,
) : EmotionRepository {

    private val book = BundledBook(content, dispatchers, "Emotions.json") { ContentJson.decodeFromString<EmotionsBookDto>(it) }

    override suspend fun header(): PublishedQuote = book.get().header.toPublishedQuote()

    override suspend fun emotions(language: Language): List<Emotion> {
        val duas = getDuaEntries(language)
        return book.get().emotions.map { it.toEmotion(language, duas) }
    }
}
