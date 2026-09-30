package com.muttaqi.shared.feature.quran.domain.usecase

import com.muttaqi.shared.core.domain.Outcome
import com.muttaqi.shared.core.model.Language
import com.muttaqi.shared.feature.quran.domain.model.TafsirEntry
import com.muttaqi.shared.feature.quran.domain.repository.TafsirRepository

/**
 * Tafsir Ibn Kathir for a surah: the abridged English, or the Urdu. There's no Hindi tafsir source, so Hindi readers
 * get the English
 */
class GetTafsir(private val repository: TafsirRepository) {
    suspend operator fun invoke(surahNumber: Int, language: Language): Outcome<List<TafsirEntry>> =
        repository.tafsir(surahNumber, language)
}
