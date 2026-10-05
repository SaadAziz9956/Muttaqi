package com.muttaqi.shared.feature.quran

import app.cash.turbine.test
import com.muttaqi.shared.core.model.Language
import com.muttaqi.shared.core.text.SearchTextFolder
import com.muttaqi.shared.feature.quran.data.repository.RoomReadingProgressRepository
import com.muttaqi.shared.feature.quran.domain.usecase.FilterSurahs
import com.muttaqi.shared.feature.quran.domain.usecase.GetLastReading
import com.muttaqi.shared.feature.quran.domain.usecase.GetSurahs
import com.muttaqi.shared.feature.quran.domain.usecase.RecordReading
import com.muttaqi.shared.feature.quran.domain.usecase.SyncQuran
import com.muttaqi.shared.feature.quran.presentation.list.QuranListEffect
import com.muttaqi.shared.feature.quran.presentation.list.QuranListIntent
import com.muttaqi.shared.feature.quran.presentation.list.QuranListViewModel
import com.muttaqi.shared.feature.quran.presentation.list.RevelationFilter
import com.muttaqi.shared.testing.FakeSelectedLanguage
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
class QuranListViewModelTest {
    private val language = FakeSelectedLanguage()
    private val progress = RoomReadingProgressRepository(FakeReadingProgressDao())

    private fun viewModel(library: FakeQuranLibrary = FakeQuranLibrary()) = QuranListViewModel(
        SyncQuran(library, FakeDownloadRecord(), language.selector()),
        GetSurahs(library),
        GetLastReading(progress, library),
        FilterSurahs(SearchTextFolder),
        language,
    )

    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun loadsTheSurahsAndTheHadithUnderTheTitle() = runTest {
        val state = viewModel().state.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertEquals(testSurahs, state.visibleSurahs)
        assertEquals("Sahih al-Bukhari 5027 · HadeethEnc.com", state.header?.source)
        assertNull(state.readingProgress)
    }

    @Test
    fun aQuranNotStoredYetIsDownloadedFirst() = runTest {
        val library = FakeQuranLibrary(textStored = false, translations = emptySet())
        val state = viewModel(library).state.value
        assertEquals(listOf("text", "en"), library.downloads)
        assertEquals(5, state.surahs.size)
    }

    @Test
    fun aFailedDownloadCanBeRetried() = runTest {
        val library = FakeQuranLibrary(textStored = false, translations = emptySet()).apply { failDownloads = true }
        val viewModel = viewModel(library)
        assertEquals("Failed to download English translation. Please check your connection.", viewModel.state.value.error)
        library.failDownloads = false
        viewModel.dispatch(QuranListIntent.Retry)
        assertNull(viewModel.state.value.error)
        assertEquals(5, viewModel.state.value.surahs.size)
    }

    @Test
    fun searchAndTheRevelationFilterNarrowTheList() = runTest {
        val viewModel = viewModel()
        viewModel.dispatch(QuranListIntent.FilterSelected(RevelationFilter.Meccan))
        assertEquals(listOf(1, 18, 114), viewModel.state.value.visibleSurahs.map { it.number })
        viewModel.dispatch(QuranListIntent.QueryChanged("kahf"))
        assertTrue(viewModel.state.value.isSearching)
        assertEquals(listOf(18), viewModel.state.value.visibleSurahs.map { it.number })
        viewModel.dispatch(QuranListIntent.FilterSelected(RevelationFilter.Medinan))
        assertEquals(emptyList(), viewModel.state.value.visibleSurahs)
        viewModel.dispatch(QuranListIntent.ClearQuery)
        assertEquals(listOf(2, 9), viewModel.state.value.visibleSurahs.map { it.number })
    }

    @Test
    fun continuingOpensWhereTheReaderLeftOff() = runTest {
        RecordReading(progress, FixedClock())(2, 3, setOf(1, 2, 3), 286)
        val viewModel = viewModel()
        assertEquals("Al-Baqara", viewModel.state.value.readingProgress?.surahEnglishName)
        viewModel.effects.test {
            viewModel.dispatch(QuranListIntent.ContinueTapped)
            assertEquals(QuranListEffect.ContinueReading(2, 3), awaitItem())
            viewModel.dispatch(QuranListIntent.SurahTapped(18))
            assertEquals(QuranListEffect.OpenSurah(18), awaitItem())
        }
    }

    @Test
    fun comingBackShowsTheNewReadingPosition() = runTest {
        val viewModel = viewModel()
        RecordReading(progress, FixedClock())(18, 10, setOf(10), 110)
        viewModel.dispatch(QuranListIntent.Appeared)
        assertEquals(10, viewModel.state.value.readingProgress?.lastAyahNumber)
    }

    @Test
    fun theHadithFollowsTheTranslationLanguage() = runTest {
        val viewModel = viewModel()
        language.switchTo(Language.Urdu)
        assertEquals("تم میں سب سے بہتر شخص وہ ہے جو قرآن سیکھے اور اسے سکھائے", viewModel.state.value.header?.text)
    }
}
