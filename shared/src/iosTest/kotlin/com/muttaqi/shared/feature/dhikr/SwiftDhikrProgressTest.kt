package com.muttaqi.shared.feature.dhikr

import com.muttaqi.shared.core.preferences.UserDefaultsLegacyDataSource
import com.muttaqi.shared.feature.dhikr.data.progress.SettingsDhikrProgressRepository
import com.muttaqi.shared.feature.dhikr.domain.model.DhikrProgress
import com.russhwolf.settings.NSUserDefaultsSettings
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSUserDefaults
import platform.Foundation.dataUsingEncoding
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** The Swift app's saved progress read from real user defaults, as it's stored on a phone before the update */
class SwiftDhikrProgressTest {
    private val suite = "com.muttaqi.shared.tests.dhikr"
    private val defaults = NSUserDefaults(suiteName = suite)

    @AfterTest
    fun tearDown() = defaults.removePersistentDomainForName(suite)

    @Suppress("CAST_NEVER_SUCCEEDS")
    private fun swiftData(json: String) = (json as NSString).dataUsingEncoding(NSUTF8StringEncoding)

    @Test
    fun theSwiftAppsDataIsReadAndReplacedByTheNextCount() {
        // What `defaults.set(try? JSONEncoder().encode(progress), forKey:)` stored after 7 taps on 2026-09-30 in Karachi
        defaults.setObject(swiftData("""{"rounds":0,"day":812401200,"count":7}"""), forKey = "dhikr_progress.subhanallah")
        val settings = NSUserDefaultsSettings(defaults)
        // Settings only reads text, which is why the data needs a reader of its own
        assertNull(settings.getStringOrNull("dhikr_progress.subhanallah"))

        val today = LocalDate(2026, 9, 30)
        val repository = SettingsDhikrProgressRepository(
            settings,
            UserDefaultsLegacyDataSource(defaults),
            timeZone = { TimeZone.of("Asia/Karachi") },
        )
        assertEquals(DhikrProgress(7, 0, today), repository.saved("subhanallah"))

        repository.save("subhanallah", DhikrProgress(8, 0, today))
        assertEquals("""{"count":8,"rounds":0,"day":812401200}""", defaults.stringForKey("dhikr_progress.subhanallah"))
        assertEquals(DhikrProgress(8, 0, today), repository.saved("subhanallah"))
    }
}
