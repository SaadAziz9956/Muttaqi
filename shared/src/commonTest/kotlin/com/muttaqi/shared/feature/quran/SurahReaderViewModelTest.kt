package com.muttaqi.shared.feature.quran

import androidx.lifecycle.ViewModelStore
import app.cash.turbine.test
import com.muttaqi.shared.core.model.Language
import com.muttaqi.shared.core.share.SharePassage
import com.muttaqi.shared.feature.quran.data.repository.RoomReadingProgressRepository
import com.muttaqi.shared.feature.quran.data.repository.SettingsReadingPreferences
import com.muttaqi.shared.feature.quran.domain.model.ReadingMode
import com.muttaqi.shared.feature.quran.domain.usecase.ObserveReadingSettings
import com.muttaqi.shared.feature.quran.domain.usecase.ReadSurah
import com.muttaqi.shared.feature.quran.domain.usecase.RecordReading
import com.muttaqi.shared.feature.quran.presentation.reader.SurahDirection
import com.muttaqi.shared.feature.quran.presentation.reader.SurahReaderContent
import com.muttaqi.shared.feature.quran.presentation.reader.SurahReaderEffect
import com.muttaqi.shared.feature.quran.presentation.reader.SurahReaderIntent
import com.muttaqi.shared.feature.quran.presentation.reader.SurahReaderViewModel
import com.muttaqi.shared.testing.FakeSelectedLanguage
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class SurahReaderViewModelTest {
    private val language = FakeSelectedLanguage()
    private val settings = MapSettings()
    private val preferences = SettingsReadingPreferences(settings, language)
    private val library = FakeQuranLibrary()
    private val progress = RoomReadingProgressRepository(FakeReadingProgressDao())

    private fun viewModel(surahNumber: Int = 2, startAyah: Int? = null) = SurahReaderViewModel(
        surahNumber,
        startAyah,
        ReadSurah(library, library),
        RecordReading(progress, FixedClock()),
        ObserveReadingSettings(preferences),
    )

    /** The ids on screen for ayahs of a surah, by number within it */
    private fun ids(surahNumber: Int, vararg numbersInSurah: Int) =
        numbersInSurah.map { n -> QuranTestData.ayahs.first { it.surah == surahNumber && it.numberInSurah == n }.number }

    private fun TestScope.settle() {
        advanceTimeBy(1_001)
        runCurrent()
    }

    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun loadsTheSurahWithItsNeighboursInTheReadersSettings() = runTest {
        settings.putInt("reading_font_size", 120)
        val state = viewModel().state.value
        val reading = assertIs<SurahReaderContent.Loaded>(state.content).reading
        assertEquals(8, reading.displayAyahs.size)
        assertEquals("Al-Baqara", state.headerSurah?.englishName)
        assertEquals(1, state.previousSurah?.number)
        assertEquals(120, state.settings.fontSize.percentage)
    }

    @Test
    fun theFirstAyahOnScreenIsWhereTheReaderIs() = runTest {
        val viewModel = viewModel()
        assertNull(viewModel.readingPosition.value)
        viewModel.dispatch(SurahReaderIntent.VisibleAyahsChanged(ids(2, 4, 3)))
        assertEquals("Ayah 3 of 286", viewModel.readingPosition.value)
    }

    @Test
    fun progressIsSavedOnceScrollingSettles() = runTest {
        val viewModel = viewModel()
        viewModel.dispatch(SurahReaderIntent.VisibleAyahsChanged(ids(2, 1, 2)))
        viewModel.dispatch(SurahReaderIntent.VisibleAyahsChanged(ids(2, 3, 4)))
        advanceTimeBy(900)
        assertNull(progress.progress(2))
        settle()
        val saved = progress.progress(2)!!
        assertEquals(3, saved.lastAyahNumber)
        assertEquals(setOf(1, 2, 3, 4), saved.readAyahs)
        assertEquals(286, saved.totalAyahs)
    }

    @Test
    fun alFatihasBismillahCountsAsReadAtTheTop() = runTest {
        val viewModel = viewModel(surahNumber = 1)
        viewModel.dispatch(SurahReaderIntent.VisibleAyahsChanged(ids(1, 2, 3)))
        settle()
        assertEquals(setOf(1, 2, 3), progress.progress(1)!!.readAyahs)
    }

    @Test
    fun inArabicOnlyEveryAyahOnAVisiblePageIsRead() = runTest {
        preferences.setMode(ReadingMode.ArabicOnly)
        val viewModel = viewModel()
        // Mushaf pages are identified by their first ayah
        viewModel.dispatch(SurahReaderIntent.VisibleAyahsChanged(ids(2, 1)))
        assertEquals("Page 2", viewModel.readingPosition.value)
        settle()
        assertEquals((1..5).toSet(), progress.progress(2)!!.readAyahs)
    }

    @Test
    fun continuingOpensAtTheAyahAndIgnoresWhatScrollsPastOnTheWay() = runTest {
        val viewModel = viewModel(startAyah = 6)
        assertEquals(ids(2, 6).single(), viewModel.state.value.startScrollTarget)
        viewModel.dispatch(SurahReaderIntent.VisibleAyahsChanged(ids(2, 1, 2)))
        viewModel.dispatch(SurahReaderIntent.VisibleAyahsChanged(ids(2, 6, 7)))
        settle()
        assertNull(progress.progress(2))
        viewModel.dispatch(SurahReaderIntent.ReachedStart)
        assertNull(viewModel.state.value.startScrollTarget)
        settle()
        assertEquals(setOf(6, 7), progress.progress(2)!!.readAyahs)
    }

    @Test
    fun inArabicOnlyContinuingOpensAtTheAyahsPage() = runTest {
        preferences.setMode(ReadingMode.ArabicOnly)
        // 2:7 is on the page that starts at 2:6
        assertEquals(ids(2, 6).single(), viewModel(startAyah = 7).state.value.startScrollTarget)
    }

    @Test
    fun movingToTheNextSurahSavesAtOnceAndSlidesForward() = runTest {
        val viewModel = viewModel(surahNumber = 1)
        viewModel.dispatch(SurahReaderIntent.VisibleAyahsChanged(ids(1, 5)))
        viewModel.dispatch(SurahReaderIntent.NextTapped)
        assertEquals(5, progress.progress(1)!!.lastAyahNumber)
        val state = viewModel.state.value
        assertEquals(2, state.surahNumber)
        assertEquals(SurahDirection.Forward, state.direction)
        assertEquals(2, state.reading?.surah?.number)
        assertNull(viewModel.readingPosition.value)
        viewModel.dispatch(SurahReaderIntent.PreviousTapped)
        assertEquals(SurahDirection.Backward, viewModel.state.value.direction)
        assertEquals(1, viewModel.state.value.headerSurah?.number)
    }

    @Test
    fun theFirstAndLastSurahsGoNoFurther() = runTest {
        val first = viewModel(surahNumber = 1)
        first.dispatch(SurahReaderIntent.PreviousTapped)
        assertEquals(1, first.state.value.surahNumber)
        val last = viewModel(surahNumber = 114)
        last.dispatch(SurahReaderIntent.NextTapped)
        assertEquals(114, last.state.value.surahNumber)
    }

    @Test
    fun leavingOrClosingTheScreenSavesAtOnce() = runTest {
        val viewModel = viewModel()
        viewModel.dispatch(SurahReaderIntent.VisibleAyahsChanged(ids(2, 2)))
        viewModel.dispatch(SurahReaderIntent.Left)
        assertEquals(2, progress.progress(2)!!.lastAyahNumber)

        val closing = viewModel()
        closing.dispatch(SurahReaderIntent.VisibleAyahsChanged(ids(2, 8)))
        ViewModelStore().apply { put("reader", closing) }.clear()
        assertEquals(8, progress.progress(2)!!.lastAyahNumber)
    }

    @Test
    fun anAyahCanBeCopiedSharedAndExplained() = runTest {
        val viewModel = viewModel()
        val ayah = QuranTestData.ayahs.first { it.surah == 2 && it.numberInSurah == 2 }
        viewModel.effects.test {
            viewModel.dispatch(SurahReaderIntent.CopyTapped(ayah.number))
            assertEquals(SurahReaderEffect.Copy("${ayah.arabic}\n\n${ayah.english}\n\nQuran (2:2)"), awaitItem())
            viewModel.dispatch(SurahReaderIntent.ShareTapped(ayah.number))
            assertEquals(SurahReaderEffect.OpenShare(SharePassage(ayah.arabic, null, ayah.english, "Quran (2:2)")), awaitItem())
            viewModel.dispatch(SurahReaderIntent.AyahExplanationTapped(2))
            assertEquals(SurahReaderEffect.OpenTafsir(2, 2), awaitItem())
            viewModel.dispatch(SurahReaderIntent.ExplanationTapped)
            assertEquals(SurahReaderEffect.OpenTafsir(2, null), awaitItem())
        }
    }

    @Test
    fun switchingTranslationReloadsTheSurahInIt() = runTest {
        val viewModel = viewModel()
        language.switchTo(Language.Urdu)
        // The settings combine the mode, font size and language, which takes a turn of the scheduler
        runCurrent()
        val first = viewModel.state.value.reading!!.displayAyahs.first()
        assertEquals(QuranTestData.ayahs.first { it.surah == 2 }.urdu, first.translation)
        assertEquals(Language.Urdu, viewModel.state.value.settings.language)
    }

    @Test
    fun aSurahThatIsntStoredSaysSo() = runTest {
        val state = viewModel(surahNumber = 3).state.value
        val failed = assertIs<SurahReaderContent.Failed>(state.content)
        assertEquals("Invalid Surah number: 3. Please select a valid Surah (1-114).", failed.message)
    }
}
