package com.muttaqi.shared.core.text

/**
 * Re-encodes standard Uthmani text for the KFGQPC Hafs font both apps use for Arabic, which assigns some marks
 * differently: its sukun is U+06E1 and its silent-letter circle is U+0652, so the standard U+06DF would draw as a
 * dotted-circle placeholder. Mapping checked against quran.com's QPC Hafs text for all 6,236 ayahs.
 */
fun String.kfgqpcEncoded(): String = buildString(length) {
    for (char in this@kfgqpcEncoded) {
        append(
            when (char.code) {
                0x0652 -> 'ۡ' // sukun
                0x06DF -> 'ْ' // silent-letter circle
                0x06E3 -> 'ۜ' // small seen (52:37)
                0x06EB -> '۬' // ishmam (12:11)
                else -> char
            },
        )
    }
}

/**
 * Marks the Quran font can't draw, since the Quran uses none: Arabic punctuation (drawn as dotted-circle
 * placeholders) and the ornate brackets (drawn as large ayah ornaments). Text from outside the Quran, such as duas,
 * sets these in the system font.
 */
val arabicMarksOutsideQuranFont: Set<Char> = setOf('،', '؛', '؟', '﴿', '﴾')

/** Urdu and other text in the Arabic script, which runs right to left and is set in Nastaliq */
fun String.isArabicScript(): Boolean = any { it.code in 0x0600..0x06FF }

/** Starts a translation cut from mid-sentence, e.g. "glory be to Allah", with a capital; the words stay as published */
fun String.sentenceCased(): String = replaceFirstChar { it.uppercaseChar() }

/** In quotation marks that face the right way for its script: Urdu opens with ” and closes with “, as it's printed */
fun String.quoted(): String = if (isArabicScript()) "”$this“" else "“$this”"
