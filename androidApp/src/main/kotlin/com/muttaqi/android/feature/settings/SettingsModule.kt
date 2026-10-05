package com.muttaqi.android.feature.settings

import com.muttaqi.android.designsystem.ColorPreference
import org.koin.dsl.module

val settingsModule = module {
    single { ColorPreference(get()) }
}
