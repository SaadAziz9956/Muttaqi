package com.muttaqi.shared.core.mvi

fun interface Reducer<S : UiState, M : UiMutation> {
    fun reduce(state: S, mutation: M): S
}
