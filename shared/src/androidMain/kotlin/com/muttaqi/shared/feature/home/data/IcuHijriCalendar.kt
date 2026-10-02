package com.muttaqi.shared.feature.home.data

import android.icu.text.SimpleDateFormat
import android.icu.util.IslamicCalendar
import android.icu.util.TimeZone
import android.icu.util.ULocale
import com.muttaqi.shared.feature.home.domain.platform.HijriCalendar
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.toInstant
import java.util.Date
import kotlinx.datetime.TimeZone as DateTimeZone

class IcuHijriCalendar : HijriCalendar {
    private val format = SimpleDateFormat(PATTERN, ULocale("en@calendar=islamic-umalqura")).apply {
        calendar = IslamicCalendar(TimeZone.GMT_ZONE, ULocale.ENGLISH).apply {
            calculationType = IslamicCalendar.CalculationType.ISLAMIC_UMALQURA
        }
        timeZone = TimeZone.GMT_ZONE
    }

    override fun format(date: LocalDate): String {
        val noon = LocalDateTime(date, LocalTime(12, 0)).toInstant(DateTimeZone.UTC)
        return synchronized(format) { format.format(Date(noon.toEpochMilliseconds())) }
    }

    private companion object {
        const val PATTERN = "MMMM d, y G"
    }
}
