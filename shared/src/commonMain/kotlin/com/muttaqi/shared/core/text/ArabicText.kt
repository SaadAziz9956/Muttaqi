package com.muttaqi.shared.core.text

val arabicMarksOutsideQuranFont: Set<Char> = setOf('،', '؛', '؟', '﴿', '﴾')

fun String.isArabicScript(): Boolean = any { it.code in 0x0600..0x06FF }

fun String.sentenceCased(): String = replaceFirstChar { it.uppercaseChar() }

fun String.quoted(): String = if (isArabicScript()) "”$this“" else "“$this”"
