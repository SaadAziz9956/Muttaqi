package com.muttaqi.shared.feature.dhikr.data.progress

import com.muttaqi.shared.feature.dhikr.domain.repository.CurrentDay
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock

object SystemCurrentDay : CurrentDay {
    override fun today(): LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
}
