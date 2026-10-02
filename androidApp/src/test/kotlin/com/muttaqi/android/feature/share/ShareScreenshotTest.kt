package com.muttaqi.android.feature.share

import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import com.muttaqi.android.designsystem.MuttaqiTheme
import com.muttaqi.android.testing.captureLightAndDark
import com.muttaqi.shared.core.content.BundledContentSource
import com.muttaqi.shared.core.domain.DispatcherProvider
import com.muttaqi.shared.core.model.Language
import com.muttaqi.shared.core.preferences.SelectedLanguage
import com.muttaqi.shared.core.share.SharePassage
import com.muttaqi.shared.feature.dua.data.repository.BundledDuaRepository
import com.muttaqi.shared.feature.dua.domain.usecase.GetDuaEntriesById
import com.muttaqi.shared.feature.share.presentation.ShareState
import com.muttaqi.shared.feature.share.presentation.ShareViewModel
import com.muttaqi.shared.feature.topics.data.repository.BundledEmotionRepository
import com.muttaqi.shared.feature.topics.data.repository.BundledExploreRepository
import com.muttaqi.shared.feature.topics.domain.model.TopicChips
import com.muttaqi.shared.feature.topics.domain.usecase.GetTopicPageTopics
import com.muttaqi.shared.feature.topics.presentation.page.TopicPageIntent
import com.muttaqi.shared.feature.topics.presentation.page.TopicPageViewModel
import com.muttaqi.shared.feature.topics.presentation.page.TopicSection
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(application = Application::class, sdk = [35], qualifiers = IPHONE_AIR)
class ShareScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    private val content = BundledContentSource { File("../content/data/$it").readText() }

    private val dispatchers = object : DispatcherProvider {
        override val main: CoroutineDispatcher = Dispatchers.Unconfined
        override val io: CoroutineDispatcher = Dispatchers.Unconfined
        override val default: CoroutineDispatcher = Dispatchers.Unconfined
    }
    private val duas = BundledDuaRepository(content, dispatchers)
    private val topics = GetDuaEntriesById(duas).let { entries ->
        GetTopicPageTopics(BundledEmotionRepository(content, dispatchers, entries), BundledExploreRepository(content, dispatchers, entries))
    }

    private fun language(language: Language) = object : SelectedLanguage {
        override val current = language
        override val changes = MutableStateFlow(language)
    }

    private fun topicPassage(topicId: String, section: TopicSection, language: Language, index: Int = 0): SharePassage =
        TopicPageViewModel(TopicChips.ExploreGroup, topicId, topics, language(language))
            .apply { dispatch(TopicPageIntent.SectionTapped(section)) }
            .state.value.passages[index].toSharePassage()

    private fun quranicDua(surah: Int, ayah: Int): SharePassage {
        val dua = runBlocking { duas.quranicDuas(Language.English) }.first { it.surahNumber == surah && it.ayahNumber == ayah }
        return SharePassage(dua.arabic, dua.transliteration, dua.translation, "Quran (${dua.reference})")
    }

    private fun share(passage: SharePassage, language: Language): ShareState = ShareViewModel(passage, language(language)).state.value

    private fun capture(name: String, state: ShareState) =
        compose.captureLightAndDark(name) { ShareScreen(state, onIntent = {}, onBack = {}) }

    @Test
    fun verse() {
        val verse = topicPassage("oneness-of-allah", TopicSection.Quran, Language.English, index = 1)
        assertEquals("Quran (2:163)", verse.reference)
        capture("share_verse", share(verse, Language.English))
    }

    @Test
    fun verseInUrdu() {
        val verse = topicPassage("oneness-of-allah", TopicSection.Quran, Language.Urdu, index = 1)
        capture("share_verse_urdu", share(verse, Language.Urdu))
    }

    @Test
    fun hadithInUrdu() {
        capture("share_hadith_urdu", share(topicPassage("fasting", TopicSection.Hadith, Language.Urdu), Language.Urdu))
    }

    @Test
    fun duaWithTransliteration() {
        capture("share_dua_transliteration", share(quranicDua(26, 83), Language.English))
    }

    @Test
    fun sharedImage() {
        lateinit var layer: GraphicsLayer
        compose.setContent {
            layer = rememberGraphicsLayer()
            MuttaqiTheme(darkTheme = true) {
                Box(Modifier.fillMaxSize()) { RecordShareCardImage(quranicDua(26, 83), layer) }
            }
        }
        compose.onRoot().captureToImage()
        val bitmap = runBlocking { layer.toImageBitmap() }.asAndroidBitmap()
        assertEquals(1170, bitmap.width)
        bitmap.captureRoboImage("screenshots/share_image.png")
    }
}

private const val IPHONE_AIR = "w420dp-h912dp-xxhdpi"
