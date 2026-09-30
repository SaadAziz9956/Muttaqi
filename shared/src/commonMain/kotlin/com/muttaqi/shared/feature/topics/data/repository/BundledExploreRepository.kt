package com.muttaqi.shared.feature.topics.data.repository

import com.muttaqi.shared.core.content.BundledContentSource
import com.muttaqi.shared.core.content.ContentJson
import com.muttaqi.shared.core.domain.DispatcherProvider
import com.muttaqi.shared.core.model.Language
import com.muttaqi.shared.core.quote.PublishedQuote
import com.muttaqi.shared.feature.dua.domain.usecase.GetDuaEntriesById
import com.muttaqi.shared.feature.topics.data.dto.ExploreBookDto
import com.muttaqi.shared.feature.topics.domain.model.ExploreGroup
import com.muttaqi.shared.feature.topics.domain.repository.ExploreRepository

/**
 * Explore.json: topics in groups, each with the Quran verses, HadeethEnc's authentic hadith and the Hisn al-Muslim
 * duas chosen for it, the duas joined by number to the Dua feature's entries. The file is decoded once and kept.
 */
class BundledExploreRepository(
    content: BundledContentSource,
    dispatchers: DispatcherProvider,
    private val getDuaEntries: GetDuaEntriesById,
) : ExploreRepository {

    private val book = BundledBook(content, dispatchers, "Explore.json") { ContentJson.decodeFromString<ExploreBookDto>(it) }

    override suspend fun header(): PublishedQuote = book.get().header.toPublishedQuote()

    override suspend fun groups(language: Language): List<ExploreGroup> {
        val duas = getDuaEntries(language)
        return book.get().groups.map { it.toGroup(language, duas) }
    }
}
