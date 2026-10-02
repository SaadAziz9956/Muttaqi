package com.muttaqi.shared.feature.names

import com.muttaqi.shared.feature.names.presentation.NamesViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

object NamesViewModels : KoinComponent {
    fun names(): NamesViewModel = get()
}
