package com.muttaqi.shared.feature.dhikr.data.repository

import com.muttaqi.shared.core.content.BundledContentSource
import com.muttaqi.shared.core.content.ContentJson
import com.muttaqi.shared.core.domain.DispatcherProvider
import com.muttaqi.shared.core.model.Language
import com.muttaqi.shared.feature.dhikr.data.dto.DhikrBookDto
import com.muttaqi.shared.feature.dhikr.domain.model.DhikrSection
import com.muttaqi.shared.feature.dhikr.domain.repository.DhikrRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * Dhikr.json: general remembrance from the Quran, Sahih al-Bukhari, Sahih Muslim and the Sunan, keeping only Quranic
 * text and hadith graded sahih or hasan. Every translation is taken word for word from a published one, named with
 * the entry: Saheeh International, Jalandhry and Junagarhi for the Quran; HadeethEnc, Hisn al-Muslim and the
 * collections' published translations for hadith. Decoded once and kept.
 */
class BundledDhikrRepository(
    private val content: BundledContentSource,
    private val dispatchers: DispatcherProvider,
) : DhikrRepository {

    private val mutex = Mutex()
    private var book: DhikrBookDto? = null

    override suspend fun sections(language: Language): List<DhikrSection> = load().toSections(language)

    private suspend fun load(): DhikrBookDto = mutex.withLock {
        book ?: withContext(dispatchers.io) {
            ContentJson.decodeFromString<DhikrBookDto>(content.read("Dhikr.json"))
        }.also { book = it }
    }
}
