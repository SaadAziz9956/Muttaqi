package com.muttaqi.shared.feature.quran.presentation.settings

import com.muttaqi.shared.core.model.Language
import com.muttaqi.shared.core.mvi.UiEffect
import com.muttaqi.shared.core.mvi.UiIntent
import com.muttaqi.shared.core.mvi.UiMutation
import com.muttaqi.shared.core.mvi.UiState
import com.muttaqi.shared.feature.quran.domain.model.FontSize
import com.muttaqi.shared.feature.quran.domain.model.ReadingMode
import com.muttaqi.shared.feature.quran.domain.model.ReadingSettings

/** The reader's settings sheet: the reading mode, the font size and the translation, which may need downloading */
data class ReadingSettingsState(
    val mode: ReadingMode = ReadingMode.WithTranslation,
    val fontSize: FontSize = FontSize(),
    val language: Language = Language.English,
    /** The translations the reader can choose */
    val languages: List<Language> = Language.entries,
    val isDownloadingLanguage: Boolean = false,
) : UiState {
    constructor(settings: ReadingSettings) : this(mode = settings.mode, fontSize = settings.fontSize, language = settings.language)
}

sealed interface ReadingSettingsIntent : UiIntent {
    data class ModeSelected(val mode: ReadingMode) : ReadingSettingsIntent
    data object FontSizeIncreased : ReadingSettingsIntent
    data object FontSizeDecreased : ReadingSettingsIntent
    data class LanguageSelected(val language: Language) : ReadingSettingsIntent
}

sealed interface ReadingSettingsMutation : UiMutation {
    data class ModeChanged(val mode: ReadingMode) : ReadingSettingsMutation
    data class FontSizeChanged(val fontSize: FontSize) : ReadingSettingsMutation
    data class LanguageChanged(val language: Language) : ReadingSettingsMutation
    data class DownloadingChanged(val isDownloading: Boolean) : ReadingSettingsMutation
}

sealed interface ReadingSettingsEffect : UiEffect {
    /** The translation couldn't be downloaded, so the language stays as it was */
    data class DownloadFailed(val message: String) : ReadingSettingsEffect
}
