package com.muttaqi.shared.feature.dhikr

import app.cash.turbine.test
import com.muttaqi.shared.core.model.Language
import com.muttaqi.shared.core.preferences.LegacyDataSource
import com.muttaqi.shared.core.share.SharePassage
import com.muttaqi.shared.feature.dhikr.DhikrTestData.today
import com.muttaqi.shared.feature.dhikr.data.progress.SettingsDhikrProgressRepository
import com.muttaqi.shared.feature.dhikr.data.repository.BundledDhikrRepository
import com.muttaqi.shared.feature.dhikr.domain.model.DhikrMilestone
import com.muttaqi.shared.feature.dhikr.domain.model.DhikrProgress
import com.muttaqi.shared.feature.dhikr.domain.model.DhikrStepPosition
import com.muttaqi.shared.feature.dhikr.domain.usecase.CountDhikr
import com.muttaqi.shared.feature.dhikr.domain.usecase.GetDhikr
import com.muttaqi.shared.feature.dhikr.domain.usecase.GetTodaysDhikrProgress
import com.muttaqi.shared.feature.dhikr.domain.usecase.ResetDhikrProgress
import com.muttaqi.shared.feature.dhikr.presentation.counter.DhikrCounterEffect
import com.muttaqi.shared.feature.dhikr.presentation.counter.DhikrCounterIntent
import com.muttaqi.shared.feature.dhikr.presentation.counter.DhikrCounterMutation
import com.muttaqi.shared.feature.dhikr.presentation.counter.DhikrCounterReducer
import com.muttaqi.shared.feature.dhikr.presentation.counter.DhikrCounterState
import com.muttaqi.shared.feature.dhikr.presentation.counter.DhikrCounterViewModel
import com.muttaqi.shared.testing.FakeContentSource
import com.muttaqi.shared.testing.FakeSelectedLanguage
import com.muttaqi.shared.testing.TestDispatchers
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class DhikrCounterViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()
    private val language = FakeSelectedLanguage()
    private val day = FakeCurrentDay()
    private val settings = MapSettings()
    private val progress = SettingsDhikrProgressRepository(settings, LegacyDataSource { null }, timeZone = { TimeZone.UTC })

    private fun viewModel(id: String) = DhikrCounterViewModel(
        dhikrId = id,
        getDhikr = GetDhikr(BundledDhikrRepository(FakeContentSource(DhikrTestData.files), TestDispatchers(dispatcher))),
        getProgress = GetTodaysDhikrProgress(progress, day),
        countDhikr = CountDhikr(progress, day),
        resetProgress = ResetDhikrProgress(progress, day),
        selectedLanguage = language,
    )

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun theReducerSetsTheDhikrAndTheProgress() {
        val empty = DhikrCounterState(progress = DhikrProgress.empty(today))
        val loaded = DhikrCounterReducer.reduce(empty, DhikrCounterMutation.Loaded(DhikrTestData.set))
        assertFalse(loaded.isLoading)
        assertEquals(DhikrStepPosition(0, 0), loaded.currentStep)
        val counted = DhikrCounterReducer.reduce(loaded, DhikrCounterMutation.ProgressChanged(DhikrProgress(3, 0, today)))
        assertEquals(DhikrStepPosition(1, 1), counted.currentStep)
        assertEquals(0.6, counted.roundProgress)
        assertTrue(counted.hasProgress)
        assertEquals(DhikrProgress.empty(today), empty.progress)
    }

    @Test
    fun opensShowingTodaysCount() = runTest {
        progress.save("subhanallah", DhikrProgress(2, 1, today))
        val state = viewModel("subhanallah").state.value
        assertEquals(2, state.count)
        assertEquals(1, state.rounds)
        assertEquals(3, state.target)
        assertEquals("SubhanAllah", state.dhikr?.transliteration)
    }

    @Test
    fun yesterdaysCountStartsAtZero() = runTest {
        progress.save("subhanallah", DhikrProgress(2, 1, LocalDate(2026, 9, 29)))
        assertFalse(viewModel("subhanallah").state.value.hasProgress)
    }

    @Test
    fun eachTapCountsSavesAndSignalsItsMilestone() = runTest {
        val viewModel = viewModel("set")
        viewModel.effects.test {
            repeat(5) { viewModel.dispatch(DhikrCounterIntent.Counted) }
            assertEquals(
                listOf(
                    DhikrMilestone.Repetition,
                    DhikrMilestone.PhraseFinished,
                    DhikrMilestone.Repetition,
                    DhikrMilestone.PhraseFinished,
                    DhikrMilestone.RoundFinished,
                ),
                List(5) { (awaitItem() as DhikrCounterEffect.Counted).milestone },
            )
        }
        val state = viewModel.state.value
        assertTrue(state.isRoundComplete)
        assertEquals(1, state.rounds)
        assertEquals(DhikrProgress(5, 1, today), progress.saved("set"))
    }

    @Test
    fun theTapAfterAFinishedRoundStartsTheNext() = runTest {
        val viewModel = viewModel("subhanallah")
        repeat(4) { viewModel.dispatch(DhikrCounterIntent.Counted) }
        assertEquals(1, viewModel.state.value.count)
        assertEquals(1, viewModel.state.value.rounds)
        assertFalse(viewModel.state.value.isRoundComplete)
    }

    @Test
    fun aCounterLeftOpenPastMidnightStartsTheNewDayAtOne() = runTest {
        val viewModel = viewModel("subhanallah")
        repeat(2) { viewModel.dispatch(DhikrCounterIntent.Counted) }
        day.day = LocalDate(2026, 10, 1)
        viewModel.dispatch(DhikrCounterIntent.Counted)
        assertEquals(DhikrProgress(1, 0, LocalDate(2026, 10, 1)), viewModel.state.value.progress)
    }

    @Test
    fun resetStartsTodayAgain() = runTest {
        val viewModel = viewModel("subhanallah")
        repeat(5) { viewModel.dispatch(DhikrCounterIntent.Counted) }
        viewModel.dispatch(DhikrCounterIntent.ResetConfirmed)
        assertFalse(viewModel.state.value.hasProgress)
        assertEquals(DhikrProgress.empty(today), progress.saved("subhanallah"))
    }

    @Test
    fun anOpenEndedDhikrHasNoRing() = runTest {
        val viewModel = viewModel("open-ended")
        repeat(3) { viewModel.dispatch(DhikrCounterIntent.Counted) }
        val state = viewModel.state.value
        assertEquals(3, state.count)
        assertNull(state.target)
        assertEquals(0.0, state.roundProgress)
        assertNull(state.currentStep)
    }

    @Test
    fun sharingASetSharesItsPhrasesTranslations() = runTest {
        val viewModel = viewModel("set")
        viewModel.effects.test {
            viewModel.dispatch(DhikrCounterIntent.ShareTapped)
            val passage = (awaitItem() as DhikrCounterEffect.OpenShare).passage
            assertEquals(
                SharePassage(
                    arabic = "سُبْحَانَ اللَّهِ، الْحَمْدُ لِلَّهِ، اللَّهُ أَكْبَرُ",
                    transliteration = "SubhanAllah x2, Alhamdulillah x2, Allahu Akbar x1",
                    translation = "glory be to Allah\npraise be to Allah\nAllah is the Most Great",
                    reference = "Sahih Muslim 597",
                ),
                passage,
            )
        }
    }

    @Test
    fun switchingLanguageKeepsTheCount() = runTest {
        val viewModel = viewModel("subhanallah")
        viewModel.dispatch(DhikrCounterIntent.Counted)
        language.switchTo(Language.Urdu)
        assertEquals("اللہ پاک ہے", viewModel.state.value.dhikr?.translation)
        assertEquals(1, viewModel.state.value.count)
    }
}
