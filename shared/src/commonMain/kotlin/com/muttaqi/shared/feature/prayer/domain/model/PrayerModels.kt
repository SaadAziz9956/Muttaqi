package com.muttaqi.shared.feature.prayer.domain.model

import kotlin.time.Instant

/** The five daily prayers, in the order of the day */
enum class Prayer(val displayName: String) {
    Fajr("Fajr"),
    Dhuhr("Dhuhr"),
    Asr("Asr"),
    Maghrib("Maghrib"),
    Isha("Isha"),
}

/** One day's prayer times, and sunrise, each rounded to the minute */
data class DailyPrayerTimes(
    val fajr: Instant,
    val sunrise: Instant,
    val dhuhr: Instant,
    val asr: Instant,
    val maghrib: Instant,
    val isha: Instant,
) {
    fun time(prayer: Prayer): Instant = when (prayer) {
        Prayer.Fajr -> fajr
        Prayer.Dhuhr -> dhuhr
        Prayer.Asr -> asr
        Prayer.Maghrib -> maghrib
        Prayer.Isha -> isha
    }
}

data class UpcomingPrayer(val prayer: Prayer, val time: Instant)

/** Today's and tomorrow's times, so there is always a next prayer, even after Isha */
data class PrayerSchedule(val today: DailyPrayerTimes, val tomorrow: DailyPrayerTimes) {
    fun nextPrayer(after: Instant): UpcomingPrayer? =
        listOf(today, tomorrow)
            .flatMap { day -> Prayer.entries.map { UpcomingPrayer(it, day.time(it)) } }
            .firstOrNull { it.time > after }
}
