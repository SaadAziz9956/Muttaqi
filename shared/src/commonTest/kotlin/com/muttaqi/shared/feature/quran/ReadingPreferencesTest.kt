package com.muttaqi.shared.feature.quran

import app.cash.turbine.test
import com.muttaqi.shared.core.model.Language
import com.muttaqi.shared.feature.quran.data.repository.SettingsReadingPreferences
import com.muttaqi.shared.feature.quran.domain.model.FontSize
import com.muttaqi.shared.feature.quran.domain.model.ReadingMode
import com.muttaqi.shared.testing.FakeSelectedLanguage
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ReadingPreferencesTest {
    private val settings = MapSettings()
    private val language = FakeSelectedLanguage()
    private val preferences = SettingsReadingPreferences(settings, language)

    @Test
    fun settingsSavedByTheIosAppAreReadUnderItsKeys() {
        settings.putString("reading_mode", "arabicOnly")
        settings.putInt("reading_font_size", 124)
        language.switchTo(Language.Urdu)
        val current = preferences.current
        assertEquals(ReadingMode.ArabicOnly, current.mode)
        assertEquals(124, current.fontSize.percentage)
        assertEquals(Language.Urdu, current.language)
    }

    @Test
    fun changesAreSavedInTheIosAppsFormat() {
        preferences.setMode(ReadingMode.ArabicOnly)
        preferences.setFontSize(FontSize(150))
        assertEquals("arabicOnly", settings.getStringOrNull("reading_mode"))
        assertEquals(150, settings.getInt("reading_font_size", 0))
    }

    @Test
    fun nothingSavedOrAnOldPointSizeGivesTheDefaults() {
        assertEquals(ReadingMode.WithTranslation, preferences.current.mode)
        assertEquals(100, preferences.current.fontSize.percentage)
        settings.putInt("reading_font_size", 24)
        assertEquals(100, preferences.current.fontSize.percentage)
    }

    @Test
    fun eachChangeIsObserved() = runTest {
        preferences.changes.test {
            assertEquals(100, awaitItem().fontSize.percentage)
            preferences.setFontSize(FontSize(102))
            assertEquals(102, awaitItem().fontSize.percentage)
            language.switchTo(Language.Urdu)
            assertEquals(Language.Urdu, awaitItem().language)
        }
    }

    @Test
    fun downloadsAreNotedUnderTheIosAppsKeys() {
        preferences.markTextDownloaded()
        preferences.markLanguageDownloaded(Language.English)
        preferences.markLanguageDownloaded(Language.Urdu)
        preferences.markLanguageDownloaded(Language.English)
        assertTrue(settings.getBoolean("quran_data_downloaded", false))
        assertEquals("en,ur", settings.getStringOrNull("downloaded_languages"))
    }

    @Test
    fun theProgressImportIsRememberedOnce() {
        assertFalse(preferences.isImported)
        preferences.markImported()
        assertTrue(SettingsReadingPreferences(settings, language).isImported)
    }

    @Test
    fun fontSizeStaysBetweenItsLimitsInStepsOfTwo() {
        assertEquals(102, FontSize().increased().percentage)
        assertEquals(70, FontSize(71).decreased().percentage)
        assertEquals(200, FontSize(250).percentage)
        assertEquals(24.0, FontSize().arabicSize)
        assertEquals(24.0, FontSize(150).translationSize)
    }
}
