package com.muttaqi.shared.feature.onboarding.domain.model

/** The first launch, one step at a time, in this order */
enum class OnboardingStep {
    Welcome,
    Name,
    Goals,
    Notification,
    Location,
    /** Downloads what the app needs before Home opens */
    Setup,
}
