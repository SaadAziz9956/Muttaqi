package com.muttaqi.shared.feature.onboarding

import app.cash.turbine.test
import com.muttaqi.shared.feature.onboarding.data.repository.SettingsOnboardingRepository
import com.muttaqi.shared.feature.onboarding.domain.model.OnboardingStep
import com.muttaqi.shared.feature.onboarding.domain.platform.FirstLaunchSetup
import com.muttaqi.shared.feature.onboarding.domain.platform.NotificationPermission
import com.muttaqi.shared.feature.onboarding.domain.usecase.FinishOnboarding
import com.muttaqi.shared.feature.onboarding.domain.usecase.IsOnboardingComplete
import com.muttaqi.shared.feature.onboarding.domain.usecase.RequestNotificationPermission
import com.muttaqi.shared.feature.onboarding.domain.usecase.SaveUserName
import com.muttaqi.shared.feature.onboarding.presentation.OnboardingEffect
import com.muttaqi.shared.feature.onboarding.presentation.OnboardingIntent
import com.muttaqi.shared.feature.onboarding.presentation.OnboardingMutation
import com.muttaqi.shared.feature.onboarding.presentation.OnboardingReducer
import com.muttaqi.shared.feature.onboarding.presentation.OnboardingState
import com.muttaqi.shared.feature.onboarding.presentation.OnboardingViewModel
import com.muttaqi.shared.feature.prayer.FakeLocationProvider
import com.muttaqi.shared.feature.prayer.data.location.DeviceLocationRepository
import com.muttaqi.shared.feature.prayer.domain.model.LocationAccess
import com.muttaqi.shared.feature.prayer.domain.usecase.RequestLocationAccess
import com.muttaqi.shared.feature.prayer.savedCoordinates
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()
    private val settings = MapSettings()
    private val repository = SettingsOnboardingRepository(settings)
    private val location = FakeLocationProvider()
    private val notifications = FakeNotificationPermission()
    private val setup = FakeFirstLaunchSetup()

    private fun viewModel() = OnboardingViewModel(
        SaveUserName(repository),
        RequestNotificationPermission(notifications),
        RequestLocationAccess(DeviceLocationRepository(location, savedCoordinates())),
        FinishOnboarding(setup, repository),
    )

    private val path = listOf(
        OnboardingIntent.Begin,
        OnboardingIntent.NameChanged("Saad"),
        OnboardingIntent.SaveName,
        OnboardingIntent.Next,
        OnboardingIntent.SkipNotification,
        OnboardingIntent.SkipLocation,
    )

    private fun OnboardingViewModel.walkTo(step: OnboardingStep) {
        for (intent in path) {
            if (state.value.step == step) return
            dispatch(intent)
        }
    }

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun theReducerMovesAndSetsUp() {
        val moved = OnboardingReducer.reduce(OnboardingState(), OnboardingMutation.MovedTo(OnboardingStep.Goals))
        assertEquals(OnboardingStep.Goals, moved.step)
        val failed = OnboardingReducer.reduce(moved.copy(isSettingUp = true), OnboardingMutation.SetupFailed("Offline"))
        assertEquals(OnboardingState(step = OnboardingStep.Goals, setupError = "Offline"), failed)
        val retried = OnboardingReducer.reduce(failed, OnboardingMutation.SetupStarted)
        assertTrue(retried.isSettingUp)
        assertNull(retried.setupError)
    }

    @Test
    fun theStepsComeInTheirOrder() {
        val viewModel = viewModel()
        val steps = mutableListOf(viewModel.state.value.step)
        for (intent in path) {
            viewModel.dispatch(intent)
            if (viewModel.state.value.step != steps.last()) steps += viewModel.state.value.step
        }
        assertEquals(OnboardingStep.entries.toList(), steps)
    }

    @Test
    fun aBlankNameStaysOnTheStep() {
        val viewModel = viewModel()
        viewModel.dispatch(OnboardingIntent.Begin)
        viewModel.dispatch(OnboardingIntent.NameChanged("   "))
        viewModel.dispatch(OnboardingIntent.SaveName)
        assertEquals(OnboardingStep.Name, viewModel.state.value.step)
        assertNull(repository.userName())
    }

    @Test
    fun theNameIsSavedAsTyped() {
        val viewModel = viewModel()
        viewModel.dispatch(OnboardingIntent.Begin)
        viewModel.dispatch(OnboardingIntent.NameChanged("Saad Aziz "))
        viewModel.dispatch(OnboardingIntent.SaveName)
        assertEquals(OnboardingStep.Goals, viewModel.state.value.step)
        assertEquals("Saad Aziz ", settings.getStringOrNull("user_name"))
    }

    @Test
    fun turningOnNotificationsAsksThenMovesOnWhateverTheAnswer() {
        notifications.answer = false
        val viewModel = viewModel()
        viewModel.walkTo(OnboardingStep.Notification)
        viewModel.dispatch(OnboardingIntent.RequestNotification)
        assertEquals(1, notifications.requests)
        assertEquals(OnboardingStep.Location, viewModel.state.value.step)
    }

    @Test
    fun findingTheCityAsksForLocationThenSetsUp() {
        val viewModel = viewModel()
        viewModel.walkTo(OnboardingStep.Location)
        viewModel.dispatch(OnboardingIntent.RequestLocation)
        assertEquals(1, location.accessRequests)
        assertEquals(LocationAccess.Granted, location.access)
        assertEquals(OnboardingStep.Setup, viewModel.state.value.step)
        assertEquals(1, setup.runs)
    }

    @Test
    fun skippingAsksForNothing() {
        val viewModel = viewModel()
        viewModel.walkTo(OnboardingStep.Setup)
        assertEquals(0, notifications.requests)
        assertEquals(0, location.accessRequests)
    }

    @Test
    fun aFinishedSetupCompletesOnboardingAndOpensHome() = runTest {
        setup.hold = true
        val viewModel = viewModel()
        viewModel.effects.test {
            viewModel.walkTo(OnboardingStep.Setup)
            assertTrue(viewModel.state.value.isSettingUp)
            assertFalse(IsOnboardingComplete(repository)())
            setup.finish(null)
            assertEquals(OnboardingEffect.Finished, awaitItem())
        }
        assertFalse(viewModel.state.value.isSettingUp)
        assertTrue(IsOnboardingComplete(repository)())
        assertEquals(true, settings.getBooleanOrNull("onboarding_complete"))
    }

    @Test
    fun aFailedSetupShowsWhyAndRetries() {
        setup.hold = true
        val viewModel = viewModel()
        viewModel.walkTo(OnboardingStep.Setup)
        setup.finish("The Internet connection appears to be offline.")
        assertEquals("The Internet connection appears to be offline.", viewModel.state.value.setupError)
        assertFalse(IsOnboardingComplete(repository)())

        viewModel.dispatch(OnboardingIntent.RetrySetup)
        assertTrue(viewModel.state.value.isSettingUp)
        assertNull(viewModel.state.value.setupError)
        assertEquals(2, setup.runs)
    }

    @Test
    fun aSetupAlreadyRunningIsntStartedAgain() {
        setup.hold = true
        val viewModel = viewModel()
        viewModel.walkTo(OnboardingStep.Setup)
        viewModel.dispatch(OnboardingIntent.RetrySetup)
        assertEquals(1, setup.runs)
    }
}

class FakeNotificationPermission(var answer: Boolean = true) : NotificationPermission {
    var requests = 0

    override fun request(onResult: (granted: Boolean) -> Unit) {
        requests++
        onResult(answer)
    }
}

/** The first-launch download; with [hold] it waits for [finish] */
class FakeFirstLaunchSetup : FirstLaunchSetup {
    var runs = 0
    var hold = false
    private var pending: Pair<() -> Unit, (String) -> Unit>? = null

    override fun run(onDone: () -> Unit, onFailed: (message: String) -> Unit) {
        runs++
        if (hold) pending = onDone to onFailed else onDone()
    }

    /** Finishes the download, failing with [error] when there is one */
    fun finish(error: String?) {
        val (onDone, onFailed) = pending ?: return
        pending = null
        if (error == null) onDone() else onFailed(error)
    }
}
