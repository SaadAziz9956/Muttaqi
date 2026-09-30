package com.muttaqi.shared.feature.quran.domain.usecase

import com.muttaqi.shared.core.domain.Outcome
import com.muttaqi.shared.core.model.Language
import com.muttaqi.shared.core.preferences.LanguageSelector
import com.muttaqi.shared.feature.quran.domain.model.DailyAyah
import com.muttaqi.shared.feature.quran.domain.model.Surah
import com.muttaqi.shared.feature.quran.domain.model.SurahReading
import com.muttaqi.shared.feature.quran.domain.repository.AyahRepository
import com.muttaqi.shared.feature.quran.domain.repository.QuranDownloadRecord
import com.muttaqi.shared.feature.quran.domain.repository.QuranLibrary
import com.muttaqi.shared.feature.quran.domain.repository.SurahRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Every surah, in order */
class GetSurahs(private val repository: SurahRepository) {
    suspend operator fun invoke(): List<Surah> = repository.surahs()
}

/** One surah's details */
class GetSurah(private val repository: SurahRepository) {
    suspend operator fun invoke(number: Int): Surah? = repository.surah(number)
}

/** A surah to read in the language, with the surahs either side of it; null if it isn't stored */
class ReadSurah(
    private val surahs: SurahRepository,
    private val ayahs: AyahRepository,
) {
    suspend operator fun invoke(number: Int, language: Language): SurahReading? {
        val all = surahs.surahs()
        val surah = all.firstOrNull { it.number == number } ?: return null
        return SurahReading(
            surah = surah,
            ayahs = ayahs.ayahs(number, language),
            previousSurah = all.firstOrNull { it.number == number - 1 },
            nextSurah = all.firstOrNull { it.number == number + 1 },
        )
    }
}

/** One ayah with its surah, e.g. Home's Ayah of the Day or a verse quoted under a title */
class GetAyah(
    private val surahs: SurahRepository,
    private val ayahs: AyahRepository,
) {
    suspend operator fun invoke(surahNumber: Int, ayahNumber: Int, language: Language): DailyAyah? {
        val surah = surahs.surah(surahNumber) ?: return null
        val ayah = ayahs.ayah(surahNumber, ayahNumber, language) ?: return null
        return DailyAyah(surah, ayah)
    }
}

/**
 * Downloads whatever the reader needs that isn't stored yet: the Arabic and transliteration, then the language's
 * translation, which becomes the reading language. One sync runs at a time, so a second waits and then finds
 * everything in place.
 */
class SyncQuran(
    private val library: QuranLibrary,
    private val record: QuranDownloadRecord,
    private val languageSelector: LanguageSelector,
) {
    private val mutex = Mutex()

    suspend operator fun invoke(language: Language): Outcome<Unit> = mutex.withLock {
        if (!library.hasText()) {
            val text = library.downloadText()
            if (text is Outcome.Failure) return text
        }
        if (!library.hasTranslation(language)) {
            val translation = library.downloadTranslation(language)
            if (translation is Outcome.Failure) return translation
        }
        record.markTextDownloaded()
        languageSelector.select(language)
        record.markLanguageDownloaded(language)
        Outcome.Success(Unit)
    }
}

/** Whether the Arabic and the language's translation are both stored, so nothing needs downloading to read */
class IsQuranStored(private val library: QuranLibrary) {
    suspend operator fun invoke(language: Language): Boolean = library.hasText() && library.hasTranslation(language)
}
