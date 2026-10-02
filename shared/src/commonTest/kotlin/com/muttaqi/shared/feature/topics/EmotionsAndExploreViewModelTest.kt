package com.muttaqi.shared.feature.topics

import app.cash.turbine.test
import com.muttaqi.shared.core.model.Language
import com.muttaqi.shared.core.text.SearchTextFolder
import com.muttaqi.shared.feature.dua.data.repository.BundledDuaRepository
import com.muttaqi.shared.feature.dua.domain.usecase.GetDuaEntriesById
import com.muttaqi.shared.feature.topics.data.repository.BundledExploreRepository
import com.muttaqi.shared.feature.topics.domain.usecase.BuildExploreSearchIndex
import com.muttaqi.shared.feature.topics.domain.usecase.GetEmotions
import com.muttaqi.shared.feature.topics.domain.usecase.GetEmotionsHeader
import com.muttaqi.shared.feature.topics.domain.usecase.GetExploreGroups
import com.muttaqi.shared.feature.topics.domain.usecase.GetExploreHeader
import com.muttaqi.shared.feature.topics.presentation.emotions.EmotionsEffect
import com.muttaqi.shared.feature.topics.presentation.emotions.EmotionsIntent
import com.muttaqi.shared.feature.topics.presentation.emotions.EmotionsViewModel
import com.muttaqi.shared.feature.topics.presentation.explore.ExploreEffect
import com.muttaqi.shared.feature.topics.presentation.explore.ExploreIntent
import com.muttaqi.shared.feature.topics.presentation.explore.ExploreViewModel
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
class EmotionsAndExploreViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()
    private val language = FakeSelectedLanguage()

    private fun emotions(): EmotionsViewModel {
        val repository = TopicsTestData.emotionRepository(dispatcher)
        return EmotionsViewModel(GetEmotionsHeader(repository), GetEmotions(repository), language)
    }

    private fun explore(files: Map<String, String> = TopicsTestData.files): ExploreViewModel {
        val duas = GetDuaEntriesById(BundledDuaRepository(FakeContentSource(files), TestDispatchers(dispatcher)))
        val repository = BundledExploreRepository(FakeContentSource(files), TestDispatchers(dispatcher), duas)
        return ExploreViewModel(GetExploreHeader(repository), GetExploreGroups(repository), BuildExploreSearchIndex(SearchTextFolder), language)
    }

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun emotionsLoadWithTheVerseUnderTheTitle() = runTest {
        val state = emotions().state.value
        assertFalse(state.isLoading)
        assertEquals(listOf("Angry", "Bored", "Happy"), state.emotions.map { it.title })
        assertEquals("Quran (65:3)", state.header?.source)
    }

    @Test
    fun tappingAnEmotionOpensItsPage() = runTest {
        val viewModel = emotions()
        viewModel.effects.test {
            viewModel.dispatch(EmotionsIntent.EmotionTapped("bored"))
            assertEquals(EmotionsEffect.OpenEmotion("bored"), awaitItem())
        }
    }

    @Test
    fun emotionsFollowTheTranslationLanguage() = runTest {
        val viewModel = emotions()
        language.switchTo(Language.Urdu)
        val state = viewModel.state.value
        assertEquals("اور جو خدا پر بھروسہ رکھے گا تو وہ اس کو کفایت کرے گا۔", state.header?.text)
        assertEquals("اور غصے کو روکتے", state.emotions.first().verses.first().translation)
    }

    @Test
    fun exploreLoadsItsGroupsAndSearchRanksNamesFirst() = runTest {
        val viewModel = explore()
        assertEquals(listOf("Worship", "Sins to Avoid"), viewModel.state.value.groups.map { it.title })
        assertEquals("Quran (29:69)", viewModel.state.value.header?.source)

        viewModel.dispatch(ExploreIntent.QueryChanged("prayer"))
        assertTrue(viewModel.state.value.isSearching)
        assertEquals(listOf("prayer", "charity-zakat"), viewModel.state.value.results.map { it.topic.id })
        assertEquals("Worship", viewModel.state.value.results.first().group.title)

        viewModel.dispatch(ExploreIntent.ClearQuery)
        assertFalse(viewModel.state.value.isSearching)
        assertTrue(viewModel.state.value.results.isEmpty())
    }

    @Test
    fun tappingATopicOpensItsPage() = runTest {
        val viewModel = explore()
        viewModel.effects.test {
            viewModel.dispatch(ExploreIntent.TopicTapped("fasting"))
            assertEquals(ExploreEffect.OpenTopic("fasting"), awaitItem())
        }
    }

    @Test
    fun switchingLanguageReloadsAndKeepsTheSearch() = runTest {
        val viewModel = explore()
        viewModel.dispatch(ExploreIntent.QueryChanged("fasting"))
        language.switchTo(Language.Urdu)
        val state = viewModel.state.value
        assertEquals("fasting", state.query)
        assertEquals(listOf("fasting"), state.results.map { it.topic.id })
        assertEquals("تم پر روزے فرض کئے گئے ہیں", state.results.single().topic.verses.single().translation)
        assertTrue(state.header!!.text.startsWith("اور جن لوگوں"))
        viewModel.dispatch(ExploreIntent.QueryChanged("روزے"))
        assertEquals(listOf("fasting"), viewModel.state.value.results.map { it.topic.id })
    }

    @Test
    fun aBrokenFileIsReportedRatherThanCrashing() = runTest {
        val state = explore(TopicsTestData.files + ("Explore.json" to "{")).state.value
        assertFalse(state.isLoading)
        assertTrue(state.failed)
    }
}
