package com.muttaqi.android.feature.prayer

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.muttaqi.shared.feature.prayer.presentation.qibla.QiblaViewModel
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel

@Serializable
data object QiblaRoute

fun NavGraphBuilder.prayerDestinations(navController: NavController) {
    composable<QiblaRoute> { QiblaRoute(viewModel = koinViewModel<QiblaViewModel>(), onBack = navController::popBackStack) }
}
