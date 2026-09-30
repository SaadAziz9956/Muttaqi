package com.muttaqi.shared.feature.names.di

import com.muttaqi.shared.feature.names.data.repository.BundledNamesRepository
import com.muttaqi.shared.feature.names.domain.repository.NamesRepository
import com.muttaqi.shared.feature.names.domain.usecase.BuildNamesSearchIndex
import com.muttaqi.shared.feature.names.domain.usecase.GetAllahNames
import com.muttaqi.shared.feature.names.domain.usecase.GetNameOfTheDay
import com.muttaqi.shared.feature.names.presentation.NamesViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** The 99 Names: the swiping cards and search */
val namesModule = module {
    single<NamesRepository> { BundledNamesRepository(get(), get()) }

    factoryOf(::GetAllahNames)
    factoryOf(::GetNameOfTheDay)
    factoryOf(::BuildNamesSearchIndex)

    viewModelOf(::NamesViewModel)
}
