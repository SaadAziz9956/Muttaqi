package com.muttaqi.shared.feature.quran

import com.muttaqi.shared.core.domain.DomainError
import com.muttaqi.shared.core.domain.Outcome
import com.muttaqi.shared.core.model.Language
import com.muttaqi.shared.core.preferences.SelectedLanguage
import com.muttaqi.shared.feature.quran.domain.model.DailyAyah
import com.muttaqi.shared.feature.quran.domain.model.ReadingProgress
import com.muttaqi.shared.feature.quran.domain.model.StoredReadingProgress
import com.muttaqi.shared.feature.quran.domain.model.Surah
import com.muttaqi.shared.feature.quran.domain.usecase.GetAyah
import com.muttaqi.shared.feature.quran.domain.usecase.GetLastReading
import com.muttaqi.shared.feature.quran.domain.usecase.GetSurahs
import com.muttaqi.shared.feature.quran.domain.usecase.ImportStoredReadingProgress
import com.muttaqi.shared.feature.quran.domain.usecase.IsQuranStored
import com.muttaqi.shared.feature.quran.domain.usecase.SyncQuran
import com.muttaqi.shared.feature.quran.presentation.list.QuranListViewModel
import com.muttaqi.shared.feature.quran.presentation.reader.SurahReaderViewModel
import com.muttaqi.shared.feature.quran.presentation.settings.ReadingSettingsViewModel
import com.muttaqi.shared.feature.quran.presentation.tafsir.TafsirViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf

/** The Quran screens' view models for Swift, from Koin: `QuranViewModels.shared.list()` */
object QuranViewModels : KoinComponent {
    fun list(): QuranListViewModel = get()

    /** [startAyah] is the ayah (number within the surah) to open at; 0 opens at the start */
    fun reader(surahNumber: Int, startAyah: Int): SurahReaderViewModel = get { parametersOf(surahNumber, startAyah) }

    fun settings(): ReadingSettingsViewModel = get()

    fun tafsir(): TafsirViewModel = get()
}

/**
 * The Quran for the Swift code outside the Quran screens (Home, Onboarding and the Journal's quote), in the reader's
 * translation language. Nothing here throws into Swift: a failure is an [Outcome] or null
 */
object QuranUseCases : KoinComponent {
    /** One ayah with its surah, e.g. the verse under Home's greeting; null if the Quran isn't stored yet */
    suspend fun ayah(surahNumber: Int, ayahNumber: Int): DailyAyah? = runCatching {
        get<GetAyah>()(surahNumber, ayahNumber, get<SelectedLanguage>().current)
    }.getOrNull()

    suspend fun surahs(): List<Surah> = runCatching { get<GetSurahs>()() }.getOrDefault(emptyList())

    /** Where the reader left off, for Home's Continue */
    suspend fun lastReading(): ReadingProgress? = runCatching { get<GetLastReading>()() }.getOrNull()

    /** Downloads the Arabic, transliteration and the language's translation, e.g. in onboarding's setup */
    suspend fun sync(language: Language): Outcome<Unit> = safely { get<SyncQuran>()(language) }

    /** Downloads what isn't stored of the Quran in the reader's language, e.g. the first launch after the update */
    suspend fun syncIfNeeded(): Outcome<Unit> = safely {
        val language = get<SelectedLanguage>().current
        if (get<IsQuranStored>()(language)) Outcome.Success(Unit) else get<SyncQuran>()(language)
    }

    /** Whether SwiftData's reading progress still has to be brought over */
    val isStoredReadingProgressImportNeeded: Boolean get() = get<ImportStoredReadingProgress>().isNeeded

    /** Brings SwiftData's reading progress into the shared database, once */
    suspend fun importStoredReadingProgress(records: List<StoredReadingProgress>): Boolean = runCatching {
        get<ImportStoredReadingProgress>()(records)
    }.isSuccess

    private inline fun safely(work: () -> Outcome<Unit>): Outcome<Unit> =
        runCatching(work).getOrElse { Outcome.Failure(DomainError.Unexpected(it.message)) }
}
