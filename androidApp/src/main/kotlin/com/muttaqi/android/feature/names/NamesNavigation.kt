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

/** The 99 Names page and its search. Registered in their tab's graph by MuttaqiApp */
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
        // The page's own view model, kept by the page's back stack entry, so picking a result turns its page
        val page = remember(entry) { navController.getBackStackEntry<NamesRoute>() }
        NamesSearchRoute(viewModel = koinViewModel<NamesViewModel>(viewModelStoreOwner = page), onBack = navController::popBackStack)
    }
}
