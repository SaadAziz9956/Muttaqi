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

internal val iosPlatformModule = module {
    single<ObservableSettings> { NSUserDefaultsSettings(NSUserDefaults.standardUserDefaults) }
    single<BundledContentSource> { BundleContentSource() }
}

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
