package com.muttaqi.shared.feature.quran.domain.model

import kotlin.time.Instant

/** How far the reader has got in one surah */
data class SurahProgress(
    val surahNumber: Int,
    /** The first ayah on screen when they last stopped, by number within the surah */
    val lastAyahNumber: Int,
    /** Every ayah of the surah that has been on screen while reading, by number within the surah */
    val readAyahs: Set<Int>,
    /** How many distinct ayahs have been read; records from before [readAyahs] was kept may count more than it holds */
    val completedAyahs: Int,
    val totalAyahs: Int,
    val lastReadAt: Instant,
)

/** Where the reader left off, with the surah's names to show it */
data class ReadingProgress(
    val surahNumber: Int,
    val surahName: String,
    val surahEnglishName: String,
    val lastAyahNumber: Int,
    val lastReadAt: Instant,
)

/** Distinct ayahs read across the whole Quran, and how many ayahs the Quran has */
data class QuranCompletion(val ayahsRead: Int, val totalAyahs: Int) {
    /** From 0 to 1 */
    val fraction: Double get() = if (totalAyahs > 0) (ayahsRead.toDouble() / totalAyahs).coerceAtMost(1.0) else 0.0
    val ayahsLeft: Int get() = (totalAyahs - ayahsRead).coerceAtLeast(0)
}

/**
 * A surah's reading record as the iOS app kept it in SwiftData before the move to shared code, handed over once so
 * no one loses their place
 */
data class StoredReadingProgress(
    val surahNumber: Int,
    val lastAyahNumber: Int,
    val readAyahs: List<Int>,
    val completedAyahs: Int,
    val totalAyahs: Int,
    val lastReadAtEpochMillis: Long,
)
