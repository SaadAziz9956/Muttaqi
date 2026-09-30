package com.muttaqi.android.feature.dhikr

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.muttaqi.android.designsystem.component.FeaturePlaceholder
import kotlinx.serialization.Serializable

@Serializable
data object DhikrListRoute

/** The Dikr screens. Registered in their tab's graph by MuttaqiApp */
fun NavGraphBuilder.dhikrDestinations(navController: NavController) {
    composable<DhikrListRoute> { FeaturePlaceholder("Dikr", onBack = navController::popBackStack) }
}
