package com.muttaqi.shared.di

import com.muttaqi.shared.core.content.BundleContentSource
import com.muttaqi.shared.core.content.BundledContentSource
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

/** Called once from Swift at launch: `IosKoinKt.doInitKoinIos()` */
fun initKoinIos() {
    initKoin(iosPlatformModule)
}
