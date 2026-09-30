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

/** The surahs' details, in order; empty until the Quran has been downloaded */
interface SurahRepository {
    suspend fun surahs(): List<Surah>
    suspend fun surah(number: Int): Surah?
}

/** The ayahs with their transliteration and, where it's stored, the language's translation */
interface AyahRepository {
    suspend fun ayahs(surahNumber: Int, language: Language): List<Ayah>
    suspend fun ayah(surahNumber: Int, numberInSurah: Int, language: Language): Ayah?
}

/** What of the Quran is kept on the device, and downloading the rest from the Quran API */
interface QuranLibrary {
    /** Whether every surah and ayah is stored */
    suspend fun hasText(): Boolean
    suspend fun hasTranslation(language: Language): Boolean
    /** The Uthmani Arabic and transliteration of every ayah, with the surahs' details */
    suspend fun downloadText(): Outcome<Unit>
    /** Replaces the language's stored translation */
    suspend fun downloadTranslation(language: Language): Outcome<Unit>
}

/**
 * What the app notes about its downloads, under the iOS app's keys. Whether something needs downloading is asked of
 * [QuranLibrary] instead, since these notes also describe what the iOS app stored before the move to shared code
 */
interface QuranDownloadRecord {
    fun markTextDownloaded()
    fun markLanguageDownloaded(language: Language)
}

/** Each surah's reading progress */
interface ReadingProgressRepository {
    suspend fun progress(surahNumber: Int): SurahProgress?
    /** The surah read most recently */
    suspend fun lastRead(): SurahProgress?
    suspend fun all(): List<SurahProgress>
    /** Saves the reading position and adds [readAyahs] to the ayahs already read in that surah */
    suspend fun record(surahNumber: Int, lastAyahNumber: Int, readAyahs: Set<Int>, totalAyahs: Int, at: Instant)
    /** Adds records kept elsewhere: read ayahs are combined and the more recent position is kept */
    suspend fun merge(records: List<SurahProgress>)
}

/** Whether the reading progress the iOS app kept before the move to shared code has been brought over */
interface StoredProgressImportMarker {
    val isImported: Boolean
    fun markImported()
}

/** Tafsir Ibn Kathir for a surah, downloaded once and then kept */
fun interface TafsirRepository {
    suspend fun tafsir(surahNumber: Int, language: Language): Outcome<List<TafsirEntry>>
}

/** The reader's settings: the translation language comes from the app-wide language preference */
interface ReadingPreferences {
    val current: ReadingSettings
    /** The current settings, then each change */
    val changes: Flow<ReadingSettings>
    fun setMode(mode: ReadingMode)
    fun setFontSize(size: FontSize)
}
