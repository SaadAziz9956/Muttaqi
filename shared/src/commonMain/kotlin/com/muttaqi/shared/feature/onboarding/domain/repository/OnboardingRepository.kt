package com.muttaqi.shared.feature.onboarding.domain.repository

interface OnboardingRepository {
    fun isComplete(): Boolean

    fun markComplete()

    fun userName(): String?

    fun saveUserName(name: String)
}
