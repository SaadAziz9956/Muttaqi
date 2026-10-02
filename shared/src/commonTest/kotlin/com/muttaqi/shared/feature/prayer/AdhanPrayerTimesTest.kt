package com.muttaqi.shared.feature.prayer

import com.muttaqi.shared.feature.prayer.data.qibla.AdhanQiblaRepository
import com.muttaqi.shared.feature.prayer.data.times.AdhanPrayerTimesRepository
import com.muttaqi.shared.feature.prayer.domain.model.Coordinates
import kotlinx.datetime.LocalDate
import kotlin.math.abs
import kotlin.math.roundToLong
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant

class AdhanPrayerTimesTest {
    private data class SwiftDay(val city: String, val date: LocalDate, val times: List<String>?)
    private data class SwiftQibla(val city: String, val bearing: Double, val meters: Double)

    private val cities = mapOf(
        "Karachi" to Coordinates(24.8607, 67.0011),
        "Lahore" to Coordinates(31.5204, 74.3587),
        "London" to Coordinates(51.5072, -0.1276),
        "Oslo" to Coordinates(59.9139, 10.7522),
        "Reykjavik" to Coordinates(64.1466, -21.9426),
        "Tromso" to Coordinates(69.6492, 18.9553),
        "Makkah" to Coordinates(21.4225, 39.8262),
        "NewYork" to Coordinates(40.7128, -74.006),
        "Sydney" to Coordinates(-33.8688, 151.2093),
    )

