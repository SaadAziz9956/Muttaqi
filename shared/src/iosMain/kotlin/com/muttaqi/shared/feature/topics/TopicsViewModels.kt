package com.muttaqi.shared.feature.topics

import com.muttaqi.shared.feature.topics.domain.model.TopicChips
import com.muttaqi.shared.feature.topics.presentation.emotions.EmotionsViewModel
import com.muttaqi.shared.feature.topics.presentation.explore.ExploreViewModel
import com.muttaqi.shared.feature.topics.presentation.page.TopicPageViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf

/** The Emotions and Explore screens' view models for Swift, from Koin: `TopicsViewModels.shared.explore()` */
object TopicsViewModels : KoinComponent {
    fun emotions(): EmotionsViewModel = get()
    fun explore(): ExploreViewModel = get()
    fun topicPage(chips: TopicChips, topicId: String): TopicPageViewModel = get { parametersOf(chips, topicId) }
}
