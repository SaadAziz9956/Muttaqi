package com.muttaqi.shared.feature.onboarding.presentation

import androidx.lifecycle.viewModelScope
import com.muttaqi.shared.core.domain.DomainError
import com.muttaqi.shared.core.domain.Outcome
import com.muttaqi.shared.core.mvi.MviViewModel
import com.muttaqi.shared.core.mvi.Reducer
import com.muttaqi.shared.feature.onboarding.domain.model.OnboardingStep
import com.muttaqi.shared.feature.onboarding.domain.usecase.FinishOnboarding
import com.muttaqi.shared.feature.onboarding.domain.usecase.RequestNotificationPermission
import com.muttaqi.shared.feature.onboarding.domain.usecase.SaveUserName
import com.muttaqi.shared.feature.prayer.domain.usecase.RequestLocationAccess
import kotlinx.coroutines.launch

internal object OnboardingReducer : Reducer<OnboardingState, OnboardingMutation> {
    override fun reduce(state: OnboardingState, mutation: OnboardingMutation) = when (mutation) {
        is OnboardingMutation.MovedTo -> state.copy(step = mutation.step)
        is OnboardingMutation.NameChanged -> state.copy(name = mutation.name)
        OnboardingMutation.SetupStarted -> state.copy(isSettingUp = true, setupError = null)
        OnboardingMutation.SetupFinished -> state.copy(isSettingUp = false)
        is OnboardingMutation.SetupFailed -> state.copy(isSettingUp = false, setupError = mutation.message)
    }
}

class OnboardingViewModel(
    private val saveUserName: SaveUserName,
    private val requestNotificationPermission: RequestNotificationPermission,
    private val requestLocationAccess: RequestLocationAccess,
    private val finishOnboarding: FinishOnboarding,
) : MviViewModel<OnboardingState, OnboardingIntent, OnboardingMutation, OnboardingEffect>(OnboardingState(), OnboardingReducer) {

    override fun handle(intent: OnboardingIntent) {
        when (intent) {
            OnboardingIntent.Begin -> moveTo(OnboardingStep.Name)
            is OnboardingIntent.NameChanged -> mutate(OnboardingMutation.NameChanged(intent.name))
            OnboardingIntent.SaveName -> if (saveUserName(state.value.name)) moveTo(OnboardingStep.Goals)
            OnboardingIntent.Next -> moveTo(OnboardingStep.Notification)
            // Whatever the reader answers, onboarding carries on; each feature asks again where it needs to
            OnboardingIntent.RequestNotification -> viewModelScope.launch {
                requestNotificationPermission()
                moveTo(OnboardingStep.Location)
            }
            OnboardingIntent.SkipNotification -> moveTo(OnboardingStep.Location)
            OnboardingIntent.RequestLocation -> viewModelScope.launch {
                requestLocationAccess()
                moveTo(OnboardingStep.Setup)
            }
            OnboardingIntent.SkipLocation -> moveTo(OnboardingStep.Setup)
            OnboardingIntent.RetrySetup -> setUp()
        }
    }

    private fun moveTo(step: OnboardingStep) {
        mutate(OnboardingMutation.MovedTo(step))
        // The setup step starts the download as it opens
        if (step == OnboardingStep.Setup) setUp()
    }

    private fun setUp() {
        if (state.value.isSettingUp) return
        mutate(OnboardingMutation.SetupStarted)
        viewModelScope.launch {
            when (val outcome = finishOnboarding()) {
                is Outcome.Success -> {
                    mutate(OnboardingMutation.SetupFinished)
                    emit(OnboardingEffect.Finished)
                }
                // The platform's own description of what went wrong, as the Swift app showed it
                is Outcome.Failure -> mutate(OnboardingMutation.SetupFailed((outcome.error as? DomainError.Unexpected)?.message.orEmpty()))
            }
        }
    }
}
