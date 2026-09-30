package com.muttaqi.shared.feature.dua.domain.usecase

import com.muttaqi.shared.core.domain.dayOfEra
import com.muttaqi.shared.core.model.Language
import com.muttaqi.shared.feature.dua.domain.model.DuaCategory
import com.muttaqi.shared.feature.dua.domain.model.DuaChapter
import com.muttaqi.shared.feature.dua.domain.model.DuaEntry
import com.muttaqi.shared.feature.dua.domain.model.QuranicDua
import com.muttaqi.shared.feature.dua.domain.repository.DuaCategoryRepository
import com.muttaqi.shared.feature.dua.domain.repository.QuranicDuaRepository
import kotlinx.datetime.LocalDate

/** Every category in the reader's language */
class GetDuaCategories(private val repository: DuaCategoryRepository) {
    suspend operator fun invoke(language: Language): List<DuaCategory> = repository.categories(language)
}

/** One category by id, e.g. to open it from a link or after the language changes */
class GetDuaCategory(private val repository: DuaCategoryRepository) {
    suspend operator fun invoke(id: String, language: Language): DuaCategory? =
        repository.categories(language).firstOrNull { it.id == id }
}

/** One chapter by id, wherever it sits */
class GetDuaChapter(private val repository: DuaCategoryRepository) {
    suspend operator fun invoke(id: String, language: Language): DuaChapter? =
        repository.categories(language).asSequence().flatMap { it.chapters }.firstOrNull { it.id == id }
}

/**
 * Hisn al-Muslim entries by their number in the book, e.g. `"hisn-176"`, for features that list duas by number
 * (Emotions and Explore)
 */
class GetDuaEntriesById(private val repository: DuaCategoryRepository) {
    suspend operator fun invoke(language: Language): Map<String, DuaEntry> =
        repository.categories(language).flatMap { it.chapters }.flatMap { it.entries }.associateBy { it.id }
}

/** The same Quranic dua all day, moving to the next at midnight, e.g. for Home's Dua of the Day */
class GetDuaOfTheDay(private val repository: QuranicDuaRepository) {
    suspend operator fun invoke(date: LocalDate, language: Language): QuranicDua? {
        val duas = repository.quranicDuas(language)
        if (duas.isEmpty()) return null
        // Counted by the day of the era, as the iOS app picked it, so the day's dua doesn't change with the move
        return duas[date.dayOfEra.mod(duas.size.toLong()).toInt()]
    }
}
