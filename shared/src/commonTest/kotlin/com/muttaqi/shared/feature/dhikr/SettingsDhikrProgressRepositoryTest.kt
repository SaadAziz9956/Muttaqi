package com.muttaqi.shared.feature.dhikr

import com.muttaqi.shared.core.preferences.LegacyDataSource
import com.muttaqi.shared.feature.dhikr.DhikrTestData.today
import com.muttaqi.shared.feature.dhikr.data.progress.SettingsDhikrProgressRepository
import com.muttaqi.shared.feature.dhikr.domain.model.DhikrProgress
import com.russhwolf.settings.MapSettings
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SettingsDhikrProgressRepositoryTest {
    private val karachi = TimeZone.of("Asia/Karachi")
    private val settings = MapSettings()
    private val legacy = mutableMapOf<String, String>()

    private fun repository(timeZone: TimeZone = karachi) =
        SettingsDhikrProgressRepository(settings, LegacyDataSource { legacy[it] }, timeZone = { timeZone })

    private val swiftJson = """{"rounds":0,"day":812401200,"count":7}"""

    @Test
    fun readsTheSwiftAppsProgressUnderItsKey() {
        settings.putString("dhikr_progress.subhanallah", swiftJson)
        assertEquals(DhikrProgress(7, 0, today), repository().saved("subhanallah"))
    }

    @Test
    fun readsProgressTheSwiftAppStoredAsData() {
        legacy["dhikr_progress.subhanallah"] = swiftJson
        assertEquals(DhikrProgress(7, 0, today), repository().saved("subhanallah"))
    }

    @Test
    fun writesTheSwiftFormatAsText() {
        repository().save("subhanallah", DhikrProgress(33, 2, today))
        assertEquals("""{"count":33,"rounds":2,"day":812401200}""", settings.getStringOrNull("dhikr_progress.subhanallah"))
    }

    @Test
    fun aNewSaveReplacesTheSwiftData() {
        legacy["dhikr_progress.subhanallah"] = swiftJson
        val repository = repository()
        repository.save("subhanallah", DhikrProgress(8, 0, today))
        assertEquals(DhikrProgress(8, 0, today), repository.saved("subhanallah"))
    }

    @Test
    fun theDayIsReadInTheReadersTimeZone() {
        settings.putString("dhikr_progress.subhanallah", swiftJson)
        assertEquals(LocalDate(2026, 9, 29), repository(TimeZone.UTC).saved("subhanallah")?.day)
    }

    @Test
    fun aSavedDayReadsBackAsTheSameDay() {
        val newYork = TimeZone.of("America/New_York")
        repository(newYork).save("subhanallah", DhikrProgress(1, 0, today))
        assertEquals(today, repository(newYork).saved("subhanallah")?.day)
    }

    @Test
    fun fractionalSecondsAndOtherKeysAreRead() {
        settings.putString("dhikr_progress.subhanallah", """{"count":1,"rounds":0,"day":812401200.25,"extra":true}""")
        assertEquals(DhikrProgress(1, 0, today), repository().saved("subhanallah"))
    }

    @Test
    fun missingOrUnreadableProgressIsNone() {
        assertNull(repository().saved("subhanallah"))
        settings.putString("dhikr_progress.subhanallah", "not json")
        assertNull(repository().saved("subhanallah"))
        settings.putString("dhikr_progress.subhanallah", """{"count":1}""")
        assertNull(repository().saved("subhanallah"))
    }
}
