package com.muttaqi.shared.feature.quran.presentation.reader

import androidx.lifecycle.viewModelScope
import com.muttaqi.shared.core.mvi.MviViewModel
import com.muttaqi.shared.feature.quran.domain.model.Ayah
import com.muttaqi.shared.feature.quran.domain.model.ReadingMode
import com.muttaqi.shared.feature.quran.domain.usecase.ObserveReadingSettings
import com.muttaqi.shared.feature.quran.domain.usecase.ReadSurah
import com.muttaqi.shared.feature.quran.domain.usecase.RecordReading
import com.muttaqi.shared.feature.quran.presentation.QuranMessages
import com.muttaqi.shared.feature.quran.presentation.copyText
import com.muttaqi.shared.feature.quran.presentation.toSharePassage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.seconds

/**
 * Reads a surah, moves to the surahs either side, and records the reading progress: every ayah that comes on screen
 * counts as read, and the first one on screen is where the reader is
 *
 * @param startAyah the ayah (number within the surah) to open at, e.g. when continuing; null opens at the start
 */
class SurahReaderViewModel(
    surahNumber: Int,
    startAyah: Int?,
    private val readSurah: ReadSurah,
    private val recordReading: RecordReading,
    observeSettings: ObserveReadingSettings,
) : MviViewModel<SurahReaderState, SurahReaderIntent, SurahReaderMutation, SurahReaderEffect>(
    SurahReaderState(surahNumber = surahNumber, pendingStartAyah = startAyah, settings = observeSettings.current),
    SurahReaderReducer,
) {
    private val position = MutableStateFlow<String?>(null)

    /**
     * Where the reader is, e.g. "Ayah 12 of 286", or the Mushaf page in Arabic Only mode; null until something is on
     * screen. Its own flow, since it changes with every scroll and only the pill that shows it needs to redraw
     */
    val readingPosition: StateFlow<String?> = position.asStateFlow()

    private var visibleIds: Set<Int> = emptySet()
    /** Ayahs of the current surah that have been on screen since it was opened */
    private val sessionReadAyahs = mutableSetOf<Int>()
    private var pendingProgress: PendingProgress? = null
    private var saveJob: Job? = null
    private var loadJob: Job? = null

    private data class PendingProgress(val surahNumber: Int, val lastAyahNumber: Int, val readAyahs: Set<Int>, val totalAyahs: Int)

    init {
        // A new translation language reloads the surah in it; the mode and font size just redraw
        viewModelScope.launch {
            var language = state.value.settings.language
            observeSettings().collect { settings ->
                mutate(SurahReaderMutation.SettingsChanged(settings))
                updatePosition()
                if (settings.language != language) {
                    language = settings.language
                    load()
                }
            }
        }
        load()
    }

    override fun handle(intent: SurahReaderIntent) {
        when (intent) {
            is SurahReaderIntent.VisibleAyahsChanged -> {
                visibleIds = intent.ids.toSet()
                updatePosition()
                recordVisible()
            }
            SurahReaderIntent.ReachedStart -> {
                mutate(SurahReaderMutation.StartReached)
                // The screen the jump lands on only reports what's visible once the reader scrolls, so count it now
                recordVisible()
            }
            SurahReaderIntent.NextTapped -> if (state.value.canGoNext) move(state.value.surahNumber + 1, SurahDirection.Forward)
            SurahReaderIntent.PreviousTapped -> if (state.value.canGoPrevious) move(state.value.surahNumber - 1, SurahDirection.Backward)
            SurahReaderIntent.ExplanationTapped -> emit(SurahReaderEffect.OpenTafsir(tafsirSurah(), null))
            is SurahReaderIntent.AyahExplanationTapped -> emit(SurahReaderEffect.OpenTafsir(tafsirSurah(), intent.numberInSurah))
            is SurahReaderIntent.CopyTapped -> ayah(intent.ayahNumber)?.let { emit(SurahReaderEffect.Copy(it.copyText())) }
            is SurahReaderIntent.ShareTapped -> ayah(intent.ayahNumber)?.let { emit(SurahReaderEffect.OpenShare(it.toSharePassage())) }
            SurahReaderIntent.Retry -> load()
            SurahReaderIntent.Left -> saveNow()
        }
    }

    override fun onCleared() {
        saveNow()
        super.onCleared()
    }

    private fun move(surahNumber: Int, direction: SurahDirection) {
        saveNow()
        sessionReadAyahs.clear()
        visibleIds = emptySet()
        position.value = null
        mutate(SurahReaderMutation.Moved(surahNumber, direction))
        load()
    }

    private fun load() {
        loadJob?.cancel()
        val surahNumber = state.value.surahNumber
        val language = state.value.settings.language
        mutate(SurahReaderMutation.Loading)
        loadJob = viewModelScope.launch {
            try {
                val reading = readSurah(surahNumber, language)
                if (reading == null) {
                    mutate(SurahReaderMutation.LoadFailed(QuranMessages.invalidSurah(surahNumber), QuranMessages.INVALID_SURAH_SUGGESTION))
                } else {
                    mutate(SurahReaderMutation.Loaded(reading))
                    updatePosition()
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                mutate(SurahReaderMutation.LoadFailed(QuranMessages.surahFailed(surahNumber), QuranMessages.SURAH_FAILED_SUGGESTION))
            }
        }
    }

    private fun firstVisibleAyah(): Ayah? =
        state.value.reading?.displayAyahs?.filter { it.number in visibleIds }?.minByOrNull { it.numberInSurah }

    private fun updatePosition() {
        val state = state.value
        val reading = state.reading
        val first = firstVisibleAyah()
        position.value = if (reading == null || first == null) {
            null
        } else {
            when (state.settings.mode) {
                ReadingMode.WithTranslation -> "Ayah ${first.numberInSurah} of ${reading.surah.numberOfAyahs}"
                ReadingMode.ArabicOnly -> "Page ${first.page}"
            }
        }
    }

    private fun recordVisible() {
        val state = state.value
        // What scrolls past while jumping to the saved position is ignored, or it would overwrite that position
        if (state.pendingStartAyah != null) return
        val reading = state.reading ?: return
        val visible = reading.displayAyahs.filter { it.number in visibleIds }
        val first = visible.minByOrNull { it.numberInSurah } ?: return

        // In Arabic Only mode a visible page shows all of its ayahs
        val onScreen = when (state.settings.mode) {
            ReadingMode.WithTranslation -> visible.map { it.numberInSurah }.toMutableSet()
            ReadingMode.ArabicOnly -> {
                val pages = visible.map { it.page }.toSet()
                reading.displayAyahs.filter { it.page in pages }.map { it.numberInSurah }.toMutableSet()
            }
        }
        // Al-Fatiha's first ayah is the Bismillah, shown above the list rather than as its own card
        if (reading.surah.number == 1 && first.numberInSurah <= 3) onScreen += 1
        sessionReadAyahs += onScreen

        pendingProgress = PendingProgress(reading.surah.number, first.numberInSurah, sessionReadAyahs.toSet(), reading.surah.numberOfAyahs)
        // Saved once scrolling settles rather than on every frame
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            delay(1.seconds)
            saveNow()
        }
    }

    private fun saveNow() {
        saveJob?.cancel()
        val progress = pendingProgress ?: return
        pendingProgress = null
        // Started at once and not cancelled with the screen, so leaving it, even as it's cleared, still saves where
        // the reader was
        viewModelScope.launch(start = CoroutineStart.UNDISPATCHED) {
            withContext(NonCancellable) {
                try {
                    recordReading(progress.surahNumber, progress.lastAyahNumber, progress.readAyahs, progress.totalAyahs)
                } catch (_: Exception) {
                    // A failed write loses one position; the next scroll records it again
                }
            }
        }
    }

    private fun ayah(number: Int): Ayah? = state.value.reading?.displayAyahs?.firstOrNull { it.number == number }

    private fun tafsirSurah(): Int = state.value.headerSurah?.number ?: state.value.surahNumber
}
