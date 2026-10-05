package com.muttaqi.shared.feature.names

import app.cash.turbine.test
import com.muttaqi.shared.core.model.Language
import com.muttaqi.shared.core.quote.DisplayedQuote
import com.muttaqi.shared.core.share.SharePassage
import com.muttaqi.shared.core.text.SearchTextFolder
import com.muttaqi.shared.feature.names.data.repository.BundledNamesRepository
import com.muttaqi.shared.feature.names.domain.model.AllahName
import com.muttaqi.shared.feature.names.domain.model.NameSearchMode
import com.muttaqi.shared.feature.names.domain.usecase.BuildNamesSearchIndex
import com.muttaqi.shared.feature.names.domain.usecase.GetAllahNames
import com.muttaqi.shared.feature.names.presentation.NamesEffect
import com.muttaqi.shared.feature.names.presentation.NamesIntent
import com.muttaqi.shared.feature.names.presentation.NamesMutation
import com.muttaqi.shared.feature.names.presentation.NamesReducer
import com.muttaqi.shared.feature.names.presentation.NamesState
import com.muttaqi.shared.feature.names.presentation.NamesViewModel
import com.muttaqi.shared.testing.FakeContentSource
import com.muttaqi.shared.testing.FakeSelectedLanguage
import com.muttaqi.shared.testing.TestDispatchers
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
class NamesViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()
    private val language = FakeSelectedLanguage()

    private fun viewModel() = NamesViewModel(
        GetAllahNames(BundledNamesRepository(FakeContentSource(NamesTestData.files), TestDispatchers(dispatcher))),
        BuildNamesSearchIndex(SearchTextFolder),
        language,
    )

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun theReducerStartsTheQueryAgainWhenTheModeChanges() {
        val name = AllahName(1, "اللَّهُ", "Allāh", "He is Allah")
        val searching = NamesReducer.reduce(
            NamesReducer.reduce(NamesState(), NamesMutation.Loaded(listOf(name), DisplayedQuote("q", "s"), emptyList())),
            NamesMutation.SearchUpdated("1", listOf(name)),
        )
        assertTrue(searching.isSearching)
        val switched = NamesReducer.reduce(searching, NamesMutation.SearchModeChanged(NameSearchMode.ByName))
        assertEquals(NameSearchMode.ByName, switched.searchMode)
        assertEquals("", switched.query)
        assertTrue(switched.results.isEmpty())
        assertFalse(switched.isSearching)
    }

    @Test
    fun loadsTheNamesAndTheHadithAtTheFootOfThePage() = runTest {
        val viewModel = viewModel()
        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals(4, state.names.size)
        assertEquals("Sahih al-Bukhari 7392, Sahih Muslim 2677 · HadeethEnc.com", state.hadith?.source)
        assertEquals(1, viewModel.position.value)
    }

    @Test
    fun searchesByNumberThenByNameAndClearing() = runTest {
        val viewModel = viewModel()
        viewModel.dispatch(NamesIntent.QueryChanged("2"))
        assertEquals(listOf(2), viewModel.state.value.results.map { it.number })
        viewModel.dispatch(NamesIntent.SearchModeChanged(NameSearchMode.ByName))
        assertEquals("", viewModel.state.value.query)
        viewModel.dispatch(NamesIntent.QueryChanged("raheem"))
        assertEquals(listOf(3), viewModel.state.value.results.map { it.number })
        viewModel.dispatch(NamesIntent.ClearQuery)
        assertFalse(viewModel.state.value.isSearching)
        assertEquals(NameSearchMode.ByName, viewModel.state.value.searchMode)
    }

    @Test
    fun pickingTheModeAlreadyShownKeepsTheQuery() = runTest {
        val viewModel = viewModel()
        viewModel.dispatch(NamesIntent.QueryChanged("2"))
        viewModel.dispatch(NamesIntent.SearchModeChanged(NameSearchMode.ByNumber))
        assertEquals("2", viewModel.state.value.query)
    }

    @Test
    fun pickingAResultTurnsThePageToIt() = runTest {
        val viewModel = viewModel()
        viewModel.effects.test {
            viewModel.dispatch(NamesIntent.ResultTapped(3))
            assertEquals(NamesEffect.ShowName(3), awaitItem())
        }
        assertEquals(3, viewModel.position.value)
    }

    @Test
    fun sharingSharesTheNameOnScreen() = runTest {
        val viewModel = viewModel()
        viewModel.dispatch(NamesIntent.NameShown(2))
        viewModel.effects.test {
            viewModel.dispatch(NamesIntent.ShareTapped)
            assertEquals(
                NamesEffect.OpenShare(SharePassage("الرَّحْمَنُ", "Ar-Raḥmān", "the Most Merciful (to the creation)", "The Names of Allah (2 of 99) · Jami at-Tirmidhi 3507")),
                awaitItem(),
            )
        }
    }

    @Test
    fun switchingLanguageReloadsAndKeepsTheSearch() = runTest {
        val viewModel = viewModel()
        viewModel.dispatch(NamesIntent.NameShown(4))
        viewModel.dispatch(NamesIntent.QueryChanged("2"))
        language.switchTo(Language.Urdu)
        val state = viewModel.state.value
        assertEquals("بہت رحم کرنے والا", state.results.single().meaning)
        assertTrue(state.hadith!!.text.contains("نام"))
        assertEquals(4, viewModel.position.value)
    }
}
