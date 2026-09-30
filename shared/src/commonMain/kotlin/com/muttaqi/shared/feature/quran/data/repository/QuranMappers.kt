package com.muttaqi.shared.feature.quran.data.repository

import com.muttaqi.shared.feature.quran.data.local.AyahEntity
import com.muttaqi.shared.feature.quran.data.local.AyahRow
import com.muttaqi.shared.feature.quran.data.local.ReadingProgressEntity
import com.muttaqi.shared.feature.quran.data.local.SurahEntity
import com.muttaqi.shared.feature.quran.data.remote.AyahDto
import com.muttaqi.shared.feature.quran.data.remote.SurahDetailDto
import com.muttaqi.shared.feature.quran.domain.model.Ayah
import com.muttaqi.shared.feature.quran.domain.model.Surah
import com.muttaqi.shared.feature.quran.domain.model.SurahProgress
import kotlin.time.Instant

internal fun SurahDetailDto.toEntity() = SurahEntity(
    number = number,
    name = name,
    englishName = englishName,
    englishNameTranslation = englishNameTranslation,
    revelationType = revelationType,
    numberOfAyahs = numberOfAyahs ?: ayahs.size,
)

internal fun AyahDto.toEntity(surahNumber: Int, transliteration: String?) = AyahEntity(
    number = number,
    numberInSurah = numberInSurah,
    surahNumber = surahNumber,
    arabicText = text,
    transliteration = transliteration,
    juz = juz,
    page = page,
    hizbQuarter = hizbQuarter,
)

internal fun SurahEntity.toDomain() = Surah(
    number = number,
    name = name,
    englishName = englishName,
    englishNameTranslation = englishNameTranslation,
    revelationType = revelationType,
    numberOfAyahs = numberOfAyahs,
)

internal fun AyahRow.toDomain() = Ayah(
    number = number,
    numberInSurah = numberInSurah,
    surahNumber = surahNumber,
    arabicText = arabicText,
    transliteration = transliteration,
    translation = translation,
    juz = juz,
    page = page,
    hizbQuarter = hizbQuarter,
)

internal fun ReadingProgressEntity.toDomain() = SurahProgress(
    surahNumber = surahNumber,
    lastAyahNumber = lastAyahNumber,
    readAyahs = readAyahs.split(',').mapNotNull { it.trim().toIntOrNull() }.toSet(),
    completedAyahs = completedAyahs,
    totalAyahs = totalAyahs,
    lastReadAt = Instant.fromEpochMilliseconds(lastReadAtEpochMillis),
)

internal fun SurahProgress.toEntity() = ReadingProgressEntity(
    surahNumber = surahNumber,
    lastAyahNumber = lastAyahNumber,
    readAyahs = readAyahs.sorted().joinToString(","),
    completedAyahs = completedAyahs,
    totalAyahs = totalAyahs,
    lastReadAtEpochMillis = lastReadAt.toEpochMilliseconds(),
)
