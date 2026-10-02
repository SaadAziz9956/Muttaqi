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

val topicsModule = module {
    single<EmotionRepository> { BundledEmotionRepository(get(), get(), get()) }
    single<ExploreRepository> { BundledExploreRepository(get(), get(), get()) }

    factoryOf(::GetEmotions)
    factoryOf(::GetEmotionsHeader)
    factoryOf(::GetExploreGroups)
    factoryOf(::GetExploreHeader)
    factoryOf(::GetTopicPageTopics)
    factoryOf(::BuildExploreSearchIndex)
    factoryOf(::GetTopicOfTheDay)
    factoryOf(::GetHadithOfTheDay)

    viewModelOf(::EmotionsViewModel)
    viewModelOf(::ExploreViewModel)
    viewModel { (chips: TopicChips, topicId: String) -> TopicPageViewModel(chips, topicId, get(), get()) }
}
