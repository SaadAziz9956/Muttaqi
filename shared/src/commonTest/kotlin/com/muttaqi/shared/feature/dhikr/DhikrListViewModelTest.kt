package com.muttaqi.shared.feature.dhikr

import app.cash.turbine.test
import com.muttaqi.shared.core.model.Language
import com.muttaqi.shared.core.quote.DisplayedQuote
import com.muttaqi.shared.feature.dhikr.data.repository.BundledDhikrRepository
import com.muttaqi.shared.feature.dhikr.domain.model.DhikrSection
import com.muttaqi.shared.feature.dhikr.domain.usecase.GetDhikrSections
import com.muttaqi.shared.feature.dhikr.presentation.list.DhikrListEffect
import com.muttaqi.shared.feature.dhikr.presentation.list.DhikrListIntent
import com.muttaqi.shared.feature.dhikr.presentation.list.DhikrListMutation
import com.muttaqi.shared.feature.dhikr.presentation.list.DhikrListReducer
import com.muttaqi.shared.feature.dhikr.presentation.list.DhikrListState
import com.muttaqi.shared.feature.dhikr.presentation.list.DhikrListViewModel
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
class DhikrListViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()
    private val language = FakeSelectedLanguage()

    private fun viewModel() = DhikrListViewModel(
        GetDhikrSections(BundledDhikrRepository(FakeContentSource(DhikrTestData.files), TestDispatchers(dispatcher))),
        language,
    )

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun theReducerPicksATabAndKeepsItWhenTheSectionsReload() {
        val sections = listOf(DhikrSection("a", "A", "", emptyList()), DhikrSection("b", "B", "", emptyList()))
        val loaded = DhikrListReducer.reduce(DhikrListState(), DhikrListMutation.Loaded(DisplayedQuote("q", "s"), sections))
        assertEquals("a", loaded.selectedSection?.id)
        val picked = DhikrListReducer.reduce(loaded, DhikrListMutation.SectionSelected("b"))
        assertEquals("b", picked.selectedSection?.id)
        assertEquals("b", DhikrListReducer.reduce(picked, DhikrListMutation.Loaded(DisplayedQuote("q", "s"), sections)).selectedSection?.id)
        assertTrue(DhikrListReducer.reduce(DhikrListState(), DhikrListMutation.LoadFailed).failed)
    }

    @Test
    fun loadsTheSectionsAndTheHadithUnderTheTitle() = runTest {
        val state = viewModel().state.value
        assertFalse(state.isLoading)
        assertEquals(listOf("tasbih", "after-prayer"), state.sections.map { it.id })
        assertEquals("tasbih", state.selectedSection?.id)
        assertEquals("Sahih al-Bukhari 6407", state.header?.source)
    }

    @Test
    fun tappingATabShowsItsDhikr() = runTest {
        val viewModel = viewModel()
        viewModel.dispatch(DhikrListIntent.SectionTapped("after-prayer"))
        assertEquals(listOf("set"), viewModel.state.value.selectedSection?.dhikr?.map { it.id })
    }

    @Test
    fun tappingADhikrOpensItsCounter() = runTest {
        val viewModel = viewModel()
        viewModel.effects.test {
            viewModel.dispatch(DhikrListIntent.DhikrTapped("subhanallah"))
            assertEquals(DhikrListEffect.OpenCounter("subhanallah"), awaitItem())
        }
    }

    @Test
    fun switchingLanguageReloadsAndKeepsTheTab() = runTest {
        val viewModel = viewModel()
        viewModel.dispatch(DhikrListIntent.SectionTapped("after-prayer"))
        language.switchTo(Language.Urdu)
        val state = viewModel.state.value
        assertEquals("after-prayer", state.selectedSection?.id)
        assertEquals("اللہ پاک ہے", state.sections[0].dhikr[0].translation)
        assertTrue(state.header!!.text.contains("رب"))
    }
}
