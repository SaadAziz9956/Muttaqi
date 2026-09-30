package com.muttaqi.shared.di

import com.muttaqi.shared.core.content.BundleContentSource
import com.muttaqi.shared.core.content.BundledContentSource
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
 * UserNotifications:
 * `IosKoinKt.doInitKoinIos(location: LocationService(), compass: CompassService(), notifications: NotificationService())`
 */
fun initKoinIos(location: LocationProvider, compass: HeadingProvider, notifications: NotificationPermission) {
    initKoin(
        module {
            includes(iosPlatformModule)
            single { location }
            single { compass }
            single { notifications }
        },
    )
}
