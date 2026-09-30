package com.muttaqi.shared.core.text

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SearchTextFolderTest {
    private val folder: TextFolder = SearchTextFolder

    @Test
    fun arabicMatchesWithOrWithoutHarakat() {
        assertEquals(folder.fold("الرحمن"), folder.fold("ٱلرَّحْمَٰنِ"))
    }

    @Test
    fun alefVariantsTaMarbutaAndAlefMaqsuraAreUnified() {
        assertEquals("ا ا ا ا", folder.fold("آ أ إ ٱ"))
        assertEquals(folder.fold("رحمه"), folder.fold("رحمة"))
        assertEquals(folder.fold("علي"), folder.fold("على"))
    }

    @Test
    fun tatweelAndQuranicMarksAreRemoved() {
        assertEquals(folder.fold("الله"), folder.fold("اللـــه"))
        assertEquals(folder.fold("قدير"), folder.fold("قَدِيرٌۖ"))
    }

    @Test
    fun transliterationMatchesWithoutAccentsOrCase() {
        assertEquals("ar-rahman", folder.fold("Ar-Raḥmān"))
        assertTrue(folder.fold("Al-Quddūs").contains(folder.fold("quddus")))
    }

    @Test
    fun fullWidthLettersAndDigitsCountAsOrdinary() {
        assertEquals("zakah 18", folder.fold("ｚａｋａｈ １８"))
    }
}
