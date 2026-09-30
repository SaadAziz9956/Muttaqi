package com.muttaqi.shared.feature.topics

import com.muttaqi.shared.core.preferences.SelectedLanguage
import com.muttaqi.shared.feature.topics.domain.model.ExploreTopic
import com.muttaqi.shared.feature.topics.domain.model.HadithPassage
import com.muttaqi.shared.feature.topics.domain.usecase.GetHadithOfTheDay
import com.muttaqi.shared.feature.topics.domain.usecase.GetTopicOfTheDay
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import kotlin.time.Clock

/**
 * Home's picks from Explore for Swift, while Home is still in Swift: today's topic and hadith in the reader's
 * language, e.g. `try await ExploreOfTheDay.shared.topic()`
 */
object ExploreOfTheDay : KoinComponent {
    suspend fun topic(): ExploreTopic? = get<GetTopicOfTheDay>()(today(), get<SelectedLanguage>().current)

    suspend fun hadith(): HadithPassage? = get<GetHadithOfTheDay>()(today(), get<SelectedLanguage>().current)

    private fun today(): LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
}
