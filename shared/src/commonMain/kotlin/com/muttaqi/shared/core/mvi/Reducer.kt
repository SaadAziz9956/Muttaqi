package com.muttaqi.shared.core.mvi

/**
 * Applies one mutation to the state. Pure: the same state and mutation always give the same result, with no side
 * effects, so every state change can be tested on its own
 */
fun interface Reducer<S : UiState, M : UiMutation> {
    fun reduce(state: S, mutation: M): S
}
