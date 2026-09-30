package com.muttaqi.shared.feature.quran.domain.usecase

import com.muttaqi.shared.core.domain.Outcome
import com.muttaqi.shared.core.domain.dayOfEra
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
import kotlinx.datetime.LocalDate

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

/** The same ayah all day from a curated list, moving to the next one at midnight, for Home's Ayah of the Day */
class GetAyahOfTheDay(private val getAyah: GetAyah) {
    suspend operator fun invoke(date: LocalDate, language: Language): DailyAyah? {
        // Counted by the day of the era, as the iOS app picked it, so the day's ayah doesn't change with the move
        val (surah, ayah) = CURATED[date.dayOfEra.mod(CURATED.size.toLong()).toInt()]
        return getAyah(surah, ayah, language)
    }

    private companion object {
        /** Well-known ayahs that read clearly on their own, rather than any ayah out of its context */
        val CURATED = listOf(
            2 to 45, 2 to 152, 2 to 153, 2 to 186, 2 to 286, 3 to 31, 3 to 92, 3 to 159, 3 to 185, 3 to 200,
            6 to 162, 7 to 56, 8 to 46, 9 to 51, 11 to 115, 13 to 11, 13 to 28, 14 to 7, 16 to 18, 16 to 97,
            16 to 128, 21 to 35, 25 to 63, 29 to 69, 30 to 21, 39 to 53, 40 to 60, 41 to 34, 47 to 7, 49 to 10,
            49 to 13, 50 to 16, 51 to 56, 55 to 13, 64 to 11, 65 to 3, 93 to 3, 93 to 5, 94 to 6,
        )
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
