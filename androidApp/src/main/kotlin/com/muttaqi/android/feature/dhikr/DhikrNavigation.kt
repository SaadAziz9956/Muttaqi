package com.muttaqi.android.feature.dhikr

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.muttaqi.android.feature.share.ShareRoute
import kotlinx.serialization.Serializable

@Serializable
data object DhikrListRoute

@Serializable
data class DhikrCounterRoute(val dhikrId: String)

fun NavGraphBuilder.dhikrDestinations(navController: NavController) {
    composable<DhikrListRoute> {
        DhikrListRoute(
            onOpenCounter = { navController.navigate(DhikrCounterRoute(it)) },
            onBack = navController::popBackStack,
        )
    }
    composable<DhikrCounterRoute> { entry ->
        DhikrCounterRoute(
            dhikrId = entry.toRoute<DhikrCounterRoute>().dhikrId,
            onShare = { navController.navigate(ShareRoute(it)) },
            onBack = navController::popBackStack,
        )
    }
}
