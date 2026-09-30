package com.muttaqi.android.feature.topics

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.muttaqi.android.feature.share.ShareRoute
import com.muttaqi.shared.feature.topics.domain.model.TopicChips
import kotlinx.serialization.Serializable

@Serializable
data object ExploreRoute

@Serializable
data object EmotionsRoute

/** One topic's page, with chips for every emotion or for the topics in its Explore group */
@Serializable
data class TopicPageRoute(val chips: TopicChips, val topicId: String)

/** The Explore tab's root: topics in groups, and search */
fun NavGraphBuilder.exploreDestinations(navController: NavController) {
    composable<ExploreRoute> {
        ExploreRoute(onOpenTopic = { navController.navigate(TopicPageRoute(TopicChips.ExploreGroup, it)) })
    }
}

/** Emotions, reached from Home */
fun NavGraphBuilder.emotionsDestinations(navController: NavController) {
    composable<EmotionsRoute> {
        EmotionsRoute(
            onOpenEmotion = { navController.navigate(TopicPageRoute(TopicChips.Emotions, it)) },
            onBack = navController::popBackStack,
        )
    }
}

/** The topic page Emotions and Explore share. Registered once, outside the tabs, since both open it */
fun NavGraphBuilder.topicPageDestinations(navController: NavController) {
    composable<TopicPageRoute> { entry ->
        val route = entry.toRoute<TopicPageRoute>()
        TopicPageRoute(
            chips = route.chips,
            topicId = route.topicId,
            onShare = { navController.navigate(ShareRoute(it)) },
            onBack = navController::popBackStack,
        )
    }
}
