package com.muttaqi.android.feature.home

import android.app.Application
import com.muttaqi.shared.feature.home.data.IcuHijriCalendar
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [35])
class IcuHijriCalendarTest {
    private val calendar = IcuHijriCalendar()

    @Test
    fun theHijriDateIsWrittenAsOnIos() {
        assertEquals("Rabiʻ II 19, 1448 AH", calendar.format(LocalDate(2026, 9, 30)))
        assertEquals("Rabiʻ II 20, 1448 AH", calendar.format(LocalDate(2026, 10, 1)))
        assertEquals("Muharram 1, 1448 AH", calendar.format(LocalDate(2026, 6, 16)))
        assertEquals("Dhuʻl-Hijjah 10, 1447 AH", calendar.format(LocalDate(2026, 5, 27)))
        assertEquals("Ramadan 1, 1448 AH", calendar.format(LocalDate(2027, 2, 8)))
    }
}
