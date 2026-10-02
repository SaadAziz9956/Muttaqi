package com.muttaqi.shared.feature.home

import app.cash.turbine.test
import com.muttaqi.shared.core.model.Language
import com.muttaqi.shared.core.share.SharePassage
import com.muttaqi.shared.feature.dhikr.domain.model.DhikrProgress
import com.muttaqi.shared.feature.dhikr.domain.repository.CurrentDay
import com.muttaqi.shared.feature.dhikr.domain.usecase.GetDhikrSaidToday
import com.muttaqi.shared.feature.dhikr.domain.usecase.GetTodaysDhikrProgress
import com.muttaqi.shared.feature.dua.domain.usecase.GetDuaOfTheDay
import com.muttaqi.shared.feature.home.HomeTestData.fridayMorning
import com.muttaqi.shared.feature.home.HomeTestData.karachi
import com.muttaqi.shared.feature.home.HomeTestData.surah
import com.muttaqi.shared.feature.home.HomeTestData.wednesdayEvening
import com.muttaqi.shared.feature.home.domain.model.LastReading
import com.muttaqi.shared.feature.home.domain.platform.dateAt
import com.muttaqi.shared.feature.home.domain.usecase.GetDailyContent
import com.muttaqi.shared.feature.home.domain.usecase.GetHijriDate
import com.muttaqi.shared.feature.home.domain.usecase.GetQuranShortcuts
import com.muttaqi.shared.feature.home.domain.usecase.LocatePrayerTimes
import com.muttaqi.shared.feature.home.presentation.DailyCard
import com.muttaqi.shared.feature.home.presentation.HomeEffect
import com.muttaqi.shared.feature.home.presentation.HomeIntent
import com.muttaqi.shared.feature.home.presentation.HomeLocation
import com.muttaqi.shared.feature.home.presentation.HomeViewModel
import com.muttaqi.shared.feature.journal.FakeJournalRepository
import com.muttaqi.shared.feature.journal.JournalTestData
import com.muttaqi.shared.feature.journal.domain.usecase.ObserveJournalEntries
import com.muttaqi.shared.feature.journal.domain.usecase.ObserveTodaysJournalEntry
import com.muttaqi.shared.feature.names.domain.usecase.GetNameOfTheDay
import com.muttaqi.shared.feature.prayer.FakeHeadingProvider
import com.muttaqi.shared.feature.prayer.FakeLocationProvider
import com.muttaqi.shared.feature.prayer.PrayerTestData.karachi as karachiCoordinates
import com.muttaqi.shared.feature.prayer.PrayerTestData.lahore
import com.muttaqi.shared.feature.prayer.data.compass.PlatformCompass
import com.muttaqi.shared.feature.prayer.data.location.DeviceLocationRepository
import com.muttaqi.shared.feature.prayer.data.qibla.AdhanQiblaRepository
import com.muttaqi.shared.feature.prayer.data.times.AdhanPrayerTimesRepository
import com.muttaqi.shared.feature.prayer.domain.model.LocationAccess
import com.muttaqi.shared.feature.prayer.domain.model.Prayer
import com.muttaqi.shared.feature.prayer.domain.usecase.FollowHeading
import com.muttaqi.shared.feature.prayer.domain.usecase.GetLocationAccess
import com.muttaqi.shared.feature.prayer.domain.usecase.GetPrayerSchedule
import com.muttaqi.shared.feature.prayer.domain.usecase.GetQiblaDirection
import com.muttaqi.shared.feature.prayer.domain.usecase.LocateReader
import com.muttaqi.shared.feature.prayer.domain.usecase.RequestLocationAccess
import com.muttaqi.shared.feature.prayer.savedCoordinates
import com.muttaqi.shared.feature.quran.domain.usecase.GetAyah
import com.muttaqi.shared.feature.quran.domain.usecase.GetAyahOfTheDay
import com.muttaqi.shared.feature.quran.domain.usecase.GetLastReading
import com.muttaqi.shared.feature.quran.domain.usecase.GetSurahs
import com.muttaqi.shared.feature.topics.domain.usecase.GetHadithOfTheDay
import com.muttaqi.shared.feature.topics.domain.usecase.GetTopicOfTheDay
import com.muttaqi.shared.testing.FakeSelectedLanguage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toInstant
import kotlin.math.roundToInt
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()
    private val clock = FakeReaderClock(wednesdayEvening)
    private val quran = FakeQuran()
    private val dhikrProgress = FakeDhikrProgress()
    private val journal = FakeJournalRepository()
    private val language = FakeSelectedLanguage()
    private val saved = savedCoordinates()
    private val qibla = GetQiblaDirection(AdhanQiblaRepository())

    private fun viewModel(
        location: FakeLocationProvider = FakeLocationProvider(LocationAccess.Granted),
        heading: FakeHeadingProvider = FakeHeadingProvider(),
    ): HomeViewModel {
        val getAyah = GetAyah(quran, quran)
        val where = DeviceLocationRepository(location, saved)
        val today = CurrentDay { clock.dateAt(clock.now()) }
        return HomeViewModel(
            GetDailyContent(
                getAyah,
                GetAyahOfTheDay(getAyah),
                GetHadithOfTheDay(fakeExplore),
                GetDuaOfTheDay(fakeDuas),
                GetNameOfTheDay(fakeNames),
                GetTopicOfTheDay(fakeExplore),
            ),
            GetQuranShortcuts(GetSurahs(quran), GetLastReading(quran, quran)),
            GetDhikrSaidToday(fakeDhikr, GetTodaysDhikrProgress(dhikrProgress, today)),
            ObserveTodaysJournalEntry(ObserveJournalEntries(journal), clock.asClock) { karachi },
            LocatePrayerTimes(LocateReader(where), GetPrayerSchedule(AdhanPrayerTimesRepository()), qibla, clock),
            GetLocationAccess(where),
            RequestLocationAccess(where),
            FollowHeading(PlatformCompass(heading)),
            GetHijriDate(fakeHijri),
            clock,
            language,
        )
    }

    private fun at(year: Int, month: Int, day: Int, hour: Int, minute: Int): Instant =
        LocalDateTime(year, month, day, hour, minute).toInstant(karachi)

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun theDaysPicksAreTheOnesTheIosAppShowed() {
        val state = viewModel().state.value
        assertEquals(63, state.nameOfTheDay?.number)
        assertEquals("food", state.topicOfTheDay?.id)
        assertEquals("en hadith worship-1", state.hadithOfTheDay?.translation)
        assertEquals("16:128", state.ayahOfTheDay?.reference)
        assertEquals("30:30", state.duaOfTheDay?.reference)

        val viewModel = viewModel()
        clock.now = at(2026, 9, 30, 23, 59)
        viewModel.dispatch(HomeIntent.Refresh)
        assertEquals(63, viewModel.state.value.nameOfTheDay?.number)
        clock.now = at(2026, 10, 1, 0, 1)
        viewModel.dispatch(HomeIntent.Refresh)
        with(viewModel.state.value) {
            assertEquals(64, nameOfTheDay?.number)
            assertEquals("tawhid", topicOfTheDay?.id)
            assertEquals("en hadith faith-1", hadithOfTheDay?.translation)
            assertEquals("21:35", ayahOfTheDay?.reference)
            assertEquals("31:31", duaOfTheDay?.reference)
        }
    }

    @Test
    fun everyTileShowsWhatItsFeatureHas() {
        saved.save(karachiCoordinates)
        dhikrProgress.save("subhanallah", DhikrProgress(count = 10, rounds = 1, day = LocalDate(2026, 9, 30)))
        dhikrProgress.save("istighfar", DhikrProgress(count = 7, rounds = 0, day = LocalDate(2026, 9, 30)))
        journal.stored.value = listOf(JournalTestData.entry("today", "Grateful for family", "", "2026-09-30T05:00:00Z"))
        quran.readUpTo(2, 9)

        val state = viewModel().state.value
        assertEquals("Hijri of 2026-09-30", state.hijriDate)
        assertEquals(HomeLocation.Available, state.location)
        assertEquals(Prayer.Maghrib, state.nextPrayer?.prayer)
        assertEquals(Prayer.Maghrib, state.nextPrayerToday)
        assertFalse(state.asksForLocation)
        assertEquals(qibla(karachiCoordinates), state.qibla)
        assertEquals("en 3:139", state.greeting?.ayah?.translation)
        assertEquals("3:139", state.greeting?.reference)
        assertEquals(50, state.dhikrToday)
        assertEquals("today", state.journalToday?.id)
        assertEquals(LastReading(surah(2), 9), state.lastReading)
        assertEquals(surah(18), state.kahf)
        assertNull(state.fridayKahf)
    }

    @Test
    fun beforeAnythingIsSavedTheTilesInviteTheReaderIn() {
        quran.stored = false
        val state = viewModel(FakeLocationProvider(LocationAccess.NotDetermined)).state.value
        assertNull(state.greeting)
        assertNull(state.ayahOfTheDay)
        assertNull(state.lastReading)
        assertNull(state.kahf)
        assertEquals(0, state.dhikrToday)
        assertNull(state.journalToday)
        assertNotNull(state.nameOfTheDay)
        assertNotNull(state.duaOfTheDay)
        assertNotNull(state.hadithOfTheDay)
        assertNotNull(state.topicOfTheDay)
    }

    @Test
    fun onFridaysAlKahfIsATapAwayUnlessItsBeingContinued() {
        clock.now = fridayMorning
        val viewModel = viewModel()
        assertEquals(surah(18), viewModel.state.value.fridayKahf)

        quran.readUpTo(18, 40)
        viewModel.dispatch(HomeIntent.Refresh)
        assertEquals(LastReading(surah(18), 40), viewModel.state.value.lastReading)
        assertNull(viewModel.state.value.fridayKahf)

        quran.last = null
        clock.now = Instant.parse("2026-10-01T23:30:00Z")
        viewModel.dispatch(HomeIntent.Refresh)
        assertEquals(surah(18), viewModel.state.value.fridayKahf)
    }

    @Test
    fun theNextPrayerAndTheHijriDateMoveOnWithTheClock() {
        saved.save(karachiCoordinates)
        val viewModel = viewModel()
        assertEquals(Prayer.Maghrib, viewModel.state.value.nextPrayerToday)
        assertEquals("Hijri of 2026-09-30", viewModel.state.value.hijriDate)

        clock.now = at(2026, 9, 30, 18, 25)
        assertEquals(Prayer.Isha, viewModel.state.value.nextPrayerToday)
        assertEquals("Hijri of 2026-10-01", viewModel.state.value.hijriDate)

        clock.now = at(2026, 9, 30, 21, 0)
        assertEquals(Prayer.Fajr, viewModel.state.value.nextPrayer?.prayer)
        assertNull(viewModel.state.value.nextPrayerToday)

        clock.now = at(2026, 10, 1, 0, 30)
        assertEquals("Hijri of 2026-10-01", viewModel.state.value.hijriDate)
        assertEquals(LocalDate(2026, 10, 1), viewModel.state.value.today)
    }

    @Test
    fun withoutALocationThePillAsksForOneAndThenShowsTheTimes() {
        val location = FakeLocationProvider(LocationAccess.NotDetermined)
        val viewModel = viewModel(location)
        with(viewModel.state.value) {
            assertEquals(HomeLocation.NeedsPermission, this.location)
            assertTrue(asksForLocation)
            assertNull(schedule)
            assertNull(qibla)
        }

        viewModel.dispatch(HomeIntent.SetLocationTapped)
        assertEquals(1, location.accessRequests)
        with(viewModel.state.value) {
            assertEquals(HomeLocation.Available, this.location)
            assertFalse(asksForLocation)
            assertEquals(Prayer.Maghrib, nextPrayer?.prayer)
            assertEquals(qibla(karachiCoordinates), qibla)
        }
    }

    @Test
    fun onceRefusedThePillOpensTheSettings() = runTest {
        val location = FakeLocationProvider(LocationAccess.Denied)
        val viewModel = viewModel(location)
        assertEquals(HomeLocation.Denied, viewModel.state.value.location)
        assertTrue(viewModel.state.value.asksForLocation)
        viewModel.effects.test {
            viewModel.dispatch(HomeIntent.SetLocationTapped)
            assertEquals(HomeEffect.OpenSettings, awaitItem())
        }
        assertEquals(0, location.accessRequests)
    }

    @Test
    fun refusedButWithASavedPlaceTheTimesStillShow() {
        saved.save(karachiCoordinates)
        val state = viewModel(FakeLocationProvider(LocationAccess.Denied)).state.value
        assertEquals(HomeLocation.Available, state.location)
        assertFalse(state.asksForLocation)
        assertNotNull(state.schedule)
    }

    @Test
    fun allowedButWithNoFixYetThePillWaitsQuietly() {
        val location = FakeLocationProvider(LocationAccess.Granted).apply { fix = null }
        val state = viewModel(location).state.value
        assertEquals(HomeLocation.Unknown, state.location)
        assertFalse(state.asksForLocation)
        assertNull(state.nextPrayer)
    }

    @Test
    fun theSavedPlaceShowsTheTimesAtOnceAndAFreshFixCorrectsThem() {
        saved.save(karachiCoordinates)
        val location = FakeLocationProvider(LocationAccess.Granted, fix = lahore).apply { holdFix = true }
        val viewModel = viewModel(location)
        assertEquals(qibla(karachiCoordinates), viewModel.state.value.qibla)

        val fixed = viewModel(FakeLocationProvider(LocationAccess.Granted, fix = lahore))
        assertEquals(qibla(lahore), fixed.state.value.qibla)
        assertEquals(lahore, saved.load())
        val lahoreMaghrib = GetPrayerSchedule(AdhanPrayerTimesRepository())(lahore, wednesdayEvening, karachi)?.today?.maghrib
        assertEquals(lahoreMaghrib, fixed.state.value.schedule?.today?.maghrib)
    }

    @Test
    fun theTextsFollowTheTranslationLanguage() {
        val viewModel = viewModel()
        language.switchTo(Language.Urdu)
        with(viewModel.state.value) {
            assertEquals("ur 3:139", greeting?.ayah?.translation)
            assertEquals("ur 16:128", ayahOfTheDay?.ayah?.translation)
            assertEquals("ur dua 30", duaOfTheDay?.translation)
            assertEquals("ur hadith worship-1", hadithOfTheDay?.translation)
            assertEquals("ur meaning 63", nameOfTheDay?.meaning)
            assertEquals("ur food", topicOfTheDay?.title)
        }
    }

    @Test
    fun refreshingCountsAgainAndTheJournalFollowsEachSave() = runTest {
        val viewModel = viewModel()
        assertEquals(0, viewModel.state.value.dhikrToday)
        dhikrProgress.save("subhanallah", DhikrProgress(count = 3, rounds = 0, day = LocalDate(2026, 9, 30)))
        viewModel.dispatch(HomeIntent.Refresh)
        assertEquals(3, viewModel.state.value.dhikrToday)

        journal.save(JournalTestData.entry("yesterday", "Rain", "", "2026-09-29T10:00:00Z"))
        assertNull(viewModel.state.value.journalToday)
        journal.save(JournalTestData.entry("today", "Family", "", "2026-09-30T10:00:00Z"))
        assertEquals("today", viewModel.state.value.journalToday?.id)

        clock.now = at(2026, 10, 1, 7, 0)
        viewModel.dispatch(HomeIntent.Refresh)
        assertNull(viewModel.state.value.journalToday)
    }

    @Test
    fun theJournalTileOpensTheListAndItsButtonTodaysEntry() = runTest {
        val viewModel = viewModel()
        viewModel.effects.test {
            viewModel.dispatch(HomeIntent.JournalTapped)
            assertEquals(HomeEffect.OpenJournal, awaitItem())
            viewModel.dispatch(HomeIntent.TodaysEntryTapped)
            assertEquals(HomeEffect.OpenJournalEntry(null), awaitItem())

            journal.save(JournalTestData.entry("today", "Family", "", "2026-09-30T10:00:00Z"))
            viewModel.dispatch(HomeIntent.TodaysEntryTapped)
            assertEquals(HomeEffect.OpenJournalEntry("today"), awaitItem())
        }
    }

    @Test
    fun eachTileOpensItsFeature() = runTest {
        clock.now = fridayMorning
        quran.readUpTo(2, 9)
        val viewModel = viewModel()
        viewModel.effects.test {
            viewModel.dispatch(HomeIntent.QiblaTapped)
            assertEquals(HomeEffect.OpenQibla, awaitItem())
            viewModel.dispatch(HomeIntent.DhikrTapped)
            assertEquals(HomeEffect.OpenDhikr, awaitItem())
            viewModel.dispatch(HomeIntent.NameTapped)
            assertEquals(HomeEffect.OpenNames, awaitItem())
            viewModel.dispatch(HomeIntent.EmotionsTapped)
            assertEquals(HomeEffect.OpenEmotions, awaitItem())
            viewModel.dispatch(HomeIntent.TopicTapped)
            assertEquals(HomeEffect.OpenTopic("prayer"), awaitItem())
            viewModel.dispatch(HomeIntent.ContinueReadingTapped)
            assertEquals(HomeEffect.OpenSurah(surah(2), 9), awaitItem())
            viewModel.dispatch(HomeIntent.KahfTapped)
            assertEquals(HomeEffect.OpenSurah(surah(18), 1), awaitItem())
            viewModel.dispatch(HomeIntent.AyahOfTheDayTapped)
            assertEquals(HomeEffect.OpenSurah(surah(25), 63), awaitItem())
        }
    }

    @Test
    fun withoutTheQuranTheShortcutsOpenNothing() = runTest {
        quran.stored = false
        val viewModel = viewModel()
        viewModel.effects.test {
            viewModel.dispatch(HomeIntent.ContinueReadingTapped)
            viewModel.dispatch(HomeIntent.KahfTapped)
            viewModel.dispatch(HomeIntent.AyahOfTheDayTapped)
            expectNoEvents()
        }
    }

    @Test
    fun theDailyCardsShareAndCopyAsTheIosAppDid() = runTest {
        val viewModel = viewModel()
        viewModel.effects.test {
            viewModel.dispatch(HomeIntent.ShareTapped(DailyCard.Ayah))
            assertEquals(HomeEffect.OpenShare(SharePassage("آية 16:128", null, "en 16:128", "Quran (16:128)")), awaitItem())
            viewModel.dispatch(HomeIntent.CopyTapped(DailyCard.Ayah))
            assertEquals(HomeEffect.Copy("آية 16:128 ۝\n\nen 16:128\n\nQuran (16:128)"), awaitItem())

            viewModel.dispatch(HomeIntent.ShareTapped(DailyCard.Dua))
            assertEquals(HomeEffect.OpenShare(SharePassage("دعاء 30", "Dua 30", "en dua 30", "Quran (30:30)")), awaitItem())
            viewModel.dispatch(HomeIntent.CopyTapped(DailyCard.Dua))
            assertEquals(HomeEffect.Copy("دعاء 30\n\nDua 30\n\nen dua 30\n\nQuran (30:30)"), awaitItem())

            viewModel.dispatch(HomeIntent.ShareTapped(DailyCard.Hadith))
            assertEquals(
                HomeEffect.OpenShare(SharePassage("حديث worship-1", null, "en hadith worship-1", "Narrated by Muslim · Authentic")),
                awaitItem(),
            )
            viewModel.dispatch(HomeIntent.CopyTapped(DailyCard.Hadith))
            assertEquals(HomeEffect.Copy("حديث worship-1\n\nen hadith worship-1\n\nNarrated by Muslim · Authentic"), awaitItem())
        }
    }

    @Test
    fun theQiblaArrowTurnsTheShortWayAsThePhoneTurns() = runTest {
        saved.save(karachiCoordinates)
        val heading = FakeHeadingProvider()
        val viewModel = viewModel(FakeLocationProvider(LocationAccess.Denied), heading)
        val bearing = qibla(karachiCoordinates).bearing
        viewModel.qiblaArrow.test {
            assertNull(awaitItem())
            heading.turnTo(10.0)
            assertEquals(bearing - 10.0, awaitItem()!!, absoluteTolerance = 1e-9)
            heading.turnTo(350.0)
            assertEquals(bearing - 10.0 + 20.0, awaitItem()!!, absoluteTolerance = 1e-9)
            heading.turnTo(90.0)
            assertEquals(178, awaitItem()!!.roundToInt())
        }
    }

    @Test
    fun theCompassRunsOnlyWhileTheTileFollowsIt() = runTest {
        saved.save(karachiCoordinates)
        val heading = FakeHeadingProvider()
        val viewModel = viewModel(FakeLocationProvider(LocationAccess.Denied), heading)
        assertFalse(heading.isRunning)
        val following = launch(dispatcher) { viewModel.qiblaArrow.collect {} }
        assertTrue(heading.isRunning)
        following.cancel()
        assertFalse(heading.isRunning)
    }

    @Test
    fun theCompassWaitsForTheQiblaAndIsntNeededWithoutOne() = runTest {
        val heading = FakeHeadingProvider()
        val viewModel = viewModel(FakeLocationProvider(LocationAccess.NotDetermined), heading)
        val following = launch(dispatcher) { viewModel.qiblaArrow.collect {} }
        assertFalse(heading.isRunning)
        following.cancel()

        saved.save(karachiCoordinates)
        val noCompass = FakeHeadingProvider(isAvailable = false)
        val withoutOne = viewModel(FakeLocationProvider(LocationAccess.Denied), noCompass)
        assertFalse(withoutOne.state.value.isCompassAvailable)
        withoutOne.qiblaArrow.test {
            assertNull(awaitItem())
            assertFalse(noCompass.isRunning)
        }
    }
}
