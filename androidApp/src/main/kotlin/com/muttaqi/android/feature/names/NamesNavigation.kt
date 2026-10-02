package com.muttaqi.android.feature.names

import androidx.compose.runtime.remember
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.muttaqi.android.feature.share.ShareRoute
import com.muttaqi.shared.feature.names.presentation.NamesViewModel
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel

@Serializable
data object NamesRoute

@Serializable
data object NamesSearchRoute

fun NavGraphBuilder.namesDestinations(navController: NavController) {
    composable<NamesRoute> {
        NamesRoute(
            viewModel = koinViewModel<NamesViewModel>(),
            onOpenSearch = { navController.navigate(NamesSearchRoute) },
            onShare = { navController.navigate(ShareRoute(it)) },
            onBack = navController::popBackStack,
        )
    }
    composable<NamesSearchRoute> { entry ->
        val page = remember(entry) { navController.getBackStackEntry<NamesRoute>() }
        NamesSearchRoute(viewModel = koinViewModel<NamesViewModel>(viewModelStoreOwner = page), onBack = navController::popBackStack)
    }
}
