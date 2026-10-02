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
    val language: String,
    val editionIdentifier: String,
    val text: String,
)

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
    val readAyahs: String,
    val completedAyahs: Int,
    val totalAyahs: Int,
    val lastReadAtEpochMillis: Long,
)

@Entity(tableName = "tafsir", primaryKeys = ["surahNumber", "ayahNumber", "language"])
internal data class TafsirEntity(
    val surahNumber: Int,
    val ayahNumber: Int,
    val language: String,
    val tafsirSource: String,
    val text: String,
)
