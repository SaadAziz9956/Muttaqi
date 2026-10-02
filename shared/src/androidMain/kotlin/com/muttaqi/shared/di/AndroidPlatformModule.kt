package com.muttaqi.shared.di

import android.content.Context
import com.muttaqi.shared.core.content.AssetContentSource
import com.muttaqi.shared.core.content.BundledContentSource
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.SharedPreferencesSettings
import org.koin.dsl.module

fun androidPlatformModule(context: Context) = module {
    single<ObservableSettings> {
        SharedPreferencesSettings(context.getSharedPreferences("muttaqi", Context.MODE_PRIVATE))
    }
    single<BundledContentSource> { AssetContentSource(context) }
}
