package com.muttaqi.shared.feature.quran.data.repository

import com.muttaqi.shared.core.domain.DispatcherProvider
import com.muttaqi.shared.core.domain.DomainError
import com.muttaqi.shared.core.domain.Outcome
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

internal class RoomQuranRepository(
    private val dao: QuranTextDao,
    private val api: QuranApi,
    private val dispatchers: DispatcherProvider,
) : SurahRepository, AyahRepository, QuranLibrary {

    override suspend fun surahs(): List<Surah> = dao.surahs().map { it.toDomain() }

    override suspend fun surah(number: Int): Surah? = dao.surah(number)?.toDomain()

    override suspend fun ayahs(surahNumber: Int, language: Language): List<Ayah> =
        dao.ayahs(surahNumber, language.code).map { it.toDomain() }

    override suspend fun ayah(surahNumber: Int, numberInSurah: Int, language: Language): Ayah? =
        dao.ayah(surahNumber, numberInSurah, language.code)?.toDomain()

    override suspend fun hasText(): Boolean = dao.surahCount() == Surah.LAST && dao.ayahCount() > 0

    override suspend fun hasTranslation(language: Language): Boolean = dao.translationCount(language.code) > 0

    override suspend fun downloadText(): Outcome<Unit> = downloading {
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
        val edition = QuranEdition.translation(language)
        val translation = api.fullQuran(edition)
        dao.replaceTranslation(
            language = language.code,
            translations = translation.surahs.flatMap { surah ->
                surah.ayahs.map { AyahTranslationEntity(it.number, language.code, edition, it.text) }
            },
        )
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
