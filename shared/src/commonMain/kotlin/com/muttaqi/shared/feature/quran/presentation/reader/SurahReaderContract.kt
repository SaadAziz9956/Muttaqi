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

enum class SurahDirection { Forward, Backward }

sealed interface SurahReaderContent {
    data object Loading : SurahReaderContent
    data class Loaded(val reading: SurahReading) : SurahReaderContent
    data class Failed(val message: String, val suggestion: String) : SurahReaderContent
}

data class SurahReaderState(
    val surahNumber: Int,
    val direction: SurahDirection = SurahDirection.Forward,
    val headerSurah: Surah? = null,
    val previousSurah: Surah? = null,
    val nextSurah: Surah? = null,
    val content: SurahReaderContent = SurahReaderContent.Loading,
    val settings: ReadingSettings = ReadingSettings(),
    val pendingStartAyah: Int? = null,
) : UiState {
    val reading: SurahReading? get() = (content as? SurahReaderContent.Loaded)?.reading
    val canGoPrevious: Boolean get() = surahNumber > Surah.FIRST
    val canGoNext: Boolean get() = surahNumber < Surah.LAST

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
    data class VisibleAyahsChanged(val ids: List<Int>) : SurahReaderIntent
    data object ReachedStart : SurahReaderIntent
    data object NextTapped : SurahReaderIntent
    data object PreviousTapped : SurahReaderIntent
    data object ExplanationTapped : SurahReaderIntent
    data class AyahExplanationTapped(val numberInSurah: Int) : SurahReaderIntent
    data class CopyTapped(val ayahNumber: Int) : SurahReaderIntent
    data class ShareTapped(val ayahNumber: Int) : SurahReaderIntent
    data object Retry : SurahReaderIntent
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
    data class OpenTafsir(val surahNumber: Int, val startAyah: Int?) : SurahReaderEffect
    data class OpenShare(val passage: SharePassage) : SurahReaderEffect
    data class Copy(val text: String) : SurahReaderEffect
}
