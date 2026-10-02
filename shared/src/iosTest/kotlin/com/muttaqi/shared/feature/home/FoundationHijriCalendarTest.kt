package com.muttaqi.shared.feature.home

import com.muttaqi.shared.feature.home.data.FoundationHijriCalendar
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class FoundationHijriCalendarTest {
    private val calendar = FoundationHijriCalendar()

    @Test
    fun theHijriDateIsWrittenAsTheSwiftAppWroteIt() {
        assertEquals("Rabiʻ II 19, 1448 AH", calendar.format(LocalDate(2026, 9, 30)))
        assertEquals("Rabiʻ II 20, 1448 AH", calendar.format(LocalDate(2026, 10, 1)))
        assertEquals("Muharram 1, 1448 AH", calendar.format(LocalDate(2026, 6, 16)))
        assertEquals("Dhuʻl-Hijjah 10, 1447 AH", calendar.format(LocalDate(2026, 5, 27)))
        assertEquals("Ramadan 1, 1448 AH", calendar.format(LocalDate(2027, 2, 8)))
    }
}
