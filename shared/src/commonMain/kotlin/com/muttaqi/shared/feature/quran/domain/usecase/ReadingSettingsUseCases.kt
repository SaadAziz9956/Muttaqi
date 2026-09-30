package com.muttaqi.shared.feature.quran.domain.usecase

import com.muttaqi.shared.core.domain.Outcome
import com.muttaqi.shared.core.model.Language
import com.muttaqi.shared.core.preferences.LanguageSelector
import com.muttaqi.shared.feature.quran.domain.model.FontSize
import com.muttaqi.shared.feature.quran.domain.model.ReadingMode
import com.muttaqi.shared.feature.quran.domain.model.ReadingSettings
import com.muttaqi.shared.feature.quran.domain.repository.QuranLibrary
import com.muttaqi.shared.feature.quran.domain.repository.ReadingPreferences
import kotlinx.coroutines.flow.Flow

/** The reader's settings, then each change, e.g. to follow the font size while the settings are open */
class ObserveReadingSettings(private val preferences: ReadingPreferences) {
    /** The settings now, so a screen opens already laid out in them */
    val current: ReadingSettings get() = preferences.current

    operator fun invoke(): Flow<ReadingSettings> = preferences.changes
}

class ChangeReadingMode(private val preferences: ReadingPreferences) {
    operator fun invoke(mode: ReadingMode) = preferences.setMode(mode)
}

class ChangeFontSize(private val preferences: ReadingPreferences) {
    operator fun invoke(size: FontSize) = preferences.setFontSize(size)
}

/** Reads in another translation, downloading it first if it isn't stored */
class ChangeTranslation(
    private val library: QuranLibrary,
    private val syncQuran: SyncQuran,
    private val languageSelector: LanguageSelector,
) {
    suspend operator fun invoke(language: Language): Outcome<Unit> {
        if (library.hasText() && library.hasTranslation(language)) {
            languageSelector.select(language)
            return Outcome.Success(Unit)
        }
        return syncQuran(language)
    }
}
