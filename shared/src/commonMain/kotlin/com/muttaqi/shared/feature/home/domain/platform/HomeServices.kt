package com.muttaqi.shared.feature.home.domain.platform

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

/**
 * The Hijri date in the Umm al-Qura calendar, from each platform's own calendar (Foundation's on iOS, ICU's on
 * Android), written as the iOS app has always written it, e.g. "Rabiʻ II 19, 1448 AH"
 */
fun interface HijriCalendar {
    /** The Hijri date that falls on this civil day */
    fun format(date: LocalDate): String
}

/** The reader's time: now, their time zone, and each new minute, so what depends on the time stays current */
interface ReaderClock {
    val timeZone: TimeZone

    fun now(): Instant

    /** The time now, then again as each minute begins, for as long as it's collected */
    fun minutes(): Flow<Instant>
}

/** The date where the reader is at [instant] */
fun ReaderClock.dateAt(instant: Instant): LocalDate = instant.toLocalDateTime(timeZone).date

/** Today's date where the reader is */
fun ReaderClock.today(): LocalDate = dateAt(now())
