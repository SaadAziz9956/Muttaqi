package com.muttaqi.shared.feature.quran.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Every Quran API response: `{"code": 200, "status": "OK", "data": …}` */
@Serializable
internal data class ApiResponse<T>(val code: Int, val status: String, val data: T)

/** GET /surah: one surah's details */
@Serializable
internal data class SurahDto(
    val number: Int,
    val name: String,
    val englishName: String,
    val englishNameTranslation: String,
    val numberOfAyahs: Int,
    val revelationType: String,
)

/** A surah with its ayahs in one edition, alone (GET /surah/{n}/{edition}) or in the whole Quran */
@Serializable
internal data class SurahDetailDto(
    val number: Int,
    val name: String,
    val englishName: String,
    val englishNameTranslation: String,
    /** Left out by some mirrors of the API in the whole-Quran response, so it falls back to the ayahs given */
    val numberOfAyahs: Int? = null,
    val revelationType: String,
    val ayahs: List<AyahDto>,
    val edition: EditionDto? = null,
)

@Serializable
internal data class AyahDto(
    /** In the whole Quran, 1 to 6,236 */
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

/** GET /quran/{edition}: the whole Quran in one edition */
@Serializable
internal data class FullQuranDto(val surahs: List<SurahDetailDto>, val edition: EditionDto)

/** GET /tafsirs/{id}/by_chapter/{n} on quran.com: a surah's commentary, as HTML, keyed by ayah */
@Serializable
internal data class TafsirResponseDto(val tafsirs: List<TafsirAyahDto>)

@Serializable
internal data class TafsirAyahDto(
    val id: Int,
    @SerialName("verse_key") val verseKey: String,
    val text: String,
)
