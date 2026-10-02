package com.muttaqi.shared.feature.quran.data.repository

import com.muttaqi.shared.core.domain.DispatcherProvider
import com.muttaqi.shared.core.domain.DomainError
import com.muttaqi.shared.core.domain.Outcome
import com.muttaqi.shared.core.domain.RecentCache
import com.muttaqi.shared.core.model.Language
import com.muttaqi.shared.feature.quran.data.local.AyahTranslationEntity
import com.muttaqi.shared.feature.quran.data.local.QuranTextDao
import com.muttaqi.shared.feature.quran.data.remote.QuranApi
import com.muttaqi.shared.feature.quran.data.remote.QuranEdition
import com.muttaqi.shared.feature.quran.domain.model.Ayah
import com.muttaqi.shared.feature.quran.domain.model.Surah
import com.muttaqi.shared.feature.quran.domain.repository.AyahRepository
import com.muttaqi.shared.feature.quran.domain.repository.QuranLibrary
import com.muttaqi.shared.feature.quran.domain.repository.SurahRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.io.IOException
import kotlin.concurrent.Volatile

internal class RoomQuranRepository(
    private val dao: QuranTextDao,
    private val api: QuranApi,
    private val dispatchers: DispatcherProvider,
) : SurahRepository, AyahRepository, QuranLibrary {

    @Volatile
    private var allSurahs: List<Surah>? = null

    @Volatile
    private var textStored = false

    @Volatile
    private var translated: Set<Language> = emptySet()

    private val surahAyahs = RecentCache<Pair<Int, Language>, List<Ayah>>(capacity = 4)
    private val singleAyahs = RecentCache<Triple<Int, Int, Language>, Ayah>(capacity = 16)

    override suspend fun surahs(): List<Surah> =
        allSurahs ?: dao.surahs().map { it.toDomain() }.also { if (it.size == Surah.LAST) allSurahs = it }

    override suspend fun surah(number: Int): Surah? {
        allSurahs?.let { all -> return all.firstOrNull { it.number == number } }
        return dao.surah(number)?.toDomain()
    }

    override suspend fun ayahs(surahNumber: Int, language: Language): List<Ayah> {
        val key = surahNumber to language
        surahAyahs[key]?.let { return it }
        return dao.ayahs(surahNumber, language.code).map { it.toDomain() }.also { if (it.isNotEmpty()) surahAyahs[key] = it }
    }

    override suspend fun ayah(surahNumber: Int, numberInSurah: Int, language: Language): Ayah? {
        val key = Triple(surahNumber, numberInSurah, language)
        singleAyahs[key]?.let { return it }
        surahAyahs[surahNumber to language]?.firstOrNull { it.numberInSurah == numberInSurah }?.let { return it }
        return dao.ayah(surahNumber, numberInSurah, language.code)?.toDomain()?.also { singleAyahs[key] = it }
    }

    override suspend fun hasText(): Boolean =
        textStored || (dao.surahCount() == Surah.LAST && dao.ayahCount() > 0).also { textStored = it }

    override suspend fun hasTranslation(language: Language): Boolean =
        language in translated || (dao.translationCount(language.code) > 0).also { if (it) translated = translated + language }

    override suspend fun downloadText(): Outcome<Unit> = downloading {
        forgetText()
        val (arabic, transliteration) = coroutineScope {
            val arabic = async { api.fullQuran(QuranEdition.ARABIC_UTHMANI) }
            val transliteration = async { api.fullQuran(QuranEdition.TRANSLITERATION) }
            arabic.await() to transliteration.await()
        }
        val transliterations = transliteration.surahs.flatMap { it.ayahs }.associate { it.number to it.text }
        dao.storeText(
            surahs = arabic.surahs.map { it.toEntity() },
            ayahs = arabic.surahs.flatMap { surah -> surah.ayahs.map { it.toEntity(surah.number, transliterations[it.number]) } },
        )
    }

    override suspend fun downloadTranslation(language: Language): Outcome<Unit> = downloading {
        forgetText()
        val edition = QuranEdition.translation(language)
        val translation = api.fullQuran(edition)
        dao.replaceTranslation(
            language = language.code,
            translations = translation.surahs.flatMap { surah ->
                surah.ayahs.map { AyahTranslationEntity(it.number, language.code, edition, it.text) }
            },
        )
    }

    private fun forgetText() {
        allSurahs = null
        textStored = false
        translated = emptySet()
        surahAyahs.clear()
        singleAyahs.clear()
    }

    private suspend fun downloading(work: suspend () -> Unit): Outcome<Unit> = try {
        withContext(dispatchers.io) { work() }
        Outcome.Success(Unit)
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (_: IOException) {
        Outcome.Failure(DomainError.NoConnection)
    } catch (error: Exception) {
        Outcome.Failure(DomainError.Unexpected(error.message))
    }
}
