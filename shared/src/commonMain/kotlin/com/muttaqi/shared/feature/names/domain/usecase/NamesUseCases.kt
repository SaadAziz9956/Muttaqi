package com.muttaqi.shared.feature.names.domain.usecase

import com.muttaqi.shared.core.domain.dayOfEra
import com.muttaqi.shared.core.model.Language
import com.muttaqi.shared.feature.names.domain.model.AllahName
import com.muttaqi.shared.feature.names.domain.repository.NamesRepository
import kotlinx.datetime.LocalDate

class GetAllahNames(private val repository: NamesRepository) {
    suspend operator fun invoke(language: Language): List<AllahName> = repository.names(language)
}

class GetNameOfTheDay(private val repository: NamesRepository) {
    suspend operator fun invoke(date: LocalDate, language: Language): AllahName? {
        val names = repository.names(language)
        if (names.isEmpty()) return null
        return names[date.dayOfEra.mod(names.size.toLong()).toInt()]
    }
}
