package com.muttaqi.shared.feature.topics

import com.muttaqi.shared.feature.dua.domain.model.DuaEntry
import com.muttaqi.shared.feature.topics.domain.model.Emotion
import com.muttaqi.shared.feature.topics.domain.model.HadithPassage
import com.muttaqi.shared.feature.topics.domain.model.QuranPassage
import com.muttaqi.shared.feature.topics.presentation.page.TopicPageMutation
import com.muttaqi.shared.feature.topics.presentation.page.TopicPageReducer
import com.muttaqi.shared.feature.topics.presentation.page.TopicPageState
import com.muttaqi.shared.feature.topics.presentation.page.TopicSection
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TopicPageReducerTest {
    private val verse = QuranPassage("3:134", "وَٱلْكَٰظِمِينَ ٱلْغَيْظَ", "and who restrain anger", "Saheeh International")
    private val urduVerse = QuranPassage("7:199", "خُذِ ٱلْعَفْوَ", "عفو اختیار کرو", "Fateh Muhammad Jalandhry")
    private val hadith = HadithPassage("«لَا تَغْضَبْ»", "Do not get angry.", "Narrated by Al-Bukhāri", "Authentic", "HadeethEnc.com")
    private val dua = DuaEntry("hisn-1", "الْحَمْدُ للهِ", "Alhamdu lillaah", "Praise is to Allah", 1, "Bukhari", "", "Hisn al-Muslim (hisnmuslim.com)")

    private val angry = Emotion("angry", "Angry", listOf(verse, urduVerse, verse.copy(reference = "42:37")), listOf(hadith), listOf(dua))
    private val bored = Emotion("bored", "Bored", listOf(verse), listOf(hadith), emptyList())
    private val happy = Emotion("happy", "Happy", emptyList(), listOf(hadith), listOf(dua))

    private fun loaded(opened: String) =
        TopicPageReducer.reduce(TopicPageState(selectedId = opened), TopicPageMutation.Loaded(listOf(angry, bored, happy)))

    @Test
    fun opensAtTheTopicTappedWithOnlyTheKindsOfTextItHas() {
        val state = loaded("bored")
        assertEquals("bored", state.selectedId)
        assertEquals(listOf(TopicSection.Quran, TopicSection.Hadith), state.sections)
        assertEquals(TopicSection.Quran, state.section)
        assertEquals(listOf("Quran (3:134)"), state.passages.map { it.source })
    }

    @Test
    fun aTopicThatsGoneFallsBackToTheFirst() {
        assertEquals("angry", loaded("missing").selectedId)
    }

    @Test
    fun movingToAnotherTopicKeepsTheKindOfTextUnlessItHasNone() {
        val onDua = TopicPageReducer.reduce(loaded("angry"), TopicPageMutation.SectionSelected(TopicSection.Dua))
        assertEquals(listOf("hisn-1"), onDua.passages.map { it.id })
        assertEquals("Alhamdu lillaah", onDua.passages.single().transliteration)

        assertEquals(TopicSection.Dua, TopicPageReducer.reduce(onDua, TopicPageMutation.TopicSelected("happy")).section)
        val boredState = TopicPageReducer.reduce(onDua, TopicPageMutation.TopicSelected("bored"))
        assertEquals(TopicSection.Quran, boredState.section)
        assertEquals("bored", boredState.selectedId)
        assertEquals(TopicSection.Hadith, TopicPageReducer.reduce(boredState, TopicPageMutation.TopicSelected("happy")).section)
    }

    @Test
    fun passagesCarryWhereTheyreFromAndCreditsAreEachTranslatorOnce() {
        val quran = loaded("angry")
        assertEquals(listOf("quran-3:134", "quran-7:199", "quran-42:37"), quran.passages.map { it.id })
        assertEquals(listOf("Saheeh International", "Fateh Muhammad Jalandhry"), quran.translationCredits)
        assertEquals(null, quran.passages.first().transliteration)

        val hadithState = TopicPageReducer.reduce(quran, TopicPageMutation.SectionSelected(TopicSection.Hadith))
        assertEquals("Narrated by Al-Bukhāri · Authentic", hadithState.passages.single().source)
        assertEquals(listOf("HadeethEnc.com"), hadithState.translationCredits)
    }

    @Test
    fun reloadingInAnotherLanguageStaysOnTheTopicAndSection() {
        val onHadith = TopicPageReducer.reduce(
            TopicPageReducer.reduce(loaded("angry"), TopicPageMutation.TopicSelected("bored")),
            TopicPageMutation.SectionSelected(TopicSection.Hadith),
        )
        val urduHadith = hadith.copy(translation = "غصہ مت کیا کرو")
        val reloaded = TopicPageReducer.reduce(onHadith, TopicPageMutation.Loaded(listOf(angry, bored.copy(hadith = listOf(urduHadith)), happy)))
        assertEquals("bored", reloaded.selectedId)
        assertEquals(TopicSection.Hadith, reloaded.section)
        assertEquals("غصہ مت کیا کرو", reloaded.passages.single().translation)
    }

    @Test
    fun reducerIsPureAndEmptyTopicsShowNothing() {
        val before = TopicPageState(selectedId = "angry")
        val after = TopicPageReducer.reduce(before, TopicPageMutation.Loaded(emptyList()))
        assertEquals(TopicPageState(selectedId = "angry"), before)
        assertTrue(after.passages.isEmpty() && after.sections.isEmpty() && !after.isLoading)
        assertTrue(!TopicPageReducer.reduce(before, TopicPageMutation.LoadFailed).isLoading)
    }
}
