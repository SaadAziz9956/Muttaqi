package com.muttaqi.shared.feature.quran.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "surahs")
internal data class SurahEntity(
    @PrimaryKey val number: Int,
    val name: String,
    val englishName: String,
    val englishNameTranslation: String,
    val revelationType: String,
    val numberOfAyahs: Int,
)

@Entity(
    tableName = "ayahs",
    indices = [Index(value = ["surahNumber", "numberInSurah"], unique = true)],
)
internal data class AyahEntity(
    /** In the whole Quran, 1 to 6,236 */
    @PrimaryKey val number: Int,
    val numberInSurah: Int,
    val surahNumber: Int,
    val arabicText: String,
    val transliteration: String?,
    val juz: Int,
    val page: Int,
    val hizbQuarter: Int,
)

@Entity(tableName = "ayah_translations", primaryKeys = ["ayahNumber", "language"])
internal data class AyahTranslationEntity(
    val ayahNumber: Int,
    /** A language code, e.g. "ur" */
    val language: String,
    /** The Quran API edition it came from, e.g. "ur.jalandhry" */
    val editionIdentifier: String,
    val text: String,
)

/** An ayah with its translation in one language, as the reader reads it */
internal data class AyahRow(
    val number: Int,
    val numberInSurah: Int,
    val surahNumber: Int,
    val arabicText: String,
    val transliteration: String?,
    val juz: Int,
    val page: Int,
    val hizbQuarter: Int,
    val translation: String?,
)

@Entity(tableName = "reading_progress")
internal data class ReadingProgressEntity(
    @PrimaryKey val surahNumber: Int,
    val lastAyahNumber: Int,
    /** Every ayah read in the surah, by number within it, comma-separated in order */
    val readAyahs: String,
    val completedAyahs: Int,
    val totalAyahs: Int,
    val lastReadAtEpochMillis: Long,
)

/** Tafsir as downloaded, one row where each passage starts; a passage covers the ayahs up to the next row */
@Entity(tableName = "tafsir", primaryKeys = ["surahNumber", "ayahNumber", "language"])
internal data class TafsirEntity(
    val surahNumber: Int,
    val ayahNumber: Int,
    val language: String,
    /** e.g. "Ibn Kathir (Abridged)" */
    val tafsirSource: String,
    /** Plain text, paragraphs separated by blank lines */
    val text: String,
)
