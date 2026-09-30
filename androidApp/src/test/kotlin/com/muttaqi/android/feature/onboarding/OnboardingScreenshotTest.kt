package com.muttaqi.android.feature.onboarding

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import com.muttaqi.android.feature.prayer.PRO_MAX
import com.muttaqi.android.testing.captureLightAndDark
import com.muttaqi.shared.feature.onboarding.domain.model.OnboardingStep
import com.muttaqi.shared.feature.onboarding.presentation.OnboardingState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Each onboarding step, on an iPhone 17 Pro Max-sized screen to compare with the iOS screenshots */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(application = Application::class, sdk = [35], qualifiers = PRO_MAX)
class OnboardingScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    private fun capture(name: String, state: OnboardingState) = compose.captureLightAndDark(name) { OnboardingScreen(state, onIntent = {}) }

    @Test
    fun welcome() = capture("onboarding_welcome", OnboardingState(step = OnboardingStep.Welcome))

    @Test
    fun name() = capture("onboarding_name", OnboardingState(step = OnboardingStep.Name))

    @Test
    fun nameTyped() = capture("onboarding_name_typed", OnboardingState(step = OnboardingStep.Name, name = "Saad Aziz"))

    @Test
    fun goals() = capture("onboarding_goals", OnboardingState(step = OnboardingStep.Goals))

    @Test
    fun notification() = capture("onboarding_notification", OnboardingState(step = OnboardingStep.Notification))

    @Test
    fun location() = capture("onboarding_location", OnboardingState(step = OnboardingStep.Location))

    @Test
    fun settingUp() = capture("onboarding_setup", OnboardingState(step = OnboardingStep.Setup, isSettingUp = true))

    @Test
    fun setupFailed() = capture(
        "onboarding_setup_failed",
        OnboardingState(step = OnboardingStep.Setup, setupError = "The Internet connection appears to be offline."),
    )
}
