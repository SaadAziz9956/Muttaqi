package com.muttaqi.shared.feature.share

import app.cash.turbine.test
import com.muttaqi.shared.core.model.Language
import com.muttaqi.shared.core.quote.DisplayedQuote
import com.muttaqi.shared.core.share.SharePassage
import com.muttaqi.shared.feature.share.presentation.ShareEffect
import com.muttaqi.shared.feature.share.presentation.ShareIntent
import com.muttaqi.shared.feature.share.presentation.ShareMutation
import com.muttaqi.shared.feature.share.presentation.ShareReducer
import com.muttaqi.shared.feature.share.presentation.ShareState
import com.muttaqi.shared.feature.share.presentation.ShareViewModel
import com.muttaqi.shared.feature.share.presentation.imageFileName
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
import kotlin.test.assertSame

@OptIn(ExperimentalCoroutinesApi::class)
class ShareViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()

    private val dua = SharePassage(
        arabic = "رَبِّ هَبْ لِي حُكْمًا وَأَلْحِقْنِي بِالصَّالِحِينَ",
        transliteration = "Rabbi hab lee hukmanw wa alhiqnee bis saaliheen",
        translation = "My Lord, grant me authority and join me with the righteous.",
        reference = "Quran (26:83)",
    )
    private val englishVerse = "Invite to the way of your Lord with wisdom and good instruction, and argue with them in a way that is best."
    private val urduVerse = "(اے پیغمبر) لوگوں کو دانش اور نیک نصیحت سے اپنے پروردگار کے رستے کی طرف بلاؤ۔ اور بہت ہی اچھے طریق سے ان سے مناظرہ کرو۔"

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun theReducerChangesOnlyTheVerse() {
        val state = ShareState(dua, DisplayedQuote("en", "Quran (16:125)"))
        val changed = ShareReducer.reduce(state, ShareMutation.VerseChanged(DisplayedQuote("ur", "Quran (16:125)")))
        assertEquals("ur", changed.verse.text)
        assertSame(dua, changed.passage)
    }

    @Test
    fun showsThePassageAsHandedOverAndTheVerseInTheReadersLanguage() = runTest {
        val state = ShareViewModel(dua, FakeSelectedLanguage(Language.Urdu)).state.value
        assertEquals(dua, state.passage)
        assertEquals(DisplayedQuote(urduVerse, "Quran (16:125)"), state.verse)
    }

    @Test
    fun theVerseFollowsTheLanguageAndThePassageStays() = runTest {
        val language = FakeSelectedLanguage(Language.English)
        val viewModel = ShareViewModel(dua, language)
        assertEquals(englishVerse, viewModel.state.value.verse.text)
        language.switchTo(Language.Urdu)
        assertEquals(urduVerse, viewModel.state.value.verse.text)
        assertEquals(dua, viewModel.state.value.passage)
        language.switchTo(Language.Hindi)
        assertEquals(englishVerse, viewModel.state.value.verse.text)
    }

    @Test
    fun sharingSendsTheImageTitledWithWhereItsFrom() = runTest {
        val viewModel = ShareViewModel(dua, FakeSelectedLanguage())
        viewModel.effects.test {
            viewModel.dispatch(ShareIntent.ShareTapped)
            assertEquals(ShareEffect.ShareImage(title = "Quran (26:83)", fileName = "Quran (26-83)"), awaitItem())
        }
    }

    @Test
    fun savingSavesTheImage() = runTest {
        val viewModel = ShareViewModel(dua, FakeSelectedLanguage())
        viewModel.effects.test {
            viewModel.dispatch(ShareIntent.SaveTapped)
            assertEquals(ShareEffect.SaveImage(fileName = "Quran (26-83)"), awaitItem())
        }
    }

    @Test
    fun theImageIsNamedAfterWhereThePassageIsFrom() {
        fun name(reference: String) = SharePassage("", null, "", reference).imageFileName()
        assertEquals("Narrated by Muslim · Authentic", name("Narrated by Muslim · Authentic"))
        assertEquals("متفق علیہ · صحیح", name("متفق علیہ · صحیح"))
        assertEquals("Hisn al-Muslim 1-2", name("Hisn al-Muslim 1/2"))
        assertEquals("Muttaqi", name("  "))
    }
}
