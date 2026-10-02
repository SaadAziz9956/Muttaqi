package com.muttaqi.shared.feature.quran.domain.usecase

import com.muttaqi.shared.core.domain.Outcome
import com.muttaqi.shared.core.model.Language
import com.muttaqi.shared.feature.quran.domain.model.TafsirEntry
import com.muttaqi.shared.feature.quran.domain.repository.TafsirRepository

class GetTafsir(private val repository: TafsirRepository) {
    suspend operator fun invoke(surahNumber: Int, language: Language): Outcome<List<TafsirEntry>> =
        repository.tafsir(surahNumber, language)
}
