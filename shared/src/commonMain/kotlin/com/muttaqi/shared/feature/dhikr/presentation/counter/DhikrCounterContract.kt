package com.muttaqi.shared.feature.dhikr.presentation.counter

import com.muttaqi.shared.core.mvi.UiEffect
import com.muttaqi.shared.core.mvi.UiIntent
import com.muttaqi.shared.core.mvi.UiMutation
import com.muttaqi.shared.core.mvi.UiState
import com.muttaqi.shared.core.share.SharePassage
import com.muttaqi.shared.feature.dhikr.domain.model.Dhikr
import com.muttaqi.shared.feature.dhikr.domain.model.DhikrMilestone
import com.muttaqi.shared.feature.dhikr.domain.model.DhikrProgress
import com.muttaqi.shared.feature.dhikr.domain.model.DhikrStepPosition
import com.muttaqi.shared.feature.dhikr.domain.model.isRoundComplete
import com.muttaqi.shared.feature.dhikr.domain.model.roundProgress
import com.muttaqi.shared.feature.dhikr.domain.model.stepPosition

/** One dhikr in full with its counter, and today's count, kept for the day */
data class DhikrCounterState(
    val isLoading: Boolean = true,
    val dhikr: Dhikr? = null,
    val progress: DhikrProgress,
) : UiState {
    val count: Int get() = progress.count
    val rounds: Int get() = progress.rounds
    val target: Int? get() = dhikr?.target
    val isRoundComplete: Boolean get() = dhikr?.isRoundComplete(count) ?: false
    /** Share of the current round said, from 0 to 1, for the ring; always 0 for open-ended remembrance */
    val roundProgress: Double get() = dhikr?.roundProgress(count) ?: 0.0
    /** For a set said in parts, the phrase to say now; null for a single phrase */
    val currentStep: DhikrStepPosition? get() = dhikr?.stepPosition(count)
    /** Whether there's anything today to reset */
    val hasProgress: Boolean get() = count > 0 || rounds > 0
}

sealed interface DhikrCounterIntent : UiIntent {
    /** The counter was tapped: one more repetition */
    data object Counted : DhikrCounterIntent
    /** Reset was confirmed */
    data object ResetConfirmed : DhikrCounterIntent
    data object ShareTapped : DhikrCounterIntent
}

sealed interface DhikrCounterMutation : UiMutation {
    data class Loaded(val dhikr: Dhikr?) : DhikrCounterMutation
    data class ProgressChanged(val progress: DhikrProgress) : DhikrCounterMutation
}

sealed interface DhikrCounterEffect : UiEffect {
    /** A repetition was counted; each app plays its own haptic, stronger at the end of a phrase or a round */
    data class Counted(val milestone: DhikrMilestone) : DhikrCounterEffect
    data class OpenShare(val passage: SharePassage) : DhikrCounterEffect
}
