package com.muttaqi.android.feature.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.muttaqi.shared.feature.onboarding.domain.usecase.IsOnboardingComplete
import com.muttaqi.shared.feature.onboarding.presentation.OnboardingViewModel
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun OnboardingGate(content: @Composable () -> Unit) {
    val isOnboardingComplete = koinInject<IsOnboardingComplete>()
    var complete by rememberSaveable { mutableStateOf(isOnboardingComplete()) }
    if (complete) {
        content()
    } else {
        OnboardingRoute(viewModel = koinViewModel<OnboardingViewModel>(), onFinished = { complete = true })
    }
}
