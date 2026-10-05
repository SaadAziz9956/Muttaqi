package com.muttaqi.android

import android.app.Application
import com.muttaqi.android.feature.onboarding.platform.onboardingServicesModule
import com.muttaqi.android.feature.prayer.platform.prayerServicesModule
import com.muttaqi.android.feature.settings.settingsModule
import com.muttaqi.android.platform.permissionsModule
import com.muttaqi.shared.di.androidPlatformModule
import com.muttaqi.shared.di.initKoin
import org.koin.android.ext.koin.androidContext

class MuttaqiApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin(androidPlatformModule(this)) {
            androidContext(this@MuttaqiApplication)
            modules(permissionsModule, prayerServicesModule, onboardingServicesModule, settingsModule)
        }
    }
}
