package com.muttaqi.shared.feature.onboarding

import com.muttaqi.shared.feature.onboarding.data.repository.SettingsOnboardingRepository
import com.muttaqi.shared.feature.onboarding.domain.usecase.SaveUserName
import com.russhwolf.settings.MapSettings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SettingsOnboardingRepositoryTest {
    private val settings = MapSettings()
    private val repository = SettingsOnboardingRepository(settings)

    @Test
    fun readsWhatTheSwiftAppSavedUnderItsKeys() {
        settings.putBoolean("onboarding_complete", true)
        settings.putString("user_name", "Saad")
        assertTrue(repository.isComplete())
        assertEquals("Saad", repository.userName())
    }

    @Test
    fun aFirstLaunchHasNothing() {
        assertFalse(repository.isComplete())
        assertNull(repository.userName())
    }

    @Test
    fun savesUnderTheSwiftKeys() {
        repository.markComplete()
        assertTrue(SaveUserName(repository)("Saad"))
        assertEquals(true, settings.getBooleanOrNull("onboarding_complete"))
        assertEquals("Saad", settings.getStringOrNull("user_name"))
        assertFalse(SaveUserName(repository)(""))
        assertEquals("Saad", repository.userName())
    }
}
