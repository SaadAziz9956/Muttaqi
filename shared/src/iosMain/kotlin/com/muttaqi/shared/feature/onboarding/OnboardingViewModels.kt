package com.muttaqi.shared.feature.onboarding

import com.muttaqi.shared.feature.onboarding.domain.usecase.IsOnboardingComplete
import com.muttaqi.shared.feature.onboarding.presentation.OnboardingViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

/** The onboarding view model for Swift, from Koin: `OnboardingViewModels.shared.onboarding()` */
object OnboardingViewModels : KoinComponent {
    fun onboarding(): OnboardingViewModel = get()
}

/** Whether the first launch is done, for the app's launch in Swift: `OnboardingStatus.shared.isComplete()` */
object OnboardingStatus : KoinComponent {
    fun isComplete(): Boolean = get<IsOnboardingComplete>()()
}
