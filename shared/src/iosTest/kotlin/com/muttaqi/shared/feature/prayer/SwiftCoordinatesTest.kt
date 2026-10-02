package com.muttaqi.shared.feature.prayer

import com.muttaqi.shared.core.preferences.UserDefaultsLegacyDataSource
import com.muttaqi.shared.feature.prayer.data.location.SavedCoordinates
import com.muttaqi.shared.feature.prayer.domain.model.Coordinates
import com.russhwolf.settings.NSUserDefaultsSettings
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSUserDefaults
import platform.Foundation.dataUsingEncoding
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SwiftCoordinatesTest {
    private val suite = "com.muttaqi.shared.tests.coordinates"
    private val defaults = NSUserDefaults(suiteName = suite)

    @AfterTest
    fun tearDown() = defaults.removePersistentDomainForName(suite)

    @Suppress("CAST_NEVER_SUCCEEDS")
    private fun swiftData(json: String) = (json as NSString).dataUsingEncoding(NSUTF8StringEncoding)

    @Test
    fun theSwiftAppsDataIsReadAndReplacedByTheNextFix() {
        defaults.setObject(swiftData("""{"longitude":67.0011,"latitude":24.8607}"""), forKey = "last_known_coordinates")
        val settings = NSUserDefaultsSettings(defaults)
        assertNull(settings.getStringOrNull("last_known_coordinates"))

        val saved = SavedCoordinates(settings, UserDefaultsLegacyDataSource(defaults))
        assertEquals(Coordinates(24.8607, 67.0011), saved.load())

        saved.save(Coordinates(31.5204, 74.3587))
        assertEquals("""{"latitude":31.5204,"longitude":74.3587}""", defaults.stringForKey("last_known_coordinates"))
        assertEquals(Coordinates(31.5204, 74.3587), saved.load())
    }
}
