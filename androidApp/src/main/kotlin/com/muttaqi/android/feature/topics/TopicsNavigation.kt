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

@Serializable
data class TopicPageRoute(val chips: TopicChips, val topicId: String)

fun NavGraphBuilder.exploreDestinations(navController: NavController) {
    composable<ExploreRoute> {
        ExploreRoute(onOpenTopic = { navController.navigate(TopicPageRoute(TopicChips.ExploreGroup, it)) })
    }
}

fun NavGraphBuilder.emotionsDestinations(navController: NavController) {
    composable<EmotionsRoute> {
        EmotionsRoute(
            onOpenEmotion = { navController.navigate(TopicPageRoute(TopicChips.Emotions, it)) },
            onBack = navController::popBackStack,
        )
    }
}

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
