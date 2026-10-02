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

class GetDuaCategories(private val repository: DuaCategoryRepository) {
    suspend operator fun invoke(language: Language): List<DuaCategory> = repository.categories(language)
}

class GetDuaCategory(private val repository: DuaCategoryRepository) {
    suspend operator fun invoke(id: String, language: Language): DuaCategory? =
        repository.categories(language).firstOrNull { it.id == id }
}

class GetDuaChapter(private val repository: DuaCategoryRepository) {
    suspend operator fun invoke(id: String, language: Language): DuaChapter? =
        repository.categories(language).asSequence().flatMap { it.chapters }.firstOrNull { it.id == id }
}

class GetDuaEntriesById(private val repository: DuaCategoryRepository) {
    suspend operator fun invoke(language: Language): Map<String, DuaEntry> =
        repository.categories(language).flatMap { it.chapters }.flatMap { it.entries }.associateBy { it.id }
}

class GetDuaOfTheDay(private val repository: QuranicDuaRepository) {
    suspend operator fun invoke(date: LocalDate, language: Language): QuranicDua? {
        val duas = repository.quranicDuas(language)
        if (duas.isEmpty()) return null
        return duas[date.dayOfEra.mod(duas.size.toLong()).toInt()]
    }
}
