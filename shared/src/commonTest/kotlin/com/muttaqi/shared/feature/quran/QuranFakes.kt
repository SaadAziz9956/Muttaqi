package com.muttaqi.shared.feature.quran

import com.muttaqi.shared.core.domain.DomainError
import com.muttaqi.shared.core.domain.Outcome
import com.muttaqi.shared.core.model.Language
import com.muttaqi.shared.core.preferences.LanguageSelector
import com.muttaqi.shared.feature.quran.data.local.AyahEntity
import com.muttaqi.shared.feature.quran.data.local.AyahRow
import com.muttaqi.shared.feature.quran.data.local.AyahTranslationEntity
import com.muttaqi.shared.feature.quran.data.local.QuranTextDao
import com.muttaqi.shared.feature.quran.data.local.ReadingProgressDao
import com.muttaqi.shared.feature.quran.data.local.ReadingProgressEntity
import com.muttaqi.shared.feature.quran.data.local.SurahEntity
import com.muttaqi.shared.feature.quran.data.local.TafsirDao
import com.muttaqi.shared.feature.quran.data.local.TafsirEntity
import com.muttaqi.shared.feature.quran.data.remote.quranClientDefaults
import com.muttaqi.shared.feature.quran.domain.model.Ayah
import com.muttaqi.shared.feature.quran.domain.model.Surah
import com.muttaqi.shared.feature.quran.domain.repository.AyahRepository
import com.muttaqi.shared.feature.quran.domain.repository.QuranDownloadRecord
import com.muttaqi.shared.feature.quran.domain.repository.QuranLibrary
import com.muttaqi.shared.feature.quran.domain.repository.SurahRepository
import com.muttaqi.shared.testing.FakeSelectedLanguage
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import kotlin.time.Clock
import kotlin.time.Instant

internal fun testSurah(number: Int): Surah = QuranTestData.surahs.first { it.number == number }.let {
    Surah(it.number, it.name, it.englishName, it.englishNameTranslation, it.revelationType, it.numberOfAyahs)
}

internal val testSurahs: List<Surah> get() = QuranTestData.surahs.map { testSurah(it.number) }

internal fun QuranTestData.AyahText.translation(language: Language): String? = when (language) {
    Language.English -> english
    Language.Urdu -> urdu
    Language.Hindi -> null
}

internal fun testAyahs(surahNumber: Int, language: Language = Language.English): List<Ayah> =
    QuranTestData.ayahs.filter { it.surah == surahNumber }.map {
        Ayah(it.number, it.numberInSurah, it.surah, it.arabic, it.transliteration, it.translation(language), it.juz, it.page, it.hizbQuarter)
    }

internal fun fullQuranJson(edition: String): String {
    fun text(ayah: QuranTestData.AyahText): String = when (edition) {
        "quran-uthmani" -> ayah.arabic
        "en.transliteration" -> ayah.transliteration
        "en.sahih" -> ayah.english
        "ur.jalandhry" -> ayah.urdu
        else -> error("No $edition in the test data")
    }
    val data = buildJsonObject {
        putJsonArray("surahs") {
            QuranTestData.surahs.forEach { surah ->
                add(
                    buildJsonObject {
                        put("number", surah.number)
                        put("name", surah.name)
                        put("englishName", surah.englishName)
                        put("englishNameTranslation", surah.englishNameTranslation)
                        put("numberOfAyahs", surah.numberOfAyahs)
                        put("revelationType", surah.revelationType)
                        put(
                            "ayahs",
                            buildJsonArray {
                                QuranTestData.ayahs.filter { it.surah == surah.number }.forEach { ayah ->
                                    add(
                                        buildJsonObject {
                                            put("number", ayah.number)
                                            put("text", text(ayah))
                                            put("numberInSurah", ayah.numberInSurah)
                                            put("juz", ayah.juz)
                                            put("manzil", 1)
                                            put("page", ayah.page)
                                            put("ruku", 1)
                                            put("hizbQuarter", ayah.hizbQuarter)
                                            put("sajda", false)
                                        },
                                    )
                                }
                            },
                        )
                    },
                )
            }
        }
        putJsonObject("edition") {
            put("identifier", edition)
            put("language", edition.substringBefore('.'))
            put("name", edition)
            put("englishName", edition)
            put("format", "text")
            put("type", "quran")
        }
    }
    return envelope(data)
}

internal fun envelope(data: JsonElement): String = buildJsonObject {
    put("code", 200)
    put("status", "OK")
    put("data", data)
}.toString()

internal fun MockRequestHandleScope.respondJson(body: String, status: HttpStatusCode = HttpStatusCode.OK): HttpResponseData =
    respond(body, status, headersOf(HttpHeaders.ContentType, "application/json"))

internal class MockHttp(handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData) {
    val requests = mutableListOf<HttpRequestData>()
    val client = HttpClient(
        MockEngine { request ->
            requests += request
            handler(request)
        },
    ) { quranClientDefaults() }
}

