package com.muttaqi.shared.feature.onboarding.presentation

import com.muttaqi.shared.core.mvi.UiEffect
import com.muttaqi.shared.core.mvi.UiIntent
import com.muttaqi.shared.core.mvi.UiMutation
import com.muttaqi.shared.core.mvi.UiState
import com.muttaqi.shared.feature.onboarding.domain.model.OnboardingStep

data class OnboardingState(
    val step: OnboardingStep = OnboardingStep.Welcome,
    /** What's typed in the name field */
    val name: String = "",
    val isSettingUp: Boolean = false,
    /** Why the first-launch download failed, shown with a retry */
    val setupError: String? = null,
) : UiState

sealed interface OnboardingIntent : UiIntent {
    /** "Begin" on the welcome step */
    data object Begin : OnboardingIntent
    data class NameChanged(val name: String) : OnboardingIntent
    /** "Save" on the name step; a blank name stays on the step */
    data object SaveName : OnboardingIntent
    /** "Begin" on the goals step */
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
    /** Onboarding is done and saved: open Home */
    data object Finished : OnboardingEffect
}
