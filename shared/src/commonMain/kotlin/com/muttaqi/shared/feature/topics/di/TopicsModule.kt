package com.muttaqi.shared.feature.topics.di

import com.muttaqi.shared.feature.topics.data.repository.BundledEmotionRepository
import com.muttaqi.shared.feature.topics.data.repository.BundledExploreRepository
import com.muttaqi.shared.feature.topics.domain.model.TopicChips
import com.muttaqi.shared.feature.topics.domain.repository.EmotionRepository
import com.muttaqi.shared.feature.topics.domain.repository.ExploreRepository
import com.muttaqi.shared.feature.topics.domain.usecase.BuildExploreSearchIndex
import com.muttaqi.shared.feature.topics.domain.usecase.GetEmotions
import com.muttaqi.shared.feature.topics.domain.usecase.GetEmotionsHeader
import com.muttaqi.shared.feature.topics.domain.usecase.GetExploreGroups
import com.muttaqi.shared.feature.topics.domain.usecase.GetExploreHeader
import com.muttaqi.shared.feature.topics.domain.usecase.GetHadithOfTheDay
import com.muttaqi.shared.feature.topics.domain.usecase.GetTopicOfTheDay
import com.muttaqi.shared.feature.topics.domain.usecase.GetTopicPageTopics
import com.muttaqi.shared.feature.topics.presentation.emotions.EmotionsViewModel
import com.muttaqi.shared.feature.topics.presentation.explore.ExploreViewModel
import com.muttaqi.shared.feature.topics.presentation.page.TopicPageViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** Emotions and Explore, which share the topic page of verses, hadith and duas */
val topicsModule = module {
    // The duas come from the Dua feature's entries (GetDuaEntriesById, in duaModule)
    single<EmotionRepository> { BundledEmotionRepository(get(), get(), get()) }
    single<ExploreRepository> { BundledExploreRepository(get(), get(), get()) }

    factoryOf(::GetEmotions)
    factoryOf(::GetEmotionsHeader)
    factoryOf(::GetExploreGroups)
    factoryOf(::GetExploreHeader)
    factoryOf(::GetTopicPageTopics)
    factoryOf(::BuildExploreSearchIndex)
    // Home's picks from Explore
    factoryOf(::GetTopicOfTheDay)
    factoryOf(::GetHadithOfTheDay)

    viewModelOf(::EmotionsViewModel)
    viewModelOf(::ExploreViewModel)
    viewModel { (chips: TopicChips, topicId: String) -> TopicPageViewModel(chips, topicId, get(), get()) }
}
