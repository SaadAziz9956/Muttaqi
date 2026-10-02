package com.muttaqi.shared.feature.topics

import com.muttaqi.shared.core.content.BundledContentSource
import com.muttaqi.shared.core.model.Language
import com.muttaqi.shared.core.text.SearchTextFolder
import com.muttaqi.shared.feature.dua.data.repository.BundledDuaRepository
import com.muttaqi.shared.feature.dua.domain.usecase.GetDuaEntriesById
import com.muttaqi.shared.feature.topics.data.repository.BundledEmotionRepository
import com.muttaqi.shared.feature.topics.data.repository.BundledExploreRepository
import com.muttaqi.shared.feature.topics.domain.usecase.BuildExploreSearchIndex
import com.muttaqi.shared.feature.topics.domain.usecase.GetHadithOfTheDay
import com.muttaqi.shared.feature.topics.domain.usecase.GetTopicOfTheDay
import com.muttaqi.shared.testing.TestDispatchers
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BundledTopicsContentTest {
    private val content = BundledContentSource { File("../content/data/$it").readText() }

    private fun duas(dispatcher: CoroutineDispatcher) = GetDuaEntriesById(BundledDuaRepository(content, TestDispatchers(dispatcher)))

    private fun listedDuas(fileName: String, topics: (JsonObject) -> List<JsonElement>) =
        topics(Json.parseToJsonElement(content.read(fileName)).jsonObject).associate { topic ->
            topic.jsonObject.getValue("id").jsonPrimitive.content to topic.jsonObject.getValue("duas").jsonArray.map { it.jsonPrimitive.int }
        }

    @Test
    fun theBundledEmotionsDecodeWithEveryDuaFound() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val repository = BundledEmotionRepository(content, TestDispatchers(dispatcher), duas(dispatcher))
        val emotions = repository.emotions(Language.Urdu)
        assertEquals(18, emotions.size)
        assertEquals("angry", emotions.first().id)
        assertEquals("Quran (65:3)", repository.header().source)
        assertTrue(emotions.all { it.verses.isNotEmpty() && it.hadith.isNotEmpty() })

        val listed = listedDuas("Emotions.json") { it.getValue("emotions").jsonArray }
        emotions.forEach { emotion -> assertEquals(listed.getValue(emotion.id).map { "hisn-$it" }, emotion.duas.map { it.id }) }
        assertTrue(emotions.flatMap { it.verses }.all { it.credit == "Fateh Muhammad Jalandhry" })
    }

    @Test
    fun theBundledExploreDecodesWithEveryDuaFound() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val repository = BundledExploreRepository(content, TestDispatchers(dispatcher), duas(dispatcher))
        val groups = repository.groups(Language.English)
        assertEquals(listOf("faith", "worship", "character", "family", "society", "daily-life", "sins"), groups.map { it.id })
        assertEquals(88, groups.sumOf { it.topics.size })
        assertEquals("Quran (29:69)", repository.header().source)

        val listed = listedDuas("Explore.json") { root -> root.getValue("groups").jsonArray.flatMap { it.jsonObject.getValue("topics").jsonArray } }
        groups.flatMap { it.topics }.forEach { topic -> assertEquals(listed.getValue(topic.id).map { "hisn-$it" }, topic.duas.map { it.id }) }

        val results = BuildExploreSearchIndex(SearchTextFolder)(groups).search("zakah")
        assertEquals("charity-zakat", results.first().topic.id)
        assertTrue(results.size > 1)
    }

    @Test
    fun homesPicksAreTheOnesTheIosAppShowedOnTheSameDays() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val repository = BundledExploreRepository(content, TestDispatchers(dispatcher), duas(dispatcher))
        val topic = GetTopicOfTheDay(repository)
        val hadith = GetHadithOfTheDay(repository)
        val days = mapOf(
            LocalDate(2026, 9, 30) to Triple("health-sickness", "‘Uthmān (may Allah be pleased with him) reported that he hea", "ابو ہریرہ رضی اللہ عنہ سے روایت ہے کہ اللہ کے نبی ﷺ نے فرمای"),
            LocalDate(2026, 10, 1) to Triple("theft", "Abu Hurayrah (may Allah be pleased with him) reported that t", "ابو ہریرہ رضی اللہ عنہ سے روایت ہے کہ اللہ کے رسول صلی اللہ "),
            LocalDate(2027, 3, 15) to Triple("names-of-allah", "Anas ibn Mālik (may Allah be pleased with him) reported: The", "ابو ہریرہ رضی اللہ عنہ سے روایت ہے کہ اللہ کے رسول صلی اللہ "),
        )
        days.forEach { (date, expected) ->
            assertEquals(expected.first, topic(date, Language.English)?.id)
            assertEquals(expected.second, hadith(date, Language.English)?.translation?.take(60))
            assertEquals(expected.third, hadith(date, Language.Urdu)?.translation?.take(60))
        }
    }
}
