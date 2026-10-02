package com.muttaqi.shared.feature.home.di

import com.muttaqi.shared.feature.home.data.SystemReaderClock
import com.muttaqi.shared.feature.home.domain.platform.HijriCalendar
import com.muttaqi.shared.feature.home.domain.platform.ReaderClock
import com.muttaqi.shared.feature.home.domain.usecase.GetDailyContent
import com.muttaqi.shared.feature.home.domain.usecase.GetHijriDate
import com.muttaqi.shared.feature.home.domain.usecase.GetQuranShortcuts
import com.muttaqi.shared.feature.home.domain.usecase.LocatePrayerTimes
import com.muttaqi.shared.feature.home.domain.usecase.PrepareContent
import com.muttaqi.shared.feature.home.presentation.HomeViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val homeModule = module {
    single<ReaderClock> { SystemReaderClock() }
    single<HijriCalendar> { platformHijriCalendar() }

    factoryOf(::GetDailyContent)
    factoryOf(::GetQuranShortcuts)
    factoryOf(::LocatePrayerTimes)
    factoryOf(::GetHijriDate)
    factoryOf(::PrepareContent)

    viewModelOf(::HomeViewModel)
}

internal expect fun platformHijriCalendar(): HijriCalendar
