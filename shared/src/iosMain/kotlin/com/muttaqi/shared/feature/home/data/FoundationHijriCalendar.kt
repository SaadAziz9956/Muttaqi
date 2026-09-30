package com.muttaqi.shared.feature.home.data

import com.muttaqi.shared.feature.home.domain.platform.HijriCalendar
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarIdentifierIslamicUmmAlQura
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale
import platform.Foundation.NSTimeZone
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.Foundation.timeZoneForSecondsFromGMT

/** Foundation's Umm al-Qura calendar, in the English the Swift app's formatter wrote, e.g. "Rabiʻ II 19, 1448 AH" */
class FoundationHijriCalendar : HijriCalendar {
    private val formatter = NSDateFormatter().apply {
        calendar = NSCalendar(calendarIdentifier = NSCalendarIdentifierIslamicUmmAlQura).apply {
            locale = NSLocale(localeIdentifier = "en")
        }
        locale = NSLocale(localeIdentifier = "en")
        dateFormat = "MMMM d, y G"
        // The day is given as a date, so it's read at noon in one fixed zone, whatever the reader's
        timeZone = NSTimeZone.timeZoneForSecondsFromGMT(0)
    }

    override fun format(date: LocalDate): String {
        val noon = LocalDateTime(date, LocalTime(12, 0)).toInstant(TimeZone.UTC)
        return formatter.stringFromDate(NSDate.dateWithTimeIntervalSince1970(noon.epochSeconds.toDouble()))
    }
}
