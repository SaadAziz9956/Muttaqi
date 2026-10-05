package com.muttaqi.shared.core.text

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ArabicTextTest {
    @Test
    fun aLineIsArabicScriptWhenItsFirstLetterIs() {
        assertTrue("(تھوڑی دیر کے بعد) ان میں سے ایک عورت".isArabicScript())
        assertTrue("رواه البخاري · صحيح".isArabicScript())
        assertFalse("Allah (تعالى) said".isArabicScript())
        assertFalse("Quran (3:139)".isArabicScript())
        assertFalse("".isArabicScript())
    }
}
