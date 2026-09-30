package com.muttaqi.android.feature.home

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.muttaqi.android.designsystem.component.FeaturePlaceholder
import kotlinx.serialization.Serializable

@Serializable
data object HomeRoute

/** The Home screens. Registered in their tab's graph by MuttaqiApp */
fun NavGraphBuilder.homeDestinations(navController: NavController) {
    composable<HomeRoute> { FeaturePlaceholder("Home", onBack = null) }
}
