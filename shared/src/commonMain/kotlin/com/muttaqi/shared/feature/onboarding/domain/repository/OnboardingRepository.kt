package com.muttaqi.shared.feature.onboarding.domain.repository

/** What onboarding keeps: whether it's done, and the name the reader gave */
interface OnboardingRepository {
    fun isComplete(): Boolean

    fun markComplete()

    fun userName(): String?

    fun saveUserName(name: String)
}
