package com.muttaqi.shared.feature.home

import com.muttaqi.shared.feature.home.HomeTestData.karachi
import com.muttaqi.shared.feature.home.HomeTestData.surah
import com.muttaqi.shared.feature.home.domain.model.LastReading
import com.muttaqi.shared.feature.home.domain.model.QuranShortcuts
import com.muttaqi.shared.feature.home.domain.usecase.GetHijriDate
import com.muttaqi.shared.feature.home.domain.usecase.GetQuranShortcuts
import com.muttaqi.shared.feature.home.presentation.HomeLocation
import com.muttaqi.shared.feature.home.presentation.HomeMutation
import com.muttaqi.shared.feature.home.presentation.HomeReducer
import com.muttaqi.shared.feature.home.presentation.HomeState
import com.muttaqi.shared.feature.prayer.PrayerTestData
import com.muttaqi.shared.feature.prayer.data.qibla.AdhanQiblaRepository
import com.muttaqi.shared.feature.prayer.data.times.AdhanPrayerTimesRepository
import com.muttaqi.shared.feature.prayer.domain.model.LocationAccess
import com.muttaqi.shared.feature.prayer.domain.usecase.GetPrayerSchedule
import com.muttaqi.shared.feature.quran.domain.usecase.GetLastReading
import com.muttaqi.shared.feature.quran.domain.usecase.GetSurahs
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.hours

class HomeUseCasesTest {
    private val hijri = GetHijriDate(fakeHijri)

    private fun karachiTime(hour: Int, minute: Int) = LocalDateTime(2026, 9, 30, hour, minute).toInstant(karachi)

    @Test
    fun theHijriDateTurnsAtMaghribNotMidnight() {
        val maghrib = karachiTime(18, 20)
        assertEquals("Hijri of 2026-09-30", hijri(karachiTime(18, 19), maghrib, karachi))
        assertEquals("Hijri of 2026-10-01", hijri(maghrib, maghrib, karachi))
        assertEquals("Hijri of 2026-10-01", hijri(karachiTime(23, 59), maghrib, karachi))
        assertEquals("Hijri of 2026-09-30", hijri(karachiTime(23, 59), null, karachi))
        assertEquals("Hijri of 2026-10-01", hijri(maghrib + 6.hours, maghrib, karachi))
    }

    @Test
    fun theDayIsTheReadersOwn() {
        val late = LocalDateTime(2026, 9, 30, 22, 0).toInstant(TimeZone.UTC)
        assertEquals("Hijri of 2026-10-01", hijri(late, null, karachi))
        assertEquals("Hijri of 2026-09-30", hijri(late, null, TimeZone.of("America/New_York")))
    }

    @Test
    fun theShortcutsNameTheSurahBeingReadAndAlKahf() = runTest {
        val quran = FakeQuran()
        val shortcuts = GetQuranShortcuts(GetSurahs(quran), GetLastReading(quran, quran))
        assertEquals(QuranShortcuts(lastReading = null, kahf = surah(18)), shortcuts())
        quran.readUpTo(2, 9)
        assertEquals(QuranShortcuts(LastReading(surah(2), 9), surah(18)), shortcuts())
        quran.stored = false
        assertEquals(QuranShortcuts(), shortcuts())
    }

    @Test
    fun withoutTimesTheLocationSaysWhy() {
        fun checked(access: LocationAccess, state: HomeState = HomeState()) =
            HomeReducer.reduce(state, HomeMutation.LocationChecked(access)).location
        assertEquals(HomeLocation.NeedsPermission, checked(LocationAccess.NotDetermined))
        assertEquals(HomeLocation.Denied, checked(LocationAccess.Denied))
        assertEquals(HomeLocation.Unknown, checked(LocationAccess.Granted))

        val schedule = GetPrayerSchedule(AdhanPrayerTimesRepository())(PrayerTestData.karachi, karachiTime(12, 0), karachi)
        val located = HomeReducer.reduce(HomeState(), HomeMutation.Located(schedule, AdhanQiblaRepository().qiblaDirection(PrayerTestData.karachi)))
        LocationAccess.entries.forEach { assertEquals(HomeLocation.Available, checked(it, located)) }
    }
}
