package com.muttaqi.shared.feature.onboarding

import app.cash.turbine.test
import com.muttaqi.shared.core.domain.DomainError
import com.muttaqi.shared.core.domain.Outcome
import com.muttaqi.shared.core.model.Language
import com.muttaqi.shared.core.preferences.LanguageSelector
import com.muttaqi.shared.feature.onboarding.data.repository.SettingsOnboardingRepository
import com.muttaqi.shared.feature.onboarding.domain.model.OnboardingStep
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
import com.muttaqi.shared.feature.quran.FakeDownloadRecord
import com.muttaqi.shared.feature.quran.domain.repository.QuranLibrary
import com.muttaqi.shared.feature.quran.domain.usecase.SyncQuran
import com.muttaqi.shared.feature.quran.presentation.QuranMessages
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.CompletableDeferred
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
    private val quran = FakeQuranDownload()
    private var readingLanguage: Language? = null

    private fun viewModel() = OnboardingViewModel(
        SaveUserName(repository),
        RequestNotificationPermission(notifications),
        RequestLocationAccess(DeviceLocationRepository(location, savedCoordinates())),
        FinishOnboarding(SyncQuran(quran, FakeDownloadRecord(), LanguageSelector { readingLanguage = it }), repository),
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
        assertEquals(1, quran.runs)
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
        quran.hold = true
        val viewModel = viewModel()
        viewModel.effects.test {
            viewModel.walkTo(OnboardingStep.Setup)
            assertTrue(viewModel.state.value.isSettingUp)
            assertFalse(IsOnboardingComplete(repository)())
            quran.finish(error = null)
            assertEquals(OnboardingEffect.Finished, awaitItem())
        }
        assertFalse(viewModel.state.value.isSettingUp)
        assertTrue(IsOnboardingComplete(repository)())
        assertEquals(true, settings.getBooleanOrNull("onboarding_complete"))
        assertEquals(Language.English, readingLanguage)
    }

    @Test
    fun aFailedSetupShowsWhyAndRetries() {
        quran.hold = true
        val viewModel = viewModel()
        viewModel.walkTo(OnboardingStep.Setup)
        quran.finish(DomainError.NoConnection)
        assertEquals("Failed to download English translation. Please check your connection.", viewModel.state.value.setupError)
        assertEquals(QuranMessages.downloadFailed(Language.English), viewModel.state.value.setupError)
        assertFalse(IsOnboardingComplete(repository)())

        viewModel.dispatch(OnboardingIntent.RetrySetup)
        assertTrue(viewModel.state.value.isSettingUp)
        assertNull(viewModel.state.value.setupError)
        assertEquals(2, quran.runs)
    }

    @Test
    fun aSetupAlreadyRunningIsntStartedAgain() {
        quran.hold = true
        val viewModel = viewModel()
        viewModel.walkTo(OnboardingStep.Setup)
        viewModel.dispatch(OnboardingIntent.RetrySetup)
        assertEquals(1, quran.runs)
    }
}

class FakeNotificationPermission(var answer: Boolean = true) : NotificationPermission {
    var requests = 0

    override fun request(onResult: (granted: Boolean) -> Unit) {
        requests++
        onResult(answer)
    }
}

class FakeQuranDownload : QuranLibrary {
    var runs = 0
    var hold = false
    private var stored = false
    private var pending: CompletableDeferred<Outcome<Unit>>? = null

    override suspend fun hasText() = stored

    override suspend fun hasTranslation(language: Language) = stored

    override suspend fun downloadText(): Outcome<Unit> {
        runs++
        val outcome = if (hold) CompletableDeferred<Outcome<Unit>>().also { pending = it }.await() else Outcome.Success(Unit)
        stored = outcome is Outcome.Success
        return outcome
    }

    override suspend fun downloadTranslation(language: Language): Outcome<Unit> = Outcome.Success(Unit)

    fun finish(error: DomainError?) {
        pending?.complete(if (error == null) Outcome.Success(Unit) else Outcome.Failure(error))
        pending = null
    }
}
