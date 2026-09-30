package com.muttaqi.shared.feature.names.domain.usecase

import com.muttaqi.shared.core.domain.dayOfEra
import com.muttaqi.shared.core.model.Language
import com.muttaqi.shared.feature.names.domain.model.AllahName
import com.muttaqi.shared.feature.names.domain.repository.NamesRepository
import kotlinx.datetime.LocalDate

/** The 99 names in order, in the reader's language */
class GetAllahNames(private val repository: NamesRepository) {
    suspend operator fun invoke(language: Language): List<AllahName> = repository.names(language)
}

/** The same Name all day, moving to the next at midnight, e.g. for Home's tile */
class GetNameOfTheDay(private val repository: NamesRepository) {
    suspend operator fun invoke(date: LocalDate, language: Language): AllahName? {
        val names = repository.names(language)
        if (names.isEmpty()) return null
        // Days are counted from the start of the Common Era, as the iOS app has always counted them (Calendar's
        // ordinality of the day in the era), so the day's Name doesn't change with the move to shared code
        return names[date.dayOfEra.mod(names.size.toLong()).toInt()]
    }
}
