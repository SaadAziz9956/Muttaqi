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

data class DhikrCounterState(
    val isLoading: Boolean = true,
    val dhikr: Dhikr? = null,
    val progress: DhikrProgress,
) : UiState {
    val count: Int get() = progress.count
    val rounds: Int get() = progress.rounds
    val target: Int? get() = dhikr?.target
    val isRoundComplete: Boolean get() = dhikr?.isRoundComplete(count) ?: false
    val roundProgress: Double get() = dhikr?.roundProgress(count) ?: 0.0
    val currentStep: DhikrStepPosition? get() = dhikr?.stepPosition(count)
    val hasProgress: Boolean get() = count > 0 || rounds > 0
}

sealed interface DhikrCounterIntent : UiIntent {
    data object Counted : DhikrCounterIntent
    data object ResetConfirmed : DhikrCounterIntent
    data object ShareTapped : DhikrCounterIntent
}

sealed interface DhikrCounterMutation : UiMutation {
    data class Loaded(val dhikr: Dhikr?) : DhikrCounterMutation
    data class ProgressChanged(val progress: DhikrProgress) : DhikrCounterMutation
}

sealed interface DhikrCounterEffect : UiEffect {
    data class Counted(val milestone: DhikrMilestone) : DhikrCounterEffect
    data class OpenShare(val passage: SharePassage) : DhikrCounterEffect
}
