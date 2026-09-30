package com.muttaqi.android.feature.topics

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.muttaqi.android.designsystem.component.FeaturePlaceholder
import kotlinx.serialization.Serializable

@Serializable
data object ExploreRoute

@Serializable
data object EmotionsRoute

/** The Explore tab's root */
fun NavGraphBuilder.exploreDestinations(navController: NavController) {
    composable<ExploreRoute> { FeaturePlaceholder("Explore") }
}

/** Emotions, reached from Home */
fun NavGraphBuilder.emotionsDestinations(navController: NavController) {
    composable<EmotionsRoute> { FeaturePlaceholder("Emotions", onBack = navController::popBackStack) }
}

/** The topic page Emotions and Explore share. Registered once, outside the tabs, since both open it */
fun NavGraphBuilder.topicPageDestinations(navController: NavController) {
}
