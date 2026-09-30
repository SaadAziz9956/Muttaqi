package com.muttaqi.shared.feature.onboarding

import com.muttaqi.shared.feature.onboarding.data.repository.SettingsOnboardingRepository
import com.russhwolf.settings.NSUserDefaultsSettings
import platform.Foundation.NSUserDefaults
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** What the Swift app's onboarding saved, read from real user defaults as it's stored on a phone before the update */
class SwiftOnboardingPreferencesTest {
    private val suite = "com.muttaqi.shared.tests.onboarding"
    private val defaults = NSUserDefaults(suiteName = suite)

    @AfterTest
    fun tearDown() = defaults.removePersistentDomainForName(suite)

    @Test
    fun aReaderWhoFinishedOnboardingIsntAskedAgain() {
        // As `defaults.set(name, forKey:)` and `defaults.set(true, forKey:)` stored them
        defaults.setObject("Saad Aziz", forKey = "user_name")
        defaults.setBool(true, forKey = "onboarding_complete")

        val repository = SettingsOnboardingRepository(NSUserDefaultsSettings(defaults))
        assertTrue(repository.isComplete())
        assertEquals("Saad Aziz", repository.userName())
    }

    @Test
    fun whatTheSharedCodeSavesTheSwiftCodeReadsTheSameWay() {
        val repository = SettingsOnboardingRepository(NSUserDefaultsSettings(defaults))
        repository.saveUserName("Saad")
        repository.markComplete()
        assertEquals("Saad", defaults.stringForKey("user_name"))
        assertTrue(defaults.boolForKey("onboarding_complete"))
    }
}
