package com.muttaqi.android.feature.prayer.platform

import com.muttaqi.shared.feature.prayer.domain.platform.HeadingProvider
import com.muttaqi.shared.feature.prayer.domain.platform.LocationProvider
import com.muttaqi.shared.feature.prayer.domain.repository.LocationRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val prayerServicesModule = module {
    single<LocationProvider> { AndroidLocationProvider(androidContext(), get()) }
    single<HeadingProvider> { RotationVectorHeadingProvider(androidContext()) { get<LocationRepository>().lastKnownCoordinates() } }
}
