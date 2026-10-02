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

internal class QuranApi(private val client: HttpClient) {

    suspend fun surahList(): List<SurahDto> = client.get("$BASE_URL/surah").body<ApiResponse<List<SurahDto>>>().data

    suspend fun fullQuran(edition: String): FullQuranDto =
        client.get("$BASE_URL/quran/$edition").body<ApiResponse<FullQuranDto>>().data

    suspend fun surah(number: Int, edition: String): SurahDetailDto =
        client.get("$BASE_URL/surah/$number/$edition").body<ApiResponse<SurahDetailDto>>().data

    companion object {
        const val BASE_URL = "https://api.qurani.ai/gw/qh/v1"
    }
}

internal object QuranEdition {
    const val ARABIC_UTHMANI = "quran-uthmani"
    const val TRANSLITERATION = "en.transliteration"

    fun translation(language: Language): String = when (language) {
        Language.English -> "en.sahih"
        Language.Urdu -> "ur.jalandhry"
        Language.Hindi -> "hi.hindi"
    }
}

internal class TafsirApi(private val client: HttpClient) {

    suspend fun tafsirByChapter(tafsirId: Int, chapterNumber: Int): TafsirResponseDto =
        client.get("$BASE_URL/tafsirs/$tafsirId/by_chapter/$chapterNumber") {
            parameter("per_page", PER_PAGE)
        }.body()

    companion object {
        const val BASE_URL = "https://api.quran.com/api/v4"
        private const val PER_PAGE = 300
    }
}

internal enum class TafsirSource(val id: Int, val title: String) {
    IbnKathirAbridged(169, "Ibn Kathir (Abridged)"),
    IbnKathirUrdu(160, "Tafsir Ibn Kathir");

    companion object {
        fun forLanguage(language: Language): TafsirSource = if (language == Language.Urdu) IbnKathirUrdu else IbnKathirAbridged
    }
}

internal fun HttpClientConfig<*>.quranClientDefaults() {
    expectSuccess = true
    install(ContentNegotiation) { json(ContentJson) }
}

internal fun quranHttpClient(): HttpClient = HttpClient { quranClientDefaults() }
