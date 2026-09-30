package com.muttaqi.android.feature.onboarding

import androidx.compose.runtime.Composable

/** Shows onboarding on first launch, then [content] (the tabs). Until onboarding is built, it goes straight in */
@Composable
fun OnboardingGate(content: @Composable () -> Unit) {
    content()
}
