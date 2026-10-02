package com.muttaqi.shared.feature.prayer

import app.cash.turbine.test
import com.muttaqi.shared.feature.prayer.PrayerTestData.karachi
import com.muttaqi.shared.feature.prayer.PrayerTestData.lahore
import com.muttaqi.shared.feature.prayer.data.compass.PlatformCompass
import com.muttaqi.shared.feature.prayer.data.location.DeviceLocationRepository
import com.muttaqi.shared.feature.prayer.data.location.SavedCoordinates
import com.muttaqi.shared.feature.prayer.data.qibla.AdhanQiblaRepository
import com.muttaqi.shared.feature.prayer.domain.model.CompassHeading
import com.muttaqi.shared.feature.prayer.domain.model.LocationAccess
import com.muttaqi.shared.feature.prayer.domain.usecase.GetLocationAccess
import com.muttaqi.shared.feature.prayer.domain.usecase.GetQiblaDirection
import com.muttaqi.shared.feature.prayer.domain.usecase.LocateReader
import com.muttaqi.shared.feature.prayer.domain.usecase.RequestLocationAccess
import com.muttaqi.shared.feature.prayer.presentation.qibla.QiblaCompass
import com.muttaqi.shared.feature.prayer.presentation.qibla.QiblaEffect
import com.muttaqi.shared.feature.prayer.presentation.qibla.QiblaIntent
import com.muttaqi.shared.feature.prayer.presentation.qibla.QiblaPhase
import com.muttaqi.shared.feature.prayer.presentation.qibla.QiblaViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.math.roundToInt
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class QiblaViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()
    private val saved: SavedCoordinates = savedCoordinates()
    private val qibla = GetQiblaDirection(AdhanQiblaRepository())
    private val karachiBearing = qibla(karachi).bearing

    private fun viewModel(
        location: FakeLocationProvider = FakeLocationProvider(),
        heading: FakeHeadingProvider = FakeHeadingProvider(),
    ): QiblaViewModel {
        val repository = DeviceLocationRepository(location, saved)
        return QiblaViewModel(
            LocateReader(repository),
            GetLocationAccess(repository),
            RequestLocationAccess(repository),
            qibla,
            PlatformCompass(heading),
        )
    }

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun theSavedLocationShowsTheQiblaThenAFreshFixCorrectsIt() {
        saved.save(karachi)
        val location = FakeLocationProvider(LocationAccess.Granted, fix = lahore).apply { holdFix = true }
        val viewModel = viewModel(location)
        assertEquals(QiblaPhase.Ready(qibla(karachi)), viewModel.state.value.phase)
        assertEquals(1, location.fixRequests)
    }

    @Test
    fun aFreshFixReplacesTheSavedOne() {
        saved.save(karachi)
        val viewModel = viewModel(FakeLocationProvider(LocationAccess.Granted, fix = lahore))
        assertEquals(qibla(lahore), viewModel.state.value.qibla)
        assertEquals(lahore, saved.load())
    }

    @Test
    fun withoutALocationItAsksForOne() {
        val location = FakeLocationProvider(LocationAccess.NotDetermined)
        val viewModel = viewModel(location)
        assertEquals(QiblaPhase.NeedsLocation(LocationAccess.NotDetermined), viewModel.state.value.phase)

        viewModel.dispatch(QiblaIntent.LocationButtonTapped)
        assertEquals(1, location.accessRequests)
        assertEquals(QiblaPhase.Ready(qibla(karachi)), viewModel.state.value.phase)
    }

    @Test
    fun onceRefusedTheButtonOpensTheSettings() = runTest {
        val location = FakeLocationProvider(LocationAccess.Denied)
        val viewModel = viewModel(location)
        assertEquals(QiblaPhase.NeedsLocation(LocationAccess.Denied), viewModel.state.value.phase)
        viewModel.effects.test {
            viewModel.dispatch(QiblaIntent.LocationButtonTapped)
            assertEquals(QiblaEffect.OpenSettings, awaitItem())
        }
        assertEquals(0, location.accessRequests)
    }

    @Test
    fun refusingThePromptLeavesTheWayToTheSettings() {
        val viewModel = viewModel(FakeLocationProvider(answer = LocationAccess.Denied))
        viewModel.dispatch(QiblaIntent.LocationButtonTapped)
        assertEquals(QiblaPhase.NeedsLocation(LocationAccess.Denied), viewModel.state.value.phase)
    }

    @Test
    fun theCompassRunsOnlyWhileTheScreenFollowsIt() = runTest {
        saved.save(karachi)
        val heading = FakeHeadingProvider()
        val viewModel = viewModel(FakeLocationProvider(LocationAccess.Denied), heading)
        assertFalse(heading.isRunning)
        val following = launch(dispatcher) { viewModel.compass.collect {} }
        assertTrue(heading.isRunning)
        following.cancel()
        assertFalse(heading.isRunning)
    }

    @Test
    fun theDialTurnsTheShortWayPastNorthAndGivesTheTurnToMake() = runTest {
        saved.save(karachi)
        val heading = FakeHeadingProvider()
        val viewModel = viewModel(FakeLocationProvider(LocationAccess.Denied), heading)
        viewModel.compass.test {
            assertNull(awaitItem())
            heading.turnTo(350.0)
            assertEquals(QiblaCompass(CompassHeading(350.0, 5.0), 350.0, karachiBearing - 350.0), awaitItem())
            heading.turnTo(10.0)
            val past = awaitItem()!!
            assertEquals(370.0, past.dialRotation)
            assertEquals(-102, past.turnAngle.roundToInt())
            heading.turnTo(270.0)
            assertEquals(270.0, awaitItem()!!.dialRotation)
        }
    }

    @Test
    fun facingTheQiblaSignalsOnceEachTime() = runTest {
        saved.save(karachi)
        val heading = FakeHeadingProvider()
        val viewModel = viewModel(FakeLocationProvider(LocationAccess.Denied), heading)
        viewModel.effects.test {
            val following = launch(dispatcher) { viewModel.compass.collect {} }
            heading.turnTo(200.0)
            heading.turnTo(266.0)
            assertEquals(QiblaEffect.FacingQibla, awaitItem())
            assertTrue(viewModel.compass.value!!.isAligned)
            heading.turnTo(268.0)
            expectNoEvents()
            heading.turnTo(300.0)
            heading.turnTo(269.0)
            assertEquals(QiblaEffect.FacingQibla, awaitItem())
            following.cancel()
        }
    }

    @Test
    fun anUnsureCompassAsksToBeCalibrated() = runTest {
        saved.save(karachi)
        val heading = FakeHeadingProvider()
        val viewModel = viewModel(FakeLocationProvider(LocationAccess.Denied), heading)
        viewModel.compass.test {
            assertNull(awaitItem())
            heading.turnTo(100.0, accuracy = 10.0)
            assertFalse(awaitItem()!!.needsCalibration)
            heading.turnTo(101.0, accuracy = 30.0)
            assertTrue(awaitItem()!!.needsCalibration)
            heading.turnTo(102.0, accuracy = -1.0)
            assertTrue(awaitItem()!!.needsCalibration)
        }
    }

    @Test
    fun withoutACompassThereAreNoReadings() = runTest {
        saved.save(karachi)
        val heading = FakeHeadingProvider(isAvailable = false)
        val viewModel = viewModel(FakeLocationProvider(LocationAccess.Denied), heading)
        assertFalse(viewModel.state.value.isCompassAvailable)
        viewModel.compass.test {
            assertNull(awaitItem())
            assertFalse(heading.isRunning)
        }
    }
}
