package com.muttaqi.shared.feature.prayer

import com.muttaqi.shared.feature.prayer.domain.model.DailyPrayerTimes
import com.muttaqi.shared.feature.prayer.domain.model.Prayer
import com.muttaqi.shared.feature.prayer.domain.model.PrayerSchedule
import com.muttaqi.shared.feature.prayer.domain.model.UpcomingPrayer
import platform.Foundation.NSDate
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.Foundation.timeIntervalSince1970
import kotlin.math.roundToLong
import kotlin.time.Instant

// Prayer times as Foundation dates, so Swift formats and compares them as it always has: `upcoming.date`,
// `times.date(prayer: .fajr)`, `schedule.nextPrayer(afterDate: .now)`

val UpcomingPrayer.date: NSDate get() = time.toDate()

fun DailyPrayerTimes.date(prayer: Prayer): NSDate = time(prayer).toDate()

fun PrayerSchedule.nextPrayer(afterDate: NSDate): UpcomingPrayer? =
    nextPrayer(Instant.fromEpochMilliseconds((afterDate.timeIntervalSince1970 * 1000).roundToLong()))

private fun Instant.toDate(): NSDate = NSDate.dateWithTimeIntervalSince1970(toEpochMilliseconds() / 1000.0)
