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

/**
 * ICU's Umm al-Qura calendar, in the same English as iOS writes it, e.g. "Rabiʻ II 19, 1448 AH". The format is made
 * for the Islamic calendar's locale, so its month and era names are the Islamic ones rather than the Gregorian
 */
class IcuHijriCalendar : HijriCalendar {
    private val format = SimpleDateFormat(PATTERN, ULocale("en@calendar=islamic-umalqura")).apply {
        calendar = IslamicCalendar(TimeZone.GMT_ZONE, ULocale.ENGLISH).apply {
            calculationType = IslamicCalendar.CalculationType.ISLAMIC_UMALQURA
        }
        // The day is given as a date, so it's read at noon in one fixed zone, whatever the reader's
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
