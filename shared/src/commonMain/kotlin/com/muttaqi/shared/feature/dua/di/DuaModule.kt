package com.muttaqi.shared.feature.dua.di

import com.muttaqi.shared.feature.dua.data.repository.BundledDuaRepository
import com.muttaqi.shared.feature.dua.domain.repository.DuaCategoryRepository
import com.muttaqi.shared.feature.dua.domain.repository.QuranicDuaRepository
import com.muttaqi.shared.feature.dua.domain.usecase.BuildDuaSearchIndex
import com.muttaqi.shared.feature.dua.domain.usecase.GetDuaCategories
import com.muttaqi.shared.feature.dua.domain.usecase.GetDuaCategory
import com.muttaqi.shared.feature.dua.domain.usecase.GetDuaChapter
import com.muttaqi.shared.feature.dua.domain.usecase.GetDuaEntriesById
import com.muttaqi.shared.feature.dua.domain.usecase.GetDuaOfTheDay
import com.muttaqi.shared.feature.dua.presentation.category.DuaCategoryViewModel
import com.muttaqi.shared.feature.dua.presentation.chapter.DuaChapterViewModel
import com.muttaqi.shared.feature.dua.presentation.list.DuaListViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.binds
import org.koin.dsl.module

/** Hisn al-Muslim and the Quranic duas: the Dua tab, its categories and chapters */
val duaModule = module {
    single { BundledDuaRepository(get(), get()) } binds arrayOf(DuaCategoryRepository::class, QuranicDuaRepository::class)

    factoryOf(::GetDuaCategories)
    factoryOf(::GetDuaCategory)
    factoryOf(::GetDuaChapter)
    factoryOf(::GetDuaEntriesById)
    factoryOf(::GetDuaOfTheDay)
    factoryOf(::BuildDuaSearchIndex)

    viewModelOf(::DuaListViewModel)
    viewModel { (categoryId: String) -> DuaCategoryViewModel(categoryId, get(), get()) }
    viewModel { (chapterId: String) -> DuaChapterViewModel(chapterId, get(), get()) }
}
