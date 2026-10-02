package com.muttaqi.shared.feature.quran.presentation.tafsir

import com.muttaqi.shared.core.model.Language
import com.muttaqi.shared.core.mvi.UiEffect
import com.muttaqi.shared.core.mvi.UiIntent
import com.muttaqi.shared.core.mvi.UiMutation
import com.muttaqi.shared.core.mvi.UiState
import com.muttaqi.shared.feature.quran.domain.model.TafsirEntry

sealed interface TafsirStatus {
    data object Idle : TafsirStatus
    data object Loading : TafsirStatus
    data class Loaded(val entries: List<TafsirEntry>) : TafsirStatus
    data class Failed(val message: String) : TafsirStatus
}

data class TafsirState(
    val surahNumber: Int? = null,
    val language: Language = Language.English,
    val status: TafsirStatus = TafsirStatus.Idle,
) : UiState {
    val showsEnglishInstead: Boolean get() = language == Language.Hindi

    fun entryCovering(ayah: Int): TafsirEntry? = (status as? TafsirStatus.Loaded)?.entries?.firstOrNull { it.covers(ayah) }
}

sealed interface TafsirIntent : UiIntent {
    data class Opened(val surahNumber: Int) : TafsirIntent
    data object Retry : TafsirIntent
}

sealed interface TafsirMutation : UiMutation {
    data class LanguageChanged(val language: Language) : TafsirMutation
    data class Loading(val surahNumber: Int) : TafsirMutation
    data class Loaded(val entries: List<TafsirEntry>) : TafsirMutation
    data class LoadFailed(val message: String) : TafsirMutation
}

sealed interface TafsirEffect : UiEffect
