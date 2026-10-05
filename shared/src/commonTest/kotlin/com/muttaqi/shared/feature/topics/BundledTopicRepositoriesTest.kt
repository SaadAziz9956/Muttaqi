package com.muttaqi.shared.feature.topics

import com.muttaqi.shared.core.model.Language
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class BundledTopicRepositoriesTest {
    @Test
    fun emotionsKeepTheirOrderAndJoinTheirDuasByNumber() = runTest {
        val emotions = TopicsTestData.emotionRepository(StandardTestDispatcher(testScheduler)).emotions(Language.English)
        assertEquals(listOf("angry", "bored", "happy"), emotions.map { it.id })
        assertEquals(listOf("hisn-1"), emotions.first().duas.map { it.id })
        assertEquals("Praise is to Allah", emotions.first().duas.single().translation)
    }

    @Test
    fun urduIsShownWherePublishedAndCreditedToWhoeverTranslatedIt() = runTest {
        val angry = TopicsTestData.emotionRepository(StandardTestDispatcher(testScheduler)).emotions(Language.Urdu).first()
        assertEquals("اور غصے کو روکتے", angry.verses[0].translation)
        assertEquals("Fateh Muhammad Jalandhry", angry.verses[0].credit)
        assertEquals("Take what is given freely", angry.verses[1].translation)
        assertEquals("Saheeh International", angry.verses[1].credit)
        assertEquals("غصہ مت کیا کرو", angry.hadith.single().translation)
        assertEquals("رواه البخاري · صحيح", angry.hadith.single().source)
        assertEquals("HadeethEnc.com", angry.hadith.single().credit)
        assertEquals("سب تعریف اللہ کے لیے", angry.duas.single().translation)
    }

    @Test
    fun aHadithsArabicIsTheTextItsTranslationFollows() = runTest {
        val repository = TopicsTestData.emotionRepository(StandardTestDispatcher(testScheduler))
        assertEquals("«لَا تَغْضَبْ»", repository.emotions(Language.English).first().hadith.single().arabic)
        assertEquals("«لا تغضب»", repository.emotions(Language.Urdu).first().hadith.single().arabic)
        assertEquals("«لَا تَغْضَبْ»", repository.emotions(Language.Hindi).first().hadith.single().arabic)
    }

    @Test
    fun englishVersesAreCreditedToSaheehInternational() = runTest {
        val verse = TopicsTestData.emotionRepository(StandardTestDispatcher(testScheduler)).emotions(Language.English).first().verses.first()
        assertEquals("Saheeh International", verse.credit)
        assertEquals("Quran (3:134)", verse.source)
    }

    @Test
    fun theHeadersAreTheVersesUnderEachTitle() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val emotions = TopicsTestData.emotionRepository(dispatcher).header()
        assertEquals("Quran (65:3)", emotions.source)
        assertEquals("اور جو خدا پر بھروسہ رکھے گا تو وہ اس کو کفایت کرے گا۔", emotions.text(Language.Urdu))
        val explore = TopicsTestData.exploreRepository(dispatcher).header()
        assertEquals("Quran (29:69)", explore.source)
        assertEquals("And those who strive for Us - We will surely guide them to Our ways.", explore.text(Language.Hindi))
    }

    @Test
    fun exploreTopicsSitInTheirGroupsWithIconsAndKeywords() = runTest {
        val groups = TopicsTestData.exploreRepository(StandardTestDispatcher(testScheduler)).groups(Language.English)
        assertEquals(listOf("worship", "sins"), groups.map { it.id })
        assertEquals(listOf("fasting", "charity-zakat", "prayer"), groups.first().topics.map { it.id })
        val charity = groups.first().topics[1]
        assertEquals("money-send-linear", charity.icon)
        assertEquals(listOf("zakah", "sadaqah"), charity.keywords)
        assertEquals(listOf("hisn-3"), groups.first().topics.first().duas.map { it.id })
    }
}
