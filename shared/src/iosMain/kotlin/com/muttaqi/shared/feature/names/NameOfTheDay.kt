package com.muttaqi.shared.feature.names

import com.muttaqi.shared.core.preferences.SelectedLanguage
import com.muttaqi.shared.feature.names.domain.model.AllahName
import com.muttaqi.shared.feature.names.domain.usecase.GetNameOfTheDay
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import kotlin.time.Clock

/**
 * Home's Name of the day for Swift, while Home is still in Swift: today's Name in the reader's language, e.g.
 * `try await NameOfTheDay.shared.name()`
 */
object NameOfTheDay : KoinComponent {
    suspend fun name(): AllahName? =
        get<GetNameOfTheDay>()(Clock.System.todayIn(TimeZone.currentSystemDefault()), get<SelectedLanguage>().current)
}
