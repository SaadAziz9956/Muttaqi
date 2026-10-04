package com.muttaqi.shared.feature.quran

import com.muttaqi.shared.core.content.BundledContentSource
import com.muttaqi.shared.feature.quran.data.repository.BundledMushaf
import com.muttaqi.shared.feature.quran.presentation.MushafSamples
import com.muttaqi.shared.testing.TestDispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import java.io.File
import java.security.MessageDigest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BundledMushafContentTest {
    private val file = File("../content/data/${BundledMushaf.FILE}")
    private val content = BundledContentSource { File("../content/data/$it").readText() }

    @Test
    fun theBundledTextIsTheFileTheKingFahdComplexPublished() {
        val digest = MessageDigest.getInstance("SHA-256").digest(file.readBytes()).joinToString("") { "%02x".format(it) }
        assertEquals("d2960b3217962e7e4252abdcece67bea3d6b48271e4cd3af45bbbb2dd5c872ca", digest)
    }

    @Test
    fun everyAyahIsThereWithItsOwnMark() = runTest {
        val mushaf = BundledMushaf(content, TestDispatchers(StandardTestDispatcher(testScheduler)))
        val counts = mapOf(1 to 7, 2 to 286, 9 to 129, 18 to 110, 114 to 6)
        for ((surah, last) in counts) {
            for (ayah in 1..last) {
                val text = assertNotNull(mushaf.ayah(surah, ayah), "$surah:$ayah")
                assertEquals((0xFC00 + ayah - 1).toChar().toString(), text.mark, "$surah:$ayah")
                assertTrue(text.words.isNotBlank() && text.words.last() != ' ' && text.words.last() != ' ', "$surah:$ayah")
            }
            assertNull(mushaf.ayah(surah, last + 1))
        }
        assertEquals(1, mushaf.ayah(1, 1)?.page)
        assertEquals(604, mushaf.ayah(114, 6)?.page)
        assertEquals(30, mushaf.ayah(114, 6)?.juz)
    }

    @Test
    fun theSamplesShownInTheAppAreCopiedFromTheMushaf() = runTest {
        val mushaf = BundledMushaf(content, TestDispatchers(StandardTestDispatcher(testScheduler)))
        val opening = mushaf.ayah(1, 1)!!
        val praise = mushaf.ayah(1, 2)!!
        assertEquals(opening.words, MushafSamples.BISMILLAH)
        assertEquals(opening.words.split(" ").take(2).joinToString(" "), MushafSamples.BISMILLAH_OPENING_WORDS)
        assertEquals(praise.words.split(" ").take(2).joinToString(" "), MushafSamples.ALHAMDU_OPENING_WORDS)
        assertEquals("${opening.words}\u00A0${opening.mark} ${praise.words}\u00A0${praise.mark}", MushafSamples.FATIHA_OPENING)
        assertEquals(mushaf.ayah(9, 108)!!.words.split(" ").takeLast(3).joinToString(" "), MushafSamples.LOVES_THE_PURE)
    }
}
