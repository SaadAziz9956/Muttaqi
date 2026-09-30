package com.muttaqi.android.feature.names

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.muttaqi.android.designsystem.component.FeaturePlaceholder
import kotlinx.serialization.Serializable

@Serializable
data object NamesRoute

/** The 99 Names screens. Registered in their tab's graph by MuttaqiApp */
fun NavGraphBuilder.namesDestinations(navController: NavController) {
    composable<NamesRoute> { FeaturePlaceholder("99 Names", onBack = navController::popBackStack) }
}
