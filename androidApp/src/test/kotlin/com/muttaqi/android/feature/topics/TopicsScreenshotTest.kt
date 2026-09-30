package com.muttaqi.android.feature.topics

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import com.muttaqi.android.testing.PHONE
import com.muttaqi.android.testing.captureLightAndDark
import com.muttaqi.shared.core.content.BundledContentSource
import com.muttaqi.shared.core.domain.DispatcherProvider
import com.muttaqi.shared.core.model.Language
import com.muttaqi.shared.core.preferences.SelectedLanguage
import com.muttaqi.shared.core.text.SearchTextFolder
import com.muttaqi.shared.feature.dua.data.repository.BundledDuaRepository
import com.muttaqi.shared.feature.dua.domain.usecase.GetDuaEntriesById
import com.muttaqi.shared.feature.topics.data.repository.BundledEmotionRepository
import com.muttaqi.shared.feature.topics.data.repository.BundledExploreRepository
import com.muttaqi.shared.feature.topics.domain.model.TopicChips
import com.muttaqi.shared.feature.topics.domain.usecase.BuildExploreSearchIndex
import com.muttaqi.shared.feature.topics.domain.usecase.GetEmotions
import com.muttaqi.shared.feature.topics.domain.usecase.GetEmotionsHeader
import com.muttaqi.shared.feature.topics.domain.usecase.GetExploreGroups
import com.muttaqi.shared.feature.topics.domain.usecase.GetExploreHeader
import com.muttaqi.shared.feature.topics.domain.usecase.GetTopicPageTopics
import com.muttaqi.shared.feature.topics.presentation.emotions.EmotionsViewModel
import com.muttaqi.shared.feature.topics.presentation.explore.ExploreIntent
import com.muttaqi.shared.feature.topics.presentation.explore.ExploreViewModel
import com.muttaqi.shared.feature.topics.presentation.page.TopicPageIntent
import com.muttaqi.shared.feature.topics.presentation.page.TopicPageViewModel
import com.muttaqi.shared.feature.topics.presentation.page.TopicSection
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

/** The screens with the real bundled texts, in the view models' own states, to compare with the iOS screenshots */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(application = Application::class, sdk = [35], qualifiers = PHONE)
class TopicsScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    private val content = BundledContentSource { File("../content/data/$it").readText() }

    // Everything runs where it's called, so each view model has loaded by the time it's made
    private val dispatchers = object : DispatcherProvider {
        override val main: CoroutineDispatcher = Dispatchers.Unconfined
        override val io: CoroutineDispatcher = Dispatchers.Unconfined
        override val default: CoroutineDispatcher = Dispatchers.Unconfined
    }
    private val duas = GetDuaEntriesById(BundledDuaRepository(content, dispatchers))
    private val emotionRepository = BundledEmotionRepository(content, dispatchers, duas)
    private val exploreRepository = BundledExploreRepository(content, dispatchers, duas)

    private fun language(language: Language) = object : SelectedLanguage {
        override val current = language
        override val changes = MutableStateFlow(language)
    }

    private fun topicPage(chips: TopicChips, topicId: String, section: TopicSection, language: Language = Language.English) =
        TopicPageViewModel(chips, topicId, GetTopicPageTopics(emotionRepository, exploreRepository), language(language))
            .apply { dispatch(TopicPageIntent.SectionTapped(section)) }
            .state.value

    private fun exploreState(query: String = "") =
        ExploreViewModel(GetExploreHeader(exploreRepository), GetExploreGroups(exploreRepository), BuildExploreSearchIndex(SearchTextFolder), language(Language.English))
            .apply { if (query.isNotEmpty()) dispatch(ExploreIntent.QueryChanged(query)) }
            .state.value

    @Test
    fun emotions() {
        val state = EmotionsViewModel(GetEmotionsHeader(emotionRepository), GetEmotions(emotionRepository), language(Language.English)).state.value
        compose.captureLightAndDark("topics_emotions") { EmotionsScreen(state, onIntent = {}, onBack = {}) }
    }

    @Test
    fun explore() {
        val state = exploreState()
        compose.captureLightAndDark("topics_explore") { ExploreScreen(state, onIntent = {}) }
    }

    @Test
    fun exploreSearch() {
        val state = exploreState("zakah")
        compose.captureLightAndDark("topics_explore_search") { ExploreScreen(state, onIntent = {}) }
    }

    @Test
    fun exploreSearchWithNoResults() {
        val state = exploreState("qwxz")
        compose.captureLightAndDark("topics_explore_search_none") { ExploreScreen(state, onIntent = {}) }
    }

    @Test
    fun emotionVerses() {
        val state = topicPage(TopicChips.Emotions, "anxious", TopicSection.Quran)
        compose.captureLightAndDark("topics_page_quran") { TopicPageScreen("Emotions", state, onIntent = {}, onBack = {}) }
    }

    @Test
    fun emotionHadith() {
        val state = topicPage(TopicChips.Emotions, "anxious", TopicSection.Hadith)
        compose.captureLightAndDark("topics_page_hadith") { TopicPageScreen("Emotions", state, onIntent = {}, onBack = {}) }
    }

    @Test
    fun emotionDuas() {
        val state = topicPage(TopicChips.Emotions, "anxious", TopicSection.Dua)
        compose.captureLightAndDark("topics_page_dua") { TopicPageScreen("Emotions", state, onIntent = {}, onBack = {}) }
    }

    @Test
    fun exploreTopicInUrdu() {
        val state = topicPage(TopicChips.ExploreGroup, "fasting", TopicSection.Hadith, Language.Urdu)
        compose.captureLightAndDark("topics_page_urdu") { TopicPageScreen("Explore", state, onIntent = {}, onBack = {}) }
    }
}
