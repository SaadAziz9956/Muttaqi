package com.muttaqi.shared.core.text

val arabicMarksOutsideQuranFont: Set<Char> = setOf('،', '؛', '؟', '﴿', '﴾')

fun String.isArabicScript(): Boolean = firstOrNull { it.isLetter() }?.isArabicLetter() ?: false

private fun Char.isArabicLetter(): Boolean = code in 0x0600..0x06FF || code in 0x0750..0x077F || code in 0xFB50..0xFDFF || code in 0xFE70..0xFEFF

fun String.quoted(): String = if (isArabicScript()) "”$this“" else "“$this”"
