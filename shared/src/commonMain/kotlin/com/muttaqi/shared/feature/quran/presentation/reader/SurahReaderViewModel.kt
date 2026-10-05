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

    val readingPosition: StateFlow<String?> = position.asStateFlow()

    private var visibleIds: Set<Int> = emptySet()
    private val sessionReadAyahs = mutableSetOf<Int>()
    private var pendingProgress: PendingProgress? = null
    private var saveJob: Job? = null
    private var loadJob: Job? = null

    private data class PendingProgress(val surahNumber: Int, val lastAyahNumber: Int, val readAyahs: Set<Int>, val totalAyahs: Int)

    init {
        launchNow {
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
        loadJob = launchNow {
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
        state.value.reading?.ayahs?.filter { it.number in visibleIds }?.minByOrNull { it.numberInSurah }

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
        if (state.pendingStartAyah != null) return
        val reading = state.reading ?: return
        val visible = reading.ayahs.filter { it.number in visibleIds }
        val first = visible.minByOrNull { it.numberInSurah } ?: return

        val onScreen = when (state.settings.mode) {
            ReadingMode.WithTranslation -> visible.map { it.numberInSurah }.toMutableSet()
            ReadingMode.ArabicOnly -> {
                val pages = visible.map { it.page }.toSet()
                reading.ayahs.filter { it.page in pages }.map { it.numberInSurah }.toMutableSet()
            }
        }
        sessionReadAyahs += onScreen

        pendingProgress = PendingProgress(reading.surah.number, first.numberInSurah, sessionReadAyahs.toSet(), reading.surah.numberOfAyahs)
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
        viewModelScope.launch(start = CoroutineStart.UNDISPATCHED) {
            withContext(NonCancellable) {
                try {
                    recordReading(progress.surahNumber, progress.lastAyahNumber, progress.readAyahs, progress.totalAyahs)
                } catch (_: Exception) {
                }
            }
        }
    }

    private fun ayah(number: Int): Ayah? = state.value.reading?.ayahs?.firstOrNull { it.number == number }

    private fun tafsirSurah(): Int = state.value.headerSurah?.number ?: state.value.surahNumber
}