internal class FakeQuranTextDao : QuranTextDao() {
    val surahRows = mutableMapOf<Int, SurahEntity>()
    val ayahRows = mutableMapOf<Int, AyahEntity>()
    val translationRows = mutableMapOf<Pair<Int, String>, AyahTranslationEntity>()

    override suspend fun surahs() = surahRows.values.sortedBy { it.number }
    override suspend fun surah(number: Int) = surahRows[number]
    override suspend fun surahCount() = surahRows.size
    override suspend fun ayahCount() = ayahRows.size
    override suspend fun ayahs(surahNumber: Int, language: String) =
        ayahRows.values.filter { it.surahNumber == surahNumber }.sortedBy { it.numberInSurah }.map { it.row(language) }
    override suspend fun ayah(surahNumber: Int, numberInSurah: Int, language: String) =
        ayahRows.values.firstOrNull { it.surahNumber == surahNumber && it.numberInSurah == numberInSurah }?.row(language)
    override suspend fun translationCount(language: String) = translationRows.keys.count { it.second == language }
    override suspend fun upsertSurahs(surahs: List<SurahEntity>) = surahs.forEach { surahRows[it.number] = it }
    override suspend fun upsertAyahs(ayahs: List<AyahEntity>) = ayahs.forEach { ayahRows[it.number] = it }
    override suspend fun deleteTranslations(language: String) {
        translationRows.keys.removeAll { it.second == language }
    }
    override suspend fun insertTranslations(translations: List<AyahTranslationEntity>) =
        translations.forEach { translationRows[it.ayahNumber to it.language] = it }

    private fun AyahEntity.row(language: String) = AyahRow(
        number, numberInSurah, surahNumber, arabicText, transliteration, juz, page, hizbQuarter,
        translationRows[number to language]?.text,
    )
}

internal class FakeReadingProgressDao : ReadingProgressDao {
    val rows = mutableMapOf<Int, ReadingProgressEntity>()
    override suspend fun progress(surahNumber: Int) = rows[surahNumber]
    override suspend fun lastRead() = rows.values.maxByOrNull { it.lastReadAtEpochMillis }
    override suspend fun all() = rows.values.sortedBy { it.surahNumber }
    override suspend fun upsert(progress: List<ReadingProgressEntity>) = progress.forEach { rows[it.surahNumber] = it }
}

internal class FakeTafsirDao : TafsirDao {
    val rows = mutableListOf<TafsirEntity>()
    override suspend fun tafsir(surahNumber: Int, language: String) =
        rows.filter { it.surahNumber == surahNumber && it.language == language }.sortedBy { it.ayahNumber }
    override suspend fun insert(entries: List<TafsirEntity>) {
        entries.forEach { entry ->
            rows.removeAll { it.surahNumber == entry.surahNumber && it.ayahNumber == entry.ayahNumber && it.language == entry.language }
            rows += entry
        }
    }
}

internal class FakeQuranLibrary(
    textStored: Boolean = true,
    translations: Set<Language> = setOf(Language.English, Language.Urdu),
) : SurahRepository, AyahRepository, QuranLibrary {
    var textStored = textStored
    val translations = translations.toMutableSet()
    var failDownloads = false
    val downloads = mutableListOf<String>()

    override suspend fun surahs() = if (textStored) testSurahs else emptyList()
    override suspend fun surah(number: Int) = surahs().firstOrNull { it.number == number }
    override suspend fun ayahs(surahNumber: Int, language: Language) =
        if (textStored) testAyahs(surahNumber, language).map { if (language in translations) it else it.copy(translation = null) } else emptyList()
    override suspend fun ayah(surahNumber: Int, numberInSurah: Int, language: Language) =
        ayahs(surahNumber, language).firstOrNull { it.numberInSurah == numberInSurah }
    override suspend fun hasText() = textStored
    override suspend fun hasTranslation(language: Language) = language in translations

    override suspend fun downloadText(): Outcome<Unit> {
        downloads += "text"
        if (failDownloads) return Outcome.Failure(DomainError.NoConnection)
        textStored = true
        return Outcome.Success(Unit)
    }

    override suspend fun downloadTranslation(language: Language): Outcome<Unit> {
        downloads += language.code
        if (failDownloads) return Outcome.Failure(DomainError.NoConnection)
        translations += language
        return Outcome.Success(Unit)
    }
}

internal class FakeDownloadRecord : QuranDownloadRecord {
    var textDownloaded = false
    val languages = mutableListOf<Language>()
    override fun markTextDownloaded() { textDownloaded = true }
    override fun markLanguageDownloaded(language: Language) { if (language !in languages) languages += language }
}

internal fun FakeSelectedLanguage.selector() = LanguageSelector { switchTo(it) }

internal class FixedClock(var now: Instant = Instant.fromEpochMilliseconds(1_790_000_000_000)) : Clock {
    override fun now(): Instant = now
}
