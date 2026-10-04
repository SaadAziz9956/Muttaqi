package com.muttaqi.shared.feature.quran.di

import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.muttaqi.shared.core.domain.DispatcherProvider
import com.muttaqi.shared.feature.quran.data.local.QuranDatabase
import com.muttaqi.shared.feature.quran.data.remote.QuranApi
import com.muttaqi.shared.feature.quran.data.remote.TafsirApi
import com.muttaqi.shared.feature.quran.data.remote.quranHttpClient
import com.muttaqi.shared.feature.quran.data.repository.BundledMushaf
import com.muttaqi.shared.feature.quran.data.repository.RoomQuranRepository
import com.muttaqi.shared.feature.quran.data.repository.RoomReadingProgressRepository
import com.muttaqi.shared.feature.quran.data.repository.RoomTafsirRepository
import com.muttaqi.shared.feature.quran.data.repository.SettingsReadingPreferences
import com.muttaqi.shared.feature.quran.domain.repository.AyahRepository
import com.muttaqi.shared.feature.quran.domain.repository.QuranDownloadRecord
import com.muttaqi.shared.feature.quran.domain.repository.QuranLibrary
import com.muttaqi.shared.feature.quran.domain.repository.ReadingPreferences
import com.muttaqi.shared.feature.quran.domain.repository.ReadingProgressRepository
import com.muttaqi.shared.feature.quran.domain.repository.StoredProgressImportMarker
import com.muttaqi.shared.feature.quran.domain.repository.SurahRepository
import com.muttaqi.shared.feature.quran.domain.repository.TafsirRepository
import com.muttaqi.shared.feature.quran.domain.usecase.ChangeFontSize
import com.muttaqi.shared.feature.quran.domain.usecase.ChangeReadingMode
import com.muttaqi.shared.feature.quran.domain.usecase.ChangeTranslation
import com.muttaqi.shared.feature.quran.domain.usecase.FilterSurahs
import com.muttaqi.shared.feature.quran.domain.usecase.GetAyah
import com.muttaqi.shared.feature.quran.domain.usecase.GetAyahOfTheDay
import com.muttaqi.shared.feature.quran.domain.usecase.GetLastReading
import com.muttaqi.shared.feature.quran.domain.usecase.GetQuranCompletion
import com.muttaqi.shared.feature.quran.domain.usecase.GetSurah
import com.muttaqi.shared.feature.quran.domain.usecase.GetSurahs
import com.muttaqi.shared.feature.quran.domain.usecase.GetTafsir
import com.muttaqi.shared.feature.quran.domain.usecase.ImportStoredReadingProgress
import com.muttaqi.shared.feature.quran.domain.usecase.IsQuranStored
import com.muttaqi.shared.feature.quran.domain.usecase.ObserveReadingSettings
import com.muttaqi.shared.feature.quran.domain.usecase.ReadSurah
import com.muttaqi.shared.feature.quran.domain.usecase.RecordReading
import com.muttaqi.shared.feature.quran.domain.usecase.SyncQuran
import com.muttaqi.shared.feature.quran.presentation.list.QuranListViewModel
import com.muttaqi.shared.feature.quran.presentation.reader.SurahReaderViewModel
import com.muttaqi.shared.feature.quran.presentation.settings.ReadingSettingsViewModel
import com.muttaqi.shared.feature.quran.presentation.tafsir.TafsirViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.binds
import org.koin.dsl.module
import kotlin.time.Clock

private val QuranHttpClient = named("quranHttpClient")

val quranModule = module {
    single {
        quranDatabaseBuilder()
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(get<DispatcherProvider>().io)
            .build()
    }

    single(QuranHttpClient) { quranHttpClient() }
    single { QuranApi(get(QuranHttpClient)) }
    single { TafsirApi(get(QuranHttpClient)) }
    single { get<QuranDatabase>().textDao() }
    single { get<QuranDatabase>().progressDao() }
    single { get<QuranDatabase>().tafsirDao() }

    single { BundledMushaf(get(), get()) }
    single { RoomQuranRepository(get(), get(), get(), get<BundledMushaf>()) } binds arrayOf(SurahRepository::class, AyahRepository::class, QuranLibrary::class)
    single<ReadingProgressRepository> { RoomReadingProgressRepository(get()) }
    single<TafsirRepository> { RoomTafsirRepository(get(), get(), get(), get()) }
    single { SettingsReadingPreferences(get(), get()) } binds
        arrayOf(ReadingPreferences::class, QuranDownloadRecord::class, StoredProgressImportMarker::class)

    factoryOf(::GetSurahs)
    factoryOf(::GetSurah)
    factoryOf(::ReadSurah)
    factoryOf(::GetAyah)
    factoryOf(::GetAyahOfTheDay)
    factoryOf(::FilterSurahs)
    factoryOf(::IsQuranStored)
    singleOf(::SyncQuran)
    factory { RecordReading(get(), Clock.System) }
    factoryOf(::GetLastReading)
    factoryOf(::GetQuranCompletion)
    factoryOf(::ImportStoredReadingProgress)
    factoryOf(::ObserveReadingSettings)
    factoryOf(::ChangeReadingMode)
    factoryOf(::ChangeFontSize)
    factoryOf(::ChangeTranslation)
    factoryOf(::GetTafsir)

    viewModelOf(::QuranListViewModel)
    viewModel { (surahNumber: Int, startAyah: Int) ->
        SurahReaderViewModel(surahNumber, startAyah.takeIf { it > 0 }, get(), get(), get())
    }
    viewModelOf(::ReadingSettingsViewModel)
    viewModelOf(::TafsirViewModel)
}

internal expect fun Scope.quranDatabaseBuilder(): RoomDatabase.Builder<QuranDatabase>
