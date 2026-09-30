package com.muttaqi.shared.feature.dhikr.data.progress

import com.muttaqi.shared.feature.dhikr.domain.repository.CurrentDay
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock

/** The device's date in its current time zone, read each time so a counter open past midnight moves to the new day */
object SystemCurrentDay : CurrentDay {
    override fun today(): LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
}
