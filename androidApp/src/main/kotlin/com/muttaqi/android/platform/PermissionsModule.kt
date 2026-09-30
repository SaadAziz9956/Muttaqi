package com.muttaqi.android.platform

import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val permissionsModule = module {
    // Made at start so it's following the first activity by the time anything asks
    single(createdAtStart = true) { ActivityPermissions(androidApplication()) }
}
