package com.muttaqi.android.feature.quran

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.muttaqi.android.designsystem.component.FeaturePlaceholder
import kotlinx.serialization.Serializable

@Serializable
data object QuranListRoute

/** The The Quran screens. Registered in their tab's graph by MuttaqiApp */
fun NavGraphBuilder.quranDestinations(navController: NavController) {
    composable<QuranListRoute> { FeaturePlaceholder("The Quran", onBack = null) }
}
