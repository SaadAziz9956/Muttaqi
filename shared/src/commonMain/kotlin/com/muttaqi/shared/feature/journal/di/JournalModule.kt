package com.muttaqi.shared.feature.journal.di

import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.muttaqi.shared.core.domain.DispatcherProvider
import com.muttaqi.shared.feature.journal.data.local.JournalDatabase
import com.muttaqi.shared.feature.journal.data.repository.RoomJournalRepository
import com.muttaqi.shared.feature.journal.data.repository.SettingsJournalImportStatus
import com.muttaqi.shared.feature.journal.domain.repository.JournalImportStatus
import com.muttaqi.shared.feature.journal.domain.repository.JournalReadRepository
import com.muttaqi.shared.feature.journal.domain.repository.JournalWriteRepository
import com.muttaqi.shared.feature.journal.domain.usecase.BuildJournalSearchIndex
import com.muttaqi.shared.feature.journal.domain.usecase.DeleteJournalEntry
import com.muttaqi.shared.feature.journal.domain.usecase.GetJournalEntry
import com.muttaqi.shared.feature.journal.domain.usecase.ImportJournalEntries
import com.muttaqi.shared.feature.journal.domain.usecase.NewJournalEntry
import com.muttaqi.shared.feature.journal.domain.usecase.ObserveJournalEntries
import com.muttaqi.shared.feature.journal.domain.usecase.ObserveTodaysJournalEntry
import com.muttaqi.shared.feature.journal.domain.usecase.SaveJournalEntry
import com.muttaqi.shared.feature.journal.presentation.entry.JournalEntryViewModel
import com.muttaqi.shared.feature.journal.presentation.list.JournalListViewModel
import com.muttaqi.shared.feature.journal.presentation.today.JournalTodayViewModel
import com.russhwolf.settings.ObservableSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.scope.Scope
import org.koin.dsl.binds
import org.koin.dsl.module
import kotlin.time.Clock

/** The gratitude journal: entries stored in its own Room database */
val journalModule = module {
    single {
        journalDatabaseBuilder()
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(get<DispatcherProvider>().io)
            .build()
    }
    single {
        // Writes outlive the screens that ask for them, so they get a scope of their own for the app's lifetime
        RoomJournalRepository(get<JournalDatabase>().journalDao(), CoroutineScope(SupervisorJob() + get<DispatcherProvider>().io))
    } binds arrayOf(JournalReadRepository::class, JournalWriteRepository::class)
    single<JournalImportStatus> { SettingsJournalImportStatus(get<ObservableSettings>()) }

    factoryOf(::ObserveJournalEntries)
    factoryOf(::GetJournalEntry)
    factoryOf(::SaveJournalEntry)
    factoryOf(::DeleteJournalEntry)
    factoryOf(::BuildJournalSearchIndex)
    factoryOf(::ImportJournalEntries)
    factory { NewJournalEntry(Clock.System) }
    factory { ObserveTodaysJournalEntry(get(), Clock.System) }

    viewModelOf(::JournalListViewModel)
    viewModel { (entryId: String?) -> JournalEntryViewModel(entryId, get(), get(), get(), get(), Clock.System) }
    viewModelOf(::JournalTodayViewModel)
}

/** The database file's builder, in each platform's place for app data */
internal expect fun Scope.journalDatabaseBuilder(): RoomDatabase.Builder<JournalDatabase>
