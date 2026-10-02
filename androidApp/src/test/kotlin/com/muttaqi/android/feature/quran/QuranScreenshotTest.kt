package com.muttaqi.android.feature.quran

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import com.muttaqi.android.designsystem.MuttaqiTheme
import com.muttaqi.android.designsystem.component.SoftBackdrop
import com.muttaqi.android.testing.PHONE
import com.muttaqi.android.testing.captureLightAndDark
import com.muttaqi.shared.core.model.Language
import com.muttaqi.shared.core.quote.DisplayedQuote
import com.muttaqi.shared.feature.quran.domain.model.FontSize
import com.muttaqi.shared.feature.quran.domain.model.ReadingMode
import com.muttaqi.shared.feature.quran.domain.model.ReadingProgress
import com.muttaqi.shared.feature.quran.domain.model.ReadingSettings
import com.muttaqi.shared.feature.quran.domain.model.SurahReading
import com.muttaqi.shared.feature.quran.domain.model.TafsirEntry
import com.muttaqi.shared.feature.quran.presentation.list.QuranListState
import com.muttaqi.shared.feature.quran.presentation.list.RevelationFilter
import com.muttaqi.shared.feature.quran.presentation.reader.SurahReaderContent
import com.muttaqi.shared.feature.quran.presentation.reader.SurahReaderState
import com.muttaqi.shared.feature.quran.presentation.settings.ReadingSettingsState
import com.muttaqi.shared.feature.quran.presentation.tafsir.TafsirState
import com.muttaqi.shared.feature.quran.presentation.tafsir.TafsirStatus
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.time.Instant

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(application = Application::class, sdk = [35], qualifiers = PHONE)
class QuranScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    private val surahs = QuranScreenshotData.surahs
    private fun surah(number: Int) = surahs.first { it.number == number }

    private val header = DisplayedQuote("The best among you [Muslims] are those who learn the Quran and teach it.", "Sahih Bukhari (5027)")

    private fun reader(reading: SurahReading, settings: ReadingSettings = ReadingSettings()) = SurahReaderState(
        surahNumber = reading.surah.number,
        headerSurah = reading.surah,
        previousSurah = reading.previousSurah,
        nextSurah = reading.nextSurah,
        content = SurahReaderContent.Loaded(reading),
        settings = settings,
    )

    private val fatiha = SurahReading(surah(1), QuranScreenshotData.fatiha, null, surah(2))
    private val baqara = SurahReading(surah(2), QuranScreenshotData.baqara, surah(1), surah(3))

    @Test
    fun list() = compose.captureLightAndDark("quran_list") {
        QuranListScreen(
            QuranListState(
                isLoading = false,
                header = header,
                surahs = surahs,
                readingProgress = ReadingProgress(2, "سورة البقرة", "Al-Baqara", 15, Instant.fromEpochMilliseconds(0)),
                visibleSurahs = surahs,
            ),
            onIntent = {},
        )
    }

    @Test
    fun listFilteredToMeccan() = compose.captureLightAndDark("quran_list_meccan") {
        QuranListScreen(
            QuranListState(
                isLoading = false,
                header = header,
                surahs = surahs,
                filter = RevelationFilter.Meccan,
                visibleSurahs = surahs.filter { it.revelationType == "Meccan" },
            ),
            onIntent = {},
        )
    }

    @Test
    fun readerWithTranslation() = compose.captureLightAndDark("quran_reader") {
        SurahReaderScreen(reader(fatiha), readingPosition = "Ayah 2 of 7", onIntent = {}, onSettings = {}, onBack = {})
    }

    @Test
    fun readerInUrdu() = compose.captureLightAndDark("quran_reader_urdu") {
        SurahReaderScreen(
            reader(SurahReading(surah(2), QuranScreenshotData.baqaraUrdu, surah(1), surah(3)), ReadingSettings(language = Language.Urdu)),
            readingPosition = "Ayah 1 of 286",
            onIntent = {},
            onSettings = {},
            onBack = {},
        )
    }

    @Test
    fun readerArabicOnly() = compose.captureLightAndDark("quran_reader_arabic_only") {
        SurahReaderScreen(
            reader(baqara, ReadingSettings(mode = ReadingMode.ArabicOnly, fontSize = FontSize(100))),
            readingPosition = "Page 2",
            onIntent = {},
            onSettings = {},
            onBack = {},
        )
    }

    @Test
    fun settings() = compose.captureLightAndDark("quran_settings") {
        Sheet { ReadingSettingsContent(ReadingSettingsState(isDownloadingLanguage = true), onIntent = {}, onChooseLanguage = {}) }
    }

    @Test
    fun languages() = compose.captureLightAndDark("quran_languages") {
        Sheet { LanguagePickerContent(ReadingSettingsState(language = Language.Urdu), onSelect = {}) }
    }

    @Test
    fun tafsir() = compose.captureLightAndDark("quran_tafsir") {
        Sheet {
            TafsirContent(
                surah = surah(1),
                state = TafsirState(
                    surahNumber = 1,
                    status = TafsirStatus.Loaded(
                        listOf(
                            TafsirEntry(1, 1, 1, QuranScreenshotData.fatihaTafsir),
                            TafsirEntry(1, 2, 2, QuranScreenshotData.fatihaTafsir2),
                        ),
                    ),
                ),
                startAyah = null,
                onRetry = {},
            )
        }
    }

    @Composable
    private fun Sheet(content: @Composable () -> Unit) {
        Box {
            SoftBackdrop()
            Box(
                Modifier
                    .padding(top = 120.dp)
                    .fillMaxSize()
                    .background(MuttaqiTheme.soft.canvas, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .padding(top = 28.dp),
            ) { content() }
        }
    }
}
