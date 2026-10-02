package com.muttaqi.shared.feature.onboarding.di

import com.muttaqi.shared.feature.onboarding.data.repository.SettingsOnboardingRepository
import com.muttaqi.shared.feature.onboarding.domain.repository.OnboardingRepository
import com.muttaqi.shared.feature.onboarding.domain.usecase.FinishOnboarding
import com.muttaqi.shared.feature.onboarding.domain.usecase.IsOnboardingComplete
import com.muttaqi.shared.feature.onboarding.domain.usecase.RequestNotificationPermission
import com.muttaqi.shared.feature.onboarding.domain.usecase.SaveUserName
import com.muttaqi.shared.feature.onboarding.presentation.OnboardingViewModel
import com.russhwolf.settings.ObservableSettings
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val onboardingModule = module {
    single<OnboardingRepository> { SettingsOnboardingRepository(get<ObservableSettings>()) }

    factoryOf(::IsOnboardingComplete)
    factoryOf(::SaveUserName)
    factoryOf(::RequestNotificationPermission)
    factoryOf(::FinishOnboarding)

    viewModelOf(::OnboardingViewModel)
}
