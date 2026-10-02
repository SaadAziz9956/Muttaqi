package com.muttaqi.shared.feature.topics

import app.cash.turbine.test
import com.muttaqi.shared.core.model.Language
import com.muttaqi.shared.core.share.SharePassage
import com.muttaqi.shared.feature.topics.domain.model.TopicChips
import com.muttaqi.shared.feature.topics.domain.usecase.GetTopicPageTopics
import com.muttaqi.shared.feature.topics.presentation.page.TopicPageEffect
import com.muttaqi.shared.feature.topics.presentation.page.TopicPageIntent
import com.muttaqi.shared.feature.topics.presentation.page.TopicPageViewModel
import com.muttaqi.shared.feature.topics.presentation.page.TopicSection
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

@OptIn(ExperimentalCoroutinesApi::class)
class TopicPageViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()
    private val language = FakeSelectedLanguage()

    private fun viewModel(chips: TopicChips, topicId: String) = TopicPageViewModel(
        chips,
        topicId,
        GetTopicPageTopics(TopicsTestData.emotionRepository(dispatcher), TopicsTestData.exploreRepository(dispatcher)),
        language,
    )

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun anEmotionOpensWithAChipForEveryEmotion() = runTest {
        val state = viewModel(TopicChips.Emotions, "bored").state.value
        assertFalse(state.isLoading)
        assertEquals(listOf("angry", "bored", "happy"), state.topics.map { it.id })
        assertEquals("bored", state.selectedId)
        assertEquals(listOf(TopicSection.Quran, TopicSection.Hadith), state.sections)
    }

    @Test
    fun anExploreTopicOpensWithTheTopicsInItsGroup() = runTest {
        val state = viewModel(TopicChips.ExploreGroup, "prayer").state.value
        assertEquals(listOf("Fasting", "Charity & Zakat", "Prayer"), state.topics.map { it.title })
        assertEquals(listOf(TopicSection.Dua), state.sections)
        assertEquals(TopicSection.Dua, state.section)
        assertEquals(listOf("Hisn al-Muslim (hisnmuslim.com)"), state.translationCredits)
    }

    @Test
    fun chipsAndTheSegmentedControlChangeWhatsShown() = runTest {
        val viewModel = viewModel(TopicChips.Emotions, "angry")
        viewModel.dispatch(TopicPageIntent.SectionTapped(TopicSection.Hadith))
        assertEquals(listOf("Do not get angry."), viewModel.state.value.passages.map { it.translation })
        viewModel.dispatch(TopicPageIntent.TopicTapped("bored"))
        assertEquals("bored", viewModel.state.value.selectedId)
        assertEquals(TopicSection.Hadith, viewModel.state.value.section)
        assertEquals(listOf("There are two blessings"), viewModel.state.value.passages.map { it.translation })
    }

    @Test
    fun sharingAPassageOpensItsCard() = runTest {
        val viewModel = viewModel(TopicChips.ExploreGroup, "fasting")
        viewModel.effects.test {
            viewModel.dispatch(TopicPageIntent.ShareTapped("quran-2:183"))
            assertEquals(
                TopicPageEffect.OpenShare(SharePassage("كُتِبَ عَلَيْكُمُ ٱلصِّيَامُ", null, "decreed upon you is fasting", "Quran (2:183)")),
                awaitItem(),
            )
            viewModel.dispatch(TopicPageIntent.SectionTapped(TopicSection.Dua))
            viewModel.dispatch(TopicPageIntent.ShareTapped("hisn-3"))
            assertEquals(
                TopicPageEffect.OpenShare(SharePassage("سُبْحَانَ الَّذِي سَخَّرَ", "Subhaanal-lathee", "How perfect He is", "Muslim")),
                awaitItem(),
            )
            viewModel.dispatch(TopicPageIntent.ShareTapped("quran-2:183"))
            expectNoEvents()
        }
    }

    @Test
    fun switchingLanguageReloadsOnTheSameTopicAndSection() = runTest {
        val viewModel = viewModel(TopicChips.Emotions, "angry")
        viewModel.dispatch(TopicPageIntent.TopicTapped("bored"))
        viewModel.dispatch(TopicPageIntent.SectionTapped(TopicSection.Hadith))
        language.switchTo(Language.Urdu)
        val state = viewModel.state.value
        assertEquals("bored", state.selectedId)
        assertEquals(TopicSection.Hadith, state.section)
        assertEquals("دو نعمتیں", state.passages.single().translation)
        assertEquals("رواه البخاري · صحيح", state.passages.single().source)
    }
}
