package com.muttaqi.shared.feature.topics

import com.muttaqi.shared.core.model.Language
import com.muttaqi.shared.core.text.SearchTextFolder
import com.muttaqi.shared.feature.topics.domain.model.TopicChips
import com.muttaqi.shared.feature.topics.domain.usecase.BuildExploreSearchIndex
import com.muttaqi.shared.feature.topics.domain.usecase.GetHadithOfTheDay
import com.muttaqi.shared.feature.topics.domain.usecase.GetTopicOfTheDay
import com.muttaqi.shared.feature.topics.domain.usecase.GetTopicPageTopics
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class TopicUseCasesTest {
    private fun TestScope.explore() = TopicsTestData.exploreRepository(StandardTestDispatcher(testScheduler))

    private suspend fun TestScope.search(query: String): List<String> {
        val index = BuildExploreSearchIndex(SearchTextFolder)(explore().groups(Language.English))
        return index.search(query).map { it.topic.id }
    }

    @Test
    fun topicsWhoseNamesMatchComeBeforeThoseWhoseTextsDo() = runTest {
        assertEquals(listOf("prayer", "charity-zakat"), search("prayer"))
    }

    @Test
    fun keywordsAndGroupNamesMatchAsNames() = runTest {
        assertEquals(listOf("charity-zakat"), search("ZAKAH"))
        assertEquals(listOf("lying"), search("sins"))
        assertEquals(listOf("fasting"), search("roza"))
    }

    @Test
    fun everyWordMustMatchInTheNamesOrTheTexts() = runTest {
        assertEquals(listOf("charity-zakat"), search("zakah establish"))
        assertEquals(emptyList(), search("zakah ramadan"))
        assertEquals(emptyList(), search("   "))
    }

    @Test
    fun duasAreSearchedByTranslationAndTransliteration() = runTest {
        assertEquals(listOf("fasting"), search("subhaanal"))
        assertEquals(listOf("fasting"), search("how perfect"))
    }

    @Test
    fun theTopicPageMovesBetweenEveryEmotionOrOneExploreGroup() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val topics = GetTopicPageTopics(TopicsTestData.emotionRepository(dispatcher), TopicsTestData.exploreRepository(dispatcher))
        assertEquals(listOf("angry", "bored", "happy"), topics(TopicChips.Emotions, "bored", Language.English).map { it.id })
        assertEquals(listOf("fasting", "charity-zakat", "prayer"), topics(TopicChips.ExploreGroup, "prayer", Language.English).map { it.id })
        assertEquals(listOf("lying"), topics(TopicChips.ExploreGroup, "lying", Language.English).map { it.id })
        assertTrue(topics(TopicChips.ExploreGroup, "missing", Language.English).isEmpty())
    }

    @Test
    fun theTopicOfTheDayStaysAllDayAndMovesAtMidnight() = runTest {
        val pick = GetTopicOfTheDay(explore())
        val today = LocalDate(2026, 9, 30)
        assertEquals("lying", pick(today, Language.English)?.id)
        assertEquals("lying", pick(today, Language.Urdu)?.id)
        assertEquals("prayer", pick(LocalDate(2026, 10, 1), Language.English)?.id)
    }

    @Test
    fun theHadithOfTheDayIsShortAndFromEverydayTopics() = runTest {
        val pick = GetHadithOfTheDay(explore())
        val picks = (0 until 10).map { pick(LocalDate(2026, 9, 1 + it), Language.English)?.translation }.toSet()
        assertEquals(setOf("Whoever fasts Ramadan", "بَ".repeat(420)), picks)
        assertNotEquals(pick(LocalDate(2026, 9, 30), Language.English), pick(LocalDate(2026, 10, 1), Language.English))
    }
}
