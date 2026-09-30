package com.muttaqi.android.feature.dua

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import com.muttaqi.android.testing.PHONE
import com.muttaqi.android.testing.captureLightAndDark
import com.muttaqi.shared.core.quote.DisplayedQuote
import com.muttaqi.shared.feature.dua.domain.model.DuaCategory
import com.muttaqi.shared.feature.dua.domain.model.DuaChapter
import com.muttaqi.shared.feature.dua.domain.model.DuaEntry
import com.muttaqi.shared.feature.dua.presentation.chapter.DuaChapterState
import com.muttaqi.shared.feature.dua.presentation.list.DuaListState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(application = Application::class, sdk = [35], qualifiers = PHONE)
class DuaScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    private val entry = DuaEntry(
        id = "hisn-39",
        arabic = "أَشْهَدُ أَنْ لاَ إِلَهَ إِلاَّ اللَّهُ وَحْدَهُ لاَ شَرِيكَ لَهُ، وَأَشْهَدُ أَنَّ مُحَمَّداً عَبْدُهُ وَرَسُولُهُ",
        transliteration = "Ashhadu an la ilaha illal-lahu wahdahu la shareeka lah",
        translation = "I bear witness that none has the right to be worshipped except Allah, alone without partner.",
        repeatCount = 3,
        source = "Muslim",
        reference = "رواه مسلم 1/209",
        translationCredit = "Hisn al-Muslim (hisnmuslim.com)",
    )
    private val chapter = DuaChapter("hisn-9", "What to say upon completing ablution", "الذكر بعد الفراغ من الوضوء", listOf(entry, entry.copy(id = "hisn-40", repeatCount = 1)))
    private val categories = listOf(
        DuaCategory("rabbana", "Rabbana Duas", List(38) { chapter }),
        DuaCategory("morning-evening", "Morning & Evening", listOf(chapter)),
        DuaCategory("sleep", "Sleep & Waking", listOf(chapter)),
        DuaCategory("prayer", "Prayer & Purification", listOf(chapter, chapter)),
    )

    @Test
    fun list() = compose.captureLightAndDark("dua_list") {
        DuaListScreen(
            DuaListState(
                isLoading = false,
                header = DisplayedQuote("And your Lord says, \"Call upon Me; I will respond to you.\"", "Quran (40:60)"),
                categories = categories,
            ),
            onIntent = {},
        )
    }

    @Test
    fun chapter() = compose.captureLightAndDark("dua_chapter") {
        DuaChapterScreen(DuaChapterState(isLoading = false, chapter = chapter, translationCredits = listOf(entry.translationCredit)), onIntent = {}, onBack = {})
    }
}
