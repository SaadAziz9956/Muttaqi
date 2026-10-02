package com.muttaqi.shared.core.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

abstract class MviViewModel<S : UiState, I : UiIntent, M : UiMutation, E : UiEffect>(
    initialState: S,
    private val reducer: Reducer<S, M>,
) : ViewModel() {

    private val mutableState = MutableStateFlow(initialState)
    val state: StateFlow<S> = mutableState.asStateFlow()

    private val effectChannel = Channel<E>(Channel.BUFFERED)
    val effects: Flow<E> = effectChannel.receiveAsFlow()

    fun dispatch(intent: I) = handle(intent)

    protected abstract fun handle(intent: I)

    protected fun mutate(mutation: M) {
        mutableState.update { reducer.reduce(it, mutation) }
    }

    protected fun launchNow(block: suspend CoroutineScope.() -> Unit): Job =
        viewModelScope.launch(start = CoroutineStart.UNDISPATCHED, block = block)

    protected fun emit(effect: E) {
        viewModelScope.launch { effectChannel.send(effect) }
    }
}
