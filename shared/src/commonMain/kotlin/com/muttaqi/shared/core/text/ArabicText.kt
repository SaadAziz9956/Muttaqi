package com.muttaqi.shared.core.text

fun String.kfgqpcEncoded(): String = buildString(length) {
    for (char in this@kfgqpcEncoded) {
        append(
            when (char.code) {
                0x0652 -> 'ۡ'
                0x06DF -> 'ْ'
                0x06E3 -> 'ۜ'
                0x06EB -> '۬'
                else -> char
            },
        )
    }
}

val arabicMarksOutsideQuranFont: Set<Char> = setOf('،', '؛', '؟', '﴿', '﴾')

fun String.isArabicScript(): Boolean = any { it.code in 0x0600..0x06FF }

fun String.sentenceCased(): String = replaceFirstChar { it.uppercaseChar() }

fun String.quoted(): String = if (isArabicScript()) "”$this“" else "“$this”"
