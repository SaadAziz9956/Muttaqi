package com.muttaqi.shared.core.preferences

import com.muttaqi.shared.core.model.Language
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.coroutines.toFlowSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

interface SelectedLanguage {
    val current: Language
    val changes: Flow<Language>
}

fun interface LanguageSelector {
    fun select(language: Language)
}

@OptIn(ExperimentalSettingsApi::class)
class SettingsLanguagePreferences(private val settings: ObservableSettings) : SelectedLanguage, LanguageSelector {
    override val current: Language
        get() = Language.fromCode(settings.getStringOrNull(KEY))

    override val changes: Flow<Language> = settings.toFlowSettings()
        .getStringOrNullFlow(KEY)
        .map(Language::fromCode)
        .onStart { emit(current) }
        .distinctUntilChanged()

    override fun select(language: Language) = settings.putString(KEY, language.code)

    private companion object {
        const val KEY = "reading_selected_language"
    }
}
