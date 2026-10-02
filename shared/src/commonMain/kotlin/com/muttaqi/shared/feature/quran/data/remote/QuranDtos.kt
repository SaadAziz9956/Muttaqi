package com.muttaqi.shared.feature.quran.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ApiResponse<T>(val code: Int, val status: String, val data: T)

@Serializable
internal data class SurahDto(
    val number: Int,
    val name: String,
    val englishName: String,
    val englishNameTranslation: String,
    val numberOfAyahs: Int,
    val revelationType: String,
)

@Serializable
internal data class SurahDetailDto(
    val number: Int,
    val name: String,
    val englishName: String,
    val englishNameTranslation: String,
    val numberOfAyahs: Int? = null,
    val revelationType: String,
    val ayahs: List<AyahDto>,
    val edition: EditionDto? = null,
)

@Serializable
internal data class AyahDto(
    val number: Int,
    val text: String,
    val numberInSurah: Int,
    val juz: Int,
    val page: Int,
    val hizbQuarter: Int,
)

@Serializable
internal data class EditionDto(
    val identifier: String,
    val language: String,
    val name: String,
    val englishName: String,
    val format: String,
    val type: String,
    val direction: String? = null,
)

@Serializable
internal data class FullQuranDto(val surahs: List<SurahDetailDto>, val edition: EditionDto)

@Serializable
internal data class TafsirResponseDto(val tafsirs: List<TafsirAyahDto>)

@Serializable
internal data class TafsirAyahDto(
    val id: Int,
    @SerialName("verse_key") val verseKey: String,
    val text: String,
)
