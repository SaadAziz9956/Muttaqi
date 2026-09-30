package com.muttaqi.shared.core.text

/** Turns text into the form search compares, so a query matches however either side is written */
fun interface TextFolder {
    fun fold(text: String): String
}

/**
 * Case- and accent-insensitive folding that also understands Arabic: harakat, Quranic marks and tatweel are removed
 * and letter variants unified (آ أ إ ٱ → ا, ة → ه, ى → ي), so "الرحمن" finds "ٱلرَّحْمَٰن" and "rahman" finds
 * "Raḥmān". Full-width Latin letters and digits count as their ordinary forms.
 */
object SearchTextFolder : TextFolder {
    override fun fold(text: String): String {
        val decomposed = text.lowercase().decomposedCanonically()
        return buildString(decomposed.length) {
            for (char in decomposed) {
                val code = char.code
                when {
                    code in 0x0300..0x036F -> Unit                       // Latin accents, split off by decomposition
                    code in 0x064B..0x065F || code == 0x0670 -> Unit       // harakat and the dagger alef
                    code in 0x06D6..0x06ED || code == 0x0640 -> Unit       // Quranic marks and tatweel
                    code == 0x0622 || code == 0x0623 || code == 0x0625 || code == 0x0671 -> append('ا')
                    code == 0x0629 -> append('ه')
                    code == 0x0649 -> append('ي')
                    code in 0xFF01..0xFF5E -> append((code - 0xFEE0).toChar())
                    else -> append(char)
                }
            }
        }
    }
}

/** The text in canonical decomposition (NFD), which splits accented letters into the letter and its accent */
internal expect fun String.decomposedCanonically(): String
