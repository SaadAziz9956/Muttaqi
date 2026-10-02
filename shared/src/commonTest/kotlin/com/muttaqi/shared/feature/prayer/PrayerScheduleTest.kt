package com.muttaqi.shared.feature.prayer

import com.muttaqi.shared.feature.prayer.PrayerTestData.karachi
import com.muttaqi.shared.feature.prayer.data.times.AdhanPrayerTimesRepository
import com.muttaqi.shared.feature.prayer.domain.model.Prayer
import com.muttaqi.shared.feature.prayer.domain.model.UpcomingPrayer
import com.muttaqi.shared.feature.prayer.domain.repository.PrayerTimesRepository
import com.muttaqi.shared.feature.prayer.domain.usecase.GetPrayerSchedule
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

class PrayerScheduleTest {
    private val karachiTime = TimeZone.of("Asia/Karachi")
    private val times = AdhanPrayerTimesRepository()
    private val getSchedule = GetPrayerSchedule(times)

    private fun schedule(now: String) = getSchedule(karachi, Instant.parse(now), karachiTime)!!

    @Test
    fun todayIsTheReadersDayNotUtcs() {
        val schedule = schedule("2026-09-29T20:00:00Z")
        assertEquals(times.prayerTimes(LocalDate(2026, 9, 30), karachi), schedule.today)
        assertEquals(times.prayerTimes(LocalDate(2026, 10, 1), karachi), schedule.tomorrow)
        assertEquals(Instant.parse("2026-09-30T00:08:00Z"), schedule.today.fajr)
    }

    @Test
    fun theNextPrayerIsTheFirstStillToCome() {
        val schedule = schedule("2026-09-30T08:00:00Z")
        assertEquals(
            UpcomingPrayer(Prayer.Asr, Instant.parse("2026-09-30T11:40:00Z")),
            schedule.nextPrayer(Instant.parse("2026-09-30T08:00:00Z")),
        )
        assertEquals(Prayer.Maghrib, schedule.nextPrayer(Instant.parse("2026-09-30T11:40:00Z"))?.prayer)
    }

    @Test
    fun afterIshaTheNextIsTomorrowsFajr() {
        val schedule = schedule("2026-09-30T16:00:00Z")
        val next = schedule.nextPrayer(Instant.parse("2026-09-30T16:00:00Z"))
        assertEquals(Prayer.Fajr, next?.prayer)
        assertEquals(schedule.tomorrow.fajr, next?.time)
    }

    @Test
    fun withoutEitherDaysTimesThereIsNoSchedule() {
        val today = LocalDate(2026, 6, 21)
        val onlyToday = PrayerTimesRepository { date, coordinates -> if (date == today) times.prayerTimes(date, coordinates) else null }
        assertNull(GetPrayerSchedule(onlyToday)(karachi, Instant.parse("2026-06-21T06:00:00Z"), karachiTime))
    }

    @Test
    fun eachPrayerHasItsTimeAndName() {
        val today = schedule("2026-09-30T08:00:00Z").today
        assertEquals(listOf(today.fajr, today.dhuhr, today.asr, today.maghrib, today.isha), Prayer.entries.map(today::time))
        assertEquals(listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha"), Prayer.entries.map { it.displayName })
    }
}
