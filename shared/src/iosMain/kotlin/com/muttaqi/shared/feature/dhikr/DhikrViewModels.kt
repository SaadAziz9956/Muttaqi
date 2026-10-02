package com.muttaqi.shared.feature.dhikr

import com.muttaqi.shared.feature.dhikr.presentation.counter.DhikrCounterViewModel
import com.muttaqi.shared.feature.dhikr.presentation.list.DhikrListViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf

object DhikrViewModels : KoinComponent {
    fun list(): DhikrListViewModel = get()
    fun counter(id: String): DhikrCounterViewModel = get { parametersOf(id) }
}
