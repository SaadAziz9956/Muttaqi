package com.muttaqi.shared.feature.quran.data.repository

import com.muttaqi.shared.core.model.Language
import com.muttaqi.shared.core.preferences.SelectedLanguage
import com.muttaqi.shared.feature.quran.domain.model.FontSize
import com.muttaqi.shared.feature.quran.domain.model.ReadingMode
import com.muttaqi.shared.feature.quran.domain.model.ReadingSettings
import com.muttaqi.shared.feature.quran.domain.repository.QuranDownloadRecord
import com.muttaqi.shared.feature.quran.domain.repository.ReadingPreferences
import com.muttaqi.shared.feature.quran.domain.repository.StoredProgressImportMarker
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.coroutines.getIntFlow
import com.russhwolf.settings.coroutines.getStringOrNullFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onStart

@OptIn(ExperimentalSettingsApi::class)
internal class SettingsReadingPreferences(
    private val settings: ObservableSettings,
    private val selectedLanguage: SelectedLanguage,
) : ReadingPreferences, QuranDownloadRecord, StoredProgressImportMarker {

    override val current: ReadingSettings
        get() = ReadingSettings(
            mode = ReadingMode.fromStoredValue(settings.getStringOrNull(Keys.READING_MODE)),
            fontSize = fontSize(settings.getInt(Keys.FONT_SIZE, 0)),
            language = selectedLanguage.current,
        )

    override val changes: Flow<ReadingSettings> = combine(
        settings.getStringOrNullFlow(Keys.READING_MODE),
        settings.getIntFlow(Keys.FONT_SIZE, 0),
        selectedLanguage.changes,
    ) { mode, fontSize, language ->
        ReadingSettings(ReadingMode.fromStoredValue(mode), fontSize(fontSize), language)
    }.onStart { emit(current) }.distinctUntilChanged()

    override fun setMode(mode: ReadingMode) = settings.putString(Keys.READING_MODE, mode.storedValue)

    override fun setFontSize(size: FontSize) = settings.putInt(Keys.FONT_SIZE, size.percentage)

    override fun markTextDownloaded() = settings.putBoolean(Keys.DATA_DOWNLOADED, true)

    override fun markLanguageDownloaded(language: Language) {
        val codes = settings.getStringOrNull(Keys.DOWNLOADED_LANGUAGES)?.split(',')?.filter { it.isNotBlank() }.orEmpty()
        if (language.code !in codes) settings.putString(Keys.DOWNLOADED_LANGUAGES, (codes + language.code).joinToString(","))
    }

    override val isImported: Boolean get() = settings.getBoolean(Keys.PROGRESS_IMPORTED, false)

    override fun markImported() = settings.putBoolean(Keys.PROGRESS_IMPORTED, true)

    private fun fontSize(stored: Int): FontSize = if (stored >= FontSize.MINIMUM_PERCENT) FontSize(stored) else FontSize()

    internal object Keys {
        const val READING_MODE = "reading_mode"
        const val FONT_SIZE = "reading_font_size"
        const val DATA_DOWNLOADED = "quran_data_downloaded"
        const val DOWNLOADED_LANGUAGES = "downloaded_languages"
        const val PROGRESS_IMPORTED = "quran_reading_progress_imported"
    }
}