    private val swiftTimes = listOf(
        SwiftDay("Karachi", LocalDate(2026, 9, 30), listOf("2026-09-30T00:08:00Z", "2026-09-30T01:24:00Z", "2026-09-30T07:23:00Z", "2026-09-30T11:40:00Z", "2026-09-30T13:20:00Z", "2026-09-30T14:36:00Z")),
        SwiftDay("Karachi", LocalDate(2026, 6, 21), listOf("2026-06-20T23:14:00Z", "2026-06-21T00:43:00Z", "2026-06-21T07:35:00Z", "2026-06-21T12:17:00Z", "2026-06-21T14:24:00Z", "2026-06-21T15:53:00Z")),
        SwiftDay("Karachi", LocalDate(2026, 12, 21), listOf("2026-12-21T00:51:00Z", "2026-12-21T02:12:00Z", "2026-12-21T07:31:00Z", "2026-12-21T11:12:00Z", "2026-12-21T12:48:00Z", "2026-12-21T14:09:00Z")),
        SwiftDay("Karachi", LocalDate(2026, 3, 20), listOf("2026-03-20T00:20:00Z", "2026-03-20T01:36:00Z", "2026-03-20T07:41:00Z", "2026-03-20T12:02:00Z", "2026-03-20T13:43:00Z", "2026-03-20T14:59:00Z")),
        SwiftDay("Lahore", LocalDate(2026, 9, 30), listOf("2026-09-29T23:35:00Z", "2026-09-30T00:56:00Z", "2026-09-30T06:54:00Z", "2026-09-30T11:08:00Z", "2026-09-30T12:49:00Z", "2026-09-30T14:10:00Z")),
        SwiftDay("Lahore", LocalDate(2026, 6, 21), listOf("2026-06-20T22:19:00Z", "2026-06-20T23:58:00Z", "2026-06-21T07:05:00Z", "2026-06-21T12:01:00Z", "2026-06-21T14:11:00Z", "2026-06-21T15:50:00Z")),
        SwiftDay("Lahore", LocalDate(2026, 12, 21), listOf("2026-12-21T00:31:00Z", "2026-12-21T01:58:00Z", "2026-12-21T07:02:00Z", "2026-12-21T10:26:00Z", "2026-12-21T12:03:00Z", "2026-12-21T13:30:00Z")),
        SwiftDay("Lahore", LocalDate(2026, 3, 20), listOf("2026-03-19T23:46:00Z", "2026-03-20T01:07:00Z", "2026-03-20T07:11:00Z", "2026-03-20T11:31:00Z", "2026-03-20T13:14:00Z", "2026-03-20T14:35:00Z")),
        SwiftDay("London", LocalDate(2026, 9, 30), listOf("2026-09-30T04:14:00Z", "2026-09-30T05:59:00Z", "2026-09-30T11:51:00Z", "2026-09-30T15:45:00Z", "2026-09-30T17:41:00Z", "2026-09-30T19:26:00Z")),
        SwiftDay("London", LocalDate(2026, 6, 21), listOf("2026-06-21T02:40:00Z", "2026-06-21T03:43:00Z", "2026-06-21T12:03:00Z", "2026-06-21T17:40:00Z", "2026-06-21T20:22:00Z", "2026-06-21T21:25:00Z")),
        SwiftDay("London", LocalDate(2026, 12, 21), listOf("2026-12-21T05:59:00Z", "2026-12-21T08:04:00Z", "2026-12-21T12:00:00Z", "2026-12-21T14:07:00Z", "2026-12-21T15:53:00Z", "2026-12-21T17:58:00Z")),
        SwiftDay("London", LocalDate(2026, 3, 20), listOf("2026-03-20T04:22:00Z", "2026-03-20T06:03:00Z", "2026-03-20T12:09:00Z", "2026-03-20T16:16:00Z", "2026-03-20T18:14:00Z", "2026-03-20T19:55:00Z")),
        SwiftDay("Oslo", LocalDate(2026, 9, 30), listOf("2026-09-30T03:33:00Z", "2026-09-30T05:20:00Z", "2026-09-30T11:08:00Z", "2026-09-30T14:45:00Z", "2026-09-30T16:53:00Z", "2026-09-30T18:40:00Z")),
        SwiftDay("Oslo", LocalDate(2026, 6, 21), listOf("2026-06-21T01:09:00Z", "2026-06-21T01:54:00Z", "2026-06-21T11:20:00Z", "2026-06-21T17:19:00Z", "2026-06-21T20:44:00Z", "2026-06-21T21:28:00Z")),
        SwiftDay("Oslo", LocalDate(2026, 12, 21), listOf("2026-12-21T05:43:00Z", "2026-12-21T08:18:00Z", "2026-12-21T11:16:00Z", "2026-12-21T12:26:00Z", "2026-12-21T14:12:00Z", "2026-12-21T16:47:00Z")),
        SwiftDay("Oslo", LocalDate(2026, 3, 20), listOf("2026-03-20T03:38:00Z", "2026-03-20T05:19:00Z", "2026-03-20T11:25:00Z", "2026-03-20T15:21:00Z", "2026-03-20T17:31:00Z", "2026-03-20T19:12:00Z")),
        SwiftDay("Reykjavik", LocalDate(2026, 9, 30), listOf("2026-09-30T05:46:00Z", "2026-09-30T07:34:00Z", "2026-09-30T13:19:00Z", "2026-09-30T16:43:00Z", "2026-09-30T19:00:00Z", "2026-09-30T20:48:00Z")),
        SwiftDay("Reykjavik", LocalDate(2026, 6, 21), listOf("2026-06-21T02:31:00Z", "2026-06-21T02:55:00Z", "2026-06-21T13:31:00Z", "2026-06-21T19:46:00Z", "2026-06-22T00:04:00Z", "2026-06-22T00:29:00Z")),
        SwiftDay("Reykjavik", LocalDate(2026, 12, 21), listOf("2026-12-21T08:32:00Z", "2026-12-21T11:22:00Z", "2026-12-21T13:27:00Z", "2026-12-21T13:55:00Z", "2026-12-21T15:29:00Z", "2026-12-21T18:20:00Z")),
        SwiftDay("Reykjavik", LocalDate(2026, 3, 20), listOf("2026-03-20T05:48:00Z", "2026-03-20T07:29:00Z", "2026-03-20T13:36:00Z", "2026-03-20T17:24:00Z", "2026-03-20T19:43:00Z", "2026-03-20T21:24:00Z")),
        SwiftDay("Tromso", LocalDate(2026, 9, 30), listOf("2026-09-30T03:05:00Z", "2026-09-30T04:55:00Z", "2026-09-30T10:35:00Z", "2026-09-30T13:40:00Z", "2026-09-30T16:11:00Z", "2026-09-30T18:01:00Z")),
        SwiftDay("Tromso", LocalDate(2026, 6, 21), null),
        SwiftDay("Tromso", LocalDate(2026, 12, 21), null),
        SwiftDay("Tromso", LocalDate(2026, 3, 20), listOf("2026-03-20T03:04:00Z", "2026-03-20T04:44:00Z", "2026-03-20T10:53:00Z", "2026-03-20T14:26:00Z", "2026-03-20T17:02:00Z", "2026-03-20T18:41:00Z")),
        SwiftDay("Makkah", LocalDate(2026, 9, 30), listOf("2026-09-30T01:58:00Z", "2026-09-30T03:12:00Z", "2026-09-30T09:12:00Z", "2026-09-30T13:30:00Z", "2026-09-30T15:10:00Z", "2026-09-30T16:23:00Z")),
        SwiftDay("Makkah", LocalDate(2026, 6, 21), listOf("2026-06-21T01:14:00Z", "2026-06-21T02:39:00Z", "2026-06-21T09:23:00Z", "2026-06-21T14:02:00Z", "2026-06-21T16:06:00Z", "2026-06-21T17:31:00Z")),
        SwiftDay("Makkah", LocalDate(2026, 12, 21), listOf("2026-12-21T02:34:00Z", "2026-12-21T03:54:00Z", "2026-12-21T09:20:00Z", "2026-12-21T13:08:00Z", "2026-12-21T14:44:00Z", "2026-12-21T16:03:00Z")),
        SwiftDay("Makkah", LocalDate(2026, 3, 20), listOf("2026-03-20T02:11:00Z", "2026-03-20T03:25:00Z", "2026-03-20T09:29:00Z", "2026-03-20T13:50:00Z", "2026-03-20T15:32:00Z", "2026-03-20T16:46:00Z")),
        SwiftDay("NewYork", LocalDate(2026, 9, 30), listOf("2026-09-30T09:20:00Z", "2026-09-30T10:52:00Z", "2026-09-30T16:47:00Z", "2026-09-30T20:54:00Z", "2026-09-30T22:40:00Z", "2026-10-01T00:11:00Z")),
        SwiftDay("NewYork", LocalDate(2026, 6, 21), listOf("2026-06-21T07:19:00Z", "2026-06-21T09:25:00Z", "2026-06-21T16:59:00Z", "2026-06-21T22:12:00Z", "2026-06-22T00:31:00Z", "2026-06-22T02:37:00Z")),
        SwiftDay("NewYork", LocalDate(2026, 12, 21), listOf("2026-12-21T10:38:00Z", "2026-12-21T12:17:00Z", "2026-12-21T16:55:00Z", "2026-12-21T19:51:00Z", "2026-12-21T21:32:00Z", "2026-12-21T23:11:00Z")),
        SwiftDay("NewYork", LocalDate(2026, 3, 20), listOf("2026-03-20T09:28:00Z", "2026-03-20T10:59:00Z", "2026-03-20T17:04:00Z", "2026-03-20T21:21:00Z", "2026-03-20T23:08:00Z", "2026-03-21T00:40:00Z")),
        SwiftDay("Sydney", LocalDate(2026, 9, 30), listOf("2026-09-29T18:10:00Z", "2026-09-29T19:34:00Z", "2026-09-30T01:46:00Z", "2026-09-30T06:11:00Z", "2026-09-30T07:57:00Z", "2026-09-30T09:21:00Z")),
        SwiftDay("Sydney", LocalDate(2026, 6, 21), listOf("2026-06-20T19:31:00Z", "2026-06-20T21:00:00Z", "2026-06-21T01:58:00Z", "2026-06-21T05:16:00Z", "2026-06-21T06:54:00Z", "2026-06-21T08:23:00Z")),
        SwiftDay("Sydney", LocalDate(2026, 12, 21), listOf("2026-12-20T16:56:00Z", "2026-12-20T18:41:00Z", "2026-12-21T01:54:00Z", "2026-12-21T06:54:00Z", "2026-12-21T09:05:00Z", "2026-12-21T10:50:00Z")),
        SwiftDay("Sydney", LocalDate(2026, 3, 20), listOf("2026-03-19T18:34:00Z", "2026-03-19T19:58:00Z", "2026-03-20T02:04:00Z", "2026-03-20T06:23:00Z", "2026-03-20T08:07:00Z", "2026-03-20T09:30:00Z")),
    )

