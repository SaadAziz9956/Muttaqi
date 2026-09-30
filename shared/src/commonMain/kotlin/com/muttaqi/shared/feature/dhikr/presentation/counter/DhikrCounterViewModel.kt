package com.muttaqi.shared.feature.dhikr.presentation.counter

import androidx.lifecycle.viewModelScope
import com.muttaqi.shared.core.mvi.MviViewModel
import com.muttaqi.shared.core.mvi.Reducer
import com.muttaqi.shared.core.preferences.SelectedLanguage
import com.muttaqi.shared.core.share.SharePassage
import com.muttaqi.shared.feature.dhikr.domain.model.Dhikr
import com.muttaqi.shared.feature.dhikr.domain.model.milestoneAt
import com.muttaqi.shared.feature.dhikr.domain.usecase.CountDhikr
import com.muttaqi.shared.feature.dhikr.domain.usecase.GetDhikr
import com.muttaqi.shared.feature.dhikr.domain.usecase.GetTodaysDhikrProgress
import com.muttaqi.shared.feature.dhikr.domain.usecase.ResetDhikrProgress
import kotlinx.coroutines.launch

internal object DhikrCounterReducer : Reducer<DhikrCounterState, DhikrCounterMutation> {
    override fun reduce(state: DhikrCounterState, mutation: DhikrCounterMutation) = when (mutation) {
        is DhikrCounterMutation.Loaded -> state.copy(isLoading = false, dhikr = mutation.dhikr)
        is DhikrCounterMutation.ProgressChanged -> state.copy(progress = mutation.progress)
    }
}

class DhikrCounterViewModel(
    private val dhikrId: String,
    getDhikr: GetDhikr,
    getProgress: GetTodaysDhikrProgress,
    private val countDhikr: CountDhikr,
    private val resetProgress: ResetDhikrProgress,
    selectedLanguage: SelectedLanguage,
) : MviViewModel<DhikrCounterState, DhikrCounterIntent, DhikrCounterMutation, DhikrCounterEffect>(
    // Today's count is read straight away, so the counter opens showing it
    DhikrCounterState(progress = getProgress(dhikrId)),
    DhikrCounterReducer,
) {
    init {
        viewModelScope.launch {
            selectedLanguage.changes.collect { language -> mutate(DhikrCounterMutation.Loaded(getDhikr(dhikrId, language))) }
        }
    }

    override fun handle(intent: DhikrCounterIntent) {
        when (intent) {
            DhikrCounterIntent.Counted -> {
                val dhikr = state.value.dhikr ?: return
                val progress = countDhikr(dhikr, state.value.progress)
                mutate(DhikrCounterMutation.ProgressChanged(progress))
                emit(DhikrCounterEffect.Counted(dhikr.milestoneAt(progress.count)))
            }
            DhikrCounterIntent.ResetConfirmed -> mutate(DhikrCounterMutation.ProgressChanged(resetProgress(dhikrId)))
            DhikrCounterIntent.ShareTapped -> state.value.dhikr?.let { emit(DhikrCounterEffect.OpenShare(it.toSharePassage())) }
        }
    }
}

/** A dhikr as a share card; a set without a translation of its own shares its phrases' translations */
fun Dhikr.toSharePassage() = SharePassage(
    arabic = arabic,
    transliteration = transliteration,
    translation = translation ?: steps.mapNotNull { it.translation }.joinToString("\n"),
    reference = reference,
)
