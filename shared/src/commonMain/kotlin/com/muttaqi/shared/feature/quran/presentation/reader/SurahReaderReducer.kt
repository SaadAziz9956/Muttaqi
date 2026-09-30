package com.muttaqi.shared.feature.quran.presentation.reader

import com.muttaqi.shared.core.mvi.Reducer

internal object SurahReaderReducer : Reducer<SurahReaderState, SurahReaderMutation> {
    override fun reduce(state: SurahReaderState, mutation: SurahReaderMutation): SurahReaderState = when (mutation) {
        // The header keeps the surah it shows until the new one has loaded
        is SurahReaderMutation.Moved -> state.copy(
            surahNumber = mutation.surahNumber,
            direction = mutation.direction,
            content = SurahReaderContent.Loading,
            pendingStartAyah = null,
        )
        SurahReaderMutation.Loading -> state.copy(content = SurahReaderContent.Loading)
        is SurahReaderMutation.Loaded -> state.copy(
            content = SurahReaderContent.Loaded(mutation.reading),
            headerSurah = mutation.reading.surah,
            previousSurah = mutation.reading.previousSurah,
            nextSurah = mutation.reading.nextSurah,
        )
        is SurahReaderMutation.LoadFailed -> state.copy(content = SurahReaderContent.Failed(mutation.message, mutation.suggestion))
        is SurahReaderMutation.SettingsChanged -> state.copy(settings = mutation.settings)
        SurahReaderMutation.StartReached -> state.copy(pendingStartAyah = null)
    }
}