    private val swiftQibla = listOf(
        SwiftQibla("Karachi", 267.741059701161, 2804339.954313),
        SwiftQibla("Lahore", 260.365857592788, 3602988.763861),
        SwiftQibla("London", 118.987253082525, 4794733.033278),
        SwiftQibla("Oslo", 139.027856055375, 4848480.509187),
        SwiftQibla("Reykjavik", 106.117644123960, 6522782.102411),
        SwiftQibla("Tromso", 154.280503298006, 5540232.158212),
        SwiftQibla("Makkah", 324.892299209339, 0.000000),
        SwiftQibla("NewYork", 58.481696500891, 10323915.883031),
        SwiftQibla("Sydney", 277.499604448740, 13236947.994741),
    )


    private val times = AdhanPrayerTimesRepository()
    private val qibla = AdhanQiblaRepository()

    @Test
    fun everyTimeIsTheMinuteSwiftGave() {
        for (day in swiftTimes) {
            val shared = times.prayerTimes(day.date, cities.getValue(day.city))
            val expected = day.times ?: continue
            val actual = shared?.let { listOf(it.fajr, it.sunrise, it.dhuhr, it.asr, it.maghrib, it.isha) }
            assertEquals(expected.map(Instant::parse), actual, "${day.city} on ${day.date}")
        }
    }

    @Test
    fun whereSwiftGaveNoTimesThereAreNone() {
        val missing = swiftTimes.filter { it.times == null }
        assertEquals(2, missing.size)
        for (day in missing) assertNull(times.prayerTimes(day.date, cities.getValue(day.city)), "${day.city} on ${day.date}")
    }

    @Test
    fun theQiblaBearingIsSwiftsAndTheDistanceCoreLocations() {
        for (expected in swiftQibla) {
            val shared = qibla.qiblaDirection(cities.getValue(expected.city))
            assertTrue(abs(shared.bearing - expected.bearing) < 1e-6, "${expected.city} bearing ${shared.bearing}")
            assertTrue(abs(shared.distanceInKilometers * 1000 - expected.meters) < 0.01, "${expected.city} ${shared.distanceInKilometers} km")
            assertEquals((expected.meters / 1000).roundToLong(), shared.distanceInKilometers.roundToLong())
        }
    }
}
