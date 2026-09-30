package com.muttaqi.shared.core.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStore

/**
 * Owns one screen's view model on iOS, where there's no Android lifecycle to clear it. The SwiftUI screen keeps the
 * owner and calls [clear] when it goes away, which cancels the view model's work
 */
class IosViewModelOwner<VM : ViewModel>(val viewModel: VM) {
    private val store = ViewModelStore().also { it.put(KEY, viewModel) }

    fun clear() = store.clear()

    private companion object {
        const val KEY = "screen"
    }
}
