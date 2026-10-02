package com.muttaqi.shared.feature.onboarding.data.repository

import com.muttaqi.shared.feature.onboarding.domain.repository.OnboardingRepository
import com.russhwolf.settings.Settings

class SettingsOnboardingRepository(private val settings: Settings) : OnboardingRepository {
    override fun isComplete(): Boolean = settings.getBoolean(COMPLETE, false)

    override fun markComplete() = settings.putBoolean(COMPLETE, true)

    override fun userName(): String? = settings.getStringOrNull(USER_NAME)

    override fun saveUserName(name: String) = settings.putString(USER_NAME, name)

    private companion object {
        const val COMPLETE = "onboarding_complete"
        const val USER_NAME = "user_name"
    }
}
