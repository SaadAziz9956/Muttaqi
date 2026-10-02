package com.muttaqi.shared.core.preferences

import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSUserDefaults
import platform.Foundation.dataUsingEncoding
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class UserDefaultsLegacyDataSourceTest {
    private val suite = "com.muttaqi.shared.tests.legacy"
    private val defaults = NSUserDefaults(suiteName = suite)
    private val source = UserDefaultsLegacyDataSource(defaults)

    @AfterTest
    fun tearDown() = defaults.removePersistentDomainForName(suite)

    @Suppress("CAST_NEVER_SUCCEEDS")
    private fun swiftData(json: String) = (json as NSString).dataUsingEncoding(NSUTF8StringEncoding)

    @Test
    fun dataIsReadAsItsText() {
        defaults.setObject(swiftData("""{"longitude":67.0011,"latitude":24.8607}"""), forKey = "last_known_coordinates")
        assertEquals("""{"longitude":67.0011,"latitude":24.8607}""", source.text("last_known_coordinates"))
    }

    @Test
    fun nothingSavedIsNothing() {
        assertNull(source.text("last_known_coordinates"))
    }

    @Test
    fun textIsLeftToSettings() {
        defaults.setObject("""{"longitude":67.0011,"latitude":24.8607}""", forKey = "last_known_coordinates")
        assertNull(source.text("last_known_coordinates"))
    }
}
