package com.muttaqi.shared.feature.quran

import com.muttaqi.shared.core.content.BundledContentSource
import com.muttaqi.shared.core.content.ContentJson
import com.muttaqi.shared.feature.onboarding.presentation.OnboardingVerses
import com.muttaqi.shared.feature.quran.data.repository.BundledMushaf
import com.muttaqi.shared.feature.quran.presentation.MushafSamples
import com.muttaqi.shared.testing.TestDispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.int
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BundledQuranPassagesContentTest {
    private val content = BundledContentSource { File("../content/data/$it").readText() }

    private data class Passage(val file: String, val surah: Int, val first: Int, val last: Int, val arabic: String)

    @Test
    fun everyBundledQuranPassageIsTheKingFahdTextCharacterForCharacter() = runTest {
        val mushaf = BundledMushaf(content, TestDispatchers(StandardTestDispatcher(testScheduler)))
        val passages = listOf("Duas.json", "Emotions.json", "Explore.json").flatMap { file ->
            mutableListOf<Passage>().also { collect(file, ContentJson.parseToJsonElement(content.read(file)), it) }
        }
        assertTrue(passages.size > 500)
        val wrong = passages.filterNot { passage ->
            val parts = mutableListOf<String>()
            for (ayah in passage.first..passage.last) {
                val text = checkNotNull(mushaf.ayah(passage.surah, ayah)) { "${passage.surah}:$ayah" }
                parts += if (ayah == passage.last) text.words else text.words + " " + text.mark
            }
            passage.arabic in parts.joinToString(" ")
        }
        assertEquals(emptyList(), wrong.map { "${it.file} ${it.surah}:${it.first}-${it.last}" })
    }

    @Test
    fun theQuranQuotedInTheAppIsTheKingFahdText() = runTest {
        val mushaf = BundledMushaf(content, TestDispatchers(StandardTestDispatcher(testScheduler)))
        assertEquals(mushaf.ayah(1, 1)?.words, MushafSamples.BISMILLAH)
        assertEquals(OnboardingVerses.basmala.arabic, mushaf.ayah(1, 1)?.words)
        assertTrue(OnboardingVerses.lovesThePure.arabic in mushaf.ayah(9, 108)!!.words)
    }

    private fun collect(file: String, element: JsonElement, into: MutableList<Passage>) {
        when (element) {
            is JsonObject -> {
                val arabic = (element["arabic"] as? JsonPrimitive)?.takeIf { it.isString }?.content
                val surah = (element["surah"] as? JsonPrimitive)?.int
                val ayah = (element["ayah"] as? JsonPrimitive)?.int
                val span = (element["reference"] as? JsonPrimitive)?.content?.let { SPAN.find(it) }
                when {
                    arabic != null && surah != null && ayah != null -> into += Passage(file, surah, ayah, ayah, arabic)
                    arabic != null && span != null -> {
                        val (s, a, b) = span.destructured
                        into += Passage(file, s.toInt(), a.toInt(), b.ifEmpty { a }.toInt(), arabic)
                    }
                }
                element.values.forEach { collect(file, it, into) }
            }
            is JsonArray -> element.forEach { collect(file, it, into) }
            else -> Unit
        }
    }

    private companion object {
        val SPAN = Regex("""^(\d+):(\d+)(?:\s*[-–]\s*(\d+))?$""")
    }
}
