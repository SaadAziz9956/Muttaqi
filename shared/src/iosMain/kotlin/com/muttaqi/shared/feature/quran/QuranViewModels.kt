package com.muttaqi.shared.feature.quran

import com.muttaqi.shared.core.domain.DomainError
import com.muttaqi.shared.core.domain.Outcome
import com.muttaqi.shared.core.preferences.SelectedLanguage
import com.muttaqi.shared.feature.quran.domain.model.StoredReadingProgress
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
 * The Quran for the Swift app's launch: the first download after the update, and bringing over the reading progress
 * SwiftData kept. Nothing here throws into Swift: a failure is an [Outcome]
 */
object QuranUseCases : KoinComponent {
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
