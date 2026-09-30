package com.muttaqi.android.feature.prayer

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.muttaqi.android.designsystem.component.FeaturePlaceholder
import kotlinx.serialization.Serializable

@Serializable
data object QiblaRoute

/** The Qibla screens. Registered in their tab's graph by MuttaqiApp */
fun NavGraphBuilder.prayerDestinations(navController: NavController) {
    composable<QiblaRoute> { FeaturePlaceholder("Qibla", onBack = navController::popBackStack) }
}
