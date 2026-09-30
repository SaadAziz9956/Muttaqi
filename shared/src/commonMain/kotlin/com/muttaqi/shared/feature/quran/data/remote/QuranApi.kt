package com.muttaqi.shared.feature.quran.data.remote

import com.muttaqi.shared.core.content.ContentJson
import com.muttaqi.shared.core.model.Language
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.serialization.kotlinx.json.json

/**
 * The Quran text and translations, from the same API (and editions) the iOS app has always synced from: an
 * alquran.cloud-compatible gateway whose Uthmani text leaves the Bismillah out of each surah's first ayah
 */
internal class QuranApi(private val client: HttpClient) {

    /** All 114 surahs' details */
    suspend fun surahList(): List<SurahDto> = client.get("$BASE_URL/surah").body<ApiResponse<List<SurahDto>>>().data

    /** The whole Quran in one edition, e.g. [QuranEdition.ARABIC_UTHMANI] */
    suspend fun fullQuran(edition: String): FullQuranDto =
        client.get("$BASE_URL/quran/$edition").body<ApiResponse<FullQuranDto>>().data

    /** One surah with its ayahs in one edition */
    suspend fun surah(number: Int, edition: String): SurahDetailDto =
        client.get("$BASE_URL/surah/$number/$edition").body<ApiResponse<SurahDetailDto>>().data

    companion object {
        const val BASE_URL = "https://api.qurani.ai/gw/qh/v1"
    }
}

/** The editions the app reads */
internal object QuranEdition {
    const val ARABIC_UTHMANI = "quran-uthmani"
    const val TRANSLITERATION = "en.transliteration"

    /** Saheeh International, Fateh Muhammad Jalandhry and the Hindi of the Quran API */
    fun translation(language: Language): String = when (language) {
        Language.English -> "en.sahih"
        Language.Urdu -> "ur.jalandhry"
        Language.Hindi -> "hi.hindi"
    }
}

/** Tafsir Ibn Kathir from quran.com, which pages ten ayahs at a time unless asked for more */
internal class TafsirApi(private val client: HttpClient) {

    suspend fun tafsirByChapter(tafsirId: Int, chapterNumber: Int): TafsirResponseDto =
        client.get("$BASE_URL/tafsirs/$tafsirId/by_chapter/$chapterNumber") {
            // The longest surah has 286 ayahs, so one page of 300 covers any surah
            parameter("per_page", PER_PAGE)
        }.body()

    companion object {
        const val BASE_URL = "https://api.quran.com/api/v4"
        private const val PER_PAGE = 300
    }
}

/** Which of quran.com's tafsirs a language reads */
internal enum class TafsirSource(val id: Int, val title: String) {
    IbnKathirAbridged(169, "Ibn Kathir (Abridged)"),
    IbnKathirUrdu(160, "Tafsir Ibn Kathir");

    companion object {
        // quran.com has no Hindi tafsir, so Hindi readers get the English one
        fun forLanguage(language: Language): TafsirSource = if (language == Language.Urdu) IbnKathirUrdu else IbnKathirAbridged
    }
}

/** JSON as both APIs send it, failing on an error status so it's reported rather than decoded */
internal fun HttpClientConfig<*>.quranClientDefaults() {
    expectSuccess = true
    install(ContentNegotiation) { json(ContentJson) }
}

/** The app's client, on the platform's engine (OkHttp on Android, NSURLSession on iOS) */
internal fun quranHttpClient(): HttpClient = HttpClient { quranClientDefaults() }
