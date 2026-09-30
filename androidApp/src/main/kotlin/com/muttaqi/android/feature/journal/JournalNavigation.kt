package com.muttaqi.android.feature.journal

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.muttaqi.android.designsystem.component.FeaturePlaceholder
import kotlinx.serialization.Serializable

@Serializable
data object JournalListRoute

/** The Journal screens. Registered in their tab's graph by MuttaqiApp */
fun NavGraphBuilder.journalDestinations(navController: NavController) {
    composable<JournalListRoute> { FeaturePlaceholder("Journal", onBack = navController::popBackStack) }
}
