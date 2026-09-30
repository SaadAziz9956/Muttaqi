package com.muttaqi.shared.di

import com.muttaqi.shared.core.domain.DefaultDispatcherProvider
import com.muttaqi.shared.core.domain.DispatcherProvider
import com.muttaqi.shared.core.preferences.LanguageSelector
import com.muttaqi.shared.core.preferences.SelectedLanguage
import com.muttaqi.shared.core.preferences.SettingsLanguagePreferences
import com.muttaqi.shared.core.text.SearchTextFolder
import com.muttaqi.shared.core.text.TextFolder
import org.koin.dsl.binds
import org.koin.dsl.module

/** What every feature can depend on. The platform module supplies ObservableSettings and BundledContentSource */
internal val coreModule = module {
    single<DispatcherProvider> { DefaultDispatcherProvider }
    single<TextFolder> { SearchTextFolder }
    single { SettingsLanguagePreferences(get()) } binds arrayOf(SelectedLanguage::class, LanguageSelector::class)
}
