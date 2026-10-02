package com.muttaqi.shared.feature.dua

import com.muttaqi.shared.feature.dua.presentation.category.DuaCategoryViewModel
import com.muttaqi.shared.feature.dua.presentation.chapter.DuaChapterViewModel
import com.muttaqi.shared.feature.dua.presentation.list.DuaListViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf

object DuaViewModels : KoinComponent {
    fun list(): DuaListViewModel = get()
    fun category(id: String): DuaCategoryViewModel = get { parametersOf(id) }
    fun chapter(id: String): DuaChapterViewModel = get { parametersOf(id) }
}
