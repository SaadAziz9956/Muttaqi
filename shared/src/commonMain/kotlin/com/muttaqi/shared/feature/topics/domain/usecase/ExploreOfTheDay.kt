package com.muttaqi.shared.feature.topics.domain.usecase

import com.muttaqi.shared.core.model.Language
import com.muttaqi.shared.feature.topics.domain.model.ExploreTopic
import com.muttaqi.shared.feature.topics.domain.model.HadithPassage
import com.muttaqi.shared.feature.topics.domain.repository.ExploreRepository
import kotlinx.datetime.LocalDate

/** The same Explore topic all day, for Home, moving to another at midnight */
class GetTopicOfTheDay(private val repository: ExploreRepository) {
    suspend operator fun invoke(date: LocalDate, language: Language): ExploreTopic? {
        val topics = repository.groups(language).flatMap { it.topics }
        if (topics.isEmpty()) return null
        return topics[(date.dayNumber * 7).mod(topics.size)]
    }
}

/** The same short authentic hadith from Explore all day, for Home, moving to another at midnight */
class GetHadithOfTheDay(private val repository: ExploreRepository) {
    suspend operator fun invoke(date: LocalDate, language: Language): HadithPassage? {
        // HadeethEnc's texts are shown in full, so Home picks from the ones short enough for a card, and from the
        // topics everyone meets day to day rather than rulings for particular situations
        val hadith = repository.groups(language)
            .filter { it.id in EVERYDAY_GROUPS }
            .flatMap { it.topics }
            .flatMap { it.hadith }
            .filter { it.translation.characterCount() <= MAX_LENGTH }
        if (hadith.isEmpty()) return null
        return hadith[(date.dayNumber * 13).mod(hadith.size)]
    }

    private companion object {
        val EVERYDAY_GROUPS = setOf("faith", "worship", "character", "society", "daily-life")
        const val MAX_LENGTH = 420
    }
}

/**
 * The day's number counted from 1 January of year 1, as the iOS app has always numbered its days (Foundation's
 * `ordinality(of: .day, in: .era)`), so each day's picks stay the same
 */
private val LocalDate.dayNumber: Long get() = toEpochDays() + DAYS_BEFORE_1970

private const val DAYS_BEFORE_1970 = 719_163L

/**
 * Length in characters as a reader counts them, and as Swift counted it: a mark on a letter, such as a haraka, isn't
 * one of its own, nor is a joiner, and an emoji is one
 */
private fun String.characterCount(): Int = count { char ->
    !char.isLowSurrogate() &&
        char != '‌' && char != '‍' &&
        char.category != CharCategory.NON_SPACING_MARK &&
        char.category != CharCategory.COMBINING_SPACING_MARK &&
        char.category != CharCategory.ENCLOSING_MARK
}
