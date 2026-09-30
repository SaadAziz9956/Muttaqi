package com.muttaqi.shared.core.preferences

import com.muttaqi.shared.core.model.Language
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.coroutines.toFlowSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/** The reader's translation language, for features that show translations */
interface SelectedLanguage {
    val current: Language
    /** The current language, then each change, e.g. when it's switched in the Quran reader's settings */
    val changes: Flow<Language>
}

/** Changes the translation language; only the screens that let the reader pick it depend on this */
fun interface LanguageSelector {
    fun select(language: Language)
}

/**
 * Stored under the key the iOS app has always used, so a language picked before the move to shared code carries over
 */
@OptIn(ExperimentalSettingsApi::class)
class SettingsLanguagePreferences(private val settings: ObservableSettings) : SelectedLanguage, LanguageSelector {
    override val current: Language
        get() = Language.fromCode(settings.getStringOrNull(KEY))

    override val changes: Flow<Language> = settings.toFlowSettings()
        .getStringOrNullFlow(KEY)
        .map(Language::fromCode)
        .distinctUntilChanged()

    override fun select(language: Language) = settings.putString(KEY, language.code)

    private companion object {
        const val KEY = "reading_selected_language"
    }
}
