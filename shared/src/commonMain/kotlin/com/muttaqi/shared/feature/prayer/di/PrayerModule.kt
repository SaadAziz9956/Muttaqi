package com.muttaqi.shared.feature.prayer.di

import com.muttaqi.shared.feature.prayer.data.compass.PlatformCompass
import com.muttaqi.shared.feature.prayer.data.location.DeviceLocationRepository
import com.muttaqi.shared.feature.prayer.data.location.SavedCoordinates
import com.muttaqi.shared.feature.prayer.data.qibla.AdhanQiblaRepository
import com.muttaqi.shared.feature.prayer.data.times.AdhanPrayerTimesRepository
import com.muttaqi.shared.feature.prayer.domain.repository.Compass
import com.muttaqi.shared.feature.prayer.domain.repository.LocationRepository
import com.muttaqi.shared.feature.prayer.domain.repository.PrayerTimesRepository
import com.muttaqi.shared.feature.prayer.domain.repository.QiblaRepository
import com.muttaqi.shared.feature.prayer.domain.usecase.FollowHeading
import com.muttaqi.shared.feature.prayer.domain.usecase.GetLocationAccess
import com.muttaqi.shared.feature.prayer.domain.usecase.GetPrayerSchedule
import com.muttaqi.shared.feature.prayer.domain.usecase.GetQiblaDirection
import com.muttaqi.shared.feature.prayer.domain.usecase.LocateReader
import com.muttaqi.shared.feature.prayer.domain.usecase.RequestLocationAccess
import com.muttaqi.shared.feature.prayer.presentation.qibla.QiblaViewModel
import com.russhwolf.settings.ObservableSettings
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val prayerModule = module {
    single<PrayerTimesRepository> { AdhanPrayerTimesRepository() }
    single<QiblaRepository> { AdhanQiblaRepository() }
    single { SavedCoordinates(get<ObservableSettings>(), get()) }
    single<LocationRepository> { DeviceLocationRepository(get(), get()) }
    single<Compass> { PlatformCompass(get()) }

    factoryOf(::GetPrayerSchedule)
    factoryOf(::GetQiblaDirection)
    factoryOf(::LocateReader)
    factoryOf(::GetLocationAccess)
    factoryOf(::RequestLocationAccess)
    factoryOf(::FollowHeading)

    viewModelOf(::QiblaViewModel)
}
