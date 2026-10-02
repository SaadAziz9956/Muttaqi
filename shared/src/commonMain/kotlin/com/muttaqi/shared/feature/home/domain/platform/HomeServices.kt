package com.muttaqi.shared.feature.home.domain.platform

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

fun interface HijriCalendar {
    fun format(date: LocalDate): String
}

interface ReaderClock {
    val timeZone: TimeZone

    fun now(): Instant

    fun minutes(): Flow<Instant>
}

fun ReaderClock.dateAt(instant: Instant): LocalDate = instant.toLocalDateTime(timeZone).date

fun ReaderClock.today(): LocalDate = dateAt(now())
