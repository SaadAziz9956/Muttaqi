package com.muttaqi.shared.core.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStore

class IosViewModelOwner<VM : ViewModel>(val viewModel: VM) {
    private val store = ViewModelStore().also { it.put(KEY, viewModel) }

    fun clear() = store.clear()

    private companion object {
        const val KEY = "screen"
    }
}
