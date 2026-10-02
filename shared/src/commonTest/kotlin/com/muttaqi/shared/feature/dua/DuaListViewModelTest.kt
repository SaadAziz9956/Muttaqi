package com.muttaqi.shared.feature.dua

import app.cash.turbine.test
import com.muttaqi.shared.core.model.Language
import com.muttaqi.shared.core.text.SearchTextFolder
import com.muttaqi.shared.feature.dua.data.repository.BundledDuaRepository
import com.muttaqi.shared.feature.dua.domain.usecase.BuildDuaSearchIndex
import com.muttaqi.shared.feature.dua.domain.usecase.GetDuaCategories
import com.muttaqi.shared.feature.dua.presentation.list.DuaListEffect
import com.muttaqi.shared.feature.dua.presentation.list.DuaListIntent
import com.muttaqi.shared.feature.dua.presentation.list.DuaListViewModel
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
class DuaListViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()
    private val language = FakeSelectedLanguage()

    private fun viewModel(): DuaListViewModel {
        val repository = BundledDuaRepository(FakeContentSource(DuaTestData.files), TestDispatchers(dispatcher))
        return DuaListViewModel(GetDuaCategories(repository), BuildDuaSearchIndex(SearchTextFolder), language)
    }

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun loadsCategoriesAndTheHeaderVerse() = runTest {
        val state = viewModel().state.value
        assertFalse(state.isLoading)
        assertEquals(3, state.categories.size)
        assertEquals("Quran (40:60)", state.header?.source)
    }

    @Test
    fun searchShowsResultsAndClearingEndsIt() = runTest {
        val viewModel = viewModel()
        viewModel.dispatch(DuaListIntent.QueryChanged("travel"))
        assertTrue(viewModel.state.value.isSearching)
        assertEquals(listOf("hisn-3"), viewModel.state.value.results.map { it.chapter.id })
        viewModel.dispatch(DuaListIntent.ClearQuery)
        assertFalse(viewModel.state.value.isSearching)
    }

    @Test
    fun aCategoryWithOneChapterOpensStraightToIt() = runTest {
        val viewModel = viewModel()
        viewModel.effects.test {
            viewModel.dispatch(DuaListIntent.CategoryTapped("travel"))
            assertEquals(DuaListEffect.OpenChapter("hisn-3"), awaitItem())
            viewModel.dispatch(DuaListIntent.CategoryTapped("sleep"))
            assertEquals(DuaListEffect.OpenCategory("sleep"), awaitItem())
        }
    }

    @Test
    fun switchingLanguageReloadsAndKeepsTheSearch() = runTest {
        val viewModel = viewModel()
        viewModel.dispatch(DuaListIntent.QueryChanged("waking"))
        language.switchTo(Language.Urdu)
        val state = viewModel.state.value
        assertEquals("waking", state.query)
        assertEquals(listOf("hisn-1", "hisn-2"), state.results.map { it.chapter.id })
        assertEquals("سب تعریف اللہ کے لیے", state.results.first().chapter.entries.single().translation)
        assertTrue(state.header!!.text.contains("دعا"))
    }
}
