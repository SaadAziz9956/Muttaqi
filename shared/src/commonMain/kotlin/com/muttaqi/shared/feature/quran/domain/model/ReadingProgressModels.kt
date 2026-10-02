package com.muttaqi.shared.feature.quran.domain.model

import kotlin.time.Instant

data class SurahProgress(
    val surahNumber: Int,
    val lastAyahNumber: Int,
    val readAyahs: Set<Int>,
    val completedAyahs: Int,
    val totalAyahs: Int,
    val lastReadAt: Instant,
)

data class ReadingProgress(
    val surahNumber: Int,
    val surahName: String,
    val surahEnglishName: String,
    val lastAyahNumber: Int,
    val lastReadAt: Instant,
)

data class QuranCompletion(val ayahsRead: Int, val totalAyahs: Int) {
    val fraction: Double get() = if (totalAyahs > 0) (ayahsRead.toDouble() / totalAyahs).coerceAtMost(1.0) else 0.0
    val ayahsLeft: Int get() = (totalAyahs - ayahsRead).coerceAtLeast(0)
}

data class StoredReadingProgress(
    val surahNumber: Int,
    val lastAyahNumber: Int,
    val readAyahs: List<Int>,
    val completedAyahs: Int,
    val totalAyahs: Int,
    val lastReadAtEpochMillis: Long,
)
