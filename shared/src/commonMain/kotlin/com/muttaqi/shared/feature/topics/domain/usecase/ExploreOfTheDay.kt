package com.muttaqi.shared.feature.topics.domain.usecase

import com.muttaqi.shared.core.domain.dayOfEra
import com.muttaqi.shared.core.model.Language
import com.muttaqi.shared.feature.topics.domain.model.ExploreTopic
import com.muttaqi.shared.feature.topics.domain.model.HadithPassage
import com.muttaqi.shared.feature.topics.domain.repository.ExploreRepository
import kotlinx.datetime.LocalDate

class GetTopicOfTheDay(private val repository: ExploreRepository) {
    suspend operator fun invoke(date: LocalDate, language: Language): ExploreTopic? {
        val topics = repository.groups(language).flatMap { it.topics }
        if (topics.isEmpty()) return null
        return topics[(date.dayOfEra * 7).mod(topics.size)]
    }
}

class GetHadithOfTheDay(private val repository: ExploreRepository) {
    suspend operator fun invoke(date: LocalDate, language: Language): HadithPassage? {
        val hadith = repository.groups(language)
            .filter { it.id in EVERYDAY_GROUPS }
            .flatMap { it.topics }
            .flatMap { it.hadith }
            .filter { it.translation.characterCount() <= MAX_LENGTH }
        if (hadith.isEmpty()) return null
        return hadith[(date.dayOfEra * 13).mod(hadith.size)]
    }

    private companion object {
        val EVERYDAY_GROUPS = setOf("faith", "worship", "character", "society", "daily-life")
        const val MAX_LENGTH = 420
    }
}

private fun String.characterCount(): Int = count { char ->
    !char.isLowSurrogate() &&
        char != '‌' && char != '‍' &&
        char.category != CharCategory.NON_SPACING_MARK &&
        char.category != CharCategory.COMBINING_SPACING_MARK &&
        char.category != CharCategory.ENCLOSING_MARK
}
