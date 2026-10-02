package com.muttaqi.shared.feature.quran.presentation.tafsir

import androidx.lifecycle.viewModelScope
import com.muttaqi.shared.core.domain.Outcome
import com.muttaqi.shared.core.mvi.MviViewModel
import com.muttaqi.shared.core.mvi.Reducer
import com.muttaqi.shared.core.preferences.SelectedLanguage
import com.muttaqi.shared.feature.quran.domain.usecase.GetTafsir
import com.muttaqi.shared.feature.quran.presentation.QuranMessages
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

internal object TafsirReducer : Reducer<TafsirState, TafsirMutation> {
    override fun reduce(state: TafsirState, mutation: TafsirMutation) = when (mutation) {
        is TafsirMutation.LanguageChanged -> state.copy(language = mutation.language, status = TafsirStatus.Idle)
        is TafsirMutation.Loading -> state.copy(surahNumber = mutation.surahNumber, status = TafsirStatus.Loading)
        is TafsirMutation.Loaded -> state.copy(status = TafsirStatus.Loaded(mutation.entries))
        is TafsirMutation.LoadFailed -> state.copy(status = TafsirStatus.Failed(mutation.message))
    }
}

class TafsirViewModel(
    private val getTafsir: GetTafsir,
    selectedLanguage: SelectedLanguage,
) : MviViewModel<TafsirState, TafsirIntent, TafsirMutation, TafsirEffect>(
    TafsirState(language = selectedLanguage.current),
    TafsirReducer,
) {
    private var loading: Job? = null

    init {
        viewModelScope.launch {
            selectedLanguage.changes.collect { language ->
                if (language == state.value.language) return@collect
                loading?.cancel()
                mutate(TafsirMutation.LanguageChanged(language))
            }
        }
    }

    override fun handle(intent: TafsirIntent) {
        when (intent) {
            is TafsirIntent.Opened -> {
                val state = state.value
                val kept = state.surahNumber == intent.surahNumber &&
                    (state.status is TafsirStatus.Loaded || state.status is TafsirStatus.Loading)
                if (!kept) load(intent.surahNumber)
            }
            TafsirIntent.Retry -> state.value.surahNumber?.let(::load)
        }
    }

    private fun load(surahNumber: Int) {
        loading?.cancel()
        mutate(TafsirMutation.Loading(surahNumber))
        val language = state.value.language
        loading = viewModelScope.launch {
            when (val outcome = getTafsir(surahNumber, language)) {
                is Outcome.Success -> mutate(TafsirMutation.Loaded(outcome.value))
                is Outcome.Failure -> mutate(TafsirMutation.LoadFailed(QuranMessages.tafsirFailed(outcome.error)))
            }
        }
    }
}
