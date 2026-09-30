package com.muttaqi.shared.feature.quran.presentation.tafsir

import com.muttaqi.shared.core.model.Language
import com.muttaqi.shared.core.mvi.UiEffect
import com.muttaqi.shared.core.mvi.UiIntent
import com.muttaqi.shared.core.mvi.UiMutation
import com.muttaqi.shared.core.mvi.UiState
import com.muttaqi.shared.feature.quran.domain.model.TafsirEntry

sealed interface TafsirStatus {
    /** Nothing asked for yet: the explanation loads when it's first opened */
    data object Idle : TafsirStatus
    data object Loading : TafsirStatus
    data class Loaded(val entries: List<TafsirEntry>) : TafsirStatus
    data class Failed(val message: String) : TafsirStatus
}

/** The explanation sheet: Tafsir Ibn Kathir for the surah being read, in the reader's language */
data class TafsirState(
    val surahNumber: Int? = null,
    val language: Language = Language.English,
    val status: TafsirStatus = TafsirStatus.Idle,
) : UiState {
    /** There's no Hindi tafsir, so Hindi readers are told they're reading the English */
    val showsEnglishInstead: Boolean get() = language == Language.Hindi

    /** The passage covering the ayah, since commentary often covers a group of ayahs */
    fun entryCovering(ayah: Int): TafsirEntry? = (status as? TafsirStatus.Loaded)?.entries?.firstOrNull { it.covers(ayah) }
}

sealed interface TafsirIntent : UiIntent {
    /** The sheet opened for the surah; the tafsir already loaded for it in the same language is kept */
    data class Opened(val surahNumber: Int) : TafsirIntent
    data object Retry : TafsirIntent
}

sealed interface TafsirMutation : UiMutation {
    data class LanguageChanged(val language: Language) : TafsirMutation
    data class Loading(val surahNumber: Int) : TafsirMutation
    data class Loaded(val entries: List<TafsirEntry>) : TafsirMutation
    data class LoadFailed(val message: String) : TafsirMutation
}

/** Nothing happens once on this sheet; everything it does is in its state */
sealed interface TafsirEffect : UiEffect
