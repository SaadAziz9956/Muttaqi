package com.muttaqi.shared.feature.quran.domain.repository

import com.muttaqi.shared.core.domain.Outcome
import com.muttaqi.shared.core.model.Language
import com.muttaqi.shared.feature.quran.domain.model.Ayah
import com.muttaqi.shared.feature.quran.domain.model.FontSize
import com.muttaqi.shared.feature.quran.domain.model.ReadingMode
import com.muttaqi.shared.feature.quran.domain.model.ReadingSettings
import com.muttaqi.shared.feature.quran.domain.model.Surah
import com.muttaqi.shared.feature.quran.domain.model.SurahProgress
import com.muttaqi.shared.feature.quran.domain.model.TafsirEntry
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant

interface SurahRepository {
    suspend fun surahs(): List<Surah>
    suspend fun surah(number: Int): Surah?
}

interface AyahRepository {
    suspend fun ayahs(surahNumber: Int, language: Language): List<Ayah>
    suspend fun ayah(surahNumber: Int, numberInSurah: Int, language: Language): Ayah?
}

interface QuranLibrary {
    suspend fun hasText(): Boolean
    suspend fun hasTranslation(language: Language): Boolean
    suspend fun downloadText(): Outcome<Unit>
    suspend fun downloadTranslation(language: Language): Outcome<Unit>
}

interface QuranDownloadRecord {
    fun markTextDownloaded()
    fun markLanguageDownloaded(language: Language)
}

interface ReadingProgressRepository {
    suspend fun progress(surahNumber: Int): SurahProgress?
    suspend fun lastRead(): SurahProgress?
    suspend fun all(): List<SurahProgress>
    suspend fun record(surahNumber: Int, lastAyahNumber: Int, readAyahs: Set<Int>, totalAyahs: Int, at: Instant)
    suspend fun merge(records: List<SurahProgress>)
}

interface StoredProgressImportMarker {
    val isImported: Boolean
    fun markImported()
}

fun interface TafsirRepository {
    suspend fun tafsir(surahNumber: Int, language: Language): Outcome<List<TafsirEntry>>
}

interface ReadingPreferences {
    val current: ReadingSettings
    val changes: Flow<ReadingSettings>
    fun setMode(mode: ReadingMode)
    fun setFontSize(size: FontSize)
}
