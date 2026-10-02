package com.muttaqi.shared.feature.onboarding.presentation

import com.muttaqi.shared.core.mvi.UiEffect
import com.muttaqi.shared.core.mvi.UiIntent
import com.muttaqi.shared.core.mvi.UiMutation
import com.muttaqi.shared.core.mvi.UiState
import com.muttaqi.shared.feature.onboarding.domain.model.OnboardingStep

data class OnboardingState(
    val step: OnboardingStep = OnboardingStep.Welcome,
    val name: String = "",
    val isSettingUp: Boolean = false,
    val setupError: String? = null,
) : UiState

sealed interface OnboardingIntent : UiIntent {
    data object Begin : OnboardingIntent
    data class NameChanged(val name: String) : OnboardingIntent
    data object SaveName : OnboardingIntent
    data object Next : OnboardingIntent
    data object RequestNotification : OnboardingIntent
    data object SkipNotification : OnboardingIntent
    data object RequestLocation : OnboardingIntent
    data object SkipLocation : OnboardingIntent
    data object RetrySetup : OnboardingIntent
}

sealed interface OnboardingMutation : UiMutation {
    data class MovedTo(val step: OnboardingStep) : OnboardingMutation
    data class NameChanged(val name: String) : OnboardingMutation
    data object SetupStarted : OnboardingMutation
    data object SetupFinished : OnboardingMutation
    data class SetupFailed(val message: String) : OnboardingMutation
}

sealed interface OnboardingEffect : UiEffect {
    data object Finished : OnboardingEffect
}
