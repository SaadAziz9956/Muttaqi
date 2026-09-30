package com.muttaqi.shared.feature.quran.presentation.settings

import androidx.lifecycle.viewModelScope
import com.muttaqi.shared.core.domain.Outcome
import com.muttaqi.shared.core.mvi.MviViewModel
import com.muttaqi.shared.core.mvi.Reducer
import com.muttaqi.shared.feature.quran.domain.model.FontSize
import com.muttaqi.shared.feature.quran.domain.usecase.ChangeFontSize
import com.muttaqi.shared.feature.quran.domain.usecase.ChangeReadingMode
import com.muttaqi.shared.feature.quran.domain.usecase.ChangeTranslation
import com.muttaqi.shared.feature.quran.domain.usecase.IsQuranStored
import com.muttaqi.shared.feature.quran.domain.usecase.ObserveReadingSettings
import com.muttaqi.shared.feature.quran.presentation.QuranMessages
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

internal object ReadingSettingsReducer : Reducer<ReadingSettingsState, ReadingSettingsMutation> {
    override fun reduce(state: ReadingSettingsState, mutation: ReadingSettingsMutation) = when (mutation) {
        is ReadingSettingsMutation.ModeChanged -> state.copy(mode = mutation.mode)
        is ReadingSettingsMutation.FontSizeChanged -> state.copy(fontSize = mutation.fontSize)
        is ReadingSettingsMutation.LanguageChanged -> state.copy(language = mutation.language)
        is ReadingSettingsMutation.DownloadingChanged -> state.copy(isDownloadingLanguage = mutation.isDownloading)
    }
}

/**
 * The reader's settings. Made with the reader rather than the sheet, so a translation still downloading when the
 * sheet is closed carries on. Only this screen changes the mode and font size, so it keeps them itself; the language
 * is followed, since it's the app's and changes once a download finishes
 */
class ReadingSettingsViewModel(
    observeSettings: ObserveReadingSettings,
    private val changeReadingMode: ChangeReadingMode,
    private val changeFontSize: ChangeFontSize,
    private val changeTranslation: ChangeTranslation,
    private val isQuranStored: IsQuranStored,
) : MviViewModel<ReadingSettingsState, ReadingSettingsIntent, ReadingSettingsMutation, ReadingSettingsEffect>(
    ReadingSettingsState(observeSettings.current),
    ReadingSettingsReducer,
) {
    init {
        viewModelScope.launch {
            observeSettings().map { it.language }.distinctUntilChanged().collect {
                mutate(ReadingSettingsMutation.LanguageChanged(it))
            }
        }
    }

    override fun handle(intent: ReadingSettingsIntent) {
        when (intent) {
            is ReadingSettingsIntent.ModeSelected -> {
                changeReadingMode(intent.mode)
                mutate(ReadingSettingsMutation.ModeChanged(intent.mode))
            }
            ReadingSettingsIntent.FontSizeIncreased -> setFontSize(state.value.fontSize.increased())
            ReadingSettingsIntent.FontSizeDecreased -> setFontSize(state.value.fontSize.decreased())
            is ReadingSettingsIntent.LanguageSelected -> selectLanguage(intent)
        }
    }

    private fun setFontSize(size: FontSize) {
        changeFontSize(size)
        mutate(ReadingSettingsMutation.FontSizeChanged(size))
    }

    private fun selectLanguage(intent: ReadingSettingsIntent.LanguageSelected) {
        if (intent.language == state.value.language || state.value.isDownloadingLanguage) return
        viewModelScope.launch {
            // Only a translation that has to be downloaded shows its progress
            val downloading = !isQuranStored(intent.language)
            if (downloading) mutate(ReadingSettingsMutation.DownloadingChanged(true))
            val outcome = changeTranslation(intent.language)
            if (downloading) mutate(ReadingSettingsMutation.DownloadingChanged(false))
            if (outcome is Outcome.Failure) emit(ReadingSettingsEffect.DownloadFailed(QuranMessages.downloadFailed(intent.language)))
        }
    }
}
