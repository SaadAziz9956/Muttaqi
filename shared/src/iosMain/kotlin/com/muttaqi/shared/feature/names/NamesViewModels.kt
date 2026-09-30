package com.muttaqi.shared.feature.names

import com.muttaqi.shared.core.preferences.SelectedLanguage
import com.muttaqi.shared.feature.names.domain.model.AllahName
import com.muttaqi.shared.feature.names.domain.usecase.GetNameOfTheDay
import com.muttaqi.shared.feature.names.presentation.NamesViewModel
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import kotlin.time.Clock

/** The 99 Names view model for Swift, from Koin: `NamesViewModels.shared.names()`, shared by the page and its search */
object NamesViewModels : KoinComponent {
    fun names(): NamesViewModel = get()

    /** Today's Name in the reader's language, for Home's tile: `try await NamesViewModels.shared.nameOfTheDay()` */
    suspend fun nameOfTheDay(): AllahName? =
        get<GetNameOfTheDay>()(Clock.System.todayIn(TimeZone.currentSystemDefault()), get<SelectedLanguage>().current)
}
