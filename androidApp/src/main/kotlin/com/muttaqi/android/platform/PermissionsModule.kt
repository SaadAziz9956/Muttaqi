package com.muttaqi.android.platform

import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val permissionsModule = module {
    single(createdAtStart = true) { ActivityPermissions(androidApplication()) }
}
