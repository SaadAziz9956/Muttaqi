package com.muttaqi.shared.di

import com.muttaqi.shared.feature.dhikr.di.dhikrModule
import com.muttaqi.shared.feature.dua.di.duaModule
import com.muttaqi.shared.feature.home.di.homeModule
import com.muttaqi.shared.feature.journal.di.journalModule
import com.muttaqi.shared.feature.names.di.namesModule
import com.muttaqi.shared.feature.onboarding.di.onboardingModule
import com.muttaqi.shared.feature.prayer.di.prayerModule
import com.muttaqi.shared.feature.quran.di.quranModule
import com.muttaqi.shared.feature.share.di.shareModule
import com.muttaqi.shared.feature.topics.di.topicsModule
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration

/** Every feature's module. A feature adds its bindings to its own module file, never here */
internal val featureModules: List<Module> = listOf(
    duaModule,
    dhikrModule,
    namesModule,
    topicsModule,
    journalModule,
    quranModule,
    prayerModule,
    homeModule,
    onboardingModule,
    shareModule,
)

/** Starts dependency injection once, at launch, with the platform's own implementations */
fun initKoin(platformModule: Module, appDeclaration: KoinAppDeclaration = {}): KoinApplication = startKoin {
    appDeclaration()
    modules(listOf(coreModule, platformModule) + featureModules)
}
