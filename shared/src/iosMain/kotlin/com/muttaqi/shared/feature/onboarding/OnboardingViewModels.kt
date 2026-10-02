package com.muttaqi.shared.feature.onboarding

import com.muttaqi.shared.feature.onboarding.domain.usecase.IsOnboardingComplete
import com.muttaqi.shared.feature.onboarding.presentation.OnboardingViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

object OnboardingViewModels : KoinComponent {
    fun onboarding(): OnboardingViewModel = get()
}

object OnboardingStatus : KoinComponent {
    fun isComplete(): Boolean = get<IsOnboardingComplete>()()
}
