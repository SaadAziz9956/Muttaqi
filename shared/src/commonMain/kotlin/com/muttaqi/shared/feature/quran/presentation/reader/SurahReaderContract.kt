package com.muttaqi.shared.feature.quran.presentation.reader

import com.muttaqi.shared.core.mvi.UiEffect
import com.muttaqi.shared.core.mvi.UiIntent
import com.muttaqi.shared.core.mvi.UiMutation
import com.muttaqi.shared.core.mvi.UiState
import com.muttaqi.shared.core.share.SharePassage
import com.muttaqi.shared.feature.quran.domain.model.ReadingMode
import com.muttaqi.shared.feature.quran.domain.model.ReadingSettings
import com.muttaqi.shared.feature.quran.domain.model.Surah
import com.muttaqi.shared.feature.quran.domain.model.SurahReading

/** Which way the reader last moved between surahs, so the next surah slides in from that side */
enum class SurahDirection { Forward, Backward }

sealed interface SurahReaderContent {
    data object Loading : SurahReaderContent
    data class Loaded(val reading: SurahReading) : SurahReaderContent
    data class Failed(val message: String, val suggestion: String) : SurahReaderContent
}

/** A surah being read, in the reader's mode, font size and translation */
data class SurahReaderState(
    val surahNumber: Int,
    val direction: SurahDirection = SurahDirection.Forward,
    /** The surah the header shows, kept until the next one has loaded */
    val headerSurah: Surah? = null,
    val previousSurah: Surah? = null,
    val nextSurah: Surah? = null,
    val content: SurahReaderContent = SurahReaderContent.Loading,
    val settings: ReadingSettings = ReadingSettings(),
    /** The ayah (number within the surah) to open at, e.g. when continuing; cleared once the screen is there */
    val pendingStartAyah: Int? = null,
) : UiState {
    val reading: SurahReading? get() = (content as? SurahReaderContent.Loaded)?.reading
    val canGoPrevious: Boolean get() = surahNumber > Surah.FIRST
    val canGoNext: Boolean get() = surahNumber < Surah.LAST

    /**
     * The on-screen id to scroll to for the ayah being continued from: the ayah itself, or in Arabic Only mode the
     * first ayah of its Mushaf page
     */
    val startScrollTarget: Int?
        get() {
            val start = pendingStartAyah ?: return null
            val reading = reading ?: return null
            val ayah = reading.displayAyahs.firstOrNull { it.numberInSurah == start } ?: return null
            return when (settings.mode) {
                ReadingMode.WithTranslation -> ayah.number
                ReadingMode.ArabicOnly -> reading.displayAyahs.first { it.page == ayah.page }.number
            }
        }
}

sealed interface SurahReaderIntent : UiIntent {
    /**
     * What's on screen, by id: each ayah's number in translation mode, and each Mushaf page's first ayah number in
     * Arabic Only mode. Every one on screen counts as read
     */
    data class VisibleAyahsChanged(val ids: List<Int>) : SurahReaderIntent
    /** The screen has scrolled to [SurahReaderState.startScrollTarget] */
    data object ReachedStart : SurahReaderIntent
    data object NextTapped : SurahReaderIntent
    data object PreviousTapped : SurahReaderIntent
    /** The header's Explanation, opening at the start of the surah */
    data object ExplanationTapped : SurahReaderIntent
    data class AyahExplanationTapped(val numberInSurah: Int) : SurahReaderIntent
    /** [ayahNumber] is the ayah's number in the whole Quran */
    data class CopyTapped(val ayahNumber: Int) : SurahReaderIntent
    data class ShareTapped(val ayahNumber: Int) : SurahReaderIntent
    data object Retry : SurahReaderIntent
    /** The reader is leaving, so any unsaved position is written now */
    data object Left : SurahReaderIntent
}

sealed interface SurahReaderMutation : UiMutation {
    data class Moved(val surahNumber: Int, val direction: SurahDirection) : SurahReaderMutation
    data object Loading : SurahReaderMutation
    data class Loaded(val reading: SurahReading) : SurahReaderMutation
    data class LoadFailed(val message: String, val suggestion: String) : SurahReaderMutation
    data class SettingsChanged(val settings: ReadingSettings) : SurahReaderMutation
    data object StartReached : SurahReaderMutation
}

sealed interface SurahReaderEffect : UiEffect {
    /** Opens the explanation, at the passage covering [startAyah] or at the start when it's null */
    data class OpenTafsir(val surahNumber: Int, val startAyah: Int?) : SurahReaderEffect
    data class OpenShare(val passage: SharePassage) : SurahReaderEffect
    /** The platform puts the text on the clipboard */
    data class Copy(val text: String) : SurahReaderEffect
}
