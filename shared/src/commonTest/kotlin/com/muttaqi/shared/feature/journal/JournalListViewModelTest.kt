package com.muttaqi.shared.feature.journal

import app.cash.turbine.test
import com.muttaqi.shared.core.model.Language
import com.muttaqi.shared.core.text.SearchTextFolder
import com.muttaqi.shared.feature.journal.JournalTestData.cafe
import com.muttaqi.shared.feature.journal.JournalTestData.parents
import com.muttaqi.shared.feature.journal.JournalTestData.walk
import com.muttaqi.shared.feature.journal.domain.usecase.BuildJournalSearchIndex
import com.muttaqi.shared.feature.journal.domain.usecase.DeleteJournalEntry
import com.muttaqi.shared.feature.journal.domain.usecase.ObserveJournalEntries
import com.muttaqi.shared.feature.journal.presentation.list.JournalListEffect
import com.muttaqi.shared.feature.journal.presentation.list.JournalListIntent
import com.muttaqi.shared.feature.journal.presentation.list.JournalListViewModel
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
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class JournalListViewModelTest {
    private val language = FakeSelectedLanguage()
    private val repository = FakeJournalRepository(walk, cafe, parents)

    private fun viewModel() = JournalListViewModel(
        ObserveJournalEntries(repository),
        BuildJournalSearchIndex(SearchTextFolder),
        DeleteJournalEntry(repository),
        language,
    )

    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun loadsEntriesNewestFirstUnderTheVerse() = runTest {
        val state = viewModel().state.value
        assertFalse(state.isLoading)
        assertEquals(listOf("walk", "parents", "cafe"), state.shownEntries.map { it.id })
        assertEquals("Nun. By the pen and what they inscribe", state.header?.text)
        assertEquals("Quran (68:1)", state.header?.source)
    }

    @Test
    fun theVerseFollowsTheLanguage() = runTest {
        val viewModel = viewModel()
        language.switchTo(Language.Urdu)
        assertEquals("نٓ۔ قلم کی اور جو (اہل قلم) لکھتے ہیں اس کی قسم", viewModel.state.value.header?.text)
        language.switchTo(Language.Hindi)
        assertEquals("Nun. By the pen and what they inscribe", viewModel.state.value.header?.text)
    }

    @Test
    fun anEmptyJournalHasLoadedWithNoEntries() = runTest {
        repository.stored.value = emptyList()
        val state = viewModel().state.value
        assertFalse(state.isLoading)
        assertFalse(state.hasEntries)
    }

    @Test
    fun searchNarrowsTheListAndFollowsNewEntries() = runTest {
        val viewModel = viewModel()
        viewModel.dispatch(JournalListIntent.QueryChanged("rain"))
        assertTrue(viewModel.state.value.isSearching)
        assertEquals(listOf("parents"), viewModel.state.value.shownEntries.map { it.id })
        repository.save(JournalTestData.entry("storm", "Rain at last", "", "2026-09-30T09:00:00Z"))
        assertEquals(listOf("storm", "parents"), viewModel.state.value.shownEntries.map { it.id })
        viewModel.dispatch(JournalListIntent.QueryChanged("  "))
        assertFalse(viewModel.state.value.isSearching)
        assertEquals(4, viewModel.state.value.shownEntries.size)
    }

    @Test
    fun deletingTakesTheEntryOutStraightAway() = runTest {
        val viewModel = viewModel()
        viewModel.dispatch(JournalListIntent.QueryChanged("a"))
        viewModel.dispatch(JournalListIntent.DeleteTapped("parents"))
        assertEquals(listOf("walk", "cafe"), viewModel.state.value.entries.map { it.id })
        assertTrue(viewModel.state.value.results.none { it.id == "parents" })
        assertEquals(listOf("delete parents"), repository.writes)
    }

    @Test
    fun tappingOpensTheEntryAndPlusOpensANewOne() = runTest {
        val viewModel = viewModel()
        viewModel.effects.test {
            viewModel.dispatch(JournalListIntent.EntryTapped("walk"))
            assertEquals(JournalListEffect.OpenEntry("walk"), awaitItem())
            viewModel.dispatch(JournalListIntent.NewEntryTapped)
            assertEquals(JournalListEffect.OpenEntry(null), awaitItem())
        }
    }
}
