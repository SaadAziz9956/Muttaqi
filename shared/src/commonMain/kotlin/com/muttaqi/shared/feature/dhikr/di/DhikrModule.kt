package com.muttaqi.shared.feature.dhikr.di

import com.muttaqi.shared.feature.dhikr.data.progress.SettingsDhikrProgressRepository
import com.muttaqi.shared.feature.dhikr.data.progress.SystemCurrentDay
import com.muttaqi.shared.feature.dhikr.data.progress.platformLegacyDhikrProgressSource
import com.muttaqi.shared.feature.dhikr.data.repository.BundledDhikrRepository
import com.muttaqi.shared.feature.dhikr.domain.repository.CurrentDay
import com.muttaqi.shared.feature.dhikr.domain.repository.DhikrProgressRepository
import com.muttaqi.shared.feature.dhikr.domain.repository.DhikrRepository
import com.muttaqi.shared.feature.dhikr.domain.usecase.CountDhikr
import com.muttaqi.shared.feature.dhikr.domain.usecase.GetDhikr
import com.muttaqi.shared.feature.dhikr.domain.usecase.GetDhikrSaidToday
import com.muttaqi.shared.feature.dhikr.domain.usecase.GetDhikrSections
import com.muttaqi.shared.feature.dhikr.domain.usecase.GetTodaysDhikrProgress
import com.muttaqi.shared.feature.dhikr.domain.usecase.ResetDhikrProgress
import com.muttaqi.shared.feature.dhikr.presentation.counter.DhikrCounterViewModel
import com.muttaqi.shared.feature.dhikr.presentation.list.DhikrListViewModel
import com.russhwolf.settings.ObservableSettings
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** Dikr: the list of dhikr by category and the counter, with today's progress */
val dhikrModule = module {
    single<DhikrRepository> { BundledDhikrRepository(get(), get()) }
    single<DhikrProgressRepository> { SettingsDhikrProgressRepository(get<ObservableSettings>(), platformLegacyDhikrProgressSource()) }
    single<CurrentDay> { SystemCurrentDay }

    factoryOf(::GetDhikrSections)
    factoryOf(::GetDhikr)
    factoryOf(::GetTodaysDhikrProgress)
    factoryOf(::CountDhikr)
    factoryOf(::ResetDhikrProgress)
    factoryOf(::GetDhikrSaidToday)

    viewModelOf(::DhikrListViewModel)
    viewModel { (dhikrId: String) -> DhikrCounterViewModel(dhikrId, get(), get(), get(), get(), get()) }
}
