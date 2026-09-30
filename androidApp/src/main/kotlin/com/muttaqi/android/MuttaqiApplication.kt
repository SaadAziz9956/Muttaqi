package com.muttaqi.android

import android.app.Application
import com.muttaqi.shared.di.androidPlatformModule
import com.muttaqi.shared.di.initKoin
import org.koin.android.ext.koin.androidContext

class MuttaqiApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin(androidPlatformModule(this)) {
            androidContext(this@MuttaqiApplication)
        }
    }
}
