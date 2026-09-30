package com.muttaqi.shared.feature.quran

import app.cash.turbine.test
import com.muttaqi.shared.core.domain.DomainError
import com.muttaqi.shared.core.domain.Outcome
import com.muttaqi.shared.core.model.Language
import com.muttaqi.shared.feature.quran.data.repository.SettingsReadingPreferences
import com.muttaqi.shared.feature.quran.domain.model.ReadingMode
import com.muttaqi.shared.feature.quran.domain.model.TafsirEntry
import com.muttaqi.shared.feature.quran.domain.repository.QuranLibrary
import com.muttaqi.shared.feature.quran.domain.repository.TafsirRepository
import com.muttaqi.shared.feature.quran.domain.usecase.ChangeFontSize
import com.muttaqi.shared.feature.quran.domain.usecase.ChangeReadingMode
import com.muttaqi.shared.feature.quran.domain.usecase.ChangeTranslation
import com.muttaqi.shared.feature.quran.domain.usecase.GetTafsir
import com.muttaqi.shared.feature.quran.domain.usecase.IsQuranStored
import com.muttaqi.shared.feature.quran.domain.usecase.ObserveReadingSettings
import com.muttaqi.shared.feature.quran.domain.usecase.SyncQuran
import com.muttaqi.shared.feature.quran.presentation.settings.ReadingSettingsEffect
import com.muttaqi.shared.feature.quran.presentation.settings.ReadingSettingsIntent
import com.muttaqi.shared.feature.quran.presentation.settings.ReadingSettingsViewModel
import com.muttaqi.shared.feature.quran.presentation.tafsir.TafsirIntent
import com.muttaqi.shared.feature.quran.presentation.tafsir.TafsirStatus
import com.muttaqi.shared.feature.quran.presentation.tafsir.TafsirViewModel
import com.muttaqi.shared.testing.FakeSelectedLanguage
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ReadingSettingsAndTafsirViewModelTest {
    private val language = FakeSelectedLanguage()
    private val settings = MapSettings()
    private val preferences = SettingsReadingPreferences(settings, language)

    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun settingsViewModel(library: FakeQuranLibrary = FakeQuranLibrary()) = ReadingSettingsViewModel(
        ObserveReadingSettings(preferences),
        ChangeReadingMode(preferences),
        ChangeFontSize(preferences),
        ChangeTranslation(library, SyncQuran(library, FakeDownloadRecord(), language.selector()), language.selector()),
        IsQuranStored(library),
    )

    @Test
    fun theModeAndFontSizeAreSavedAsTheyChange() = runTest {
        val viewModel = settingsViewModel()
        viewModel.dispatch(ReadingSettingsIntent.ModeSelected(ReadingMode.ArabicOnly))
        viewModel.dispatch(ReadingSettingsIntent.FontSizeIncreased)
        viewModel.dispatch(ReadingSettingsIntent.FontSizeIncreased)
        assertEquals(ReadingMode.ArabicOnly, viewModel.state.value.mode)
        assertEquals(104, viewModel.state.value.fontSize.percentage)
        assertEquals("arabicOnly", settings.getStringOrNull("reading_mode"))
        assertEquals(104, settings.getInt("reading_font_size", 0))
        viewModel.dispatch(ReadingSettingsIntent.FontSizeDecreased)
        assertEquals(102, viewModel.state.value.fontSize.percentage)
    }

    @Test
    fun aStoredTranslationIsChosenStraightAway() = runTest {
        val library = FakeQuranLibrary()
        val viewModel = settingsViewModel(library)
        viewModel.dispatch(ReadingSettingsIntent.LanguageSelected(Language.Urdu))
        runCurrent()
        assertEquals(Language.Urdu, viewModel.state.value.language)
        assertTrue(library.downloads.isEmpty())
        assertEquals(listOf(Language.English, Language.Urdu, Language.Hindi), viewModel.state.value.languages)
    }

    @Test
    fun anotherTranslationIsDownloadedFirst() = runTest {
        val gate = CompletableDeferred<Unit>()
        val library = object : QuranLibrary by FakeQuranLibrary() {
            override suspend fun downloadTranslation(language: Language): Outcome<Unit> {
                gate.await()
                return Outcome.Success(Unit)
            }
        }
        val viewModel = ReadingSettingsViewModel(
            ObserveReadingSettings(preferences),
            ChangeReadingMode(preferences),
            ChangeFontSize(preferences),
            ChangeTranslation(library, SyncQuran(library, FakeDownloadRecord(), language.selector()), language.selector()),
            IsQuranStored(library),
        )
        viewModel.dispatch(ReadingSettingsIntent.LanguageSelected(Language.Hindi))
        assertTrue(viewModel.state.value.isDownloadingLanguage)
        assertEquals(Language.English, viewModel.state.value.language)
        gate.complete(Unit)
        runCurrent()
        assertFalse(viewModel.state.value.isDownloadingLanguage)
        assertEquals(Language.Hindi, viewModel.state.value.language)
    }

    @Test
    fun aFailedDownloadKeepsTheLanguage() = runTest {
        val library = FakeQuranLibrary().apply { failDownloads = true }
        val viewModel = settingsViewModel(library)
        viewModel.effects.test {
            viewModel.dispatch(ReadingSettingsIntent.LanguageSelected(Language.Hindi))
            assertEquals(ReadingSettingsEffect.DownloadFailed("Failed to download Hindi translation. Please check your connection."), awaitItem())
        }
        assertEquals(Language.English, viewModel.state.value.language)
        assertFalse(viewModel.state.value.isDownloadingLanguage)
    }

    private class FakeTafsir : TafsirRepository {
        val requests = mutableListOf<Pair<Int, Language>>()
        var fail = false
        override suspend fun tafsir(surahNumber: Int, language: Language): Outcome<List<TafsirEntry>> {
            requests += surahNumber to language
            if (fail) return Outcome.Failure(DomainError.NoConnection)
            return Outcome.Success(listOf(TafsirEntry(surahNumber, 1, 5, "One\n\nTwo"), TafsirEntry(surahNumber, 6, 7, "Three")))
        }
    }

    @Test
    fun theTafsirLoadsWhenFirstOpenedAndIsKeptAfter() = runTest {
        val tafsir = FakeTafsir()
        val viewModel = TafsirViewModel(GetTafsir(tafsir), language)
        assertEquals(TafsirStatus.Idle, viewModel.state.value.status)
        viewModel.dispatch(TafsirIntent.Opened(1))
        assertIs<TafsirStatus.Loaded>(viewModel.state.value.status)
        assertEquals(6, viewModel.state.value.entryCovering(7)?.ayahNumber)
        viewModel.dispatch(TafsirIntent.Opened(1))
        assertEquals(1, tafsir.requests.size)
        viewModel.dispatch(TafsirIntent.Opened(2))
        assertEquals(listOf(1 to Language.English, 2 to Language.English), tafsir.requests)
    }

    @Test
    fun anotherLanguageLoadsTheTafsirAgainWhenNextOpened() = runTest {
        val tafsir = FakeTafsir()
        val viewModel = TafsirViewModel(GetTafsir(tafsir), language)
        viewModel.dispatch(TafsirIntent.Opened(2))
        language.switchTo(Language.Hindi)
        assertEquals(TafsirStatus.Idle, viewModel.state.value.status)
        assertTrue(viewModel.state.value.showsEnglishInstead)
        viewModel.dispatch(TafsirIntent.Opened(2))
        assertEquals(2 to Language.Hindi, tafsir.requests.last())
    }

    @Test
    fun aFailedTafsirCanBeRetried() = runTest {
        val tafsir = FakeTafsir().apply { fail = true }
        val viewModel = TafsirViewModel(GetTafsir(tafsir), language)
        viewModel.dispatch(TafsirIntent.Opened(2))
        assertEquals(TafsirStatus.Failed("The Internet connection appears to be offline."), viewModel.state.value.status)
        tafsir.fail = false
        viewModel.dispatch(TafsirIntent.Retry)
        assertIs<TafsirStatus.Loaded>(viewModel.state.value.status)
    }
}
