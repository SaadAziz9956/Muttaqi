package com.muttaqi.shared.di

import com.muttaqi.shared.core.content.BundleContentSource
import com.muttaqi.shared.core.content.BundledContentSource
import com.muttaqi.shared.feature.onboarding.domain.platform.FirstLaunchSetup
import com.muttaqi.shared.feature.onboarding.domain.platform.NotificationPermission
import com.muttaqi.shared.feature.prayer.domain.platform.HeadingProvider
import com.muttaqi.shared.feature.prayer.domain.platform.LocationProvider
import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.ObservableSettings
import org.koin.dsl.module
import platform.Foundation.NSUserDefaults

/** iOS's implementations of what the shared code needs from the platform */
internal val iosPlatformModule = module {
    // The standard defaults, so preferences the Swift code wrote before the move are read as they were
    single<ObservableSettings> { NSUserDefaultsSettings(NSUserDefaults.standardUserDefaults) }
    single<BundledContentSource> { BundleContentSource() }
}

/**
 * Called once from Swift at launch with the app's own services, which stay in Swift on Core Location and
 * UserNotifications: `IosKoinKt.doInitKoinIos(location: LocationService(), compass: CompassService(), …)`.
 * [firstLaunchSetup] is the Quran download, which is Swift's until the Quran is shared
 */
fun initKoinIos(
    location: LocationProvider,
    compass: HeadingProvider,
    notifications: NotificationPermission,
    firstLaunchSetup: FirstLaunchSetup,
) {
    initKoin(
        module {
            includes(iosPlatformModule)
            single { location }
            single { compass }
            single { notifications }
            single { firstLaunchSetup }
        },
    )
}
